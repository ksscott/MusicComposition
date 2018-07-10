package composing.strategy;

import static composing.RandomUtil.random;
import static composing.RandomUtil.roll;

import composing.IncompleteComposition;
import main.BeadsTimbre;
import performance.Dynamic;
import performance.MidiNote;
import performance.instrument.Instrument;
import theory.Accidental;
import theory.Key;
import theory.Measure;
import theory.Note;
import theory.Scale;

public class TwelveBarImprovStrategy extends TwelveBarBluesStrategy {

	private int octavesUp = 2;
	private int[] pitches;
	
	private Instrument trumpet;
	
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
		this.trumpet = new Instrument("Trumpet", BeadsTimbre.getSineTimbre());
		noteIndex = 100; // will be random
	}
	
	@Override
	public Measure generateFirstMeasure() {
		Scale scale = Key.BLUES_SCALE;
		setScale(scale);
		Measure measure = super.generateFirstMeasure();
		measure.setMetaInfo(scale.name());
		return measure;
	}

	@Override
	protected Measure composeBar(IncompleteComposition composition) {
		int bar = getCurrentBar(composition);
		Measure measure = super.composeBar(composition);
		measure.addInstrument(trumpet);
		
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
		int downBeatChance = random(45, 90);
		int offBeatChance = random(20, 75);
		int noteSkipChance = 7;
		int scaleChangeChance = 15;
		
		// change scale
		if (roll(scaleChangeChance)) {
			Scale scale = knownScales[random(knownScales.length)];
			setScale(scale);
			measure.setMetaInfo(scale.name());
		}
		
		// prepare the downbeat and offbeat of each beat of the measure randomly
		for (int i=0; i<measure.beats(); i++) {
			if (roll(noteSkipChance)) // skip around once in a while
				noteIndex = random(pitches.length);
			
			if (roll(downBeatChance)) {
				// play on downbeat
				final MidiNote note = new MidiNote(pitches[noteIndex], 2/3.0*beatValue);
				note.setDynamic(Dynamic.MEZZO_PIANO);
				measure.add(trumpet, note, i*beatValue);
				noteIndex += step();
				noteIndex = (noteIndex + pitches.length) % pitches.length;
			}
			if (roll(offBeatChance)) {
				// play synchopation
				final double location = i*beatValue + 2/3.0*beatValue;
				measure.add(trumpet, new MidiNote(pitches[noteIndex], 1/3.0*beatValue), location);
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
				MidiNote note = new MidiNote(tonic.get() + octavesUp*12 + 7, 1/3.0*beatValue);
				measure.add(trumpet, note, i*beatValue);
			}
			MidiNote note = new MidiNote(tonic.get() + octavesUp*12 + 7, 1/3.0*beatValue);
			final double location = i*beatValue + 2/3.0*beatValue;
			measure.add(trumpet, note, location);
		}
		return measure;
	}
	
	private void setScale(Scale scale) {
		int[] intervals = scale.intervalsFromRoot();
		pitches = new int[intervals.length+1]; // include root and also octave up
		pitches[0] = this.tonic.get() + 12*octavesUp;
		for (int i=0; i<intervals.length; i++) {
			pitches[i+1] = this.tonic.get() + intervals[i] + 12*octavesUp;
		}
		pitches[intervals.length] = this.tonic.get() + 12*(octavesUp+1); // the octave on top
		
		// make sure noteIndex is in bounds
		if (noteIndex >= pitches.length)
			noteIndex = random(pitches.length); // hack
	}
	
	/**
	 * @return -1, 0, or 1 randomly
	 */
	private static int step() {
		Accidental[] steps = Accidental.values();
		return steps[random(steps.length)].pitchAdjustment();
	}
	
}
