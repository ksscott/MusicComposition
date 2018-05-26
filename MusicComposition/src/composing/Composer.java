package composing;

import composing.strategy.ComposingStrategy;
import composing.strategy.PrettyProgressionStrategy;
import composing.strategy.TwelveBarImprovStrategy;
import theory.Key;
import theory.Letter;
import theory.Measure;
import theory.Note;

public class Composer {
	
	private ComposerThread thread;
	
	private ComposingStrategy[] oldTricks = new ComposingStrategy[] { 
					new TwelveBarImprovStrategy(new Note(Letter.C)),
					new PrettyProgressionStrategy(new Key(new Note(Letter.C), Key.MAJOR)),
			};
	
	public Measure beginComposing() {
//		return beginComposing(randomRepertoire());
		return beginComposing(oldTricks[1]);
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
	
	private ComposingStrategy randomRepertoire() {
		return oldTricks[(int) (Math.random() * oldTricks.length)];
	}
}
