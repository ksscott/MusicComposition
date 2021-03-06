package theory.progression;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import theory.ChordSpec;
import theory.ChordSpec.Degree;
import theory.ChordSpec.Quality;
import theory.Key;
import theory.MidiPitch;
import theory.Note;
import theory.progression.ChordProgressions.ChordProgression.ProgressionNode;

/**
 * Simplistic graph of chords and their possible successors
 * 
 * @author kennethscott
 */
public class ChordProgressions {
	
	// util class
	private ChordProgressions() {}
	
	public static KeyChordProgression standardMajorProgression(Note tonic) {
		return majorProgression(tonic, true);
	}
	
	public static KeyChordProgression polyphonicMajorProgression(Note tonic) {
		return majorProgression(tonic, false);
	}
	
	// TODO "secondsAndSevenths" is a crude, temporary hack
	private static KeyChordProgression majorProgression(Note tonic, boolean secondsAndSevenths) {
		KeyChordProgression progression = new KeyChordProgression(new Key(tonic, Key.MAJOR));
		ChordSpec five = progression.getKey().chordSpec(5);
		five.setDegree(Degree.SEVENTH);
		five.setDegreeQuality(Degree.SEVENTH, Quality.MINOR);
		progression.put(1,4,2);
		progression.put(1,6);
		progression.put(1,3);
//		progression.put(2,5,2);
		progression.put(progression.getKey().chordSpec(2),five,2);
		progression.put(2,4);
		progression.put(2,7);
		progression.put(3,1);
		progression.put(3,6);
//		progression.put(4,5,4);
		if (secondsAndSevenths)
			progression.put(progression.getKey().chordSpec(4),five,4);
		progression.put(4,6);
		if (secondsAndSevenths)
			progression.put(4,3);
		progression.put(4,2);
//		progressions.put(4,1); // XXX debugging only
//		progression.put(5,1,3);
		progression.put(five,progression.getKey().chordSpec(1),3);
//		progression.put(5,6);
		if (secondsAndSevenths)
			progression.put(five,progression.getKey().chordSpec(6));
//		progression.put(5,4);
		if (secondsAndSevenths)
			progression.put(five,progression.getKey().chordSpec(4));
		progression.put(6,4,2);
		progression.put(6,2,2);
		progression.put(6,1);
//		progression.put(7,5);
		progression.put(progression.getKey().chordSpec(7),five);
		if (secondsAndSevenths)
			progression.put(7,1);
		return progression;
	}
	
	public static class ChordProgression implements Cloneable, Iterable<ProgressionNode> {

		protected Set<ProgressionNode> nodes;

		public ChordProgression() {
			this.nodes = new HashSet<>();
		}
		
		protected ProgressionNode get(ChordSpec chord) {
			for (ProgressionNode node : this) {
				if (node.getChord().getTonic().equals(chord.getTonic()))
					return node;
			}
			return null;
		}

		public void put(ChordSpec from, ChordSpec to) {
			put(from, to, 1);
		}

		public void put(ChordSpec from, ChordSpec to, int weight) {
			ProgressionNode fromNode = get(from);
			if (fromNode == null) {
				fromNode = node(from);
				nodes.add(fromNode);
			}
			ProgressionNode toNode = get(to);
			if (toNode == null) {
				toNode = node(to);
				nodes.add(toNode);
			}
			for (int i=0; i<weight; i++)
				fromNode.addSuccessor(toNode);
		}
		
		protected ProgressionNode node(ChordSpec spec) {
			return new ProgressionNode(spec);
		}

		public void remove(ChordSpec from, ChordSpec to) {
			ProgressionNode fromNode = get(from);
			ProgressionNode toNode = get(to);
			if (fromNode == null || toNode == null)
				throw new IllegalStateException("Mapping not present in this ChordProgression");
			fromNode.removeSuccessor(toNode);
		}

		public ChordSpec getNext(ChordSpec from) {
			ProgressionNode fromNode = get(from);
			if (fromNode == null)
				throw new IllegalArgumentException("Chord not present in this ChordProgression");
			return fromNode.getRandomSuccessor().getChord();
		}
		
		@Override
		public ChordProgression clone() {
			ChordProgression clone = new ChordProgression();
			
			for (ProgressionNode node : this) {
				for (ProgressionNode succ : node.getSuccessors())
					clone.put(node.getChord(), succ.getChord());
			}
			
			return clone;
		}

		@Override
		public Iterator<ProgressionNode> iterator() {
			return nodes.iterator(); // change to have it perpetually iterate through this progression?
		}

		static class ProgressionNode {

			protected ChordSpec chord;
			protected List<ProgressionNode> successors;

			public ProgressionNode(ChordSpec chord) {
				this.chord = chord;
				this.successors = new ArrayList<>();
			}
			
			public ChordSpec getChord() {
				return chord;
			}

			public void addSuccessor(ProgressionNode nextChord) {
				successors.add(nextChord);
			}

