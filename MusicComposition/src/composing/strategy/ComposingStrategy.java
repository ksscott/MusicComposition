package composing.strategy;

import composing.IncompleteComposition;
import theory.Measure;

public interface ComposingStrategy {

	public Measure generateFirstMeasure();
	
	/**
	 * @param composition the composition to iterate on
	 * @return true if the strategy wishes to wait for another measure 
	 * to be written before being given a chance to iterate further
	 */
	public boolean iterate(IncompleteComposition composition);
	
}
