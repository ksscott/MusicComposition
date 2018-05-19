package composing.strategy;

import composing.IncompleteComposition;
import theory.Accidental;
import theory.Key;
import theory.Measure;
import theory.MidiNote;
import theory.Note;
import theory.Scale;

public class TwelveBarImprovStrategy extends TwelveBarBluesStrategy {

	private int octavesUp = 2;
	private int[] pitches;
	
	private int noteIndex; // hack
	
	private Scale[] knownScales = new Scale[] { Key.BLUES_SCALE,
												Key.BLUES_SCALE,
												Key.BLUES_SCALE,
												Key.STANDARD_PENTATONIC,
												Key.STANDARD_PENTATONIC,
//												Key.HARMONIC_MINOR,
												Key.MELODIC_MINOR
												};
	
	public TwelveBarImprovStrategy(Note tonic) {
		super(tonic);
		noteIndex = 100; // will be random
	}
	
	@Override
	public Measure generateFirstMeasure() {
		final String scaleName = setScale(Key.BLUES_SCALE).name();
		Measure measure = super.generateFirstMeasure();
		measure.setMetaInfo(scaleName);
		return measure;
	}

	@Override
	protected Measure composeBar(IncompleteComposition composition) {
		int bar = getCurrentBar(composition);
		Measure measure = super.composeBar(composition);
		
		if (bar == 11 && roll(25)) 
			return composeBarTwelve(measure);
		
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
		int downBeatChance = random(45, 90); // 50 - 80
		int offBeatChance = random(25, 80); // 30 - 60
		int noteSkipChance = 7;
		int scaleChangeChance = 15;
		
		// change scale
		if (roll(scaleChangeChance)) {
			Scale scale = setScale(knownScales[random(knownScales.length)]);
			measure.setMetaInfo(scale.name());
		}
		
		// prepare the downbeat and offbeat of each beat of the measure randomly
		for (int i=0; i<measure.beats(); i++) {
			if (roll(noteSkipChance)) // skip around once in a while
				noteIndex = random(pitches.length);
			
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
	
	private Measure composeBarTwelve(Measure measure) {
		double beatValue = measure.beatValue();
		for (int i=0; i<measure.beats(); i++) {
			if (i != 0) {
				MidiNote note = new MidiNote(tonic + octavesUp*12 + 7, 1/3.0*beatValue);
				measure.addNote(note, i*beatValue);
			}
			MidiNote note = new MidiNote(tonic + octavesUp*12 + 7, 1/3.0*beatValue);
			final double location = i*beatValue + 2/3.0*beatValue;
			measure.addNote(note, location);
		}
		return measure;
	}
	
	private Scale setScale(Scale scale) {
		int[] intervals = scale.intervalsFromRoot();
		pitches = new int[intervals.length+2]; // one to include root, one for an extra note on top
		pitches[0] = this.tonic + 12*octavesUp;
		for (int i=1; i<=intervals.length; i++) {
			pitches[i] = this.tonic + intervals[i-1] + 12*octavesUp;
		}
		pitches[intervals.length+1] = this.tonic + 12*(octavesUp+1); // the octave on top
		
		// make sure noteIndex is in bounds
		if (noteIndex >= pitches.length)
			noteIndex = random(pitches.length); // hack
		
		return scale;
	}
	
	/**
	 * @return -1, 0, or 1 randomly
	 */
	private static int step() {
		Accidental[] steps = Accidental.values();
		return steps[random(steps.length)].pitchAdjustment();
	}
	
	public static boolean roll(int percentChance) {
		return Math.random()*100 < percentChance;
	}
	
	public static int random(int max) {
		return (int) (Math.random()*max);
	}
	
	public static int random(int min, int max) {
		return min + ((int) (Math.random()*max));
	}
}
