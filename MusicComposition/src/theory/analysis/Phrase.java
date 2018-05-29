package theory.analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import theory.MidiNote;

public class Phrase {
	
	private Map<Double,List<MidiNote>> notes;

	public Phrase() {
		this.notes = new HashMap<>();
	}
	
	public double getStart() {
		if (notes.isEmpty())
			return 0;
		double earliest = Integer.MAX_VALUE;
		for (Double dub : notes.keySet()) {
			if (!notes.get(dub).isEmpty())
				earliest = Math.max(earliest, dub);
		}
		return earliest;
	}
	
	public double getEnd() {
		return latestNoteEnd();
	}
	
	public Map<Double,List<MidiNote>> getNotes() {
		return new HashMap<>(notes);
	}
	
	public void addNote(MidiNote note) {
		addNote(note, latestNoteEnd());
	}
	
	public void addNote(MidiNote note, double offset) {
		List<MidiNote> list = notes.get(new Double(offset));
		if (list == null) {
			list = new ArrayList<>();
			notes.put(new Double(offset), list);
		}
		list.add(note);
	}
	
	public Phrase expand(double timeRatio) {
		Phrase phrase = new Phrase();
		for (Double time : notes.keySet()) {
			List<MidiNote> list = notes.get(time);
			List<MidiNote> newList = new ArrayList<>();
			for (MidiNote note : list)
				newList.add(new MidiNote(note.getPitch(), note.getDuration()*timeRatio));
			phrase.notes.put(time*timeRatio, list);
		}
		return phrase;
	}
	
	private double latestNoteEnd() {
		if (notes.isEmpty())
			return 0;
		double latest = Integer.MIN_VALUE;
		for (Double dub : notes.keySet()) {
			for (MidiNote note : notes.get(dub)) {
				latest = Math.max(latest, dub + note.getDuration());
			}
		}
		return latest;
	}
	
}
