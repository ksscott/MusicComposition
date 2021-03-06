package theory;

import static composing.RandomUtil.modPos;
import static theory.Mode.AEOLIAN;
import static theory.Mode.IONIAN;
import static theory.Mode.LOCRIAN;
import static theory.Mode.LYDIAN;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import theory.ChordSpec.Degree;
import theory.ChordSpec.Quality;

/**
 * A {@code Key} has a root {@link Note} (tonic) and a scale 
 * which is assumed to repeat infinitely above and below.
 * <p>
 * WARNING: Currently does not support scales that do not repeat identically every octave
 */
public class Key implements Cloneable {
	
	public static final Scale MAJOR = IONIAN;
	public static final Scale MINOR = AEOLIAN;
	public static final Scale HARMONIC_MINOR		 = new ScaleImpl(new int[]{2,1,2,2,1,3,1},	"Harmonic Minor");
	public static final Scale MELODIC_MINOR			 = new ScaleImpl(new int[]{2,1,2,2,2,2,1},	"Melodic Minor");
	public static final Scale STANDARD_PENTATONIC	 = new ScaleImpl(new int[]{2,2,3,2,3},		"Pentatonic");
	public static final Scale BLUES_SCALE			 = new ScaleImpl(new int[]{3,2,1,1,3,2},	"Blues");
	
	// TODO should tonic,scale be replaced with a set of notes? perhaps in a supertype?
	
	private Note tonic;
	private ScaleImpl scale;
	
	public Key(Note tonic, Scale scale) {
		this.tonic = tonic;
		this.scale = new ScaleImpl(scale);
		if (modPos(12, scale.getWidth()) != 0)
			throw new RuntimeException("WARNING: Key not guaranteed to function properly "
									+ "in all cases if scale does not repeat every octave.");
	}
	
	public Note getTonic() {
		return tonic.clone();
	}
	
	public Scale getScale() {
		return new ScaleImpl(scale); // TODO return original?
	}
	
	public boolean contains(Note note) {
		if (scale.getWidth() != 12)
			throw new IllegalStateException("Only keys repeating every octave support contains at this time.");
		int intervalAbove = tonic.halfStepsTo(note);
		for (int i : scale.intervalsFromRoot())
			if (i == intervalAbove)
				return true;
		return false;
	}
	
	public boolean contains(MidiPitch pitch) {
		MidiPitch tonicPitch = new MidiPitch(tonic, 1);
		int halfStepsAboveTonic = modPos(tonicPitch.halfStepsTo(pitch), scale.getWidth());
		for (int interval : scale.intervalsFromRoot())
			if (interval == halfStepsAboveTonic)
				return true;
//		System.out.println("scale doesn't contain note halfStepsAboveTonic: " + halfStepsAboveTonic);
		return false;
	}
	
	/**
	 * Currently doesn't support chromatic pitches; only scale members are supported.
	 * @param pitch
	 * @return the scale degree that the given pitch represents in this key
	 */
	public int scaleDegree(MidiPitch pitch) {
		MidiPitch tonicPitch = new MidiPitch(tonic, 1);
		int halfStepsAboveTonic = modPos(tonicPitch.halfStepsTo(pitch), scale.getWidth());
		int[] intervals = scale.intervalsFromRoot();
		for (int i=0; i<intervals.length; i++)
			if (intervals[i] == halfStepsAboveTonic)
				return i+1;
		throw new IllegalArgumentException("The given pitch was not contained in this key: "+ pitch);
	}
	
	public int scaleDegree(Note note) {
		int halfStepsAboveTonic = tonic.halfStepsTo(note);
		int[] intervals = scale.intervalsFromRoot();
		for (int i=0; i<intervals.length; i++)
			if (intervals[i] == halfStepsAboveTonic)
				return i+1;
		throw new IllegalArgumentException("The given note was not contained in this key: "+ note);
	}
	
	/**
	 * @param steps supports positive or negative number of steps in this key
	 * @param pitch
	 * @return the MidiPitch that is the given number of steps above the given pitch in this key.
	 */
	public MidiPitch stepsAbove(int steps, MidiPitch pitch) {
		if (!contains(pitch))
			throw new IllegalArgumentException("Only supports pitches contained in this key. " + pitch + " -!-> " + this);
		int scaleDegree = scaleDegree(pitch);
		int[] intervals = scale.intervals();
		int length = intervals.length;
		int currentPitch = pitch.get();
		for (int i=0; i<steps; i++) {
			currentPitch += intervals[modPos(i + scaleDegree - 1, length)];
		}
		for (int j=0; j>steps; j--) {
			currentPitch -= intervals[modPos(j + scaleDegree - 2, length)];
		}
		return new MidiPitch(currentPitch);
	}
	
