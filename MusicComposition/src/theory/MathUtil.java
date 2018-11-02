package theory;

import java.util.function.UnaryOperator;

public class MathUtil {

	/**
	 * Performs the mod operation, always returning a non-negative result 
	 * between zero (inclusing) and the given "mod" (exclusive)
	 */
	public static int modPos(int number, int mod) {
		return ((number % mod) + mod) % mod;
	}
	
	/** Includes (0,0) and (1,1). Is linear. */
	public static final Curve LINEAR = d -> d;
	/** Includes (0,0) and (1,1). Shaped like the second half of a cosine curve with period 2, with dy/dx=0 at x=0 and at x=1. */
	public static final Curve SINUSOIDAL = d -> (Math.cos((d+1) * Math.PI) + 1) / 2.0;
	/** Includes (0,0) and (1,1). The first quarter of a sine curve with period 4. */
	public static final Curve QUICK = d -> Math.sin(d * Math.PI / 2.0);
	/** Includes (0,0) and (1,1). Shaped like the last quarter of a sine curve with period 4. */
	public static final Curve ACCELERATING = d -> (Math.cos((d+2) * Math.PI / 2.0) + 1);

	/** Intended to map values from domain [0,1] to values in range [0,1] */
	public static interface Curve extends UnaryOperator<Double> {}

}
