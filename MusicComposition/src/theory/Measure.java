package theory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Measure {
	
	// key signature:
	private int beats;
	private double beatValue;
	
	private Map<Double, List<MidiNote>> notes;
	
	public Measure(int beats, double beatValue) {
		this.beats = beats;
		this.beatValue = beatValue;
		this.notes = new HashMap<>();
	}
	
	/**
	 * Intended to add a note at the latest silence in this measure
	 * 
	 * @param note
	 * @return this measure
	 */
	public Measure addNote(MidiNote note) {
		double offset = 0; // FIXME
		return addNote(note, offset);
	}
	
	public Measure addNote(MidiNote note, double offset) {
		List<MidiNote> list = notes.get(new Double(offset));
		if (list == null)
			list = new ArrayList<>();
		list.add(note);
		return this;
	}
	
	public List<MidiNote> getNotes(double time) {
		List<MidiNote> list = notes.get(new Double(time));
		return new ArrayList<>(list);
	}

}
