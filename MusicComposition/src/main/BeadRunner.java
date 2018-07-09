package main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Scanner;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;

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
import theory.Dynamic;
import theory.Measure;
import theory.MidiNote;

public class BeadRunner {
	
	private static Queue<String> userInputs = new PriorityQueue<>();
	private static boolean empty = true;
	private static final List<String> STOP_COMMANDS = Arrays.asList(new String[] { "stop", "end", "quit", "kill" });

	public static void main(String[] args) {
		InputThread inputThread = new InputThread();
		inputThread.start();
		
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
					float maxVolume = 0.5f;
					
					int resolution = 1000;
					Buffer buffer = new Buffer(resolution);
					{
						for (int i=0; i<resolution; i++) {
							float value = 0;
							for (int j=1; j<8; j++) {
								value += (1/(double)j/(double)j/(double)j) * Math.sin(i*j*2*Math.PI/resolution);
							}
							buffer.buf[i] = value;
						}
					}
					
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
								return;
//								System.exit(0); // best termination solution?
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
						double lastTime = (double) (countThisMeasure-1)*beatValue/((double) c.getTicksPerBeat());
						
						// play notes
//						List<MidiNote> notes = measure.getNotes(time); // misses times in between beats
						List<MidiNote> notes = measure.getNotes(lastTime, time);
						if (!notes.isEmpty())
							System.out.print(String.format("Beat %.2f: ", time / beatValue + 1));
						for (MidiNote note : notes) {
							pitch = note.getPitch();
							System.out.print(pitch + " ");
							float freq = Pitch.mtof(pitch);
							WavePlayer wp = new WavePlayer(ac, freq, buffer);
							Gain g = new Gain(ac, 1, new Envelope(ac, 0));
							g.addInput(wp);
							ac.out.addInput(g);
							float millisPerBeat = c.getIntervalUGen().getValue();
							int duration = (int) (millisPerBeat * note.getDuration() / beatValue);
							float volume = (float) volume(note.getDynamic());
							((Envelope)g.getGainUGen()).addSegment(maxVolume*volume, note.getPeakMillis());
							((Envelope)g.getGainUGen()).addSegment(0, duration, new KillTrigger(g));
						}
						if (!notes.isEmpty())
							System.out.println();
						
						
//						// tutorial
//						Clock c = (Clock)message;
//						if(c.isBeat()) {
//							//choose some nice frequencies
//							if(random(1) < 0.5) return;
//							pitch = Pitch.forceToScale((int)random(12), Pitch.dorian);
//							float freq = Pitch.mtof(pitch + (int)random(5) * 12 + 32);
//							WavePlayer wp = new WavePlayer(ac, freq, Buffer.SINE);
//							Gain g = new Gain(ac, 1, new Envelope(ac, 0));
//							g.addInput(wp);
//							ac.out.addInput(g);
//							((Envelope)g.getGainUGen()).addSegment(0.1f, random(200));
//							((Envelope)g.getGainUGen()).addSegment(0, random(7000), new KillTrigger(g));
//						}
//						if(c.getCount() % 4 == 0) {
//							//choose some nice frequencies
//							int pitchAlt = pitch;
//							if(random(1) < 0.2) pitchAlt = Pitch.forceToScale((int)random(12), Pitch.dorian) + (int)random(2) * 12;
//							float freq = Pitch.mtof(pitchAlt + 32);
//							WavePlayer wp = new WavePlayer(ac, freq, Buffer.SQUARE);
//							Gain g = new Gain(ac, 1, new Envelope(ac, 0));
//							g.addInput(wp);
//							Panner p = new Panner(ac, random(1));
//							p.addInput(g);
//							ac.out.addInput(p);
//							((Envelope)g.getGainUGen()).addSegment(random(0.1), random(50));
//							((Envelope)g.getGainUGen()).addSegment(0, random(400), new KillTrigger(p));
//						}
//						if(c.getCount() % 4 == 0) {
//							Noise n = new Noise(ac);
//							Gain g = new Gain(ac, 1, new Envelope(ac, 0.05f));
//							g.addInput(n);
//							Panner p = new Panner(ac, random(0.5) + 0.5f);
//							p.addInput(g);
//							ac.out.addInput(p);
//							((Envelope)g.getGainUGen()).addSegment(0, random(100), new KillTrigger(p));
//						}
					}
				};
		clock.addMessageListener(bead);
		ac.out.addDependent(clock);
//		ac.out.addInput(clock);
//		System.out.println("ac.out outputs: " + ac.out.getOuts());
//		System.out.println("ac.out ins: " + ac.out.getIns());
		System.out.println("ac.out connected ugens: " + ac.out.getNumberOfConnectedUGens(0));
		System.out.println("ac.out connected inputs: " + ac.out.getConnectedInputs().size());
		bead.setKillListener(new AudioContextStopTrigger(ac)); // y u no work?
		ac.start();
//		System.out.println("Connected UGens: " + ac.out.getNumberOfConnectedUGens(0));

	}

	public static float random(double x) {
		return (float)(Math.random() * x);
	}
	
	/**
	 * @param dynamic the volume to be represented
	 * @return value between 0 (silent) and 1 (maximum)
	 */
	public static double volume(Dynamic dynamic) {
		return (Math.atan(dynamic.getValue()) / Math.PI) + 0.5;
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
