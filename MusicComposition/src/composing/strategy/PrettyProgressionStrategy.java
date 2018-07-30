package composing.strategy;

import static composing.RandomUtil.roll;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import composing.IncompleteComposition;
import composing.writer.PrettyMelodyWriter;
import performance.Dynamic;
import performance.MidiNote;
import performance.Tempo;
import performance.instrument.Instrument;
import theory.Chord;
import theory.ChordSpec;
import theory.Key;
import theory.Measure;
import theory.MidiPitch;
import theory.analysis.Analysis;
import theory.analysis.Phrase;
import theory.analysis.Section;
import theory.progression.ChordProgressions;
import theory.progression.ChordProgressions.ChordProgression;
import theory.progression.ChordProgressions.KeyChange;
import theory.progression.ChordProgressions.KeyChordProgression;
import theory.progression.VoiceLeading;

/**
 * Yes, it's messy. Leave me alone.
 */
public class PrettyProgressionStrategy extends ChordsSectionWriter {
	
	PrettyMelodyWriter melodyWriter;
	
	protected int octave = 3;
	protected Tempo currentTempo;
	
	protected Instrument piano;
	protected Instrument solo;
	
	public PrettyProgressionStrategy(Key key) {
		super(key);
		this.melodyWriter = new PrettyMelodyWriter();
		this.currentTempo = Tempo.ADAGIETTO;
		this.piano = Instrument.PIANO;
		this.solo = Instrument.SOLO;
	}
	
	@Override
	public Measure generateFirstMeasure() {
		Measure firstBar = composeBar((Measure) null, firstChord);
		writeMelody(Arrays.asList(firstBar), key); // TODO melody jumps up high after first bar; given no context
		return firstBar;
	}

	@Override
	protected int getNextSectionSize(IncompleteComposition composition) {
		return 8;
	}
	
	@Override
	protected ChordProgression nextSectionProgression(Analysis analysis) {
		Key lastKey = key;
		Key nextKey = null;
		
		// NOTE: This method is messy. Bite me.
		
		List<Section> sections = analysis.getSections();
		if (sections.isEmpty())
			return ChordProgressions.standardMajorProgression(lastKey.getTonic());
		
		Section lastSection = sections.get(sections.size()-1);
		Set<Key> lastSectionKeys = lastSection.getAllKeys();
		// FIXME definitely need a more robust way of knowing what key we're supposed to be in
		if (lastSectionKeys.size() < 1) {
			throw new IllegalStateException("Expecting to find Keys in the Analysis.");
		} else if (lastSectionKeys.size() > 1) {
			// key change happened last section
			// WARNING: the following code might get a random key from the last measure
//			System.out.println("COMPOSING SECTION AFTER A KEY CHANGE!!!");
			lastKey = lastSection.getKeys(lastSection.size()).stream().findAny().orElse(key);
			// leave nextKey null to not change keys again
		} else {
			// last section was not a key change section
//			System.out.println("COMPOSING SECTION - TBD");
			lastKey = lastSectionKeys.iterator().next(); // any (only?) key in the last section
			if (sections.size() == 1)
				return ChordProgressions.standardMajorProgression(lastKey.getTonic());
			Section secondLastSection = sections.get(sections.size()-2);
			if (secondLastSection.getKeys(secondLastSection.size()-1).size() >= 1) { // set back to == when done testing
				// presume same as lastKey
				// after two sections in this key, let's change keys
//				System.out.println("ATTEMPTING TO CHANGE KEYS!!!");
				if (lastKey.equals(key)) {
					nextKey = roll(50) ? new Key(lastKey.note(4), Key.MAJOR) : new Key(lastKey.note(5), Key.MAJOR);
				} else {
					// let's come back
					nextKey = key;
				}
			} else {
				// let's write a second section in the key lastKey
			}
			// hijack control flow:
			nextKey = new Key(lastKey.note(4), Key.MAJOR); // circle of fifths!
//			nextKey = lastKey.parallelKey().relativeKey(); // up by minor third; this feels uncomfortable...
//			System.out.println("Last key: " + lastKey + " ... New key: " + nextKey);
		}
		
		KeyChordProgression lastKeyProgression = 
				ChordProgressions.standardMajorProgression(lastKey.getTonic());
		if (nextKey != null) {
			// change keys from lastKey to nextKey
			return new KeyChange(lastKeyProgression, 
					ChordProgressions.standardMajorProgression(nextKey.getTonic()));
		} else {
			return lastKeyProgression;
		}
	}
	
