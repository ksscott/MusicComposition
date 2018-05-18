package composing.strategy;

import java.util.List;
import java.util.Queue;

import composing.IncompleteComposition;
import theory.Measure;
import theory.MidiNote;

public class TwelveBarBluesStrategy implements ComposingStrategy {
	
	private int[] chordsHalfSteps = new int[] { 0, 0, 0, 0, 5, 5, 0, 0, 7, 5, 0, 0 };
	private int[] arpeggioHalfSteps= new int[] { 0, 4, 7, 9, 10 };

	@Override
	public Measure generateFirstMeasure() {
		return composeBar(new IncompleteComposition());
	}

	@Override
	public boolean iterate(IncompleteComposition composition) {
		composeBar(composition);
		return composition.getFuture().size() >= 12;
	}

	/**
	 * Allows subclasses to extend this method and write additional music on top of the base line. e.g.
	 * <p>
	 * Override <br>
	 * protected Measure composeBar(IncompleteComposition composition) { <br>
	 * Measure measure = super(composition); <br>
	 * -- Whatever you want to compose here -- <br>
	 * return measure; <br>
	 * }
	 * 
	 * @param composition
	 * @return
	 */
	protected Measure composeBar(IncompleteComposition composition) {
		int currentBar = getCurrentBar(composition);
		Measure measure;
		
		if (currentBar % 2 == 0 || currentBar == 9) // odd measures (but also 9, which is "even")
			measure = composeArpeggioA();
		else
			measure = composeArpeggioB();
		
		return measure;
	}

	private Measure composeArpeggioB() {
		int beats = 4;
		double beatValue = 1/4.0;
		Measure measure = new Measure(beats, beatValue);
		for (int i=0; i<beats; i++)
			measure.addNote(new MidiNote(arpeggioHalfSteps[i], beatValue), i*beatValue); // FIXME
		return measure;
	}

	private Measure composeArpeggioA() {
		int beats = 4;
		double beatValue = 1/4.0;
		Measure measure = new Measure(beats, beatValue);
		for (int i=0; i<beats; i++)
			measure.addNote(new MidiNote(arpeggioHalfSteps[4-i], beatValue), i*beatValue); // FIXME
		return measure;
	}
	
	protected int getCurrentBar(IncompleteComposition composition) {
		return (composition.getMeasures().size() + composition.getFuture().size()) % 12;
	}
	
}
