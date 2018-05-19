package composing;

import composing.strategy.ComposingStrategy;
import composing.strategy.TwelveBarImprovStrategy;
import theory.Accidental;
import theory.Letter;
import theory.Measure;
import theory.Note;

public class Composer {
	
	private ComposerThread thread;
	
	public Measure beginComposing() {
		return beginComposing(new TwelveBarImprovStrategy(new Note(Letter.C, Accidental.NONE)));
	}
	
	public Measure beginComposing(ComposingStrategy strategy) {
		Composition composition = new Composition();
		Measure measure = strategy.generateFirstMeasure();
		composition.addMeasure(measure);
		thread = new ComposerThread(composition, strategy);
		thread.start();
		return measure;
	}
	
	public Measure writeNextMeasure() {
		return thread.writeNextMeasure();
	}
	
	public Composition finishComposing() {
		return thread.stopComposing();
	}
	
}
