package composing.outline;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class DiscreteOutline<T> implements Outline<T> {
	
	protected Map<Double,T> map;
	
	public DiscreteOutline() {
		this.map = new HashMap<>();
	}
	
	/**
	 * @param time to look for a value
	 * @return the value written at exactly the given time
	 */
	public T get(Double time) {
		return map.get(time);
	}
	
	public List<Double> getTimes() {
		return new ArrayList<>(map.keySet());
	}
	
	/**
	 * @param time to look for value
	 * @return the value written at the given time, or most recently before the given time
	 */
	public T getLast(Double time) {
		Double lastTime = time;
		if (!map.containsKey(lastTime))
			lastTime = map.keySet()
						  .stream()
						  .mapToDouble(d -> d)
						  .filter(key -> key<=time)
						  .max()
						  .orElse(0); // ?
		return map.get(lastTime);
	}
	
	public void add(T type, Double time) {
		map.put(time, type);
	}

}
