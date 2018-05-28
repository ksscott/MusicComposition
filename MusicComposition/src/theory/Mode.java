package theory;

/**
 * The seven rotations of the diatonic scale
 * 
 * @author kennethscott
 */
public enum Mode implements Scale {

	IONIAN,
	DORIAN,
	PHRYGIAN,
	LYDIAN,
	MIXOLYDIAN,
	AEOLIAN,
	LOCRIAN,
	;
	
	public static final int[] DIATONIC_INTERVALS = new int[] { 2, 2, 1, 2, 2, 2, 1 };
	
	@Override
	public int[] intervals() {
		int length = DIATONIC_INTERVALS.length;
		int[] intervals = new int[length];
		for (int i=0; i<length; i++) {
			intervals[i] = DIATONIC_INTERVALS[(i + ordinal() + length) % length];
		}
		return intervals;
	}
	
//	public Scale scale() {
//		return new ScaleImpl(intervals());
//	}
	
	/**
	 * @param steps number of steps to revolve the root upwards by
	 * @return the mode that results from moving the root of this mode up by 
	 * the given number of steps through the existing notes in this mode
	 */
	public Mode revolve(int steps) {
		Mode[] values = values();
		return values[(ordinal()+steps)%values.length];
	}
	
	/**
	 * @param scale
	 * @return the equivalent diatonic mode, or <code>null</code> if there is none
	 */
	public static Mode equivalent(Scale scale) {
		for (Mode mode : Mode.values())
			if (mode.intervals().equals(scale.intervals()))
				return mode;
		return null;
	}

}
