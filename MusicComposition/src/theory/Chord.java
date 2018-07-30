package theory;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Effectively a sorted set of {@link MidiPitch}
 */
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
	
	public Chord(List<MidiPitch> pitches) {
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
	
	public boolean contains(MidiPitch pitch) {
		return pitches.contains(pitch);
	}
	
	public boolean isEmpty() {
		return pitches.isEmpty();
	}

	@Override
	public Iterator<MidiPitch> iterator() {
		return get().iterator();
	}
	
	public Iterator<MidiPitch> arpeggiator() {
		return new Iterator<MidiPitch>(){

			int index = 0;
			boolean up = true;
			List<MidiPitch> notes = get();
			
			@Override
			public boolean hasNext() {
				return !notes.isEmpty();
			}

			@Override
			public MidiPitch next() {
				if (notes.size() == 0)
					return null;
				if (notes.size() == 1)
					return notes.get(0);
				
				int nextIndex = up ? index++ : index--;
				if (index == 0)
					up = true;
				else if (index == notes.size()-1)
					up = false;
				
				return notes.get(nextIndex);
			}
		};
	}
	
	public Iterator<MidiPitch> albertiBass() {
		if (pitches.size() != 3)
			throw new IllegalStateException("Cannot play Alberti Bass pattern on chord of size: " + pitches.size());
		
		return new Iterator<MidiPitch>(){
			
			int index = 0;
			int[] whichNotes = new int[] { 0,2,1,2 }; // Alberti Bass pattern
			List<MidiPitch> notes = get();

			@Override
			public boolean hasNext() {
				return true; // already checked chord size above, so this is always true
			}

			@Override
			public MidiPitch next() {
				MidiPitch pitch = notes.get(whichNotes[index++]);
				if (index > 3)
					index = 0;
				return pitch;
			}
			
		};
	}
	
	@Override
	public String toString() {
		String string = "Chord(";
		for (MidiPitch pitch : get())
			string += pitch;
		string += ")";
		return string;
	}
}
