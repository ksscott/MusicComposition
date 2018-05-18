package composing;

import composing.strategy.ComposingStrategy;
import composing.strategy.TwelveBarBluesStrategy;
import theory.Measure;

public class Composer {
	
	private ComposerThread thread;
	
	public Composer() {
		this(new TwelveBarBluesStrategy());
	}
	
	public Composer(ComposingStrategy strategy) {
		this.thread = new ComposerThread(strategy);
	}
	
	public void compose() {
		thread.start();
	}
	
	public Measure writeNextMeasure() {
		return thread.writeNextMeasure();
	}
	
	public Composition finishComposing() {
		return thread.stopComposing();
	}
	
}
