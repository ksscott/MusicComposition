package performance;

import theory.NoteDuration;

public class MidiAction implements Cloneable {

	protected NoteDuration duration;
	
	public MidiAction(NoteDuration duration) {
		this.duration = duration;
	}

	public NoteDuration getDuration() {
		return duration;
	}
	
	public double duration() {
		return duration.duration();
	}
	
//	/** Returns a new clone, lengthened by the given timeRatio */
//	public MidiAction expand(double timeRatio) {
//		if (timeRatio <= 0)
//			throw new IllegalArgumentException("Only positive ratios accepted for expansions.");
//		return new MidiAction(timeRatio*duration);
//	}
	
	/**
	 * @param power multiply the duration of this action by 2 ^ (given power)
	 * @return a clone, lengthened or contracted by the given power of 2
	 * @seealso {@link NoteDuration#expand(int)}
	 */
	public MidiAction expand(int power) {
		return new MidiAction(duration.expand(power));
	}
	
	@Override
	public MidiAction clone() {
		return new MidiAction(this.duration);
	}
	
	public static class MidiRest extends MidiAction {
		
		public MidiRest(NoteDuration duration) {
			super(duration);
		}
		
		@Override
		public MidiRest clone() {
			return new MidiRest(MidiRest.this.duration);
		}
		
		@Override
		public String toString() {
			return "MidiRest[" + duration + "]";
		}
	}
}
