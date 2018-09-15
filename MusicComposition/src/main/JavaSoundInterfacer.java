package main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Scanner;

import javax.sound.midi.Instrument;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Synthesizer;

import composing.Composer;
import performance.MidiNote;
import theory.Measure;

public class JavaSoundInterfacer {
	
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
	/** whether user input queue is empty */
	private static boolean empty = true;
	private static final List<String> STOP_COMMANDS = Arrays.asList(new String[] { "stop", "end", "quit", "kill" });

	private static Synthesizer synthesizer;
	private static Receiver synthRcvr;
	
	public static void main(String[] args) throws MidiUnavailableException, InvalidMidiDataException {
		
		synthesizer = MidiSystem.getSynthesizer();
		synthesizer.open();
	    synthRcvr = synthesizer.getReceiver();
	    
	    composeMusic();
	}
	
	private static void composeMusic() {
		int maxVolume = 127;
		Queue<Measure> measures = new PriorityQueue<>();
		Measure measure;
		Composer composer = new Composer();
		InputThread inputThread = new InputThread();
		inputThread.start();
		
		System.out.println(openingString);
		
		measures.add(composer.beginComposing());
		while (true) {
			Double ticker = 0.0; // TODO align this to currentTimeMillis() ? could prevent possible stutter
			if (measures.size() < 1)
				measures.add(composer.writeNextMeasure());
			measure = measures.poll();
			System.out.println("[Measure: " + measure.getMeasureNumber() + "] " + measure.getMetaInfo());
			System.out.println(measure.stringDrawing());
			//		System.out.println("Notes: " + measure.getNotes(measure1.getInstruments().iterator().next(), 0, measure1.length()));
			List<Double> times = new ArrayList<>(measure.getTimes());
			Collections.sort(times);

			double bpm = measure.getBpm();
			double beatValue = measure.beatValue().duration();
			// millis = time / beatValue / bpm * (60,000 millis/min)

			for (Double time : times) {
				if (!empty) {
					final String input = receiveUserInput();
					if (STOP_COMMANDS.contains(input)) {
						// TODO figure out best stopping procedure
						inputThread.end();
						composer.finishComposing();
						System.out.println(closingString);
						System.exit(0); // best termination solution?
						return; // dead code
					}
					Measure onTheFlyMeasure = composer.receiveInput(input);
					if (onTheFlyMeasure != null)
						measures.add(onTheFlyMeasure);
				}
				
				// check if time < ticker? should be unnecessary
				if (ticker < time)
					waitWholeNotes(time - ticker, beatValue, bpm);
				ticker = time;
				
				for (performance.instrument.Instrument instrument : measure.getInstruments()) {
					List<MidiNote> notes = measure.getNotes(instrument, time);
					for (MidiNote note : notes) {
						noteOn(0, note.getPitch(), (int) (maxVolume * note.getDynamic().volume())); // TODO
					}
				}
				
			}
			if (ticker < measure.length())
				waitWholeNotes(measure.length() - ticker, beatValue, bpm);
			for (int i=1; i<120; i++) // TODO adjust midipitch range
				noteOff(0, i); // FIXME not working for some unknown reason
		}
	}
	
	private static void javaSoundPractice() throws MidiUnavailableException, InvalidMidiDataException {
		System.out.println("loaded instruments: " + synthesizer.getLoadedInstruments().length);
		System.out.println("Synthesizer max polyphony: " + synthesizer.getMaxPolyphony());
		Soundbank defaultSoundbank = synthesizer.getDefaultSoundbank();
		System.out.println("soundbank instruments: " + defaultSoundbank.getInstruments().length);
		Instrument[] availableInstruments = synthesizer.getAvailableInstruments(); // size 129
		Instrument instrument = availableInstruments[10];
		System.out.print("Instrument: " + instrument.getName());
		System.out.println(" - instrument supported? " + synthesizer.isSoundbankSupported(instrument.getSoundbank()));
		
		synthesizer.remapInstrument(availableInstruments[0], instrument);
		
		System.out.println("Loading instrument, success? " + synthesizer.loadInstrument(instrument));
		System.out.println("Loading all instruments, success? " + synthesizer.loadAllInstruments(instrument.getSoundbank()));
		System.out.println("loaded instruments: " + synthesizer.getLoadedInstruments().length);
		System.out.println("synth channels: " + synthesizer.getChannels().length);
		MidiChannel channel1 = synthesizer.getChannels()[0];
		channel1.programChange(instrument.getPatch().getBank(), instrument.getPatch().getProgram());
		channel1.setChannelPressure(50);
		channel1.programChange(instrument.getPatch().getProgram());
		
	    System.out.println("Pressure: " + channel1.getChannelPressure());
	    System.out.println("Mono: " + channel1.getMono());
	    System.out.println("Omni: " + channel1.getOmni());
	    System.out.println("Pitch bend: " + channel1.getPitchBend()); // it seems my default synthesizer does not support pitch bend?
	    System.out.println("Program: " + channel1.getProgram());
	    // Check for null; maybe not all 16 channels exist.
	    if (channel1 != null) {
//	         channel1.noteOn(60, 93); 
	    }
	    
	    
//	    ShortMessage myMsg = new ShortMessage();
	    // Play the note Middle C (60) moderately loud
	    // (velocity = 93)on channel 4 (zero-based).
//	    myMsg.setMessage(ShortMessage.NOTE_ON, 4, 60, 93);
	    ShortMessage programChangeMsg = new ShortMessage();
	    programChangeMsg.setMessage(ShortMessage.PROGRAM_CHANGE, instrument.getPatch().getBank(), instrument.getPatch().getProgram());
	    synthRcvr.send(programChangeMsg, -1);
//	    synthRcvr.send(myMsg, -1); // -1 means no time stamp
	    noteOn(4, 60, 93);
	    try { Thread.sleep(1000); } catch (Exception e) { }
	    ShortMessage decayMsg = new ShortMessage();
	    decayMsg.setMessage(ShortMessage.CHANNEL_PRESSURE, 0, 0);
	    synthRcvr.send(decayMsg, -1);
	    ShortMessage offMsg = new ShortMessage();
	    offMsg.setMessage(ShortMessage.NOTE_OFF, 4, 60);
	    synthRcvr.send(offMsg, -1);
//	    synthRcvr.send(myMsg, -1); // -1 means no time stamp
	    noteOn(4, 60, 93);
	    System.out.println("off requested");
	    try { Thread.sleep(1000); } catch (Exception e) { }
	    try { Thread.sleep(1000); } catch (Exception e) { }
	    try { Thread.sleep(1000); } catch (Exception e) { }
	}
	
	private static void noteOn(int channel, int midiPitch, int velocity) {
		ShortMessage noteOnMsg = new ShortMessage();
		try {
			noteOnMsg.setMessage(ShortMessage.NOTE_ON, channel, midiPitch, velocity);
		} catch (InvalidMidiDataException e) {
			e.printStackTrace();
		}
		synthRcvr.send(noteOnMsg, -1);
	}
	
	private static void noteOff(int channel, int midiPitch) {
		ShortMessage noteOffMsg = new ShortMessage();
		try {
			noteOffMsg.setMessage(ShortMessage.NOTE_OFF, channel, midiPitch);
		} catch (InvalidMidiDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		synthRcvr.send(noteOffMsg,  -1);
	}
	
	private static void waitWholeNotes(double wholeNotes, double beatValue, double bpm) {
		int millis = (int) (wholeNotes / beatValue / bpm * 60000);
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
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
