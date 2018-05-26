package theory;

public class Note implements Cloneable {
	
	/** Half steps above A */
	private int index;

	private Note(int index) {
		if (index < 0 || index > 11)
			throw new IllegalArgumentException("Let me teach you a little something about notes, friend!");
		this.index = index;
	}
	
	public Note(Letter letter) {
		this(letter, Accidental.NONE);
	}
	
	public Note(Letter letter, Accidental accidental) {
		this(indexOf(letter, accidental));
	}
	
	public int halfStepsTo(Note other) {
		return (other.index - index + 12) % 12;
	}
	
	public Note halfStepsAbove(int steps) {
		return new Note((index + steps) % 12);
	}
	
	@Override
	public Note clone() {
		return new Note(index);
	}
	
	private static int indexOf(Letter letter, Accidental accidental) {
		int halfSteps = 0;
		switch(letter) {
			default:
			case A:
				halfSteps = 0;
				break;
			case B:
				halfSteps = 2;
				break;
			case C:
				halfSteps = 3;
				break;
			case D:
				halfSteps = 5;
				break;
			case E:
				halfSteps = 7;
				break;
			case F:
				halfSteps = 8;
				break;
			case G:
				halfSteps = 10;
				break;
		}
		return (halfSteps + accidental.pitchAdjustment() + 12) % 12;
	}
	
}
