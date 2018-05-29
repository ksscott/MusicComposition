package composing;

import java.util.LinkedList;
import java.util.Queue;

import theory.Measure;
import theory.analysis.Analysis;

public class IncompleteComposition extends Composition {

	private Queue<Measure> future;
	private Analysis analysis;
	
	public IncompleteComposition() {
		super();
		this.future = new LinkedList<>();
		this.analysis = new Analysis();
	}
	
	public IncompleteComposition(Composition other) {
		super(other);
		if (other instanceof IncompleteComposition)
			this.future = ((IncompleteComposition) other).future;
		else
			this.future = new LinkedList<>();
	}
	
	/**
	 * Intended to allow consumers to directly edit the incomplete portion of this Composition
	 * 
	 * @return the actual object instance of this Composition's incomplete measures
	 */
	public Queue<Measure> getFuture() {
		return future;
	}

	public Measure writeNextMeasure() {
		final Measure measure = future.poll();
		this.measures.add(measure);
		return measure;
	}
	
	public Composition finishComposition() {
		// TODO do other work here?
		return new Composition(this);
	}
	
	public Analysis getAnalysis() {
		return analysis;
	}
	
}
