package theory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static composing.RandomUtil.*;

/**
 * Simplistic graph of chords and their possible successors
 * 
 * @author kennethscott
 */
public class ChordProgressions {
	
	/**
	 * Only respects inversions 0,1,2 and higher order equivalents thereof.
	 * <p>
	 * TODO respect degree, add2, etc.
	 * 
	 * @param lastChord
	 * @param nextChordSpec
	 * @return
	 */
	public static Chord voiceLead(Chord lastChord, ChordSpec nextChordSpec, int bassMin, int bassMax) {
		Chord retval = new Chord();
		
		List<MidiPitch> lastChordPitches = lastChord.get();
		MidiPitch previousBassPitch = lastChordPitches.get(0);
		
		Chord nextChord = nextChordSpec.build();
//		System.out.println("Voice leading to pitches: ");
//		for (MidiPitch pitch : nextChord.get())
//			System.out.println(pitch);
		if (nextChord.isEmpty())
			return retval;
		Note bassNote = Key.toFlatNote(nextChord.get().get(0));
//		Set<Note> acceptableNotes = nextChord.get()
//											 .stream()
//											 .map(Key::toFlatNote)
//											 .collect(Collectors.toSet());
		
//		System.out.println("Number of acceptable notes: " + acceptableNotes.size());
		
		// decide which way to take the bass note
		MidiPitch bassPitch = downFiveOrUpFour(previousBassPitch, bassNote); // TODO maybe revisit
		if (bassPitch.get() < bassMin)
			bassPitch = bassPitch.above(12);
		if (bassPitch.get() > bassMax)
			bassPitch = bassPitch.below(12);
		retval.add(bassPitch);
		int lastPitchAdded = bassPitch.get();
//		boolean bassNoteUp = bassPitch.compareTo(previousBassPitch) < 0; // TODO maybe use this
		
		for (MidiPitch pitch : lastChordPitches.subList(1, lastChordPitches.size())) {
//			System.out.println("Leading from pitch: " + pitch);
			// for remaining pitches, attempt to step downward
			for (int i=pitch.get()-2; i<pitch.get()+12; i++) {
//				System.out.println("Testing pitch: " + i);
				if (i <= lastPitchAdded)
					continue;
				
				boolean found = false;
				for (MidiPitch acceptable : nextChord.get()) {
					if (acceptable.halfStepsTo(new MidiPitch(i)) % 12 == 0) { // FIXME inefficient
						found = true;
						break;
					}
				}
				if (found) {
//					System.out.println("Voice leading to pitch: " + i);
					retval.add(new MidiPitch(i));
					lastPitchAdded = i;
					break;
				}
			}
		}

		return retval;
	}
	
	public static class ChordProgression {

		private Key key;
		private Map<Integer, ChordProgression.ProgressionNode> nodes;

		public ChordProgression(Key key) {
			this.key = key;
			this.nodes = new HashMap<>();
			int numChords = key.getScale().intervals().length;
			for (int i=1; i<=numChords; i++) {
				nodes.put(i, new ProgressionNode(i));
			}
		}

		public void put(int from, int to) {
			put(from, to, 1);
		}

		public void put(int from, int to, int weight) {
			ChordProgression.ProgressionNode fromNode = nodes.get(from);
			ChordProgression.ProgressionNode toNode = nodes.get(to);
			for (int i=0; i<weight; i++)
				fromNode.addSuccessor(toNode);
		}

		public void remove(int from, int to) {
			nodes.get(from).removeSuccessor(nodes.get(to));
		}

		public int getNext(int from) {
			return nodes.get(from).getRandomSuccessor().getScaleDegree();
		}

		private static class ProgressionNode {

			private int scaleDegree;
			private List<ChordProgression.ProgressionNode> successors;

			public ProgressionNode(int scaleDegree) {
				this.scaleDegree = scaleDegree;
				this.successors = new ArrayList<>();
			}

			public int getScaleDegree() {
				return scaleDegree;
			}

			public void addSuccessor(ChordProgression.ProgressionNode nextChord) {
				successors.add(nextChord);
			}

			public void removeSuccessor(ChordProgression.ProgressionNode formerSuccessor) {
				List<ChordProgression.ProgressionNode> list = new ArrayList<>();
				list.add(formerSuccessor);
				successors.removeAll(list);
			}

			//			public Set<ProgressionNode> getSuccessors() {
			//				return new HashSet<>(successors);
			//			}

			public ChordProgression.ProgressionNode getRandomSuccessor() {
				return successors.get((int) (Math.random()*successors.size()));
			}
		}

	}
	
	/**
	 * @param startingPitch
	 * @param targetNote
	 * @return
	 */
	public static MidiPitch downFiveOrUpFour(MidiPitch startingPitch, Note targetNote) {
		int halfStepsTo = Key.toFlatNote(startingPitch).halfStepsTo(targetNote);
		if (halfStepsTo >= 6)
			halfStepsTo -= 12;
		if (halfStepsTo == 5 && roll(50))
			halfStepsTo -= 12;
		return startingPitch.above(halfStepsTo);
	}

}