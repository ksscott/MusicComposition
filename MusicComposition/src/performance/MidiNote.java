package performance;

import theory.MidiPitch;

public class MidiNote {
	
	private int pitch;
	private double duration;
	private int peakMillis = 150;
	private Dynamic dynamic;
	
	// experimental:
	private int tiedFromPitch;
	private boolean tiesOver;
	
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
	
	/** @return the pitch of the previous note that this note is tied to */
	public int getTiedFromPitch() {
		return tiedFromPitch;
	}
	
	/** @return true if this note ends with a tie to another note */
	public boolean tiesOver() {
		return tiesOver;
	}
	
	public static void tieOver(MidiNote from, MidiNote to) {
		from.tiesOver = true;
		to.tiedFromPitch = from.pitch;
	}
}
