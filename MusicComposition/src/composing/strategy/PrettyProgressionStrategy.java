package composing.strategy;

import static composing.RandomUtil.roll;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import composing.IncompleteComposition;
import composing.writer.PrettyMelodyWriter;
import theory.Chord;
import theory.ChordProgressions;
import theory.ChordProgressions.ChordProgression;
import theory.ChordProgressions.KeyChange;
import theory.ChordProgressions.KeyChordProgression;
import theory.ChordSpec;
import theory.Dynamic;
import theory.Key;
import theory.Measure;
import theory.MidiNote;
import theory.MidiPitch;
import theory.Tempo;
import theory.analysis.Analysis;
import theory.analysis.Phrase;
import theory.analysis.Section;

/**
 * Yes, it's messy. Leave me alone.
 */
public class PrettyProgressionStrategy implements ComposingStrategy {
	
	PrettyMelodyWriter melodyWriter;
	
	protected int octave = 3;
	protected Key key;
	protected Tempo currentTempo;
	
	protected ChordSpec firstChord;
	
	public PrettyProgressionStrategy(Key key) {
		this.melodyWriter = new PrettyMelodyWriter();
		this.key = key;
		this.currentTempo = Tempo.ADAGIETTO;
		firstChord = key.chordSpec(1);
	}
	
	@Override
	public Measure generateFirstMeasure() {
		Measure firstBar = composeBar((Measure) null, firstChord);
		writeMelody(Arrays.asList(firstBar), key); // TODO melody jumps up high after first bar; given no context
		return firstBar;
	}

