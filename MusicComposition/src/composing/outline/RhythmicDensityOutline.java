package composing.outline;

public class RhythmicDensityOutline extends ContinuousOutline<Double> {

	private Resolver resolver;
	
	public RhythmicDensityOutline() {
		super();
		resolver = new Resolver() {
			@Override
			public Double resolve(Double startValue, Double endValue, Double distance) {
				return startValue + distance*(endValue-startValue);
			}
		};
	}
	
	@Override
	protected ContinuousOutline<Double>.Resolver getResolver() {
		return resolver;
	}

}
