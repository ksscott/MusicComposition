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
	
}