	public Note note(int scaleDegree) {
		int[] intervals = scale.intervalsFromRoot();
		int notes = intervals.length;
		int degree = modPos(scaleDegree - 1, notes) + 1;
		int steps = intervals[degree - 1];
		return tonic.halfStepsAbove(steps);
	}
	
	// unsure if this is the best method signature
	public Chord chord(int scaleDegree, int octave) {
		return chord(scaleDegree, Degree.NONE, octave);
	}
	
	public Chord chord(int scaleDegree, Degree chordDegree, int octave) {
		Chord chord = new Chord();
		
		int[] intervals = scale.intervals();
		int length = intervals.length;
		if (scaleDegree < 1 || scaleDegree > length)
			throw new IllegalArgumentException();
			
		int numThirdsAbove = 2 + chordDegree.ordinal(); // how many times to add a third above the root
		
		int pitch = MidiPitch.inOctave(note(scaleDegree), octave);
		chord.add(new MidiPitch(pitch));
//		System.out.println("pitch " + pitch);
		
		for (int i=0; i<numThirdsAbove; i++) {
			for (int j=0; j<2; j++) {
				pitch += intervals[modPos((2*i+j+scaleDegree-1), length)];
			}
			chord.add(new MidiPitch(pitch));
//			System.out.println("pitch " + pitch);
		}
		
		return chord;
	}
	
	public ChordSpec chordSpec(int scaleDegree) {
		return new ChordSpec(note(scaleDegree), chordQuality(chord(scaleDegree, 1))); // any octave
	}
	
	public Key relativeKey() {
		if (scale.equals(MAJOR))
			return new Key(tonic.halfStepsAbove(-3), MINOR);
		if (scale.equals(MINOR))
			return new Key(tonic.halfStepsAbove(3), MAJOR);
		throw new UnsupportedOperationException("Getting the relative key is only supported for the MAJOR and MINOR modes.");
	}
	
	public Key parallelKey() {
		if (scale.equals(MAJOR))
			return new Key(tonic, MINOR);
		if (scale.equals(MINOR))
			return new Key(tonic, MAJOR);
		throw new UnsupportedOperationException("Getting the parallel key is only supported for the MAJOR and MINOR modes.");
	}
	
	public Key tonicize(int scaleDegree) {
		if (!scale.isDiatonic())
			throw new UnsupportedOperationException("Operation currently only supported for diatonic scales");
		return new Key(note(scaleDegree), Mode.equivalent(scale).revolve(scaleDegree - 1));
	}
	
	/**
	 * Gives {@link Accidental#FLAT flat} versions of notes.
	 * <br> (TODO general implementation requires much more context)
	 * 
	 * @param pitch the pitch to be named as a Note
	 * @return one acceptable Note name for the given MidiPitch
	 */
	public static Note toFlatNote(MidiPitch pitch) {
		final MidiPitch aFour = new MidiPitch(69);
		int halfStepsAboveA = modPos(aFour.halfStepsTo(pitch), 12);
		int[] intervals = MINOR.intervalsFromRoot();
		for (int i=0; i<intervals.length; i++) {
			if (halfStepsAboveA == intervals[i])
				return new Note(Letter.values()[i]);
			if (halfStepsAboveA < intervals[i])
				return new Note(Letter.values()[i], Accidental.FLAT);
		}
		return new Note(Letter.A, Accidental.FLAT); // didn't check below A or above G, must be Ab
	}
	
	/**
	 * Gives {@link Accidental#SHARP sharp} versions of notes.
	 * <br> (TODO general implementation requires much more context)
	 * 
	 * @param pitch the pitch to be named as a Note
	 * @return one acceptable Note name for the given MidiPitch
	 */
	public static Note toSharpNote(MidiPitch pitch) {
		final MidiPitch aFour = new MidiPitch(69);
		int halfStepsAboveA = modPos(aFour.halfStepsTo(pitch), 12);
		int[] intervals = Key.MINOR.intervalsFromRoot();
		for (int i=0; i<intervals.length; i++) {
			if (halfStepsAboveA == intervals[i])
				return new Note(Letter.values()[i]);
			if (halfStepsAboveA < intervals[i])
				return new Note(Letter.values()[i-1], Accidental.SHARP);
		}
		return new Note(Letter.G, Accidental.SHARP); // didn't check below A or above G, must be Ab
	}
	
