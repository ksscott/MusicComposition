package main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Scanner;

import composing.Composer;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.data.Pitch;
import net.beadsproject.beads.events.AudioContextStopTrigger;
import net.beadsproject.beads.events.KillTrigger;
import net.beadsproject.beads.ugens.Clock;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.WavePlayer;
import performance.MidiNote;
import performance.instrument.Instrument;
import theory.Measure;

public class BeadRunner {
	
	private static final String openingString = "\uD834\uDD1E Live Music Composition \uD834\uDD1E\n"
			  + "Commands:\n"
			  + "repertoire\n"
			  + "restart\n"
			  + "request [piece name]\n"
			  + "switch\n"
			  + "tempo [up or down]\n"
			  + "quit\n";
	private static final String closingString = "\uD834\uDD1E Terminating Live Music Composition \uD834\uDD1E";

	private static Queue<String> userInputs = new PriorityQueue<>();
	private static boolean empty = true;
	private static final List<String> STOP_COMMANDS = Arrays.asList(new String[] { "stop", "end", "quit", "kill" });
	
	private static HeldNotesManager heldNotes;

	public static void main(String[] args) {
		InputThread inputThread = new InputThread();
		inputThread.start();
		
		System.out.println(openingString);
		
		heldNotes = new HeldNotesManager();
		final AudioContext ac;

		ac = new AudioContext();
		/*
		 * In this example a Clock is used to trigger events. We do this by
		 * adding a listener to the Clock (which is of type Bead).
		 * 
		 * The Bead is made on-the-fly. All we have to do is to give the Bead a
		 * callback method to make notes.
		 * 
		 * This example is more sophisticated than the previous ones. It uses
		 * nested code.
		 */
		Clock clock = new Clock(ac, 500);
		
		//this is the on-the-fly bead
		Bead bead = new Bead() {
					Queue<Measure> measures = new PriorityQueue<>();
					Measure measure;
					Composer composer = new Composer();
					{ measures.add(composer.beginComposing()); }
					int startOfMeasure = 0;
					float maxVolume = 0.4f;
					
					int pitch;
					public void messageReceived(Bead message) {
						Clock c = (Clock) message;
						
						if (!empty) {
							final String input = receiveUserInput();
							if (STOP_COMMANDS.contains(input)) {
								// TODO figure out best stopping procedure
								inputThread.end();
								composer.finishComposing();
								clock.kill();
								ac.out.kill();
								System.out.println(closingString);
								System.exit(0); // best termination solution?
								return; // dead code
							}
							Measure onTheFlyMeasure = composer.receiveInput(input);
							if (onTheFlyMeasure != null)
								measures.add(onTheFlyMeasure);
						}
						
						int beat = c.getBeatCount();
						
						if (c.isBeat()) {
//							System.out.println("Beat: " + beat);
//							System.out.println("Count this measure: " + countThisMeasure);
							if (beat == 0 || beat >= startOfMeasure + measure.beats()) { // is new measure
//								System.out.println("Beats this measure: " + measure.beats());
								if (measures.size() < 1) {
									final Measure nextMeasure = composer.writeNextMeasure();
									if (nextMeasure != null)
										measures.add(nextMeasure);
								}
								measure = measures.poll(); // TODO maybe use multiple threads to make this smoother
								int millisPerSec = (int) (60000 / measure.getBpm());
								c.getIntervalUGen().setValue(millisPerSec);
								System.out.println("[Measure " + measure.getMeasureNumber() + "] " + measure.getMetaInfo());
								System.out.println(measure.stringDrawing());
								startOfMeasure = beat;
							}
//							System.out.println("Time: " + time);
						}
//						int beatThisMeasure = beat - startOfMeasure;
						long count = c.getCount();
						int countThisMeasure = (int) (count - (startOfMeasure*c.getTicksPerBeat()));
//						int beatThisMeasure = beat - startOfMeasure;
//						int beats = measure.beats();
						double beatValue = measure.beatValue();
						double time = (double) countThisMeasure*beatValue/((double) c.getTicksPerBeat());
						double nextTime = (double) (countThisMeasure+1)*beatValue/((double) c.getTicksPerBeat());
						float millisPerBeat = c.getIntervalUGen().getValue();
						double millisPerWholeNote = millisPerBeat / beatValue; // (millis / beat) / (whole-notes / beat)
						
						// play notes
						boolean playedAnyNotes = false;
						for (Instrument instrument : measure.getInstruments()) {
							List<MidiNote> notes = measure.getNotes(instrument, time, nextTime);
							if (!notes.isEmpty()) {
//								if (!playedAnyNotes) // for old note printing method
//									System.out.print(String.format("Beat %.2f: ", time/beatValue + 1));
								playedAnyNotes = true;
								playNotes(instrument, notes, millisPerWholeNote);
							}
						}
//						if (playedAnyNotes) // for old note printing method
//							System.out.println();

						// CLEANUP:
						if (playedAnyNotes) {
							Collection<Gain> obsolete = heldNotes.endAcquisitionPhase();
							for (Gain old : obsolete) {
								((Envelope) old.getGainUGen()).addSegment(0, 1, new KillTrigger(old));
//								System.out.println("Cleaning obsolete note");
							}
						}
					}
							
							
					private void playNotes(Instrument instrument, List<MidiNote> notes, double millisPerWholeNote) {
						BeadsTimbre timbre = (BeadsTimbre) instrument.getTimbre(); // FIXME true for now
						int attackTime = timbre.getPeakMillis();
						Buffer buffer = timbre.getWaveform();
						
						for (MidiNote note : notes) {
							pitch = note.getPitch();
//							System.out.print(pitch + " "); // for old note printing method
							float freq = Pitch.mtof(pitch);
							int durationMillis = (int) (millisPerWholeNote * note.getDuration()); // (millis / whole-note) * whole-notes
							float volume = (float) note.getDynamic().volume();
							Gain g;
							
							int tiedFrom = note.getTiedFromPitch();
							if (tiedFrom != 0) {
								g = heldNotes.acquire(instrument, tiedFrom);
								WavePlayer tiedWp = (WavePlayer) g.getConnectedInputs().iterator().next();
								tiedWp.setFrequency(freq);
//								tiedWp.addInput(arg0);
							} else {
								// start note:
								g = new Gain(ac, 1, new Envelope(ac, 0));
								WavePlayer wp = new WavePlayer(ac, freq, buffer);
								g.addInput(wp);
								// attackTime = note.getPeakMillis(); // haven't decided who decides this
								((Envelope)g.getGainUGen()).addSegment(maxVolume*volume, attackTime);
								ac.out.addInput(g);
							}
							if (!note.tiesOver()) {
								// add note end:
								((Envelope)g.getGainUGen()).addSegment(0, durationMillis, new KillTrigger(g));
							} else {
								// prepare tie to next note
								heldNotes.register(instrument, pitch, g);
							}
						}
					}
				};
		clock.addMessageListener(bead);
		ac.out.addDependent(clock);
//		ac.out.addInput(clock);
//		System.out.println("ac.out outputs: " + ac.out.getOuts());
//		System.out.println("ac.out ins: " + ac.out.getIns());
//		System.out.println("ac.out connected ugens: " + ac.out.getNumberOfConnectedUGens(0));
//		System.out.println("ac.out connected inputs: " + ac.out.getConnectedInputs().size());
		bead.setKillListener(new AudioContextStopTrigger(ac)); // y u no work?
		ac.start();
//		System.out.println("Connected UGens: " + ac.out.getNumberOfConnectedUGens(0));

	}
	
