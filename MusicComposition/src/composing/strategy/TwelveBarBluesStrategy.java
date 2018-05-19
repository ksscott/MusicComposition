package composing.strategy;

import java.util.Queue;

import composing.IncompleteComposition;
import theory.Measure;
import theory.MidiNote;
import theory.MidiPitch;
import theory.Note;
import theory.Scale;
import theory.ScaleImpl;

public class TwelveBarBluesStrategy implements ComposingStrategy {
	
	private int tonic;
	private int[] chordsHalfSteps = new int[] { 0, 0, 0, 0, 5, 5, 0, 0, 7, 5, 0, 0 };
	private Scale baseline = new ScaleImpl(new int[] { 0, 4, 7, 9, 10 });

	public TwelveBarBluesStrategy(Note tonic) {
		this.tonic = MidiPitch.inOctave(tonic, 3);
	}
	
	@Override
	public Measure generateFirstMeasure() {
		return composeBar(new IncompleteComposition());
	}

	@Override
	public boolean iterate(IncompleteComposition composition) {
		final Queue<Measure> future = composition.getFuture();
		future.add(composeBar(composition));
		return future.size() >= 12;
	}

	/**
	 * Allows subclasses to extend this method and write additional music on top of the base line. e.g.
	 * <p>
	 * Override <br>
	 * protected Measure composeBar(IncompleteComposition composition) { <br>
	 * Measure measure = super.composeBar(composition); <br>
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
		int chordRoot = tonic + chordsHalfSteps[currentBar];
		
		if (currentBar % 2 == 0 || currentBar == 9) // odd measures (but also 9, which is "even")
			measure = composeArpeggioA(chordRoot);
		else
			measure = composeArpeggioB(chordRoot);
		
//		for (int i=0; i<4; i++) {
//			List<MidiNote> notes = measure.getNotes(i*1/4.0);
//			for (MidiNote note : notes) {
//				System.out.println("Note: " + note.getPitch());
//			}
//		}
		
		return measure;
	}

	private Measure composeArpeggioA(int root) {
		int beats = 4;
		double beatValue = 1/4.0;
		Measure measure = new Measure(beats, beatValue);
		for (int i=0; i<beats; i++)
			measure.addNote(new MidiNote(baseline.intervals()[i] + root, beatValue), i*beatValue);
		return measure;
	}

	private Measure composeArpeggioB(int root) {
		int beats = 4;
		double beatValue = 1/4.0;
		Measure measure = new Measure(beats, beatValue);
		for (int i=0; i<beats; i++)
			measure.addNote(new MidiNote(baseline.intervals()[4-i] + root, beatValue), i*beatValue);
		return measure;
	}
	
	protected int getCurrentBar(IncompleteComposition composition) {
		return (composition.getMeasures().size() + composition.getFuture().size()) % 12;
	}
	
}
