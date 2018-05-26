package theory;

public interface Scale {
	
	public default String name() { return "<anonymous scale> " + intervals(); }

	/**
	 * Should not include the root note. For example, the major scale would be:
	 * <p>
	 * [2, 2, 1, 2, 2, 2, 1]
	 * <p>
	 * to indicate the following scale degrees:
	 * <p>
	 * (do) [re, mi, fa, sol, la, ti, (do)]
	 * 
	 * @return an int array indicating half steps for each interval
	 */
	public int[] intervals();
	
	/**
	 * Includes the root note but not the start of the scale above. For example, the major intervals are:
	 * <p>
	 * [0, 2, 4, 5, 7, 9, 11]
	 * <p>
	 * to indicate the following scale degrees:
	 * <p>
	 * [do, re, mi, fa, sol, la, ti] (do)
	 * 
	 * @return the distance in half steps that each scale degree is above the root
	 */
	public default int[] intervalsFromRoot() {
		int[] intervals = intervals();
		int length = intervals.length;
		int[] retval = new int[length];
		retval[0] = 0;
		for (int i=0; i<length-1; i++) {
			int sum = 0;
			for (int j=0; j<=i; j++) {
				sum += intervals[j];
			}
			retval[i+1] = sum;
		}
		return retval;
	}
	
	public default boolean isDiatonic() {
		int[] intervals = intervals();
		int length = intervals.length;
		if (length != 7)
			return false;
		
		int firstHalfStep = -1;
		int secondHalfStep = -1;
		
		for (int i=0; i<length; i++) {
			if (intervals[i] == 1) {
				if (firstHalfStep == -1) {
					firstHalfStep = 1;
					continue;
				}
				if (secondHalfStep == -1) {
					secondHalfStep = i;
					continue;
				}
			}
			if (intervals[i] != 2)
				return false; // found an interval other than a half or whole step
		}
		
		if (firstHalfStep == -1 || secondHalfStep == -1)
			return false; // found fewer than two half steps
		
		return true;
	}
	
}
