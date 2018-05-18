package theory;

public class MidiNote {
	
	private int pitch;
	private double duration;
	
	public MidiNote(int midiPitch, double duration) {
		this.pitch = midiPitch;
		this.duration = duration;
	}
	
	public MidiNote(MidiPitch pitch, double length) {
		this(pitch.get(), length);
	}

}
