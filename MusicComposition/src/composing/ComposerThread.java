package composing;

import composing.strategy.ComposingStrategy;
import theory.Measure;

public class ComposerThread extends Thread {

	private ComposingStrategy strategy;
	private IncompleteComposition composition;
	
	private boolean paused;
	private boolean stopped;
	
	public ComposerThread(Composition composition, ComposingStrategy strategy) {
		this.strategy = strategy;
		this.composition = new IncompleteComposition(composition);
	}
	
	@Override
	public void run() {
		System.out.println("Composing " + strategy);
		while (!stopped) {
			if (!paused)
				if (strategy.iterate(composition))
					paused = true;
			try { Thread.sleep(100); } catch (Exception e) {}
		}
	}
	
	public Measure writeNextMeasure() {
		Measure measure = composition.writeNextMeasure();
		paused = false;
		return measure;
	}
	
	ComposingStrategy getStrategy() {
		return strategy;
	}
	
	public Composition stopComposing() {
		this.stopped = true;
		System.out.println("Done composing " + strategy);
		return composition.finishComposition();
	}

}
