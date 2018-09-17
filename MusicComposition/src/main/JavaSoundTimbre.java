package main;

import java.util.HashMap;
import java.util.Map;

import performance.Timbre;
import performance.instrument.Instrument;

public class JavaSoundTimbre implements Timbre {
	
	private static JavaSoundTimbre instance;
	
	private static Map<Instrument,Integer> map = new HashMap<>();
	
	/**
	 * 
	 */
	private JavaSoundTimbre() {
		// VOICE
		map.put(Instrument.SOPRANO_VOICE, 54);
		map.put(Instrument.ALTO_VOICE, 54);
		map.put(Instrument.TENOR_VOICE, 54);
		map.put(Instrument.BASS_VOICE, 54);
		
		map.put(Instrument.PIANO, 0);
		
		// STRINGS
		map.put(Instrument.VIOLIN, 40);
		map.put(Instrument.VIOLA, 41);
		map.put(Instrument.CELLO, 42);
		map.put(Instrument.CONTRABASS, 43);
		map.put(Instrument.PIZZICATO_STRINGS, 45);
		map.put(Instrument.HARP, 46);
		
		// PIPES
		map.put(Instrument.FLUTE, 73);
		
		// BRASS
		map.put(Instrument.TRUMPET, 56);
		map.put(Instrument.MUTED_TRUMPET, 59);
		map.put(Instrument.FRENCH_HORN, 60);
		
		// REEDS
		map.put(Instrument.BARITONE_SAXOPHONE, 67);
		
		// GUITAR
		map.put(Instrument.NYLON_GUITAR, 24);
		map.put(Instrument.ACOUSTIC_BASS, 32);
		
		// CHROMATIC PERCUSSION
		map.put(Instrument.XYLOPHONE, 13);
		
		// "ETHNIC"
		map.put(Instrument.SITAR, 104);
		map.put(Instrument.BAGPIPE, 109);
		
		// PERCUSSIVE
		map.put(Instrument.STEEL_DRUMS, 114);
	}

	public static JavaSoundTimbre getInstance() {
		if (instance == null)
			instance = new JavaSoundTimbre();
		return instance;
	}
	
	/**
	 * @param instrument to be rendered into Java Sound
	 * @return Midi code for the instrument
	 * @see {@link https://en.wikipedia.org/wiki/General_MIDI#Program_change_events}
	 */
	public int render(Instrument instrument) {
		Integer integer = map.get(instrument);
		return integer == null ? 0 : integer;
	}
	
}
