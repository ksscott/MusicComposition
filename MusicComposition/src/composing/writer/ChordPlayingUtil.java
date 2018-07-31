package composing.writer;

import java.util.Iterator;
import java.util.List;

import performance.Dynamic;
import performance.MidiNote;
import performance.instrument.Instrument;
import theory.Chord;
import theory.Measure;
import theory.MidiPitch;
import theory.analysis.Phrase;

public class ChordPlayingUtil {

	private ChordPlayingUtil() {} // util class
	
	/** Modifies the given measure */
	public static void playChordOnBeats(Chord chord, Instrument instrument, Measure measure) {
		measure.addInstrument(instrument);
		double beatValue = measure.beatValue();
		for (int beat=0; beat<measure.beats(); beat++) {
			for (MidiPitch pitch : chord) {
				MidiNote note = new MidiNote(pitch, beatValue);
				if (beat != 0)
					note.setDynamic(Dynamic.below(note.getDynamic()));
				measure.add(instrument, note, beat*beatValue);
			}
		}
	}
	
	/** Assumes quarter-note beats */
	public static Phrase playChordOnBeats(Chord chord) {
		Phrase phrase = new Phrase();
		for (MidiPitch pitch : chord)
			phrase.add(new MidiNote(pitch, 1/4.0), 0);
		return phrase;
	}
	
	/** Modifies the given measure */
	public static void arpeggiateChordHalfBeats(Chord chord, Instrument instrument, Measure measure) {
		Iterator<MidiPitch> arpeggiator = chord.arpeggiator();
		measure.addInstrument(instrument);
		double beatValue = measure.beatValue();
		double noteLength = beatValue/2.0;
		for (int beat=0; beat<measure.beats(); beat++) {
			MidiNote note1 = new MidiNote(arpeggiator.next(), noteLength);
			MidiNote note2 = new MidiNote(arpeggiator.next(), noteLength);
			if (beat != 0)
				note1.setDynamic(Dynamic.below(note1.getDynamic()));
			note2.setDynamic(Dynamic.below(note1.getDynamic()));
			measure.add(instrument,  note1, beat*beatValue);
			measure.add(instrument,  note2, beat*beatValue + noteLength);
		}
	}
	
	/** Assumes quarter-note beats */
	public static Phrase arpeggiateChordHalfBeats(Chord chord) {
		Iterator<MidiPitch> arpeggiator = chord.arpeggiator();
		Phrase phrase = new Phrase();
		MidiPitch first = arpeggiator.next();
		MidiPitch next = first;
		do {
			phrase.add(new MidiNote(next, 1/8.0));
			next = arpeggiator.next();
		} while (!next.equals(first));
		return phrase;
	}
	
	/** Modifies the given measure */
	public static void triplets(Chord chord, Instrument instrument, Measure measure) {
		List<MidiPitch> pitches = chord.get();
		if (pitches.size() != 3)
			throw new IllegalArgumentException("Only chords with exactly three pitches are supported.");
		measure.addInstrument(instrument);
		double beatValue = measure.beatValue();
		double noteLength = beatValue/3.0;
		for (int beat=0; beat<measure.beats(); beat++) {
			for (int i=0; i<pitches.size(); i++) {
				MidiNote note = new MidiNote(pitches.get(i), noteLength);
				if (i == 0)
					note.setDynamic(Dynamic.above(note.getDynamic()));
				if (beat != 0)
					note.setDynamic(Dynamic.below(note.getDynamic()));
				measure.add(instrument, note, beat*beatValue + i*noteLength);
			}
		}
	}
	
	/** Assumes quarter-note beats */
	public static Phrase triplets(Chord chord) {
		List<MidiPitch> pitches = chord.get();
		if (pitches.size() != 3)
			throw new IllegalArgumentException("Only chords with exactly three pitches are supported.");
		Phrase phrase = new Phrase();
		for (MidiPitch pitch : chord) {
			phrase.add(new MidiNote(pitch, 1/12.0));
		}
		return phrase;
	}
	
	/** Modifies the given measure */
	public static void albertiBassHalfBeats(Chord chord, Instrument instrument, Measure measure) {
		Iterator<MidiPitch> albertiBass = chord.albertiBass();
		measure.addInstrument(instrument);
		double beatValue = measure.beatValue();
		double noteLength = beatValue/2.0;
		for (int beat=0; beat<measure.beats(); beat++) {
			MidiNote note1 = new MidiNote(albertiBass.next(), noteLength);
			MidiNote note2 = new MidiNote(albertiBass.next(), noteLength);
			if (beat != 0)
				note1.setDynamic(Dynamic.below(note1.getDynamic()));
			note2.setDynamic(Dynamic.below(note1.getDynamic()));
			measure.add(instrument,  note1, beat*beatValue);
			measure.add(instrument,  note2, beat*beatValue + noteLength);
		}
	}
	
	/** Assumes quarter-note beats */
	public static Phrase albertiBassHalfBeats(Chord chord) {
		Iterator<MidiPitch> albertiBass = chord.albertiBass(); // checks that size == 3
		Phrase phrase = new Phrase();
		MidiPitch first = albertiBass.next();
		MidiPitch next = first;
		do {
			phrase.add(new MidiNote(next, 1/8.0));
			next = albertiBass.next();
		} while (!next.equals(first));
		return phrase;
	}
	
	/** Modifies the given measure */
	public static void chordOscillation(Chord chord, Instrument instrument, Measure measure) {
		List<MidiPitch> pitches = chord.get();
		if (pitches.size() < 3)
			throw new IllegalArgumentException("Must have at least three pitches to oscillate a chord.");
		measure.addInstrument(instrument);
		
		MidiPitch bass = pitches.get(0);
		List<MidiPitch> rest = pitches.subList(1, pitches.size());
		double beatValue = measure.beatValue();
		double noteLength = beatValue/2.0;
		
		for (int beat=0; beat<measure.beats(); beat++) {
			Dynamic dynamic = null;
			for (MidiPitch pitch : rest) {
				MidiNote chordNote = new MidiNote(pitch, noteLength);
				if (beat != 0)
					chordNote.setDynamic(Dynamic.below(chordNote.getDynamic()));
				measure.add(instrument, chordNote, beat*beatValue);
				dynamic = chordNote.getDynamic();
			}
			MidiNote bassNote = new MidiNote(bass, noteLength);
			bassNote.setDynamic(Dynamic.below(dynamic));
			measure.add(instrument, bassNote, beat*beatValue + noteLength);
		}
	}
	
	/** Assumes quarter-note beats */
	public static Phrase chordOscillation(Chord chord) {
		List<MidiPitch> pitches = chord.get();
		if (pitches.size() < 3)
			throw new IllegalArgumentException("Must have at least three pitches to oscillate a chord.");
		
		Phrase phrase = new Phrase();
		MidiPitch bass = pitches.get(0);	
		List<MidiPitch> rest = pitches.subList(1, pitches.size());
		
		for (MidiPitch pitch : rest)
			phrase.add(new MidiNote(pitch, 1/8.0), 0); // add all on the beat
		phrase.add(new MidiNote(bass, 1/8.0)); // add after the beat
		
		return phrase;
	}
	
}
