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
	
	public void add(MidiPitch pitch) {
		pitches.add(pitch);
	}
	
	public List<MidiPitch> get() {
		return new ArrayList(pitches);
	}

}
