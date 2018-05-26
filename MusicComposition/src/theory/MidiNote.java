package theory;

public class MidiNote {
	
	private int pitch;
	private double duration;
	private int peakMillis = 150;
	private Dynamic dynamic;
	
	public MidiNote(int midiPitch, double duration) {
		this.pitch = midiPitch;
		this.duration = duration;
		this.dynamic = Dynamic.MEZZO_FORTE;
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
	
	public Dynamic getDynamic() {
		return Dynamic.of(dynamic);
	}
	
	public void setDynamic(Dynamic dynamic) {
		this.dynamic = dynamic;
	}

}
