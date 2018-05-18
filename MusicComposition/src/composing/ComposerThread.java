package composing;

import composing.strategy.ComposingStrategy;
import theory.Measure;

public class ComposerThread extends Thread {

	private ComposingStrategy strategy;
	private IncompleteComposition composition;
	
	private boolean paused;
	private boolean stopped;
	
	public ComposerThread(ComposingStrategy strategy) {
		this.strategy = strategy;
		this.composition = new IncompleteComposition();
	}
	
	@Override
	public void run() {
		while (!stopped) {
			if (!paused)
				if (strategy.iterate(composition))
					paused = true;
			try { Thread.sleep(100); } catch (Exception e) {}
		}
		System.out.println("Done composing");
	}
	
	public Measure writeNextMeasure() {
		Measure measure = composition.writeNextMeasure();
		paused = false;
		return measure;
	}
	
	public Composition stopComposing() {
		this.stopped = true;
		return composition.finishComposition();
	}

}
