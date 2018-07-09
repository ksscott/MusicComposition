package main;

import javax.sound.midi.Instrument;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Synthesizer;

public class JavaSoundInterfacer {

	public static void main(String[] args) throws MidiUnavailableException, InvalidMidiDataException {
		Synthesizer synthesizer = MidiSystem.getSynthesizer();
		System.out.println("Synthesizer max polyphony: " + synthesizer.getMaxPolyphony());
		Soundbank defaultSoundbank = synthesizer.getDefaultSoundbank();
		System.out.println("soundbank instruments: " + defaultSoundbank.getInstruments().length);
		Instrument[] availableInstruments = synthesizer.getAvailableInstruments();
		Instrument piano = availableInstruments[10];
		
		synthesizer.loadInstrument(piano);
		System.out.println("synth channels: " + synthesizer.getChannels().length);
		MidiChannel channel1 = synthesizer.getChannels()[0];
		channel1.programChange(piano.getPatch().getBank(), piano.getPatch().getProgram());
		channel1.setChannelPressure(50);
		channel1.programChange(piano.getPatch().getProgram());
		
	    System.out.println("Pressure: " + channel1.getChannelPressure());
	    System.out.println("Mono: " + channel1.getMono());
	    System.out.println("Omni: " + channel1.getOmni());
	    System.out.println("Pitch bend: " + channel1.getPitchBend());
	    System.out.println("Program: " + channel1.getProgram());
	    // Check for null; maybe not all 16 channels exist.
	    if (channel1 != null) {
//	         channel1.noteOn(60, 93); 
	    }
	    
	    synthesizer.open();
	    Receiver synthRcvr = synthesizer.getReceiver();
	    ShortMessage myMsg = new ShortMessage();
	    // Play the note Middle C (60) moderately loud
	    // (velocity = 93)on channel 4 (zero-based).
	    myMsg.setMessage(ShortMessage.NOTE_ON, 4, 60, 93); 
	    ShortMessage programChangeMsg = new ShortMessage();
	    programChangeMsg.setMessage(ShortMessage.PROGRAM_CHANGE, piano.getPatch().getBank(), piano.getPatch().getProgram());
	    synthRcvr.send(programChangeMsg, -1);
	    synthRcvr.send(myMsg, -1); // -1 means no time stamp
	    try { Thread.sleep(1000); } catch (Exception e) { }
	    ShortMessage decayMsg = new ShortMessage();
	    decayMsg.setMessage(ShortMessage.CHANNEL_PRESSURE, 0, 0);
	    ShortMessage offMsg = new ShortMessage();
	    offMsg.setMessage(ShortMessage.NOTE_OFF, 4, 60);
//	    synthRcvr.send(offMsg, -1);
	    synthRcvr.send(myMsg, -1); // -1 means no time stamp
	    System.out.println("off requested");
	    // while (true) {}
	}
	
}
