package theory;

import java.util.Arrays;

/**
 * Usable implementation of a {@link Scale}
 */
public class ScaleImpl implements Scale,Cloneable {

	protected String name;
	protected int[] intervals;
	
	/**
	 * @param intervals must be an array of a positive number of positive integers
	 */
	public ScaleImpl(int[] intervals) {
		this(intervals, "");
	}
	
	/**
	 * @param intervals must be an array of a positive number of positive integers
	 * @param name a name for this scale
	 */
	public ScaleImpl(int[] intervals, String name) {
		this.name = name;
		if (intervals.length == 0)
			throw new IllegalArgumentException("Scale must have some number of intervals");
		for (int interval : intervals)
			if (interval < 0)
				throw new IllegalArgumentException("Scale intervals must be positive");
		this.intervals = intervals;
	}
	
	/** Creates a copy of the given scale, to the extent that {@link Scale} is aware. */
	public ScaleImpl(Scale other) {
		this(other.intervals(), other.name());
	}
	
	@Override
	public String name() {
		return new String(name);
	}
	
	@Override
	public int[] intervals() {
		int[] retval = new int[intervals.length];
		System.arraycopy(intervals, 0, retval, 0, intervals.length);
		return retval;
	}
	
	@Override
	public String toString() {
		String name = name();
		if ("".equals(name)) {
			for (int interval : intervalsFromRoot())
				name += " " + interval;
			name = "[" + name.trim() + "]";
		}
		return name;
	}
	
	@Override
	public ScaleImpl clone() {
		return new ScaleImpl(this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(intervals);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Scale)) // am I breaking the equals contract here?
			return false;
		if (!Arrays.equals(intervals, ((Scale) obj).intervals()))
			return false;
		return true;
	}

}
