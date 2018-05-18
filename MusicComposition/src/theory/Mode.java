package theory;

public enum Mode implements Scale {

	IONIAN(1),
	DORIAN(2),
	PHRYGIAN(3),
	LYDIAN(4),
	MIXOLYDIAN(5),
	AEOLIAN(6),
	LOCRIAN(7),
	;
	
	public static final int[] DIATONIC_INTERVALS = new int[] { 2, 2, 1, 2, 2, 2, 1};
	
	private Mode(int index) {
		
	}
	
	@Override
	public int[] intervals() {
		return scale().intervals();
	}
	
	public Scale scale() {
		int length = DIATONIC_INTERVALS.length;
		int[] intervals = new int[length];
		for (int i=0; i<length; i++) {
			intervals[i] = DIATONIC_INTERVALS[(i + ordinal() - 1) % 12];
		}
		return new ScaleImpl(intervals);
	}

}
