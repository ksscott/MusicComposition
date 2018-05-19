package theory;

public class MidiPitch {

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
	
	public static int inOctave(Note note, int octave) {
		if (octave < 0 || octave > 8)
			throw new IllegalArgumentException("Only octaves 0-8 are currently supported");
		return 12 + (12 * octave) + new Note(Letter.C, Accidental.NONE).halfStepsTo(note);
	}
	
}
