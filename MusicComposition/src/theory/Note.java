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
	
	public int halfStepsTo(Note other) {
		return modPos(other.index - index, 12);
	}
	
	public Note halfStepsAbove(int steps) {
		return new Note(modPos(index + steps, 12));
	}
	
	public boolean isEnharmonic(Note other) {
		return other.index == this.index;
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
		// temporary:
		return "Note" + index;
	}
	
}
