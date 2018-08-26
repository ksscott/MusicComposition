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
		
		for (ChordNote chordNote : noteMask) {
			int halfStepsAboveRoot = 0;
			int stepsAboveRoot = chordNote.scaleDegree - 1;
			int octavesAbove = 0;
			while (stepsAboveRoot >= degrees) {
				// cut off whole octaves
				stepsAboveRoot -= degrees;
				octavesAbove++;
			}
			
			halfStepsAboveRoot += octavesAbove*scale.getWidth();
			halfStepsAboveRoot += intervalsFromRoot[stepsAboveRoot];
			halfStepsAboveRoot += chordNote.accidental.pitchAdjustment();
			chord.add(rootPitch.above(halfStepsAboveRoot));
		}
		
		return new Chord(chord);
	}
	
	public ChordBuilder setRoot(Note root) {
		this.root = root;
//		System.out.println("Builder root set to: " + MidiPitch.inOctave(this.root, octave));
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
	
	/**
	 * @param scaleDegree scale degree of the note to add
	 * @return this adjusted ChordBuilder
	 */
	public ChordBuilder addPitch(int scaleDegree) {
		return addPitch(scaleDegree, Accidental.NONE);
	}
	
	public ChordBuilder addPitch(int scaleDegree, Accidental adjustment) { // consider changing Accidental to some other concept
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
	
	public ChordBuilder invert() {
		return invert(1);
	}
	
	public ChordBuilder invert(int inversions) {
		for (int i=0; i<inversions; i++) {
			ChordNote lowestNote = noteMask.stream().sorted().findFirst().orElse(new ChordNote(1, Accidental.NONE));
			removePitch(lowestNote.scaleDegree, lowestNote.accidental);
			addPitch(lowestNote.scaleDegree+7, lowestNote.accidental);
		}
		return this;
	}
	
	public ChordBuilder uninvert() {
		return uninvert(1);
	}
	
	public ChordBuilder uninvert(int uninversions) {
		// TODO
		throw new RuntimeException("Unimplemented");
//		return this;
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