	@Override
	public boolean iterate(IncompleteComposition composition) {
		int sectionSize = 8;
		
		final Queue<Measure> future = composition.getFuture();
		if (future.size() > 2*sectionSize)
			return true; // no need to iterate now
		Analysis analysis = composition.getAnalysis();
		int measuresWithoutSection = composition.size() - analysis.lastEndOfSection();
		int missingMeasures = -measuresWithoutSection;
//		System.out.println("Missing measures: " + missingMeasures);
		
		if (missingMeasures <= 0 && future.size() <= sectionSize) {
			// sections are full, write a new one
			ChordsSection nextSection = new ChordsSection(sectionSize);
			ChordSpec precedingChord;
			if (measuresWithoutSection > 0) {
				// measures at end of piece without a section
				List<Measure> rogueMeasures = composition.getMeasures(
						composition.size()-measuresWithoutSection+1, composition.size());
				if (rogueMeasures.size() != 1)
					throw new IllegalStateException("Somehow have the wrong number of rogue measures...");
				nextSection.putKey(1, key); // relies on first-measure behavior of generateFirstMeasure() -> composeBar()
				nextSection.putChord(1, firstChord); // making some assumptions here
				precedingChord = null; // can pass null because nextSection is not empty
			} else {
				// get last chord
				List<Section> sections = analysis.getSections();
				ChordsSection section = (ChordsSection) sections.get(sections.size() - 1);
				precedingChord = section.getChord(section.size());
			}
			fillSection(nextSection, nextSectionProgression(analysis), precedingChord);
			analysis.addSection(nextSection);
//			System.out.println("Adding new section.");
		} else if (missingMeasures > 0) {
//			System.out.println("Writing measure.");
			// fill out the latest section with written measures
			// compose measures to fill section
			List<Section> sections = analysis.getSections();
//			System.out.println("Sections: " + sections.size());
			ChordsSection lastSection = (ChordsSection) sections.get(sections.size() - 1); // enforced softly in this class
			if (lastSection.size() < missingMeasures)
				throw new IllegalStateException("Something is wrong... must have miscounted.");
			ChordSpec nextChord = lastSection.getChord(lastSection.size() - missingMeasures + 1);
			Measure nextMeasure = composeBar(composition.getMeasure(composition.size()), nextChord);
			Set<Key> keys = lastSection.getKeys(lastSection.size() - missingMeasures + 1);
			String keyString =  "[";
			for (Key key : keys)
				keyString += " " + key + " ";
			keyString += "] ";
			nextMeasure.setMetaInfo(keyString + nextMeasure.getMetaInfo()); // flipped
			future.add(nextMeasure); // XXX
//			future.add(composeBar(composition)); // old implementation
			int lastEndOfSection = analysis.lastEndOfSection();
			if (composition.size() >= lastEndOfSection) {
				// melody
				try {
					List<Measure> measuresWithoutMelody = future.stream()
							.filter(measure -> measure.getMeasureNumber() <= lastEndOfSection)
							.filter(measure -> !measure.getMetaInfo().contains("melody"))
							.collect(Collectors.toList());
					if (measuresWithoutMelody.size() >= 8) {
						writeMelody(measuresWithoutMelody);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return future.size() > 16;
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

	/**
	 * Modifies the given Section object
	 * 
	 * @param nextSection an incomplete section to be filled out
	 * @param progression
	 */
	private void fillSection(ChordsSection nextSection, ChordProgression progression, ChordSpec precedingChord) {
		List<ChordSpec> chordSpecs = nextSection.getAllChords();
		
		ChordSpec lastChordSpec = !chordSpecs.isEmpty() ? chordSpecs.get(chordSpecs.size() - 1) : 
			(precedingChord == null ? firstChord : precedingChord);
		
		int i = chordSpecs.size() + 1;
		if (progression instanceof KeyChange) {
			KeyChange keyChange = (KeyChange) progression;
			Key fromKey = keyChange.getFromKey();
			Key toKey = keyChange.getToKey();
			
			// TODO look further back for lastChordSpec:
			int fromScaleDegree = lastChordSpec == null ? 1 : fromKey.scaleDegree(lastChordSpec.getTonic());
			if (!chordSpecs.isEmpty()) {
				chordSpecs.remove(chordSpecs.size() - 1); // to be replaced below
				i--; // boy, this is messy...
			}
			chordSpecs.addAll(keyChange.progress(fromScaleDegree, 1, 8-chordSpecs.size()));
			
			for (ChordSpec chord : chordSpecs) {
				nextSection.putChord(i, chord);
				if (keyChange.isInFromKey(chord)) {
					nextSection.putKey(i, fromKey);
				}
				if (keyChange.isInToKey(chord)) {
					nextSection.putKey(i, toKey);
				}
				lastChordSpec = chord;
				i++;
			}
			if (i <= nextSection.size())
				progression = ChordProgressions.standardMajorProgression(toKey.getTonic());
		}
		// whether empty or partially full from the KeyChange progression,
		// fill chordSpecs up to 8 chords
//		System.out.print("Progressing to measures:");
		while (i <= nextSection.size()) {
			Key progressionKey = ((KeyChordProgression) progression).getKey();
			ChordSpec nextChordSpec = progression.getNext(lastChordSpec);
			nextSection.putChord(i, nextChordSpec);
			nextSection.putKey(i, progressionKey); // relies on behavior in nextSectionProgression()
			lastChordSpec = nextChordSpec;
			i++;
//			System.out.print(" " + i + ":" + lastChordSpec + "->" + nextChordSpec);
		}
//		System.out.println();
	}
	
	private ChordProgression nextSectionProgression(Analysis analysis) {
		Key lastKey = key;
		Key nextKey = null;
		
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
	
	/**
	 * Allows subclasses to extend this method and write additional music on top of the base line. e.g.
	 * <p>
	 * Override <br>
	 * protected Measure composeBar(IncompleteComposition composition) { <br>
	 * Measure measure = super.composeBar(composition); <br>
	 * -- Whatever you want to compose here -- <br>
	 * return measure; <br>
	 * }
	 * 
	 * @param lastMeasure measure before the measure to be returned, used for voice leading
	 * @param nextChordSpec chord for writing background chords
	 * @return next Measure with background chords written into it
	 */
	protected Measure composeBar(Measure lastMeasure, ChordSpec nextChordSpec) {
		Chord previousChord = key.chord(1, octave); // for first measure only
		if (lastMeasure != null) {
			// FIXME must find a much better way than this to get the last chord
			// probably by abstracting a high-level Measure class and also adding multiple voices
//			System.out.println("Last measure empty? " + meas.isEmpty());
			List<MidiNote> lastBeatNotes = lastMeasure.getNotes(lastMeasure.beatValue()*(lastMeasure.beats()-1));
//			System.out.println("Last beat notes size: " + notes.size());
//			System.out.println("Last beat notes: ");
//			for (MidiNote note : lastBeatNotes)
//				System.out.println(note);
			previousChord = new Chord();
			for (MidiNote note : lastBeatNotes) {
				previousChord.add(new MidiPitch(note.getPitch()));
			}
		}
		if (previousChord.get().size() < 3)
			throw new IllegalStateException("A random bug appears! " + previousChord); // rare, have yet to diagnose
		Measure measure = backgroundChord(previousChord, nextChordSpec);
		measure.setMetaInfo("(" + nextChordSpec + ")");
		
		measure.setBpm(currentTempo.getBpm());
		
		return measure;
	}

//	private int readPreviousChordDegree(Measure lastMeasure) {
//		String metaInfo = lastMeasure.getMetaInfo();
//		Matcher matcher = Pattern.compile("(\\()"+"([0-9])"+"(\\))"+"(.*)").matcher(metaInfo);
//		matcher.matches(); // I don't understand this API, apparently
//		int previousChordDegree = Integer.valueOf(matcher.group(2));
//		return previousChordDegree;
//	}
	
	private Measure backgroundChord(Chord previousChord, ChordSpec nextChordSpec) {
//		System.out.println("Previous chord pitches: ");
//		for (MidiPitch pitch : previousChord.get())
//			System.out.println(pitch);
		
		Measure measure = new Measure(4, 1/4.0);
		
		Chord previousChordHacked = new Chord(); // gross hack, awaiting "instrument" parts
		for (int i=0; i<3; i++)
			previousChordHacked.add(previousChord.get().get(i));
		//		System.out.println("scaleDegree: " + scaleDegree);
//		System.out.println("Tonic: " + MidiPitch.inOctave(key.getTonic(), 4));
//		ChordSpec nextChordSpec = key.chordSpec(scaleDegree, octave); // FIXME not necessarily the tonic
		int bassMin = MidiPitch.inOctave(key.getTonic(), octave);
		int bassMax = bassMin + 19;
		Chord voiceLeadChord = ChordProgressions.voiceLead(previousChordHacked, nextChordSpec, bassMin, bassMax);
		
		for (int i=0; i<measure.beats(); i++) {
//			for (MidiPitch pitch : key.chord(scaleDegree, octave).get()) {
			for (MidiPitch pitch : voiceLeadChord) {
//				System.out.println("Adding pitch: " + pitch);
				MidiNote note = new MidiNote(pitch, measure.beatValue());
				if (i != 0)
					note.setDynamic(Dynamic.MEZZO_PIANO);
				measure.add(note, i*measure.beatValue());
			}
		}
		
		return measure;
	}
	
	private void writeMelody(List<Measure> measuresWithoutMelody) {
		Phrase melody = melodyWriter.writeMelody(measuresWithoutMelody);
		Measure.writeOnto(melody, measuresWithoutMelody, 0.0);
		measuresWithoutMelody.forEach(measure -> measure.setMetaInfo(measure.getMetaInfo() + " melody"));
	}

	private void writeMelody(List<Measure> measuresWithoutMelody, Key key) {
		Phrase melody = melodyWriter.writeMelody(measuresWithoutMelody, key);
		Measure.writeOnto(melody, measuresWithoutMelody, 0.0);
		measuresWithoutMelody.forEach(measure -> measure.setMetaInfo(measure.getMetaInfo() + " melody"));
	}

	public String toString() {
		return "Pretty Chord Progression";
	}
	
	protected static class ChordsSection extends Section {

		protected Map<Integer,ChordSpec> chords;
		
		public ChordsSection(int measures) {
			super(measures);
			this.chords = new HashMap<>();
		}
		
		/**
		 * @return all ChordSpecs for all measures, in order as mapped (missing mappings not accounted for)
		 */
		public List<ChordSpec> getAllChords() {
			return new ArrayList<>(chords.keySet()).stream()
					.sorted()
					.map(measure -> chords.get(measure))
					.collect(Collectors.toList());
		}
		
		public ChordSpec getChord(int measure) {
			return chords.get(measure);
		}
		
		public void putChord(int measure, ChordSpec chord) {
			chords.put(measure, chord);
		}
	}

}
