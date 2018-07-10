package composing;

import composing.strategy.ComposingStrategy;
import composing.strategy.PrettyProgressionStrategy;
import performance.Tempo;
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
	
	Measure writeNextMeasure() {
		Measure measure = composition.writeNextMeasure();
		paused = false;
		return measure;
	}
	
	ComposingStrategy getStrategy() {
		return strategy;
	}
	
	Composition stopComposing() {
		this.stopped = true;
		System.out.println("Done composing " + strategy);
		return composition.finishComposition();
	}
	
	void requestTempoChange(boolean increase) {
		// FIXME support all or else have a failure mode
		if (strategy instanceof PrettyProgressionStrategy) {
			Tempo newTempo = ((PrettyProgressionStrategy) strategy).requestTempoChange(increase);
			// update measures already composed (consider changing)
			for (Measure measure : composition.getFuture())
				measure.setBpm(newTempo.getBpm());
			System.out.println((increase ? "Increased" : "Decreased") + " tempo to " + newTempo.name());
		}
	}
}
