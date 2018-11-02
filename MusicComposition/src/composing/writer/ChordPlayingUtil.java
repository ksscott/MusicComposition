package composing.writer;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import composing.outline.DynamicOutline;
import composing.outline.RhythmicDensityOutline;
import composing.outline.RhythmicOutline;
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
					note.setDynamic(note.getDynamic().down());
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
				note1.setDynamic(note1.getDynamic().down());
			note2.setDynamic(note1.getDynamic().down());
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
		if (chord.size() != 3)
			throw new IllegalArgumentException("Only chords with exactly three pitches are supported.");
		measure.addInstrument(instrument);
		double beatValue = measure.beatValue();
		double noteLength = beatValue/3.0;
		for (int beat=0; beat<measure.beats(); beat++) {
			for (int i=0; i<pitches.size(); i++) {
				MidiNote note = new MidiNote(pitches.get(i), noteLength);
				if (i == 0)
					note.setDynamic(note.getDynamic().up());
				if (beat != 0)
					note.setDynamic(note.getDynamic().down());
				measure.add(instrument, note, beat*beatValue + i*noteLength);
			}
		}
	}
	
	/** Assumes quarter-note beats */
	public static Phrase triplets(Chord chord) {
		if (chord.size() != 3)
			throw new IllegalArgumentException("Only chords with exactly three pitches are supported.");
		Phrase phrase = new Phrase();
		
		for (MidiPitch pitch : chord)
			phrase.add(new MidiNote(pitch, 1/12.0));
		
		return phrase;
	}
	
	/** Modifies the given measure */
	public static void albertiTriplets(Chord chord, Instrument instrument, Measure measure) {
		if (chord.size() != 3)
			throw new IllegalArgumentException("Only chords with exactly three pitches are supported.");
		measure.addInstrument(instrument);
		double beatValue = measure.beatValue();
		double noteLength = beatValue/3.0;
		for (int beat=0; beat<measure.beats(); beat++) {
			for (int i=0; i<3; i++) {
				Iterator<MidiPitch> albertiBass = chord.albertiBass();
				MidiNote note = new MidiNote(albertiBass.next(), noteLength);
				if (i == 0)
					note.setDynamic(note.getDynamic().up());
				if (beat != 0)
					note.setDynamic(note.getDynamic().down());
				measure.add(instrument, note, beat*beatValue + i*noteLength);
			}
		}
	}
	
	/** Assumes quarter-note beats */
	public static Phrase albertiTriplets(Chord chord) {
		if (chord.size() != 3)
			throw new IllegalArgumentException("Only chords with exactly three pitches are supported.");
		Phrase phrase = new Phrase();
		
		Iterator<MidiPitch> albertiBass = chord.albertiBass();
		for (int i=0; i<3; i++)
			phrase.add(new MidiNote(albertiBass.next(), 1/12.0));
		
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
				note1.setDynamic(note1.getDynamic().down());
			note2.setDynamic(note1.getDynamic().down());
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
		if (chord.size() < 3)
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
					chordNote.setDynamic(chordNote.getDynamic().down());
				measure.add(instrument, chordNote, beat*beatValue);
				dynamic = chordNote.getDynamic();
			}
			MidiNote bassNote = new MidiNote(bass, noteLength);
			bassNote.setDynamic(dynamic.down());
			measure.add(instrument, bassNote, beat*beatValue + noteLength);
		}
	}
	
	/** Assumes quarter-note beats */
	public static Phrase chordOscillation(Chord chord) {
		List<MidiPitch> pitches = chord.get();
		if (chord.size() < 3)
			throw new IllegalArgumentException("Must have at least three pitches to oscillate a chord.");
		
		Phrase phrase = new Phrase();
		MidiPitch bass = pitches.get(0);	
		List<MidiPitch> rest = pitches.subList(1, pitches.size());
		
		for (MidiPitch pitch : rest)
			phrase.add(new MidiNote(pitch, 1/8.0), 0); // add all on the beat
		phrase.add(new MidiNote(bass, 1/8.0)); // add after the beat
		
		return phrase;
	}
	
	public static Phrase subdivide(Chord chord, RhythmicDensityOutline density, DynamicOutline dynamic) {
		if (chord.isEmpty())
			throw new IllegalArgumentException("Cannot play a chord without pitches.");
		List<MidiPitch> notes = chord.get();
		
		Phrase phrase = new Phrase();
		RhythmicOutline rhythms = RhythmicOutline.simpleDensityReification(density, dynamic);
		
		List<Double> times = rhythms.getTimes();
		if (times.isEmpty())
			return phrase;
		Collections.sort(times);
		
		Iterator<Double> timesIt = times.iterator();
		Double thisTime = timesIt.next();
		Double nextTime = null;
		int index = 0;
		while (true) {
			if (timesIt.hasNext()) {
				nextTime = timesIt.next();
				Double duration = Math.max(1/8.0, nextTime - thisTime); // TODO change
				MidiPitch pitch = notes.get(index++ % notes.size()); // TODO change
				MidiNote note = new MidiNote(pitch, duration);
				note.setDynamic(dynamic.get(thisTime));
				phrase.add(note, thisTime);
				
				thisTime = nextTime;
			} else {
				Double duration = Math.max(1/8.0, 1.0 - thisTime); // TODO change
				MidiPitch pitch = notes.get(index++ % notes.size()); // TODO change
				MidiNote note = new MidiNote(pitch, duration);
				note.setDynamic(dynamic.get(thisTime));
				phrase.add(note, thisTime);
				
				break;
			}
		}
		
		return phrase;
	}
	
	public static Phrase albertiSubdivide(Chord chord, RhythmicDensityOutline density, DynamicOutline dynamic) {
		if (chord.isEmpty())
			throw new IllegalArgumentException("Cannot play a chord without pitches.");
		
		Phrase phrase = new Phrase();
		RhythmicOutline rhythms = RhythmicOutline.simpleDensityReification(density, dynamic);
		
		List<Double> times = rhythms.getTimes();
		Collections.sort(times);
		
		
		
		// TODO
		
		return phrase;
	}
	
}