			public void removeSuccessor(ProgressionNode formerSuccessor) {
				List<ProgressionNode> list = new ArrayList<>();
				list.add(formerSuccessor);
				successors.removeAll(list);
			}

			public ProgressionNode getRandomSuccessor() {
				return successors.get((int) (Math.random()*successors.size()));
			}

			protected List<ProgressionNode> getSuccessors() {
				return new ArrayList<>(successors);
			}
		}
	}
	
	public static abstract class DestinationProgression extends ChordProgression {
			
			public DestinationProgression() {
				super();
			}
			
			protected abstract Comparator<List<ProgressionNode>> pathComparator();
			
			public List<ChordSpec> progress(ChordSpec fromChord, ChordSpec toChord, int maxChords) {
				ProgressionNode fromNode = get(fromChord);
				ProgressionNode toNode = get(toChord);
				
				// check inputs:
				if (fromNode == null)
					throw new IllegalArgumentException("Requested chord not present in this progression: " + fromChord);
				if (toNode == null)
					throw new IllegalArgumentException("Requested chord not present in this progression: " + toChord);
				
				ArrayList<ProgressionNode> list = new ArrayList<>();
				list.add(fromNode);
				List<ProgressionNode> result = recurse(list, toNode, maxChords);
				if (result == null)
					throw new RuntimeException("No valid progression found to fulfill the given requirements.");
				
				List<ChordSpec> chords = result.stream().map(ProgressionNode::getChord).collect(Collectors.toList());
				return chords;
			}
			
			/**
			 * @param visited
			 * @param destination
			 * @param maxChords max number of chords to include, counting first and last
			 * @return
			 */
			private List<ProgressionNode> recurse(List<ProgressionNode> visited, ProgressionNode destination, int maxChords) {
				if (visited.size() >= maxChords)
					return null;
				ProgressionNode lastChord = visited.get(visited.size()-1);
				if (lastChord.equals(destination) && visited.size() > 1)
					return visited;
				
				List<List<ProgressionNode>> paths = new ArrayList<>();
				
				for (ProgressionNode successor : lastChord.getSuccessors()) {
					// commenting to allow repeats:
	//				if (visited.contains(successor))
	//					continue;
					ArrayList<ProgressionNode> visited2 = new ArrayList<>(visited);
					visited2.add((ProgressionNode) successor);
					paths.add(recurse(visited2, destination, maxChords));
				}
				
				// prune dead ends:
				paths.removeIf(path -> path == null);
				
				Collections.sort(paths, pathComparator());
				
				return paths.isEmpty() ? null : paths.get(0);
			}
			
		}

	public static class KeyChordProgression extends DestinationProgression {
		
		protected Key key;
		
		public KeyChordProgression(Key key) {
			this.key = key;
		}
		
		public Key getKey() {
			return key.clone();
		}
		
		public void put(int from, int to) {
			put(key.chordSpec(from), key.chordSpec(to));
		}
		
		public void put(int from, int to, int weight) {
			put(key.chordSpec(from), key.chordSpec(to), weight);
		}
		
		public void remove(int from, int to) {
			remove(key.chordSpec(from), key.chordSpec(to));
		}

		public ChordSpec getNext(int from) {
			return getNext(key.chordSpec(from));
		}
		
		@Override
		protected Comparator<List<ProgressionNode>> pathComparator() {
			return new Comparator<List<ProgressionNode>>(){
				@Override public int compare(List<ProgressionNode> o1, List<ProgressionNode> o2) { return 0; }};
		}

		@Override
		public KeyChordProgression clone() {
			KeyChordProgression clone = new KeyChordProgression(key);
			
			for (ProgressionNode node : this) {
				for (ProgressionNode succ : node.getSuccessors())
					clone.put(node.getChord(), succ.getChord());
			}
			
			return clone;
		}
	}
	
	public static class KeyChange extends DestinationProgression {
		
		protected KeyChordProgression from;
		protected KeyChordProgression to;
		
		public KeyChange(KeyChordProgression from, KeyChordProgression to) {
			super();
			this.from = from;
			this.to = to;
			
			// add "to" key first, which will record the chord's tonic note name, 
			// as stored in the ProgressionNode, in the context of the "to" key
			for (ProgressionNode node : to)
				for (ProgressionNode succ : node.getSuccessors())
					put(node.getChord(), succ.getChord());
			for (ProgressionNode node : from)
				for (ProgressionNode succ : node.getSuccessors())
					put(node.getChord(), succ.getChord());
		}
		
		@Override
		protected ProgressionNode node(ChordSpec spec) {
			boolean inFromKey = true;
			boolean inToKey = true;
			for (MidiPitch pitch : spec.build()) {
				if (!from.key.contains(pitch))
					inFromKey = false;
				if (!to.key.contains(pitch))
					inToKey = false;
			}
			return new KeyChangeProgressionNode(spec, inFromKey, inToKey);
		}
		
		public Key getFromKey() {
			return from.getKey();
		}
		
		public Key getToKey() {
			return to.getKey();
		}
		