	/** @see #inferDiatonic(Collection) */
	public static Key inferDiatonic(Set<Note> notes) {
		return inferDiatonic(notes.stream().map(note -> new MidiPitch(note, 1)).collect(Collectors.toSet()));
	}

	/**
	 * A key can be determined from a collection of pitches if and only if 
	 * they contain exactly one tritone and the rest of the pitches all lie within 
	 * either the {@link Mode#LYDIAN} scale or the {@link Mode#LOCRIAN} scale, but not both.
	 * 
	 * @param pitches
	 * @return {@link #MAJOR} key represented by the given pitches
	 */
	public static Key inferDiatonic(Collection<MidiPitch> pitches) {
		if (pitches.size() < 3)
			throw new IllegalArgumentException("Must have at least 3 pitches to infer a diatonic key.");
		List<Integer> integers = pitches.stream()
										.map(MidiPitch::get)
										.map(integer -> integer % 12)
										.sorted()
										.collect(Collectors.toList());
		// from here on, the list must represent a set of at least three integers from 0-11
		List<Integer> faScale = Arrays.stream(LYDIAN.intervalsFromRoot()).boxed().collect(Collectors.toList());
		List<Integer> tiScale = Arrays.stream(LOCRIAN.intervalsFromRoot()).boxed().collect(Collectors.toList());
		
//		System.out.println("Searching for diatonic in pitches: " + integers);
		
		boolean diatonicPossible = false;
		for (int i=0; i<integers.size(); i++) {
			boolean isTritone = false;
			boolean notFa = false;
			boolean notTi = false;
			for (Integer other : integers) {
				int diff = modPos(other - integers.get(i), 12);
				if (diff == 6)
					isTritone = true;
				if (!faScale.contains(diff))
					notFa = true;
				if (!tiScale.contains(diff))
					notTi = true;
			}
			if (notFa && notTi)
				continue;
			// right here, notFa is equal to !notTi
			if (isTritone) {
				// all done, return the key converted to major
				Note faOrTi = toFlatNote(new MidiPitch(integers.get(i)));
				Note tonic = faOrTi.halfStepsAbove(notTi ? 7 : 1);
				return new Key(tonic, MAJOR);
			}
			// 
			diatonicPossible = true;
		}
		if (diatonicPossible)
			throw new IllegalArgumentException("Notes given can belong to more than one possible key.");
		else
			throw new IllegalArgumentException("Notes given cannot fit in a diatonic scale.");
	}
	
	/** @see #inferKey(Collection) */
	public static Key inferKey(Set<Note> notes) {
		return inferKey(notes.stream().map(note -> new MidiPitch(note, 1)).collect(Collectors.toSet()));
	}
	
	/**
	 * Attempt to infer a diatonic key, or else create a key 
	 * from all given pitches and an arbitrarily chosen tonic.
	 * 
	 * @see #inferDiatonic(Collection)
	 */
	public static Key inferKey(Collection<MidiPitch> pitches) {
		Key key;
		try {
			key = Key.inferDiatonic(pitches);
			System.out.println("Detecting key " + key);
		} catch (IllegalArgumentException e) {
			// too many or too few pitches to decide on a single key
			// let's just work with what we know
			List<Integer> pitchList = pitches.stream()
					.map(MidiPitch::get)
					.map(integer -> modPos(integer, 12)) // causes arbitrary ordering
					.distinct()
					.sorted()
					.collect(Collectors.toList());
			int[] intervals = new int[pitchList.size()];
//			System.out.println("tonicWhyNot: " + newMidiPitch.get());
			for (int i=0; i<intervals.length-1; i++)
				intervals[i] = pitchList.get(i+1) - pitchList.get(i);
			int last = intervals.length-1;
			intervals[last] = 12 - (pitchList.get(last) - pitchList.get(0));
//			System.out.println("CREATED INTERVALS:");
//			for (int interval : intervals)
//				System.out.println("" + interval);
			Note tonicWhyNot = Key.toFlatNote(new MidiPitch(pitchList.get(0)));
			Scale scaleSureOk = new ScaleImpl(intervals);
			key = new Key(tonicWhyNot, scaleSureOk);
		}
		return key;
	}

