package composing;

import java.util.ArrayList;
import java.util.List;

import theory.Measure;

public class Composition {
	
	protected List<Measure> measures;
	
	public Composition() {
		measures = new ArrayList<>();
	}
	
	public Composition(Composition other) {
		this.measures = other.measures;
	}
	
	public List<Measure> getMeasures() {
		return new ArrayList<>(measures);
	}
	
	public Measure getMeasure(int measureNumber) {
		if (measureNumber <= 0)
			throw new IllegalArgumentException("Only natural numbered measures can be returned.");
		if (measureNumber > measures.size())
			throw new IllegalArgumentException("Composition only contains " + measures.size()
			+ " measures; cannot get measure number " + measureNumber);
		return measures.get(measureNumber - 1);
	}
	
	/**
	 * @param firstMeasureNumber first measure to be returned
	 * @param lastMeasureNumber last measure to be returned
	 * @return list of measures between the given measure numbers, inclusive
	 */
	public List<Measure> getMeasures(int firstMeasureNumber, int lastMeasureNumber) {
		if (lastMeasureNumber < firstMeasureNumber)
			throw new IllegalArgumentException("Please don't do that.");
		if (firstMeasureNumber < 1 || lastMeasureNumber > measures.size())
			throw new IllegalArgumentException("Requested range of measures outside this composition's size.");
		return measures.subList(firstMeasureNumber-1, lastMeasureNumber);
	}

	public int size() {
		return measures.size();
	}
	
	public void addMeasure(Measure measure) {
		measures.add(measure);
		measure.setMeasureNumber(measures.size());
	}

}
