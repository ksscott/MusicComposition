package theory;

import java.util.HashSet;
import java.util.Set;

/**
 * By default, builds a major triad.
 * 
 * @author kennethscott
 */
public class ChordBuilder {

	private Scale scale = Key.MAJOR;
	private Note root;
	private int octave;
//	private boolean major;
	
	private Set<ChordNote> noteMask;
	
	public ChordBuilder() {
		root = new Note(Letter.A);
		octave = 4;
//		major = true;
		this.noteMask = new HashSet<>();
		
		noteMask.add(new ChordNote(1, Accidental.NONE));
		noteMask.add(new ChordNote(3, Accidental.NONE));
		noteMask.add(new ChordNote(5, Accidental.NONE));
	}
	
	public Chord build() {
		MidiPitch rootPitch = new MidiPitch(root, octave);
		Chord chord = new Chord();
		
		int[] intervalsFromRoot = scale.intervalsFromRoot();
		int degrees = intervalsFromRoot.length;
		int scaleSize = 0;
		for (int interval : scale.intervals())
			scaleSize += interval;
		
		for (ChordNote chordNote : noteMask) {
			int halfStepsAboveRoot = 0;
			int steps = chordNote.scaleDegree;
			int octavesAbove = 0;
			while (steps >= degrees) {
				// cut off whole octaves
				steps -= degrees;
				octavesAbove++;
			}
			
			halfStepsAboveRoot += octavesAbove*scaleSize;
			halfStepsAboveRoot += intervalsFromRoot[steps];
			halfStepsAboveRoot += chordNote.accidental.pitchAdjustment();
			chord.add(rootPitch.above(halfStepsAboveRoot));
		}
		
		return new Chord(chord);
	}
	
	public ChordBuilder setRoot(Note root) {
		this.root = root;
		return this;
	}
	
	public ChordBuilder setOctave(int octave) {
		if (octave < 0)
			throw new IllegalArgumentException("Octave must be non-negative.");
		this.octave = octave;
		return this;
	}
	
//	public ChordBuilder major() {
//		major = true;
//		return this;
//	}
//	
//	public ChordBuilder minor() {
//		major = false;
//		return this;
//	}
	
	public ChordBuilder addPitch(int scaleDegree) {
		return addPitch(scaleDegree, Accidental.NONE);
	}
	
	public ChordBuilder addPitch(int scaleDegree, Accidental adjustment) {
		noteMask.add(new ChordNote(scaleDegree, adjustment)); // set add prohibits duplicates
		return this;
	}
	
	public ChordBuilder removePitch(int scaleDegree, Accidental adjustment) {
		noteMask.remove(new ChordNote(scaleDegree, adjustment));
		return this;
	}
	
	public ChordBuilder removePitch(int scaleDegree) {
		noteMask.remove(new ChordNote(scaleDegree, Accidental.FLAT));
		noteMask.remove(new ChordNote(scaleDegree, Accidental.NONE));
		noteMask.remove(new ChordNote(scaleDegree, Accidental.SHARP));
		return this;
	}
	
	private static class ChordNote {
		int scaleDegree;
		Accidental accidental;
		public ChordNote(int scaleDegree, Accidental accidental) {
			if (scaleDegree < 1)
				throw new IllegalArgumentException("Only natural-numbered scale degrees allowed.");
			this.scaleDegree = scaleDegree;
			this.accidental = accidental;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((accidental == null) ? 0 : accidental.hashCode());
			result = prime * result + scaleDegree;
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ChordNote other = (ChordNote) obj;
			if (accidental != other.accidental)
				return false;
			if (scaleDegree != other.scaleDegree)
				return false;
			return true;
		}
	}
	
}
