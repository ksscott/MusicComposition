package theory;

import static composing.RandomUtil.modPos;

public class Note implements Cloneable, Comparable<Note> {
	
	/** Half steps above A */
	private int index;

	private Note(int index) {
		if (index < 0 || index > 11)
			throw new IllegalArgumentException("Let me teach you a little something about notes, friend! " + index);
		this.index = index;
	}
	
	public Note(Letter letter) {
		this(letter, Accidental.NONE);
	}
	
	public Note(Letter letter, Accidental accidental) {
		this(indexOf(letter, accidental));
	}
	
	/**
	 * @param other note assumed to be above this note
	 * @return the number of half steps the given note is above this note
	 */
	public int halfStepsTo(Note other) {
		return modPos(other.index - index, 12);
	}
	
	/**
	 * @param steps half steps above this note, all integers supported (come on, be reasonable!)
	 * @return note reached by raising this note the given number of half steps
	 */
	public Note halfStepsAbove(int steps) {
		return new Note(modPos(index + steps, 12));
	}
	
	/**
	 * @param other note to test for equivalence
	 * @return true if the given note is harmonically equivalent to this note (e.g. Ab & G# or E## & Gb)
	 */
	public boolean isEnharmonic(Note other) {
		return other.index == this.index;
	}
	
	// Unsure if I want to keep these two methods here:
	public String toFlatNoteName() {
		int halfStepsAboveA = index;
		int[] intervals = Key.MINOR.intervalsFromRoot();
		for (int i=0; i<intervals.length; i++) {
			if (halfStepsAboveA == intervals[i])
				return Letter.values()[i].name();
			if (halfStepsAboveA < intervals[i])
				return Letter.values()[i].name() + Accidental.FLAT.getSymbol();
		}
		return Letter.A.name() + Accidental.FLAT.getSymbol(); // didn't check below A or above G, must be Ab
	}
	
	public String toSharpNoteName() {
		int halfStepsAboveA = index;
		int[] intervals = Key.MINOR.intervalsFromRoot();
		for (int i=0; i<intervals.length; i++) {
			if (halfStepsAboveA == intervals[i])
				return Letter.values()[i].name();
			if (halfStepsAboveA < intervals[i])
				return Letter.values()[i-1].name() + Accidental.SHARP.getSymbol();
		}
		return Letter.G.name() + Accidental.SHARP.getSymbol(); // didn't check below A or above G, must be G#
	}
	
	@Override
	public Note clone() {
		return new Note(index);
	}
	
	@Override
	public int compareTo(Note o) {
		return this.index - o.index;
	}
	
	@Override
	public int hashCode() { // FIXME Ab != G#
		final int prime = 31;
		int result = 1;
		result = prime * result + index;
		return result;
	}

	@Override
	public boolean equals(Object obj) { // FIXME Ab != G#
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Note other = (Note) obj;
		if (index != other.index)
			return false;
		return true;
	}

	private static int indexOf(Letter letter, Accidental accidental) {
		return modPos(Key.MINOR.intervalsFromRoot()[letter.ordinal()] + accidental.pitchAdjustment(), 12);
	}
	
	@Override
	public String toString() {
//		// temporary:
//		return "Note" + index;
		
		return toFlatNoteName();
	}
	
}
