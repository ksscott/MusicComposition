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
	
	public void addMeasure(Measure measure) {
		measures.add(measure);
	}

}
