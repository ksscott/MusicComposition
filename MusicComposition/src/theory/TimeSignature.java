package theory;

public class TimeSignature implements Cloneable {
	
	private final int beats;
	private final NoteDuration beatValue;
	
	public TimeSignature(int beats, NoteDuration beatValue) {
		if (beats < 1)
			throw new IllegalArgumentException("Time Signature must have at least one beat.");
		if (beatValue.getPower() > 0)
			throw new IllegalArgumentException("Time Signature beats cannot be larger than a whole note.");
		if (beatValue.getDots() > 0 || beatValue.isTriplet())
			throw new IllegalArgumentException("Time Signature beats cannot be dotted or triplets.");
		
		this.beats = beats;
		this.beatValue = beatValue.clone();
	}
	
	public int getBeats() {
		return beats;
	}
	
	public NoteDuration getBeatValue() {
		return beatValue.clone();
	}
	
	/** @return length of a measure with this time signature, in whole notes */
	public double length() {
		return beats * beatValue.duration();
	}
	
	public TimeSignature subdivide() { // probably totally useless, but here it is
		return new TimeSignature(beats*2, beatValue.halved());
	}
	
	public static TimeSignature commonTime() {
		return new TimeSignature(4, NoteDuration.quarter());
	}
	
	public static TimeSignature cutTime() {
		return new TimeSignature(2, NoteDuration.half());
	}
	
	public static TimeSignature sixEight() {
		return new TimeSignature(6, NoteDuration.eighth());
	}
	
	public static TimeSignature threeTwo() {
		return new TimeSignature(3, NoteDuration.half());
	}
	
	public TimeSignature clone() {
		return new TimeSignature(beats, beatValue);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((beatValue == null) ? 0 : beatValue.hashCode());
		result = prime * result + beats;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TimeSignature other = (TimeSignature) obj;
		if (beatValue == null) {
			if (other.beatValue != null)
				return false;
		} else if (!beatValue.equals(other.beatValue))
			return false;
		if (beats != other.beats)
			return false;
		return true;
	}

}
