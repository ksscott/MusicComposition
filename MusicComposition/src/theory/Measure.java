package theory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import theory.analysis.Phrase;

public class Measure {
	
	// time signature:
	private int beats;
	private double beatValue;
	private double bpm = 60;
	
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
		if (offset < 0)
			throw new IllegalArgumentException("Cannot add notes before start of measure.");
		if (offset + note.getDuration() > beats * beatValue)
			throw new IllegalArgumentException("Note would end after end of measure.");
		
		List<MidiNote> list = notes.get(new Double(offset));
		if (list == null) {
			list = new ArrayList<>();
			notes.put(new Double(offset), list);
		}
		list.add(note);
		return this;
	}
	
	public Measure addPhrase(Phrase phrase) {
		return addPhrase(phrase, latestNoteEnd());
	}
	
	public Measure addPhrase(Phrase phrase, double offset) {
		if (phrase.getStart() < 0)
			throw new IllegalArgumentException("No part of the phrase can start before the start of the measure.");
		if (offset + phrase.getEnd() > beats * beatValue)
			throw new IllegalArgumentException("Phrase would end after end of measure.");
		
		Map<Double, List<MidiNote>> phraseNotes = phrase.getNotes();
		for (Double time : phraseNotes.keySet())
			for (MidiNote note : phraseNotes.get(time))
				addNote(note, offset + time);
		
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
	
	public double getBpm() {
		return bpm;
	}
	
	public void setBpm(double beatsPerMinute) {
		this.bpm = beatsPerMinute;
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
