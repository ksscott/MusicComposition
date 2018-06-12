package theory.analysis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import theory.Key;

public class Section {

	private int measures;
	private Map<Integer,Set<Key>> keys;
	
	public Section(int measures) {
		this.measures = measures;
		this.keys = new HashMap<>();
	}
	
	public int size() {
		return measures;
	}
	
	public Set<Key> getKeys(int measureNumber) {
		return keys.get(measureNumber);
	}
	
	public Set<Key> getAllKeys() {
		return keys.values().stream().flatMap(Set::stream).collect(Collectors.toSet());
	}
	
	public void putKey(int measureNumber, Key key) {
		if (measureNumber < 1 || measureNumber > measures)
			throw new IllegalArgumentException("Cannot add key outside this section.");
		Set<Key> set = keys.get(measureNumber);
		if (set == null) {
			set = new HashSet<>();
			keys.put(measureNumber, set);
		}
		set.add(key);
	}
	
	public void putKey(int firstMeasureNumber, int lastMeasureNumber, Key key) {
		for (int i=firstMeasureNumber; i<=lastMeasureNumber; i++)
			putKey(i, key);
	}
	
	public void removeKey(int measureNumber, Key key) {
		Set<Key> set = keys.get(measureNumber);
		if (!(set == null))
			set.remove(key);
	}
	
}
