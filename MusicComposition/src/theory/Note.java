package theory;

public class Note {
	
	public Letter letter;
	public Accidental accidental;
	/** Half steps above A */
	private int index;

	public Note(Letter letter) {
		this(letter, Accidental.NONE);
	}
	
	public Note(Letter letter, Accidental accidental) {
		this.letter = letter;
		this.accidental = accidental;
		this.index = indexOf(letter, accidental);
	}
	
	public int halfStepsTo(Note other) {
		return (other.index - index + 12) % 12;
	}
	
	private int indexOf(Letter letter, Accidental accidental) {
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
