package theory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import performance.MidiNote;
import performance.instrument.Instrument;
import theory.analysis.Phrase;

public class Measure {
	
	// time signature:
	private int beats;
	private double beatValue;
	
	int measureNumber;
	private double bpm = 60;
	
	private String metaInfo = "";
	
	private Set<InstrumentMeasure> instruments;
	
	public Measure(int beats, double beatValue) {
		this.beats = beats;
		this.beatValue = beatValue;
		this.instruments = new HashSet<>();
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
	
	public Set<Instrument> getInstruments() {
		return instruments.stream().map(InstrumentMeasure::getInstrument).collect(Collectors.toSet());
	}
	
	public void addInstrument(Instrument instrument) {
		instruments.add(new InstrumentMeasure(instrument));
	}
	
	public void removeInstrument(Instrument removed) {
		instruments.removeIf(inst -> inst.getInstrument().equals(removed));
	}
	
	public List<MidiNote> getNotes(Instrument instrument, double time) {
		return getInstrument(instrument).getNotes(time);
	}

	/**
	 * @param time start, exclusive
	 * @param time end, inclusive
	 * @return notes in a range
	 */
	public List<MidiNote> getNotes(Instrument instrument, double start, double end) {
		return getInstrument(instrument).getNotes(start, end);
	}

	/**
	 * Intended to add a note at the latest silence in this measure
	 * 
	 * @param note
	 * @return this measure
	 */
	public Measure add(Instrument instrument, MidiNote note) {
		getInstrument(instrument).add(note);
		return this;
	}
	
	public Measure add(Instrument instrument, MidiNote note, double offset) {
		getInstrument(instrument).add(note, offset);
		return this;
	}
	
	// TODO add a remove(MidiNote) method?
	
	public Measure add(Instrument instrument, Phrase phrase) {
		getInstrument(instrument).add(phrase);
		return this;
	}
	
	public Measure add(Instrument instrument, Phrase phrase, double offset) {
		getInstrument(instrument).add(phrase, offset);
		return this;
	}
	
	// TODO add a remove(Phrase) method?
	
	public Set<Double> getTimes() {
		Set<Double> retval = new HashSet<>();
		for (InstrumentMeasure instrument : instruments)
			retval.addAll(instrument.getTimes());
		return retval;
	}
	
	public boolean isEmpty() {
		for (InstrumentMeasure instrument : instruments)
			if (!instrument.isEmpty())
				return false;
		return true;
	}
	
	public void absorb(Measure other) {
		for (InstrumentMeasure im : other.instruments) {
			Instrument otherInstrument = im.getInstrument();
			if (!this.getInstruments().contains(otherInstrument))
				this.addInstrument(otherInstrument);
			getInstrument(otherInstrument).absorb(im);
		}
	}
	
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
	
	public static void writeOnto(Instrument instrument, Phrase phrase, List<Measure> measures, double offset) { // FIXME honor the offset
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
				measure.add(instrument, note, location);
//				if (location == 0)
//					System.out.println("downbeat" + note.getPitch() + " ");
			}
//			System.out.println("Writing, time/ticker/location/measureIndex " 
//					+ time + "/" + ticker + "/" + location + "/" + measureIndex);
		}
//		System.out.println();
	}
	
	private InstrumentMeasure getInstrument(Instrument instrument) {
		return instruments.stream().filter(inst -> inst.getInstrument().equals(instrument)).
				findAny().orElseThrow(() -> new RuntimeException("Instrument not present: " + instrument));
	}
	
//	private double latestNoteEnd() {
//		return instruments.stream().mapToDouble(InstrumentMeasure::latestNoteEnd).max().orElse(0.0);
//	}
	
	// TODO support multiple voices of the same instrument
	private class InstrumentMeasure {
		
		private Instrument instrument;
		private Map<Double,List<MidiNote>> notes;
		
		public InstrumentMeasure(Instrument instrument) {
			this.instrument = instrument;
			this.notes = new HashMap<>();
		}
		
		public Instrument getInstrument() {
			return instrument;
		}
		
		// are these necessary?
		public int beats() { return Measure.this.beats(); }
		public double beatValue() { return Measure.this.beatValue(); }
		public double length() { return Measure.this.length(); }
		
		public List<MidiNote> getNotes(double time) {
			List<MidiNote> list = notes.get(new Double(time));
			return list == null ? new ArrayList<>() : new ArrayList<>(list);
		}
		
		public List<MidiNote> getNotes(double start, double end) {
			List<MidiNote> allNotes = new ArrayList<>();
			Set<Double> keySet = notes.keySet();
			for (Double dub : keySet) {
				if ((dub > start && dub <= end) || dub == end)
					allNotes.addAll(notes.get(dub));
			}
			// TODO sort?
			return allNotes;
		}
		
		public void add(MidiNote note) {
			add(note, latestNoteEnd());
		}
		
		public void add(MidiNote note, double offset) {
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
		}
		
		public void add(Phrase phrase) {
			add(phrase, latestNoteEnd());
		}
		
		public void add(Phrase phrase, double offset) {
			if (offset + phrase.getStart() < 0)
				throw new IllegalArgumentException("No part of the phrase can start before the start of the measure.");
			if (offset + phrase.getEnd() > beats * beatValue)
				throw new IllegalArgumentException("Phrase would end after end of measure.");

			Map<Double, List<MidiNote>> phraseNotes = phrase.getNotes();
			for (Double time : phraseNotes.keySet())
				for (MidiNote note : phraseNotes.get(time))
					add(note, offset + time);
		}
		
		public Set<Double> getTimes() {
			return notes.keySet();
		}
		
		public boolean isEmpty() {
			return notes.keySet().isEmpty();
		}
		
		public void absorb(InstrumentMeasure other) {
			if (other.beats() != beats() || other.beatValue() != beatValue())
				throw new IllegalArgumentException("Cannot absorb a measure with a different key signature.");
			notes.putAll(other.notes);
//			setMetaInfo(metaInfo + "\n" + other.getMetaInfo());
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
}
