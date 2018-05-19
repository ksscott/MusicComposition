package composing.strategy;

import composing.IncompleteComposition;
import theory.Measure;
import theory.Note;

public class TwelveBarImprovStrategy extends TwelveBarBluesStrategy {

	public TwelveBarImprovStrategy(Note tonic) {
		super(tonic);
	}

	@Override
	protected Measure composeBar(IncompleteComposition composition) {
		Measure measure = super.composeBar(composition);
		
		// TODO compose improvisational magic here!
		
		return measure;
	}
	
}
