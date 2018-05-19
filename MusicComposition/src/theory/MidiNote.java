package theory;

public class MidiNote {
	
	private int pitch;
	private double duration;
	int peakMillis = 100;
	
	public MidiNote(int midiPitch, double duration) {
		this.pitch = midiPitch;
		this.duration = duration;
	}
	
	public MidiNote(MidiPitch pitch, double length) {
		this(pitch.get(), length);
	}
	
	public int getPitch() {
		return pitch;
	}
	
	public double getDuration() {
		return duration;
	}
	
	public int getPeakMillis() {
		return peakMillis;
	}

}
