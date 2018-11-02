package composing.outline;

public class RhythmicDensityOutline extends ContinuousOutline<Double> {

	private Resolver<Double> resolver;
	
	public RhythmicDensityOutline() {
		super();
		resolver = new Resolver<Double>() {
			@Override
			public Double resolve(Double startValue, Double endValue, Double distance) {
				return startValue + distance*(endValue-startValue);
			}
		};
	}
	
	@Override
	protected ContinuousOutline.Resolver<Double> getResolver() {
		return resolver;
	}

}
