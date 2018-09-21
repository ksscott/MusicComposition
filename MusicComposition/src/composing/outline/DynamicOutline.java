package composing.outline;

import performance.Dynamic;

public class DynamicOutline extends ContinuousOutline<Dynamic> {

	private Resolver resolver;
	
	public DynamicOutline() {
		super();
		this.resolver = new Resolver() {
			@Override
			public Dynamic resolve(Dynamic startValue, Dynamic endValue, Double distance) {
				return startValue.up((int) (distance * startValue.levelsTo(endValue))); // rounding
			}
		};
	}
	
	@Override
	protected ContinuousOutline<Dynamic>.Resolver getResolver() {
		return resolver;
	}

}
