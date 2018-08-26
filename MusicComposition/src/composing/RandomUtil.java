package composing;

public class RandomUtil {
	
	private RandomUtil() {} // util class

	public static boolean roll(int percentChance) {
		return Math.random()*100 < percentChance;
	}

	/**
	 * @param max
	 * @return random int including zero but not max <br> [0,max)
	 */
	public static int random(int max) {
		return (int) (Math.random()*max);
	}

	/**
	 * @param min
	 * @param max
	 * @return a random int including min but not max <br> [min,max)
	 */
	public static int random(int min, int max) {
		return min + ((int) (Math.random()*max));
	}
	
	/**
	 * Performs the mod operation, always returning a non-negative result 
	 * between zero (inclusing) and the given "mod" (exclusive)
	 */
	public static int modPos(int number, int mod) {
		return ((number % mod) + mod) % mod;
	}

}
