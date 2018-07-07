package theory;

import java.util.Arrays;

public class ScaleImpl implements Scale {

	protected String name;
	protected int[] intervals;
	
	public ScaleImpl(int[] intervals) {
		this(intervals, "");
	}
	
	public ScaleImpl(int[] intervals, String name) {
		this.name = name;
		this.intervals = intervals;
	}
	
	public ScaleImpl(Scale other) {
		this.name = other.name();
		this.intervals = other.intervals();
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
