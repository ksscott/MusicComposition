package composing.strategy;

import composing.IncompleteComposition;
import theory.Accidental;
import theory.Key;
import theory.Measure;
import theory.MidiNote;
import theory.Note;

public class TwelveBarImprovStrategy extends TwelveBarBluesStrategy {

	private int octavesUp = 2;
	private int[] pitches;
	
	private int noteIndex; // hack
	
	public TwelveBarImprovStrategy(Note tonic) {
		super(tonic);
		
		int[] intervals = Key.BLUES_SCALE.intervalsFromRoot();
		pitches = new int[intervals.length+2]; // one to include root, one for an extra note on top
		pitches[0] = this.tonic + 12*octavesUp;
		for (int i=1; i<=intervals.length; i++) {
			pitches[i] = this.tonic + intervals[i-1] + 12*octavesUp;
		}
		pitches[intervals.length+1] = this.tonic + 12*(octavesUp+1); // the octave on top
		
		noteIndex = (int) Math.random() * pitches.length; // hack
	}

	@Override
	protected Measure composeBar(IncompleteComposition composition) {
		Measure measure = super.composeBar(composition);
		
		// crude attempt to primitively locate last note
//		List<Measure> measures = composition.getMeasures();
//		Measure lastMeasure = measures.get(measures.size()); // last measure
//		Double max = lastMeasure.getTimes().stream().filter(dub -> {
//			for (MidiNote note : lastMeasure.getNotes(dub)) {
//				if (note.getPitch() > pitches[0]) // crude
//					return true;
//			}
//			return false;
//		}).max(Double::compare).orElse(0.0);
		
		// TODO compose improvisational magic here!
		double beatValue = measure.beatValue();
		
		// change character every measure
		int downBeatChance = (int) (40 + Math.random()*50); // 50 - 80
		int offBeatChance = (int) (20 + Math.random()*55); // 30 - 60
		
		// prepare the downbeat and offbeat of each beat of the measure randomly
		for (int i=0; i<measure.beats(); i++) {
			if (roll(7)) // skip around once in a while
				noteIndex = ((int) Math.random()*pitches.length);
			
			if (roll(downBeatChance)) {
				// play on downbeat
				measure.addNote(new MidiNote(pitches[noteIndex], 2/3.0*beatValue), i*beatValue);
				noteIndex += step();
				noteIndex = (noteIndex + pitches.length) % pitches.length;
			}
			if (roll(offBeatChance)) {
				// play synchopation
				final double location = i*beatValue + 2/3.0*beatValue;
				measure.addNote(new MidiNote(pitches[noteIndex], 1/3.0*beatValue), location);
				noteIndex += step();
				noteIndex = (noteIndex + pitches.length) % pitches.length;
			}
		}
		
		return measure;
	}
	
	/**
	 * @return -1, 0, or 1 randomly
	 */
	private static int step() {
		Accidental[] steps = Accidental.values();
		return steps[(int) (Math.random()*steps.length)].pitchAdjustment();
	}
	
	private static boolean roll(int percentChance) {
		return Math.random()*100 < percentChance;
	}
}
