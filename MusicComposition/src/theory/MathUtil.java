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
	
	public static final Curve LINEAR = d -> d;
	public static final Curve SINUSOIDAL = d -> (Math.cos((d+1) * Math.PI) + 1) / 2.0;
	public static final Curve QUICK = d -> Math.sin(d * Math.PI / 2.0);
	public static final Curve ACCELERATING = d -> (Math.cos((d+2) * Math.PI / 2.0) + 1);

	/** Intended to map values from (0,1) inclusive onto values (0,1) inclusive */
	public static interface Curve extends UnaryOperator<Double> {}

}
