package main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
	
	private static Queue<String> userInputs = new LinkedList<>();
	/** whether user input queue is empty */
	private static boolean empty = true;
	private static final List<String> STOP_COMMANDS = Arrays.asList(new String[] { "stop", "end", "quit", "kill" });

	private static Synthesizer synthesizer;
	private static Receiver synthRcvr;
	
	public static void main(String[] args) throws MidiUnavailableException, InvalidMidiDataException {
		
		synthesizer = MidiSystem.getSynthesizer();
		synthesizer.open();
	    synthRcvr = synthesizer.getReceiver();
	    
//	    javaSoundPractice();
	    composeMusic();
	}
	
	private static void composeMusic() {
		int maxVolume = 127;
		Queue<Measure> measures = new PriorityQueue<>();
		Measure measure;
		Composer composer = new Composer();
		InputThread inputThread = new InputThread();
		inputThread.start();
		InstrumentBank instrumentBank = new InstrumentBank();
		
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
			double beatValue = measure.beatValue();
			// millis = time / beatValue / bpm * (60,000 millis/min)

			for (Double time : times) {
				if (!empty) {
					final String input = receiveUserInput(); // TODO move this
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
					Instrument midiInstrument = instrumentBank.translate(instrument);
					List<MidiNote> notes = measure.getNotes(instrument, time);
					if (!notes.isEmpty() &&!instrument.equals(performance.instrument.Instrument.PIANO))
						allNotesOff(midiInstrument);
					for (MidiNote note : notes) {
						noteOn(midiInstrument, note.getPitch(), (int) (maxVolume * note.getDynamic().volume())); // TODO
					}
				}
				
			}
			if (ticker < measure.length())
				waitWholeNotes(measure.length() - ticker, beatValue, bpm);
			
			// attempt to turn off all notes at measure end:
			for (performance.instrument.Instrument instrument : measure.getInstruments())
				allNotesOff(instrumentBank.translate(instrument)); // FIXME not working for some unknown reason
		}
	}
	
	@SuppressWarnings("unused")
	private static void javaSoundPractice() throws MidiUnavailableException, InvalidMidiDataException {
		System.out.println("loaded instruments: " + synthesizer.getLoadedInstruments().length);
		System.out.println("Synthesizer max polyphony: " + synthesizer.getMaxPolyphony());
		Soundbank defaultSoundbank = synthesizer.getDefaultSoundbank();
		System.out.println("soundbank instruments: " + defaultSoundbank.getInstruments().length);
		Instrument[] availableInstruments = synthesizer.getAvailableInstruments(); // size 129
		Instrument instrument = availableInstruments[73];
		System.out.print("Instrument: " + instrument.getName());
		System.out.println(" - instrument supported? " + synthesizer.isSoundbankSupported(instrument.getSoundbank()));
		
//		System.out.println("Instrument 0: " + availableInstruments[0].getName());
		System.out.println("Remapping instrument, success? "+ synthesizer.remapInstrument(availableInstruments[0], instrument));
		System.out.println("Instrument 0: " + availableInstruments[0].getName());
		
		System.out.println("Loading instrument, success? " + synthesizer.loadInstrument(instrument));
		System.out.println("Loading all instruments, success? " + synthesizer.loadAllInstruments(instrument.getSoundbank()));
		System.out.println("loaded instruments: " + synthesizer.getLoadedInstruments().length);
		System.out.println("synth channels: " + synthesizer.getChannels().length);
		MidiChannel channel1 = synthesizer.getChannels()[0];
		channel1.programChange(instrument.getPatch().getBank(), instrument.getPatch().getProgram());
		channel1.setChannelPressure(50);
		channel1.programChange(instrument.getPatch().getProgram());
		for (MidiChannel channel : synthesizer.getChannels())
			channel.programChange(instrument.getPatch().getProgram());
		System.out.println("Instrument 0: " + availableInstruments[0].getName());
		System.out.println("Instrument: " + instrument.getName());
		
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
	    noteOn(instrument, 60, 93);
	    try { Thread.sleep(1000); } catch (Exception e) { }
	    ShortMessage decayMsg = new ShortMessage();
	    decayMsg.setMessage(ShortMessage.CHANNEL_PRESSURE, 0, 0);
	    synthRcvr.send(decayMsg, -1);
	    ShortMessage offMsg = new ShortMessage();
	    offMsg.setMessage(ShortMessage.NOTE_OFF, 4, 60);
	    synthRcvr.send(offMsg, -1);
//	    noteOff(4, 60);
//	    synthRcvr.send(myMsg, -1); // -1 means no time stamp
	    noteOn(instrument, 60, 93);
	    System.out.println("off requested");
	    try { Thread.sleep(1000); } catch (Exception e) { }
	    try { Thread.sleep(1000); } catch (Exception e) { }
	    try { Thread.sleep(1000); } catch (Exception e) { }
	}
	
	private static void noteOn(Instrument instrument, int midiPitch, int velocity) {
		ShortMessage noteOnMsg = new ShortMessage();
		int channel = MidiChannelRegistrar.getInstance().getForInstrument(instrument);
		try {
			noteOnMsg.setMessage(ShortMessage.NOTE_ON, channel, midiPitch, velocity);
		} catch (InvalidMidiDataException e) {
			e.printStackTrace();
		}
		synthRcvr.send(noteOnMsg, -1);
	}
	
	private static void noteOff(Instrument instrument, int midiPitch) {
//		ShortMessage noteOffMsg = new ShortMessage();
		int channel = MidiChannelRegistrar.getInstance().getForInstrument(instrument);
		synthesizer.getChannels()[channel].noteOff(midiPitch);
//		try {
//			noteOffMsg.setMessage(ShortMessage.NOTE_OFF, channel, midiPitch);
//		} catch (InvalidMidiDataException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		synthRcvr.send(noteOffMsg,  -1);
	}
	
	private static void allNotesOff(Instrument instrument) {
		int channel = MidiChannelRegistrar.getInstance().getForInstrument(instrument);
		synthesizer.getChannels()[channel].allNotesOff();;
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
	
	private static class InstrumentBank {
		
		JavaSoundTimbre timbre = JavaSoundTimbre.getInstance();
		private Map<performance.instrument.Instrument,Instrument> map;
		
		public InstrumentBank() {
			this.map = new HashMap<>();
		}
		
		public Instrument translate(performance.instrument.Instrument requested) {
			if (map.containsKey(requested))
				return map.get(requested);
			int instrumentCode = timbre.render(requested);
			Instrument midiInstrument = synthesizer.getAvailableInstruments()[instrumentCode];
			map.put(requested, midiInstrument);
			return midiInstrument;
		}
		
	}
	
	private static class MidiChannelRegistrar { // FIXME Apparently "Channel 10" is reserved for drum sounds
		
		private static MidiChannelRegistrar instance;
		private static Queue<MidiChannel> unusedChannels;
		private static Map<Instrument,MidiChannel> registered;
		private static Map<MidiChannel,Integer> channelIDs;
		
		private MidiChannelRegistrar() {
			unusedChannels = new LinkedList<>();
			registered = new HashMap<>();
			channelIDs = new HashMap<>();
			
			int i=0;
		    for (MidiChannel channel : synthesizer.getChannels()) {
		    	unusedChannels.add(channel);
		    	channelIDs.put(channel, i++);
		    }
		}
		
		public static MidiChannelRegistrar getInstance() {
			if (instance == null)
				instance = new MidiChannelRegistrar();
			return instance;
		}
		
		/**
		 * Locates and appropriate MidiChannel and loads the given instrument if necessary.
		 * 
		 * @param instrument to play on the returned channel
		 * @return index of the MidiChannel with the given instrument loaded
		 * @throws IllegalStateException if all channels are in use by other instruments
		 */
		public synchronized int getForInstrument(Instrument instrument) {
			if (registered.containsKey(instrument))
				return channelIDs.get(registered.get(instrument));
			
			if (unusedChannels.isEmpty())
				throw new IllegalStateException("No more free channels!");
			
			int program = instrument.getPatch().getProgram();
			MidiChannel channel = null;
			
			// check if an unused channel is already setup for the instrument
			for (MidiChannel unused : unusedChannels) {
				if (unused.getProgram() == program) {
					channel = unused;
					break;
				}
			}
			if (channel != null) {
				unusedChannels.remove(channel);
			} else {
				channel = unusedChannels.poll();
				channel.programChange(instrument.getPatch().getProgram());
			}
			registered.put(instrument, channel);
			Integer channelID = channelIDs.get(channel);
//			System.out.println("Returned channel " + channelID + " for " + instrument.getName());
			return channelID;
		}
		
		public synchronized void release(Instrument instrument) {
			MidiChannel channel = registered.get(instrument);
			if (channel != null) {
				unusedChannels.add(channel);
				registered.remove(instrument);
			}
		}
		
		public synchronized void releaseAll() {
			unusedChannels = new LinkedList<>();
			for (MidiChannel channel : synthesizer.getChannels())
				unusedChannels.add(channel);
			registered = new HashMap<>();
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
