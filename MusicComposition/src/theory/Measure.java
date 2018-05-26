package theory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Measure {
	
	// key signature:
	private int beats;
	private double beatValue;
	
	private String metaInfo = "";
	
	private Map<Double,List<MidiNote>> notes;
	
	public Measure(int beats, double beatValue) {
		this.beats = beats;
		this.beatValue = beatValue;
		this.notes = new HashMap<>();
	}
	
	public int beats() {
		return beats;
	}

	public double beatValue() {
		return beatValue;
	}

	/**
	 * Intended to add a note at the latest silence in this measure
	 * 
	 * @param note
	 * @return this measure
	 */
	public Measure addNote(MidiNote note) {
		return addNote(note, latestNoteEnd());
	}
	
	public Measure addNote(MidiNote note, double offset) {
		List<MidiNote> list = notes.get(new Double(offset));
		if (list == null) {
			list = new ArrayList<>();
			notes.put(new Double(offset), list);
		}
		list.add(note);
		return this;
	}
	
	public List<MidiNote> getNotes(double time) {
		List<MidiNote> list = notes.get(new Double(time));
		return list == null ? new ArrayList<>() : new ArrayList<>(list);
	}
	
	/**
	 * @param time start, exclusive
	 * @param time end, inclusive
	 * @return notes in a range
	 */
	public List<MidiNote> getNotes(double start, double end) {
		List<MidiNote> allNotes = new ArrayList<>();
		Set<Double> keySet = notes.keySet();
		for (Double dub : keySet) {
			if ((dub > start && dub <= end) || dub == end)
				allNotes.addAll(notes.get(dub));
		}
		return allNotes;
	}
	
	public Set<Double> getTimes() {
		return notes.keySet();
	}
	
	public boolean isEmpty() {
		return notes.keySet().isEmpty();
	}
	
	public String getMetaInfo() {
		return new String(metaInfo);
	}
	
	public void setMetaInfo(String info) {
		this.metaInfo = info;
	}
	
	public void absorb(Measure other) {
		if (other.beats() != beats() || other.beatValue() != beatValue())
			throw new IllegalArgumentException("Cannot absorb a measure with a different key signature.");
		notes.putAll(other.notes);
		setMetaInfo(metaInfo + "\n" + other.getMetaInfo());
	}

	private double latestNoteEnd() {
		double latest = 0;
		for (Double dub : notes.keySet()) {
			for (MidiNote note : notes.get(dub)) {
				latest = Math.max(latest, dub + note.getDuration());
			}
		}
		return latest;
	}

}