	protected Measure composeBar(Measure lastMeasure, ChordSpec nextChordSpec) {
			Chord previousChord = key.chord(1, octave); // for first measure only
			if (lastMeasure != null) {
				List<MidiNote> lastMeasureNotes = lastMeasure.getNotes(piano, 0, lastMeasure.length());
				if (lastMeasureNotes.size() < 3) {
					throw new IllegalStateException("A random bug appears! ... The plot thickens!"); // rare, have yet to diagnose
				}
				previousChord = new Chord(lastMeasureNotes.stream()
													   .map(MidiNote::getPitch)
													   .map(MidiPitch::new)
													   .collect(Collectors.toList()));
			}
			Measure measure = backgroundChord(previousChord, nextChordSpec, piano);
//			if (lastMeasure != null && roll(50))
//				tieMeasures(lastMeasure, measure);
			measure.setMetaInfo("(" + nextChordSpec + ")");
			
			measure.setBpm(currentTempo.getBpm());
			
			return measure;
		}

	@Override
	protected void onSectionsFilled(IncompleteComposition composition) {
		// melody
		try {
			List<Measure> measuresWithoutMelody = composition.getFuture().stream()
//					.filter(measure -> measure.getMeasureNumber() <= lastEndOfSection) // don't worry about measures outside a section (shouldn't happen)
					.filter(measure -> !measure.getMetaInfo().contains("melody"))
					.collect(Collectors.toList());
			if (measuresWithoutMelody.size() >= 8) {
				writeMelody(measuresWithoutMelody);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @param increase <code>true</code> to increase the tempo by 1, or false to decrease it by 1
	 * @return the tempo after the change
	 */
	public Tempo requestTempoChange(boolean increase) {
		int newTempoOrdinal = currentTempo.ordinal() + (increase ? 1 : -1);
		Tempo[] tempi = Tempo.values();
		// bound:
		newTempoOrdinal = Math.max(0, Math.min(tempi.length-1, newTempoOrdinal));
		currentTempo = tempi[newTempoOrdinal];
		return currentTempo;
	}

	private Measure backgroundChord(Chord previousChord, ChordSpec nextChordSpec, Instrument instrument) {
		Measure measure = new Measure(4, 1/4.0);
		
		int bassMin = MidiPitch.inOctave(key.getTonic(), octave);
		int bassMax = bassMin + 19;
		Chord voiceLeadChord = VoiceLeading.voiceLead(previousChord, nextChordSpec, bassMin, bassMax);
		
		chordOscillation(voiceLeadChord, instrument, measure);
		
		return measure;
	}
	
	@SuppressWarnings("unused")
	private void playChordOnBeats(Chord chord, Instrument instrument, Measure measure) {
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
	
	@SuppressWarnings("unused")
	private void arpeggiateChordHalfBeats(Chord chord, Instrument instrument, Measure measure) {
		Iterator<MidiPitch> arpeggiator = chord.arpeggiator();
		measure.addInstrument(instrument);
		double beatValue = measure.beatValue();
		double noteLength = beatValue/2.0;
		for (int beat=0; beat<measure.beats(); beat++) {
			MidiNote note1 = new MidiNote(arpeggiator.next(), noteLength);
			MidiNote note2 = new MidiNote(arpeggiator.next(), noteLength);
			measure.add(instrument,  note1, beat*beatValue);
			measure.add(instrument,  note2, beat*beatValue + noteLength);
		}
	}
	
	@SuppressWarnings("unused")
	private void albertiBassHalfBeats(Chord chord, Instrument instrument, Measure measure) {
		Iterator<MidiPitch> arpeggiator = chord.albertiBass();
		measure.addInstrument(instrument);
		double beatValue = measure.beatValue();
		double noteLength = beatValue/2.0;
		for (int beat=0; beat<measure.beats(); beat++) {
			MidiNote note1 = new MidiNote(arpeggiator.next(), noteLength);
			MidiNote note2 = new MidiNote(arpeggiator.next(), noteLength);
			measure.add(instrument,  note1, beat*beatValue);
			measure.add(instrument,  note2, beat*beatValue + noteLength);
		}
	}
	
	@SuppressWarnings("unused")
	private void chordOscillation(Chord chord, Instrument instrument, Measure measure) {
		List<MidiPitch> pitches = chord.get();
		if (pitches.size() < 3)
			throw new IllegalArgumentException("Must have at least three pitches to oscillate a chord.");
		measure.addInstrument(instrument);
		
		MidiPitch bass = pitches.get(0);
		List<MidiPitch> rest = pitches.subList(1, pitches.size());
		double beatValue = measure.beatValue();
		double noteLength = beatValue/2.0;
		
		for (int beat=0; beat<measure.beats(); beat++) {
			for (MidiPitch pitch : rest) {
				MidiNote chordNote = new MidiNote(pitch, noteLength);
				if (beat != 0)
					chordNote.setDynamic(Dynamic.below(chordNote.getDynamic()));
				measure.add(instrument, chordNote, beat*beatValue);
			}
			MidiNote bassNote = new MidiNote(bass, noteLength);
			bassNote.setDynamic(Dynamic.below(bassNote.getDynamic()));
			measure.add(instrument, bassNote, beat*beatValue + noteLength);
		}
	}
	
	@SuppressWarnings("unused")
	private void tieMeasures(Measure lastMeasure, Measure nextMeasure) {
		for (Instrument instrument : lastMeasure.getInstruments()) {
			List<MidiNote> lastNotes = lastMeasure.getNotes(instrument, lastMeasure.beatValue()*(lastMeasure.beats()-1));
			List<MidiNote> firstNotes = nextMeasure.getNotes(instrument, 0.0); // hard coded :(
			if (lastNotes.size() != firstNotes.size())
				System.out.println("Warning! Tied measure has unequal numbers of notes for instrument "
						+ instrument + " " + lastNotes.size() + " -> " + firstNotes.size());
			Comparator<MidiNote> noteSorter = new Comparator<MidiNote>() {
				@Override public int compare(MidiNote o1, MidiNote o2) { return o1.getPitch() - o2.getPitch(); }
			};
			Collections.sort(lastNotes, noteSorter);
			Collections.sort(firstNotes, noteSorter);
			for (int i=0; i<lastNotes.size(); i++) {
				MidiNote last = lastNotes.get(i);
				MidiNote next = firstNotes.get(i);
				MidiNote.tieOver(last, next);
			}
		}
	}
	
	private void writeMelody(List<Measure> measuresWithoutMelody) {
		writeMelody(measuresWithoutMelody, null);
	}

	private void writeMelody(List<Measure> measuresWithoutMelody, Key key) {
		measuresWithoutMelody.forEach(measure -> measure.addInstrument(solo));
		Phrase melody = key == null ? melody = melodyWriter.writeMelody(measuresWithoutMelody)
									: melodyWriter.writeMelody(measuresWithoutMelody, key);
		Measure.writeOnto(solo, melody, measuresWithoutMelody, 0.0);
		measuresWithoutMelody.forEach(measure -> measure.setMetaInfo(measure.getMetaInfo() + " melody"));
	}

	public String toString() {
		return "Pretty Chord Progression";
	}
	
}
