package composing.outline;

/**
 * A mapping of a single dimension as a function of time.
 * 
 * @param <T> the type of the dimension being outlined
 */
public interface Outline<T> {

	public T get(Double time);
	
}
