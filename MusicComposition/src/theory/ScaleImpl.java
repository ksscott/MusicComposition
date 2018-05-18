package theory;

public class ScaleImpl implements Scale {

	protected int[] intervals;
	
	public ScaleImpl(int[] intervals) {
		this.intervals = intervals;
	}
	
	@Override
	public int[] intervals() {
		int[] retval = new int[intervals.length];
		System.arraycopy(intervals, 0, retval, 0, intervals.length);
		return retval;
	}

}
