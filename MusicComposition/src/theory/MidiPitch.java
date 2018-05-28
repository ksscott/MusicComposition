package theory;

public class MidiPitch implements Comparable<MidiPitch> {

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
	
	public MidiPitch above(int halfSteps) {
		return new MidiPitch(pitch + halfSteps);
	}
	
	public MidiPitch below(int halfSteps) {
		return new MidiPitch(pitch - halfSteps);
	}
	
	public static int inOctave(Note note, int octave) {
		if (octave < 0 || octave > 8)
			throw new IllegalArgumentException("Only octaves 0-8 are currently supported");
		return 12 + (12 * octave) + new Note(Letter.C, Accidental.NONE).halfStepsTo(note);
	}

	@Override
	public int compareTo(MidiPitch o) {
		return new Integer(pitch).compareTo(new Integer(o.pitch));
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
