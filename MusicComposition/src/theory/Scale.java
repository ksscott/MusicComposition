package theory;

public interface Scale {

	public int[] intervals();
	
	public default int[] intervalsFromRoot() {
		int[] intervals = intervals();
		int length = intervals.length;
		int[] retval = new int[length];
		for (int i=0; i<length; i++) {
			int sum = 0;
			for (int j=0; j<=i; j++) {
				sum += intervals[j];
			}
			retval[i] = sum;
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
