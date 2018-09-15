package composing;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import theory.Measure;
import theory.analysis.Analysis;

/**
 * @author kennethscott
 *
 */
public class IncompleteComposition extends Composition {

	private Queue<Measure> future;
	private Analysis analysis;
	
	public IncompleteComposition() {
		super();
		initFields();
	}
	
	public IncompleteComposition(Composition other) {
		super(other);
		if (other instanceof IncompleteComposition) {
			// TODO deep copy?
			this.future = ((IncompleteComposition) other).future;
			this.analysis = ((IncompleteComposition) other).analysis;
		}
		else {
			initFields();
		}
	}
	
	private void initFields() {
		this.future = new LinkedList<>();
		this.analysis = new Analysis();
	}
	
	@Override
	public synchronized Measure getMeasure(int measureNumber) {
		if (measureNumber <= measures.size())
			return super.getMeasure(measureNumber);
		if (measureNumber > measures.size() + future.size())
			throw new IllegalArgumentException("Have not yet written measure " + measureNumber);
		return new ArrayList<>(future).get(measureNumber - measures.size() - 1);
	}
	
	@Override
	public synchronized List<Measure> getMeasures(int firstMeasureNumber, int lastMeasureNumber) {
		if (lastMeasureNumber <= measures.size())
			return super.getMeasures(firstMeasureNumber, lastMeasureNumber);
		if (lastMeasureNumber < firstMeasureNumber)
			throw new IllegalArgumentException("Please don't do that.");
		if (firstMeasureNumber < 1 || lastMeasureNumber > measures.size() + future.size())
			throw new IllegalArgumentException("Requested range of measures outside this composition's size.");
		if (firstMeasureNumber > measures.size())
			return new ArrayList<>(future).subList(
					firstMeasureNumber - measures.size() - 1, lastMeasureNumber - measures.size());
		List<Measure> retval = new ArrayList<>();
		retval.addAll(measures.subList(firstMeasureNumber - 1, measures.size()));
		retval.addAll(new ArrayList<>(future).subList(0, lastMeasureNumber));
		return retval;	
	}
	
	@Override
	public synchronized int size() {
		return measures.size() + future.size();
	}
	
	/**
	 * The measures of this composition that have yet to be finalized. They can be modified in any way.
	 * <p>
	 * Intended to allow consumers to directly edit the incomplete portion of this Composition
	 * 
	 * @return the actual object instance of this Composition's incomplete measures
	 */
	public synchronized Queue<Measure> getFuture() {
		return future;
	}

	public synchronized Measure writeNextMeasure() {
		final Measure measure = future.poll();
		if (measure == null)
			return Measure.commonTime();
		addMeasure(measure);
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
