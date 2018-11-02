package composing.outline;

import java.util.Set;

import performance.Dynamic;

public class RhythmicOutline extends DiscreteOutline<Dynamic> {

	
	
	
	// POC work:
	/**
	 * Creates ever shrinking binary divisions of a whole note, like the ticks on a ruler
	 * 
	 * @param density
	 * @param dynamic
	 * @return one whole-note of rhythms
	 */
	public static RhythmicOutline simpleDensityReification(RhythmicDensityOutline density, DynamicOutline dynamic) {
		RhythmicOutline outline = new RhythmicOutline();
		
		Set<Double> range = density.getAllPossibleEndpoints(0.0, 1.0);
		Double max = range.stream()
						  .peek(d -> {if (d < 0 || d > 1) throw new IllegalStateException();})
						  .mapToDouble(d -> d)
						  .max()
						  .orElse(0.0);
		if (max == 0) // don't play any rhythms ever
			return outline;
		Double smallestDuration = 1 - max;
		
		// start from a the whole-note downbeat and visit all smaller values in measure like a binary search
		Double noteValue = 1.0;
		int iterations = 0;
		while (noteValue >= smallestDuration) {
			iterations++;
			
			// place each beat of this value if necessary
			for (int i=0; i<Math.pow(2,iterations-1); i++) {
				Double time = i*noteValue;
				if ((1-density.get(time)) <= noteValue) {
					// dense enough to play this noteValue at this time
					// calculate played dynamic
					Dynamic envelope = dynamic.get(time); // TODO decide whether to envelope this here or closer to playing time
					Dynamic playedDynamic = envelope.down(iterations-1);
					outline.add(playedDynamic, time);
				}
			}
			
			// attempt to proceed to next smallest note value
			noteValue /= 2.0;
		}
		
		return outline;
	}
	
}
