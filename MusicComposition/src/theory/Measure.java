package theory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import theory.analysis.Phrase;

public class Measure {
	
	// time signature:
	private int beats;
	private double beatValue;
	
	int measureNumber;
	private double bpm = 60;
	
	private String metaInfo = "";
	
	private Map<Double,List<MidiNote>> notes;
	
	public Measure(int beats, double beatValue) {
		this.beats = beats;
		this.beatValue = beatValue;
		this.notes = new HashMap<>();
	}
	
	/** @return number of beats in this measure */
	public int beats() { return beats; }
	/** @return duration of each beat as a fraction of a whole note */
	public double beatValue() { return beatValue; }
	/** @return duration of this measure as a fraction of a whole note, equivalent to fraction created by time signature */
	public double length() { return beats()*beatValue(); }

	public int getMeasureNumber() { return measureNumber; }
	public void setMeasureNumber(int number) { this.measureNumber = number; }

	public double getBpm() { return bpm; }
	public void setBpm(double beatsPerMinute) { this.bpm = beatsPerMinute; }

	public String getMetaInfo() { return new String(metaInfo); }
	public void setMetaInfo(String info) { this.metaInfo = info; }

	/**
	 * Intended to add a note at the latest silence in this measure
	 * 
	 * @param note
	 * @return this measure
	 */
	public Measure add(MidiNote note) {
		return add(note, latestNoteEnd());
	}
	
	public Measure add(MidiNote note, double offset) {
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
	
	public Measure add(Phrase phrase) {
		return add(phrase, latestNoteEnd());
	}
	
	public Measure add(Phrase phrase, double offset) {
		if (offset + phrase.getStart() < 0)
			throw new IllegalArgumentException("No part of the phrase can start before the start of the measure.");
		if (offset + phrase.getEnd() > beats * beatValue)
			throw new IllegalArgumentException("Phrase would end after end of measure.");
		
		Map<Double, List<MidiNote>> phraseNotes = phrase.getNotes();
		for (Double time : phraseNotes.keySet())
			for (MidiNote note : phraseNotes.get(time))
				add(note, offset + time);
		
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
	
	public void absorb(Measure other) {
		if (other.beats() != beats() || other.beatValue() != beatValue())
			throw new IllegalArgumentException("Cannot absorb a measure with a different key signature.");
		notes.putAll(other.notes);
		setMetaInfo(metaInfo + "\n" + other.getMetaInfo());
	}
	
	// can't remember what this was used for...
//	public String timesAndNotes() {
//		String measureString = "";
//		Map<Double, List<MidiNote>> measureNotes = notes;
//		List<Double> times = new ArrayList<>(measureNotes.keySet());
//		Collections.sort(times);
//		for (Double time : times) {
//			measureString += String.format("(%.2f,", time);
//			List<MidiNote> list = measureNotes.get(time);
//			for (MidiNote measureNote : list) {
//				measureString += measureNote.getPitch() + ",";
//			}
//			measureString = measureString.substring(0, measureString.length() - 1);
//			measureString += ") ";
//		}
//		return measureString;
//	}
	
	public static void writeOnto(Phrase phrase, List<Measure> measures, double offset) { // FIXME honor the offset
		if (offset + phrase.getStart() < 0)
			throw new IllegalArgumentException("Phrase would start before start of measures.");
		Optional<Double> totalLength = measures.stream().map(Measure::length).reduce((a, b) -> a+b);
		if (!totalLength.isPresent() || totalLength.get() < offset + phrase.getEnd())
			throw new IllegalArgumentException("Given measures are too short to accommodate the given phrase.");
		
//		System.out.println("Writing a Phrase: " + phrase.timesAndNotes());
		
		int measureIndex = 0;
		Measure measure = measures.get(measureIndex);
		Double ticker = 0.0; // slide the ticker to the start of each measure
		Map<Double, List<MidiNote>> phraseNotes = phrase.getNotes();
		List<Double> times = new ArrayList<>(phraseNotes.keySet());
		Collections.sort(times);
		
//		System.out.println("Written Measures: ");
		for (Double time : times) {
			while (time >= ticker + measure.length()) {
//				System.out.println("metainfo " + measure.getMetaInfo());
//				System.out.println("Measure " + measure.timesAndNotes());
				
				ticker += measure.length();
				measure = measures.get(++measureIndex);
			}
			double location = time - ticker;
			List<MidiNote> notes = phraseNotes.get(time);
//			if (notes.size() > 1)
//				throw new IllegalStateException("bug!");
			for (MidiNote note : notes) {
				measure.add(note, location);
//				if (location == 0)
//					System.out.println("downbeat" + note.getPitch() + " ");
			}
//			System.out.println("Writing, time/ticker/location/measureIndex " 
//					+ time + "/" + ticker + "/" + location + "/" + measureIndex);
		}
//		System.out.println();
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
