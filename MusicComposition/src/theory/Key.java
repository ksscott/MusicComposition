package theory;

import static theory.Mode.*;

import java.util.Arrays;

public class Key {
	
	public static final Scale MAJOR = IONIAN;
	public static final Scale MINOR = AEOLIAN;
	public static final Scale HARMONIC_MINOR		 = new ScaleImpl(new int[]{2,1,2,2,1,3,1},	"Harmonic Minor");
	public static final Scale MELODIC_MINOR			 = new ScaleImpl(new int[]{2,1,2,2,2,2,1},	"Melodic Minor");
	public static final Scale STANDARD_PENTATONIC	 = new ScaleImpl(new int[]{2,2,3,2,3},		"Pentatonic");
	public static final Scale BLUES_SCALE			 = new ScaleImpl(new int[]{3,2,1,1,3,2},	"Blues");
	
	private Note tonic;
	private Scale scale;
	
	public Key(Note tonic, Scale scale) {
		this.tonic = tonic;
		this.scale = scale;
	}
	
	public Note getTonic() {
		return tonic.clone();
	}
	
	public Scale getScale() {
		return new ScaleImpl(scale); // TODO return original?
	}
	
	public boolean contains(Note note) {
		return Arrays.asList(scale.intervalsFromRoot()).contains(tonic.halfStepsTo(note));
	}
	
	public Note note(int scaleDegree) {
		if (scaleDegree < 1)
			throw new IllegalArgumentException("Scale degrees less than 1 are not currently supported.");
		int[] intervals = scale.intervalsFromRoot();
		int notes = intervals.length;
		int degree = scaleDegree % notes;
		int steps = intervals[degree];
		return tonic.halfStepsAbove(steps);
	}

	// unsure if this is the best method signature
	public Chord chord(int scaleDegree, int octave) {
		Chord chord = new Chord();
		
		int[] intervals = scale.intervals();
		int length = intervals.length;
		if (scaleDegree < 1 || scaleDegree > length)
			throw new IllegalArgumentException();
			
		int numThirdsAbove = 2; // how many times to add a third above the root
		
		int pitch = MidiPitch.inOctave(note(scaleDegree), octave);
		chord.add(new MidiPitch(pitch));
//		System.out.println("pitch " + pitch);
		
		for (int i=0; i<numThirdsAbove; i++) {
			for (int j=0; j<2; j++) {
				pitch += intervals[(2*i+j+scaleDegree-1) % length];
			}
			chord.add(new MidiPitch(pitch));
//			System.out.println("pitch " + pitch);
		}
		
		return chord;
	}
	
	public Key relativeKey() {
		if (scale == MAJOR)
			return new Key(tonic.halfStepsAbove(-3), MINOR);
		if (scale == MINOR)
			return new Key(tonic.halfStepsAbove(3), MAJOR);
		throw new UnsupportedOperationException("Getting the relative key is only supported for the MAJOR and MINOR modes.");
	}
	
	public Key parallelKey() {
		if (scale == MAJOR)
			return new Key(tonic, MINOR);
		if (scale == MINOR)
			return new Key(tonic, MAJOR);
		throw new UnsupportedOperationException("Getting the parallel key is only supported for the MAJOR and MINOR modes.");
	}
	
}
