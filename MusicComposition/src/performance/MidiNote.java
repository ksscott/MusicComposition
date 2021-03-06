package performance;

import theory.MidiPitch;

public class MidiNote extends MidiAction {
	
	private int pitch;
	private int peakMillis;
	private Dynamic dynamic;
	
	// experimental:
	private int tiedFromPitch;
	private boolean tiesOver;
	
	public MidiNote(int midiPitch, double duration) {
		super(duration);
		this.pitch = midiPitch;
		this.peakMillis = 150;
		this.dynamic = Dynamic.MEZZO_FORTE;
	}
	
	public MidiNote(MidiPitch pitch, double duration) {
		this(pitch.get(), duration);
	}
	
	public int getPitch() {
		return pitch;
	}
	
	public int getPeakMillis() {
		return peakMillis;
	}
	
	public void setPeakMillis(int milliseconds) {
		this.peakMillis = milliseconds;
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
	
	/**
	 * Note: does not preserve note ties 
	 * @see {@link MidiAction#expand(double)}
	 */
	@Override
	public MidiNote expand(double timeRatio) {
		if (timeRatio <= 0)
			throw new IllegalArgumentException("Only positive ratios accepted for expansions.");
		MidiNote note = new MidiNote(pitch, duration*timeRatio);
		note.peakMillis = this.peakMillis;
		note.dynamic = this.dynamic;
		// choosing not to preserve any ties
		return note;
	}
	
	/** discouraged, use #tieOver */
	public void setTiesOver(boolean ties) {
		tiesOver = ties;
	}
	
	public static void tieOver(MidiNote from, MidiNote to) {
		from.tiesOver = true;
		to.tiedFromPitch = from.pitch;
	}
	
	@Override
	public String toString() {
		return "MidiNote[" + pitch + "," + duration + "]";
	}
	
	@Override
	public MidiNote clone() {
		return expand(1);
	}
}
