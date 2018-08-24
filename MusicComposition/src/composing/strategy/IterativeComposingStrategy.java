package composing.strategy;

import composing.IncompleteComposition;

public abstract class IterativeComposingStrategy implements ComposingStrategy {

	@Override
	public boolean iterate(IncompleteComposition composition) {
		ComposingStage next = nextComposingStage(composition);
		if (next == null)
			return true;
		next.apply(composition);
		return false;
	}
	
	/**
	 * @param composition the composition to iterate on
	 * @return <code>null</code> if no further composing is necessary until the next measure is played
	 */
	protected abstract ComposingStage nextComposingStage(IncompleteComposition composition);
	
	public static interface ComposingStage {
		/**
		 * Iterate on the incomplete composition in whatever modular manner this stage is responsible for
		 * 
		 * @param composition
		 */
		public void apply(IncompleteComposition composition);
	}
	
	

}
