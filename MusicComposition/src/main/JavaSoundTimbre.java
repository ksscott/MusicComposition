package main;

import performance.Timbre;
import performance.instrument.Instrument;

public class JavaSoundTimbre implements Timbre {

	/**
	 * @param instrument to be rendered into Java Sound
	 * @return Midi code for the instrument
	 * @see {@link https://en.wikipedia.org/wiki/General_MIDI#Program_change_events}
	 */
	public static int render(Instrument instrument) {
		if (instrument == Instrument.SOPRANO_VOICE
				|| instrument == Instrument.ALTO_VOICE
				|| instrument == Instrument.TENOR_VOICE
				|| instrument == Instrument.BASS_VOICE
				)
			return 54;
		
		if (instrument == Instrument.SOLO) 
			return 66; // currently Tenor Saxophone
		if (instrument == Instrument.PIANO) 
			return 0;
//		
		if (instrument == Instrument.TRUMPET) 
			return 56;
		if (instrument == Instrument.BASS) 
			return 32;
		
		return 0;
	}
	
}