	public static float random(double x) {
		return (float)(Math.random() * x);
	}
	
	private static synchronized void addUserInput(String input) {
		userInputs.add(input);
		empty = false;
	}
	
	private static synchronized String receiveUserInput() {
		String input = userInputs.poll();
		if (userInputs.isEmpty())
			empty = true;
		return input;
	}
	
	@SuppressWarnings("unused")
	private static synchronized List<String> receiveAllUserInputs() {
		List<String> inputs = new ArrayList<>(userInputs);
		empty = true;
		return inputs;
	}
	
	/**
	 * Allows multiple notes to change at once, possibly acquiring each other's notes. After every round of 
	 * simultaneous note changes, only one instance of each note is allowed to be held per {@link Instrument}.
	 * 
	 * <p> Call {@link #register(Instrument, Integer, Gain) register} to begin holding a note.
	 * <p> Call {@link #acquire(Instrument, Integer)} to take over ownership of a held note, which will no longer be tracked
	 * <p> Be sure to call {@link #endAcquisitionPhase()} after every round of simultaneous note changes.
	 * The map returned contains obsolete held notes which will no longer be tracked. 
	 * Be sure to do any necessary cleanup on these notes.
	 */
	private static class HeldNotesManager {
		
		private Map<Instrument,Map<Integer,Gain>> previousHeldNotes;
		private Map<Instrument,Map<Integer,Gain>> nextHeldNotes;
		
		public HeldNotesManager() {
			this.previousHeldNotes = new HashMap<>();
			this.nextHeldNotes = new HashMap<>();
		}
		
		/**
		 * @param instrument
		 * @param note the note to be held
		 * @param gain the gain which produces the held note
		 */
		public void register(Instrument instrument, Integer note, Gain gain) {
			if (!nextHeldNotes.containsKey(instrument))
				nextHeldNotes.put(instrument, new HashMap<>());
			nextHeldNotes.get(instrument).put(note, gain);
		}
		
		/**
		 * @param instrument
		 * @param note the pitch to acquire
		 * @return the associated gain, which will be removed from the list of held pitches
		 */
		public Gain acquire(Instrument instrument, Integer note) {
			Map<Integer, Gain> notes = previousHeldNotes.get(instrument);
			return notes == null ? null : notes.remove(note);
		}
		
		/**
		 * Call this method at the end of a round of simultaneous note changes.
		 * 
		 * @return obsolete collection of previously held notes which will now be forgotten 
		 * by this manager forever; the consumer should be sure to clean them up as necessary.
		 */
		public Collection<Gain> endAcquisitionPhase() {
			// XXX THIS NEEDS TO BE REVISED
			// probably it needs to take into account the original length of the note
			List<Gain> forgotten = new ArrayList<>();
			
			Map<Instrument,Map<Integer,Gain>> obsolete = previousHeldNotes;
			boolean notEmpty = !nextHeldNotes.isEmpty();
			// carry over any notes that have not been stepped on
			for (Instrument instrument : obsolete.keySet()) {
				Map<Integer, Gain> oldInstNoteMap = obsolete.get(instrument);
				for (Integer note : oldInstNoteMap.keySet()) {
					if (notEmpty && (nextHeldNotes.get(instrument) == null || 
							!nextHeldNotes.get(instrument).containsKey(note)))
						register(instrument, note, oldInstNoteMap.remove(note));
					else
						forgotten.add(oldInstNoteMap.get(note));
				}
			}
			previousHeldNotes = nextHeldNotes;
			nextHeldNotes = new HashMap<>();
			
			return forgotten;
		}
	}
	
	public static class InputThread extends Thread {
		
		private boolean stopped;
		
		@Override
		public void run() {
			Scanner scanner = new Scanner(System.in);
			while(!stopped)
				addUserInput(scanner.nextLine());
			scanner.close();
		}
		
		public void end() {
			stopped = true;
		}
	}

}
