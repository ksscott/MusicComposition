package theory;

public class MidiPitch implements Cloneable,Comparable<MidiPitch> {

	private int pitch;
	
	public MidiPitch(int midiPitch) {
		this.pitch = midiPitch;
	}
	
	public MidiPitch(Note note, int octave) {
		this.pitch = inOctave(note, octave);
	}
	
	public int get() {
		return pitch;
	}
	
	/**
	 * @param halfSteps number of half steps above this pitch
	 * @return a MidiPitch that is the given number of half steps above this pitch
	 */
	public MidiPitch above(int halfSteps) {
		return new MidiPitch(pitch + halfSteps);
	}
	
	/**
	 * @param halfSteps number of half steps below this pitch
	 * @return a MidiPitch that is the given number of half steps below this pitch
	 */
	public MidiPitch below(int halfSteps) {
		return new MidiPitch(pitch - halfSteps);
	}
	
	/**
	 * @param other
	 * @return the number of half steps the given pitch is above this pitch
	 */
	public int halfStepsTo(MidiPitch other) {
		return other.pitch - this.pitch;
	}
	
	/**
	 * In the event of a tritone, returns a tritone above this pitch
	 * 
	 * @param note to search for
	 * @return the nearest instance of the given note to this pitch
	 */
	public MidiPitch nearest(Note note) {
		int halfStepsTo = Key.toFlatNote(this).halfStepsTo(note);
		if (halfStepsTo > 6)
			halfStepsTo -= 12;
		return above(halfStepsTo);
	}
	
	public static int inOctave(Note note, int octave) {
		if (octave < 0 || octave > 8)
			throw new IllegalArgumentException("Only octaves 0-8 are currently supported");
		return 12 + (12 * octave) + new Note(Letter.C, Accidental.NONE).halfStepsTo(note);
	}
	
	@Override
	public MidiPitch clone() {
		return new MidiPitch(pitch);
	}

	@Override
	public int compareTo(MidiPitch o) {
		return new Integer(pitch).compareTo(new Integer(o.pitch));
	}
	
	@Override
	public String toString() {
		return "" + pitch;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + pitch;
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
		MidiPitch other = (MidiPitch) obj;
		if (pitch != other.pitch)
			return false;
		return true;
	}
	
}
