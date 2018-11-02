package composing.outline;

import performance.Dynamic;

public class DynamicOutline extends ContinuousOutline<Dynamic> {

	private Resolver<Dynamic> resolver;
	
	public DynamicOutline() {
		super();
		this.resolver = new Resolver<Dynamic>() {
			// curving through Dynamic space is pretty straightforward,
			// assume there is a linear distribution of Dynamics between one and another
			@Override
			public Dynamic resolve(Dynamic startValue, Dynamic endValue, Double distance) {
				return startValue.up((int) (distance * startValue.levelsTo(endValue))); // rounding
			}
		};
	}
	
	@Override
	protected ContinuousOutline.Resolver<Dynamic> getResolver() {
		return resolver;
	}

}
