package composing.strategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import composing.IncompleteComposition;
import theory.Chord;
import theory.Key;
import theory.Measure;
import theory.MidiNote;
import theory.MidiPitch;

public class PrettyProgressionStrategy implements ComposingStrategy {
	
	protected int octave = 4;
	protected Key key;
	protected int tonic;
	private ChordProgressions progressions;
	
	private int currentChord; // hack
	
	public PrettyProgressionStrategy(Key key) {
		this.key = key;
		this.tonic = MidiPitch.inOctave(key.getTonic(), octave);
		
		this.progressions = new ChordProgressions(key);
		progressions.put(1,4);
		progressions.put(1,6);
//		progressions.put(1,3);
		progressions.put(4,5);
		progressions.put(4,6);
//		progressions.put(4,3);
		progressions.put(4,2);
		progressions.put(6,4);
		progressions.put(6,2);
		progressions.put(6,1);
		progressions.put(3,1);
		progressions.put(3,6);
		progressions.put(5,1);
		progressions.put(5,6);
		progressions.put(5,4);
		progressions.put(2,5);
		progressions.put(2,4);
		progressions.put(2,7);
		progressions.put(7,5);
		progressions.put(7,1);
		
		currentChord = 1;
	}
	
	@Override
	public Measure generateFirstMeasure() {
		return composeBar(new IncompleteComposition());
	}

	@Override
	public boolean iterate(IncompleteComposition composition) {
		final Queue<Measure> future = composition.getFuture();
		future.add(composeBar(composition));
		return true;
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
//		System.out.println("Previous chord: " + currentChord);
//		System.out.println("Current chord: " + currentChord);
		Measure measure = backgroundChord(currentChord);
		measure.setMetaInfo("(" + currentChord + ")");
		
		currentChord = progressions.getNext(currentChord);
		
		return measure;
	}
	
	// TODO probably accept a parameter besides int
	private Measure backgroundChord(int scaleDegree) {
//		System.out.println("Composing chord: " + scaleDegree);
		
		int beats = 4;
		double beatValue = 1/4.0;
		Measure measure = new Measure(beats, beatValue);
		
		List<MidiPitch> pitches = key.chord(scaleDegree, octave).get();
		Collections.sort(pitches);
		
		for (int i=0; i<beats; i++) {
			for (MidiPitch pitch : pitches) {
				measure.addNote(new MidiNote(pitch, beatValue), i*beatValue);
			}
		}
		
		return measure;
	}
	
	/**
	 * Simplistic graph of chords and their possible successors
	 * 
	 * @author kennethscott
	 */
	private static class ChordProgressions {
		
		private Key key;
		private Map<Integer, ProgressionNode> nodes;
		
		public ChordProgressions(Key key) {
			this.key = key;
			this.nodes = new HashMap<>();
			int numChords = key.getScale().intervals().length;
			for (int i=1; i<=numChords; i++) {
				nodes.put(i, new ProgressionNode(i));
			}
		}
		
		public void put(int from, int to) {
			nodes.get(from).addSuccessor(nodes.get(to));
		}
		
		public int getNext(int from) {
			return nodes.get(from).getRandomSuccessor().getScaleDegree();
		}
		
		private static class ProgressionNode {
			
			private int scaleDegree;
			private List<ProgressionNode> successors;
			
			public ProgressionNode(int scaleDegree) {
				this.scaleDegree = scaleDegree;
				this.successors = new ArrayList<>();
			}
			
			public int getScaleDegree() {
				return scaleDegree;
			}
			
			public void addSuccessor(ProgressionNode nextChord) {
				successors.add(nextChord);
			}
			
//			public Set<ProgressionNode> getSuccessors() {
//				return new HashSet<>(successors);
//			}
			
			public ProgressionNode getRandomSuccessor() {
				return successors.get((int) (Math.random()*successors.size()));
			}
		}
		
	}

}
