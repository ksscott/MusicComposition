package theory;

import static composing.RandomUtil.modPos;

public class Note implements Cloneable {
	
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
	
	@Override
	public Note clone() {
		return new Note(index);
	}
	
	private static int indexOf(Letter letter, Accidental accidental) {
		return modPos(Key.MINOR.intervalsFromRoot()[letter.ordinal()] + accidental.pitchAdjustment(), 12);
	}
	
}
