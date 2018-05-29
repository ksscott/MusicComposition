package theory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Chord {
	
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
		this.pitches.addAll(other.get());
	}
	
	public List<MidiPitch> get() {
		return new ArrayList<>(pitches);
	}
	
	public void add(MidiPitch pitch) {
		pitches.add(pitch); // set add prohibits duplicates
	}
	
	public void remove(MidiPitch pitch) {
		pitches.remove(pitch);
	}
	
}
