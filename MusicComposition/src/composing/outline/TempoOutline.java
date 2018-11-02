package composing.outline;

import performance.Tempo;
import performance.Tempo.TempoImpl;

public class TempoOutline extends ContinuousOutline<Tempo> {

	private Resolver<Tempo> resolver;
	
	public TempoOutline() {
		this.resolver = new Resolver<Tempo>() {
			@Override
			public Tempo resolve(Tempo startValue, Tempo endValue, Double distance) {
				int start = startValue.getBpm();
				int end = endValue.getBpm();
				return new TempoImpl((int) (start + distance * (end - start))); // rounding
			}
		};
	}
	
	@Override
	protected ContinuousOutline.Resolver<Tempo> getResolver() {
		return resolver;
	}

}
