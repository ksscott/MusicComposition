package theory;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Chord implements Iterable<MidiPitch> {
	
	private Set<MidiPitch> pitches;
	
	public Chord() {
		this.pitches = new HashSet<>();
	}
	
	public Chord(MidiPitch... pitches) {
		this();
		for (MidiPitch p : pitches)
			this.pitches.add(p);
	}
	
	public Chord(Chord other) {
		this();
		this.pitches.addAll(other.pitches);
	}
	
	/** @return sorted list of pitches in this chord */
	public List<MidiPitch> get() {
		return pitches.stream().sorted().collect(Collectors.toList());
	}
	
	public void add(MidiPitch pitch) {
		pitches.add(pitch); // set add prohibits duplicates
	}
	
	public void remove(MidiPitch pitch) {
		pitches.remove(pitch);
	}
	
	public boolean isEmpty() {
		return pitches.isEmpty();
	}

	@Override
	public Iterator<MidiPitch> iterator() {
		return get().iterator();
	}
}