		public boolean isInFromKey(ChordSpec chord) {
			KeyChangeProgressionNode node = (KeyChangeProgressionNode) get(chord);
			return (node == null) ? false : (node.isInFromKey());
		}
		
		public boolean isInToKey(ChordSpec chord) {
			KeyChangeProgressionNode node = (KeyChangeProgressionNode) get(chord);
			return (node == null) ? false : (node.isInToKey());
		}
		
		public boolean isInBothKeys(ChordSpec chord) {
			KeyChangeProgressionNode node = (KeyChangeProgressionNode) get(chord);
			return (node == null) ? false : (node.isInFromKey() && node.isInToKey());
		}
		
		/**
		 * @see #progress(int, int, int)
		 * @param maxChords max number of chords to include, counting first and last
		 * @return list of chords to play to change keys from tonic to tonic
		 */
		public List<ChordSpec> progress(int maxChords) {
			return progress(1,1,maxChords);
		}

		/**
		 * @param fromChordDegree
		 * @param toChordDegree
		 * @param maxChords max number of chords to include, counting first and last
		 * @return list of chords to play to change keys, including the first and last given
		 */
		public List<ChordSpec> progress(int fromChordDegree, int toChordDegree, int maxChords) {
			Key fromKey = getFromKey();
			Key toKey = getToKey();
			List<ChordSpec> progression = progress(fromKey.chordSpec(fromChordDegree), toKey.chordSpec(toChordDegree), maxChords);
			System.out.print("Key Change [" + fromKey + " -> " + toKey + "] ");
			for (ChordSpec chord : progression)
				System.out.print(chord + " ");
			System.out.println();
			return progression;
		}

		@Override
		protected Comparator<List<ProgressionNode>> pathComparator() {
			return new Comparator<List<ProgressionNode>>(){
				@Override
				public int compare(List<ProgressionNode> path1, List<ProgressionNode> path2) {
					// prefer progressions that fill the entire requested length
					if (path1.size() != path2.size())
						return path2.size() - path1.size(); // puts longer path first
					
					// prefer non-repetitive progressions (fewer chord repeats)
					long path1Duplicates = path1.size() - path1.stream().distinct().count();
					long path2Duplicates = path2.size() - path2.stream().distinct().count();
					if (path1Duplicates != path2Duplicates)
						return (int) (path1Duplicates - path2Duplicates); // puts path with fewer duplicates first
					
					// prefer ambiguous progressions (in both keys for longer)
					int streak = 0;
					int path1Max = 0;
					int path2Max = 0;
					for (ProgressionNode node : path1) {
						if (((KeyChangeProgressionNode) node).isInFromKey()
								&& ((KeyChangeProgressionNode) node).isInToKey())
							path1Max = Math.max(++streak, path1Max);
						else
							streak = 0;
					}
					for (ProgressionNode node : path2) {
						if (((KeyChangeProgressionNode) node).isInFromKey()
								&& ((KeyChangeProgressionNode) node).isInToKey())
							path2Max = Math.max(++streak, path2Max);
						else
							streak = 0;
					}
					if (path1Max != path2Max)
						return path2Max - path1Max; // flipped to put greater one first
					
//					long path1CommonChords = path1.stream()
//							.map(node -> (KeyChangeProgressionNode) node) // softly enforced in KeyChange
//							.filter(KeyChangeProgressionNode::isInFromKey)
//							.filter(KeyChangeProgressionNode::isInToKey)
//							.count();
//					long path2CommonChords = path2.stream()
//							.map(node -> (KeyChangeProgressionNode) node) // softly enforced in KeyChange
//							.filter(KeyChangeProgressionNode::isInFromKey)
//							.filter(KeyChangeProgressionNode::isInToKey)
//							.count();
//					if (path1CommonChords != path2CommonChords)
//						return (int) (path2CommonChords - path1CommonChords); // flipped to put greater one first
					
					// favor the path with a full cadence
					Note path1PenultimateChordTonic = path1.get(path1.size()-2).getChord().getTonic();
					Note path2PenultimateChordTonic = path2.get(path1.size()-2).getChord().getTonic();
					int scaleDegree1 = to.getKey().scaleDegree(path1PenultimateChordTonic);
					int scaleDegree2 = to.getKey().scaleDegree(path2PenultimateChordTonic);
					if (scaleDegree1 == 5) {
						if (scaleDegree2 == 5) {}
						else { return -1; }
					} else {
						if (scaleDegree2 == 5) { return 1; }
						else {}
					}
					
					return 0; // TODO other criteria
				}
			};
		}

		public class KeyChangeProgressionNode extends ProgressionNode {

			private boolean fromKey;
			private boolean toKey;
			
			public KeyChangeProgressionNode(ChordSpec chord, boolean fromKey, boolean toKey) {
				super(chord);
				this.fromKey = fromKey;
				this.toKey = toKey;
			}
			
			public boolean isInFromKey() {
				return fromKey;
			}
			
			public boolean isInToKey() {
				return toKey;
			}
		}
	}
	
}