	// semi-redundant, but useful
	public static Set<Note> possibleIonianTonics(Set<Note> playedNotes) {
		// assume all is possible to start
		Set<Note> possible = new HashSet<>();
		Note a = new Note(Letter.A);
		for (int i=0; i<12; i++) {
			possible.add(a.halfStepsAbove(i));
		}
	
		// mask out keys obviated by each note
		List<int[]> chromatic = Arrays.asList(new int[]{1,3,6,8,10});
		for (Note played : playedNotes) {
			possible.removeIf(possibleTonic -> chromatic.contains(possibleTonic.halfStepsTo(played)));
		}
	
		return possible;
	}
	
	/**
	 * This method returns the maximum set of notes that can be played 
	 * while still implying all of the given keys.
	 */
	public static Set<Note> playableNotes(Set<Key> impliedKeys) {
		Set<Note> playable = new HashSet<>();
		Note a = new Note(Letter.A);
		for (int i=0; i<12; i++) {
			playable.add(a.halfStepsAbove(i));
		}
		for (Key key : impliedKeys)
			playable.removeIf(note -> !key.contains(note));
		return playable;
	}
	
	public static Key KeySuperposition(Set<Key> impliedKeys, Note tonic) {
		Set<Note> playable = playableNotes(impliedKeys);
		if (!playable.contains(tonic))
			throw new IllegalArgumentException("Not possible, you dummy!");
		List<Integer> intervals = new ArrayList<>();
		for (Note note : playable)
			intervals.add(tonic.halfStepsTo(note));
		Collections.sort(intervals);
		Integer lastIntervalFromRoot = 0;
		for (Integer interval : intervals) {
			interval -= lastIntervalFromRoot;
			lastIntervalFromRoot += interval;
		}
		int[] intArray = intervals.stream().mapToInt(i -> i).toArray();
		return new Key(tonic, new ScaleImpl(intArray));
	}

	public static Quality chordQuality(Chord chord) {
		if (chord.size() != 3)
			throw new IllegalArgumentException("Only chords with three pitches currently supported.");
		
		int min = 12; // store the least greatest interval
		List<Integer> intervals = new ArrayList<>();
		for (MidiPitch pitch : chord) {
			int max = 0;
			List<Integer> candidateIntervals = new ArrayList<>();
			for (MidiPitch other : chord) {
				int halfStepsTo = modPos(pitch.halfStepsTo(other), 12);
				candidateIntervals.add(halfStepsTo);
				max = halfStepsTo > max ? halfStepsTo : max;
			}
			if (max < min) {
				min = max;
				intervals = candidateIntervals;
			}
		}
		Collections.sort(intervals);
		if (!(intervals.get(0) == 0))
			throw new IllegalStateException("what?");
		
		Quality quality = null;
		int third = intervals.get(1);
		int fifth = intervals.get(2);
		if (third == 3) {
			if (fifth == 6) {
				quality = Quality.DIMINISHED;
			} else if (fifth == 7) {
				quality = Quality.MINOR;
			} else {
				throw new IllegalArgumentException("Given chord did not map to a Quality.");
			}
		} else if (third == 4) {
			if (fifth == 7) {
				quality = Quality.MAJOR;
			} else if (fifth == 8) {
				quality = Quality.AUGMENTED;
			} else {
				throw new IllegalArgumentException("Given chord did not map to a Quality.");
			}
		} else {
			throw new IllegalArgumentException("Given chord did not map to a Quality.");
		}
		return quality;
	}

	@Override
	public Key clone() {
		return new Key(tonic.clone(), new ScaleImpl(scale));
	}
	
	@Override
	public String toString() {
		return tonic + "-" + scale;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((scale == null) ? 0 : scale.hashCode());
		result = prime * result + ((tonic == null) ? 0 : tonic.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Key other = (Key) obj;
		if (scale == null) {
			if (other.scale != null)
				return false;
		} else if (!scale.equals(other.scale))
			return false;
		if (tonic == null) {
			if (other.tonic != null)
				return false;
		} else if (!tonic.equals(other.tonic))
			return false;
		return true;
	}
	
}
