package composing.strategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import composing.IncompleteComposition;
import composing.writer.PrettyMelodyWriter;
import theory.Chord;
import theory.ChordProgressions;
import theory.ChordProgressions.ChordProgression;
import theory.ChordSpec;
import theory.Dynamic;
import theory.Key;
import theory.Measure;
import theory.MidiNote;
import theory.MidiPitch;
import theory.Tempo;
import theory.analysis.Phrase;

public class PrettyProgressionStrategy implements ComposingStrategy {
	
	protected int octave = 2;
	protected Key key;
	private ChordProgression progression;
	
	public PrettyProgressionStrategy(Key key) {
		this.key = key;
		
		this.progression = new ChordProgression(key);
		progression.put(1,4,2);
		progression.put(1,6);
		progression.put(1,3);
		progression.put(4,5,4);
		progression.put(4,6);
		progression.put(4,3);
		progression.put(4,2);
//		progressions.put(4,1); // XXX debugging only
		progression.put(6,4,2);
		progression.put(6,2,2);
		progression.put(6,1);
		progression.put(3,1);
		progression.put(3,6);
		progression.put(5,1,3);
		progression.put(5,6);
		progression.put(5,4);
		progression.put(2,5,2);
		progression.put(2,4);
		progression.put(2,7);
		progression.put(7,5);
		progression.put(7,1);
	}
	
	@Override
	public Measure generateFirstMeasure() {
		return composeBar(new IncompleteComposition());
	}

	@Override
	public boolean iterate(IncompleteComposition composition) {
		final Queue<Measure> future = composition.getFuture();
		future.add(composeBar(composition));
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
			currentChordDegree = progression.getNext(previousChordDegree);
			
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
		Measure measure = backgroundChord(previousChord, currentChordDegree);
		measure.setMetaInfo("(" + currentChordDegree + ")");
		
		measure.setBpm(Tempo.ADAGIO.getBpm());
		
		return measure;
	}
	
	// TODO probably accept a parameter besides int
	private Measure backgroundChord(Chord previousChord, int scaleDegree) {
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
		ChordSpec nextChordSpec = new ChordSpec(key.note(scaleDegree), Key.chordQuality(key.chord(scaleDegree, octave))); // FIXME not necessarily the tonic
		int bassMin = MidiPitch.inOctave(key.getTonic(), octave);
		int bassMax = bassMin + 19;
		Chord voiceLeadChord = ChordProgressions.voiceLead(previousChordHacked, nextChordSpec, bassMin, bassMax);
		
		List<MidiPitch> pitches = key.chord(scaleDegree, octave).get();
		pitches = voiceLeadChord.get();
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
