package composing.strategy;

import static composing.RandomUtil.roll;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import composing.IncompleteComposition;
import composing.writer.PrettyMelodyWriter;
import theory.Chord;
import theory.ChordProgressions;
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

public class PrettyProgressionStrategy implements ComposingStrategy {
	
	protected int octave = 2;
	protected Key key;
	private KeyChordProgression progression;
	
	public PrettyProgressionStrategy(Key key) {
		this.key = key;
		
		this.progression = ChordProgressions.standardMajorProgression(key.getTonic());
	}
	
	@Override
	public Measure generateFirstMeasure() {
		return composeBar(new IncompleteComposition());
	}

	@Override
	public boolean iterate(IncompleteComposition composition) {
		final Queue<Measure> future = composition.getFuture();
//		future.add(composeBar(composition)); // TODO TODO TODO
		// chord progression
		if (future.size() < 8) {
			Analysis analysis = composition.getAnalysis();
			List<Section> sections = analysis.getSections();
			Section lastSection = sections.get(sections.size()-1);
			Set<Key> lastSectionKeys = lastSection.getAllKeys();
			// FIXME definitely need a more robust way of knowing what key we're supposed to be in
			Key lastKey;
			Key nextKey = null;
			if (lastSectionKeys.size() < 1) {
				throw new IllegalStateException("Expecting to find Keys in the Analysis.");
			} else if (lastSectionKeys.size() > 1) {
				// key change happened last section
				// WARNING: the following code might get a random key from the last measure
				lastKey = lastSection.getKeys(lastSection.size()).stream().findAny().orElse(key);
				// leave nextKey null to not change keys again
			} else {
				lastKey = lastSectionKeys.iterator().next();
				if (lastSection.getKeys(lastSection.size()-1).size() == 1) {
					// presume same as lastKey
					// after two sections in this key, let's change keys
					if (lastKey.equals(key)) {
						nextKey = roll(50) ? key.tonicize(4) : key.tonicize(5);
					} else {
						// let's come back
						nextKey = key;
					}
				} else {
					// let's write a second section in the key lastKey
				}
			}
			Section nextSection = new Section(8);
			if (nextKey != null) {
				// change keys from lastKey to nextKey
				
			} else {
				// compose section in lastKey
				KeyChordProgression lastKeyProgression = 
						ChordProgressions.standardMajorProgression(lastKey.getTonic());
				KeyChordProgression nextKeyProgression = 
						ChordProgressions.standardMajorProgression(nextKey.getTonic());
				KeyChange keyChange = new KeyChange(lastKeyProgression, nextKeyProgression);
				keyChange.progress(1, 1, 8);
			}
			analysis.addSection(nextSection);
			// TODO
		}
		// melody
		if (future.size() >= 8) {
			try {
				List<Measure> measuresWithoutMelody = future.stream().filter(measure -> !measure.getMetaInfo().contains("melody")).collect(Collectors.toList());
				if (measuresWithoutMelody.size() >= 8) {
					Phrase melody = new PrettyMelodyWriter().writeMelody(measuresWithoutMelody);
					Measure.writeOnto(melody, measuresWithoutMelody, 0.0);
					measuresWithoutMelody.forEach(measure -> measure.setMetaInfo(measure.getMetaInfo() + " melody"));
				}
			} catch (Exception e) { e.printStackTrace(); }
		}
		return future.size() > 16;
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
	 * @param composition
	 * @return
	 */
	protected Measure composeBar(IncompleteComposition composition) {
		List<Measure> measures = composition.getMeasures();
		int currentChordDegree = 1;
		Chord previousChord = key.chord(currentChordDegree, octave);
//		System.out.println("Previous chord pitches: ");
//		for (MidiPitch pitch : previousChord.get())
//			System.out.println(pitch);
		if (!measures.isEmpty()) {
			if (!composition.getFuture().isEmpty())
				measures = new ArrayList<Measure>(composition.getFuture());
			String metaInfo = measures.get(measures.size()-1).getMetaInfo();
			Matcher matcher = Pattern.compile("(\\()"+"([0-9])"+"(\\))"+"(.*)").matcher(metaInfo);
			matcher.matches(); // I don't understand this API, apparently
			int previousChordDegree = Integer.valueOf(matcher.group(2));
			
			currentChordDegree = key.scaleDegree(progression.getNext(previousChordDegree).getTonic());
			
			// FIXME must find a much better way than this to get the last chord
			// probably by abstracting a high-level Measure class and also adding multiple voices
			Measure meas = measures.get(measures.size() - 1);
//			System.out.println("Last measure empty? " + meas.isEmpty());
			List<MidiNote> notes = meas.getNotes(meas.beatValue()*(meas.beats()-1));
//			System.out.println("Last beat notes size: " + notes.size());
//			System.out.println("Last beat notes: ");
//			for (MidiNote note : notes)
//				System.out.println(note);
			previousChord = new Chord();
			for (MidiNote note : notes) {
				previousChord.add(new MidiPitch(note.getPitch()));
			}
		}
//		Measure measure = backgroundChord(previousChord, currentChordDegree);
		Measure measure = backgroundChord(previousChord, key.chordSpec(currentChordDegree, octave));
		measure.setMetaInfo("(" + currentChordDegree + ")");
		
		measure.setBpm(Tempo.ADAGIO.getBpm());
		
		return measure;
	}
	
	// TODO probably accept a parameter besides int
	private Measure backgroundChord(Chord previousChord, ChordSpec nextChordSpec) {
//		System.out.println("Previous chord pitches: ");
//		for (MidiPitch pitch : previousChord.get())
//			System.out.println(pitch);
		
		int beats = 4;
		double beatValue = 1/4.0;
		Measure measure = new Measure(beats, beatValue);
		
		List<MidiPitch> previousPitches = previousChord.get();
		Chord previousChordHacked = new Chord();
		for (int i=0; i<3; i++)
			previousChordHacked.add(previousPitches.get(i));
		//		System.out.println("scaleDegree: " + scaleDegree);
//		System.out.println("Tonic: " + MidiPitch.inOctave(key.getTonic(), 4));
//		ChordSpec nextChordSpec = key.chordSpec(scaleDegree, octave); // FIXME not necessarily the tonic
		int bassMin = MidiPitch.inOctave(key.getTonic(), octave);
		int bassMax = bassMin + 19;
		Chord voiceLeadChord = ChordProgressions.voiceLead(previousChordHacked, nextChordSpec, bassMin, bassMax);
		
//		List<MidiPitch> pitches = key.chord(scaleDegree, octave).get();
		List<MidiPitch> pitches = voiceLeadChord.get();
		Collections.sort(pitches);
		
		for (int i=0; i<beats; i++) {
			for (MidiPitch pitch : pitches) {
//				System.out.println("Adding pitch: " + pitch);
				MidiNote note = new MidiNote(pitch, beatValue);
				if (i != 0)
					note.setDynamic(Dynamic.MEZZO_PIANO);
				measure.add(note, i*beatValue);
			}
		}
		
		return measure;
	}
	
	public String toString() {
		return "Pretty Chord Progression";
	}
	

}
