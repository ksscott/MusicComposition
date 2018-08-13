package performance;

public class MidiAction implements Cloneable {

	protected double duration;
	
	public MidiAction(double duration) {
		if (duration <= 0)
			throw new IllegalArgumentException("MidiNote must have a positive duration.");
		this.duration = duration;
	}

	public double getDuration() {
		return duration;
	}
	
	/** Returns a new clone, lengthened by the given timeRatio */
	public MidiAction expand(double timeRatio) {
		if (timeRatio <= 0)
			throw new IllegalArgumentException("Only positive ratios accepted for expansions.");
		return new MidiAction(timeRatio*duration);
	}
	
	@Override
	public MidiAction clone() {
		return new MidiAction(this.duration);
	}
	
	public static class MidiRest extends MidiAction {
		
		public MidiRest(double duration) {
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
