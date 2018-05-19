package theory;

public enum Mode implements Scale {

	IONIAN,
	DORIAN,
	PHRYGIAN,
	LYDIAN,
	MIXOLYDIAN,
	AEOLIAN,
	LOCRIAN,
	;
	
	public static final int[] DIATONIC_INTERVALS = new int[] { 2, 2, 1, 2, 2, 2, 1};
	
	@Override
	public int[] intervals() {
		return scale().intervals();
	}
	
	public Scale scale() {
		int length = DIATONIC_INTERVALS.length;
		int[] intervals = new int[length];
		for (int i=0; i<length; i++) {
			intervals[i] = DIATONIC_INTERVALS[(i + ordinal() + length) % length];
		}
		return new ScaleImpl(intervals);
	}

}
