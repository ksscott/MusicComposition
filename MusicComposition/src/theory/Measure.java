package theory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import performance.MidiAction;
import performance.MidiNote;
import performance.instrument.Instrument;
import theory.analysis.Phrase;

public class Measure implements Comparable<Measure> {
	
	// time signature:
	private TimeSignature timeSignature;
	
	int measureNumber;
	private double bpm = 60;
	
	private String metaInfo = "";
	
	private Set<InstrumentMeasure> instruments;
	
	public Measure(TimeSignature timeSignature) {
		this.timeSignature = timeSignature;
		this.instruments = new HashSet<>();
	}
	
	/** @return number of beats in this measure */
	public int beats() { return timeSignature.getBeats(); }
	/** @return duration of each beat as a fraction of a whole note */
	public NoteDuration beatValue() { return timeSignature.getBeatValue(); }
	/** @return duration of this measure as a fraction of a whole note, equivalent to fraction created by time signature */
	public double length() { return beats()*beatValue().duration(); }

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
	
	/**
	 * @param instrument to query
	 * @param time within measure
	 * @return notes and rests occurring at this time
	 */
	public List<MidiAction> getActions(Instrument instrument, double time) {
		return getInstrument(instrument).getActions(time);
	}

	/**
	 * @param time start, inclusive
	 * @param time end, exclusive
	 * @return notes and rests occurring in a range of times
	 */
	public List<MidiAction> getActions(Instrument instrument, double start, double end) {
		return getInstrument(instrument).getActions(start, end);
	}
	
	/**
	 * @param instrument to query
	 * @param time within measure
	 * @return notes occurring at this time
	 */
	public List<MidiNote> getNotes(Instrument instrument, double time) {
		return getInstrument(instrument).getNotes(time);
	}
	
	/**
	 * @param time start, inclusive
	 * @param time end, exclusive
	 * @return notes occurring in a range of times
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
	
	public Measure add(Instrument instrument, MidiAction note, double offset) {
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
	
	/**
	 * @return a set of times within the measure when some number of notes (or rests) is played. 
	 * Each time is a Double representing an offset into the measure having a unit of whole notes. 
	 * For example, a time of .75 would mean an action occurs on the fourth beat of a 4/4 measure. 
	 */
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
	
	public String stringDrawing() {
		// this implementation is kinda gross
		String drawing = "";
		List<String> partStrings = new ArrayList<>();
		List<Instrument> instList = instruments.stream().map(InstrumentMeasure::getInstrument)
											   .sorted().collect(Collectors.toList());
		int widestInstrumentName = 0;
		for (Instrument instrument : instList) {
			String title = instrument.toString() + ":";
			partStrings.add(title);
			widestInstrumentName = Math.max(title.length(), widestInstrumentName);
		}
		for (int i=0; i<partStrings.size(); i++) {
			String temp = partStrings.get(i);
			int diff = widestInstrumentName - temp.length();
			for (int j=0; j<diff; j++)
				temp += " ";
			partStrings.set(i, temp);
		}
		
		List<Double> times = new ArrayList<>(getTimes());
		Collections.sort(times);
		
		for (Instrument instrument : instList) {
			String instString = partStrings.get(instList.indexOf(instrument));
			Map<Double,List<MidiNote>> playedTimes = new HashMap<>();
			int maxSimulNotes = times.stream()
									 // shh, you saw nothing. this is how streams were meant to be used:
									 .peek(time -> playedTimes.put(time, getNotes(instrument, time)))
									 .peek(time -> Collections.sort(playedTimes.get(time), new Comparator<MidiNote>() {
										@Override public int compare(MidiNote o1, MidiNote o2) {
											return o2.getPitch() - o1.getPitch(); // flipped, highest on top
										}}))
									 .map(time -> getActions(instrument, time)) // TODO consider using getNotes() instead
									 .mapToInt(List::size)
									 .max()
									 .orElse(0);
			for (int row=0; row<maxSimulNotes; row++) {
				if (row > 0)
					for (int i=0; i<widestInstrumentName; i++)
						instString += " ";
				for (double time : times) {
					List<MidiNote> notes = playedTimes.get(time);
					if (row > notes.size() - 1) {
						instString += "   "; // TODO this assumes all 2-digit pitches
						continue;
					}
					instString += " " + notes.get(row).getPitch() 
//							+ " " + time 
							+ " " + notes.get(row).getDynamic().notation()
							;
				}
				instString += System.lineSeparator();
			}
			drawing += instString;
		}
		
		if (drawing.length() > System.lineSeparator().length()) {
			// mushes all measures together, no break between:
			drawing = drawing.substring(0,drawing.length()-System.lineSeparator().length());
		}
		
		return drawing;
	}
	
	public static Measure commonTime() {
		return new Measure(new TimeSignature(4, NoteDuration.quarter()));
	}
	
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
		Map<Double, List<MidiAction>> phraseNotes = phrase.getNotes();
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
			List<MidiAction> notes = phraseNotes.get(time);
//			if (notes.size() > 1)
//				throw new IllegalStateException("bug!");
			for (MidiAction note : notes) {
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
	
	@Override
	public int compareTo(Measure o) {
		return measureNumber - o.measureNumber; // what could go wrong
	}

	// TODO support multiple voices of the same instrument
	private class InstrumentMeasure {
		
		private Instrument instrument;
		private Map<Double,List<MidiAction>> notes;
		
		public InstrumentMeasure(Instrument instrument) {
			this.instrument = instrument;
			this.notes = new HashMap<>();
		}
		
		public Instrument getInstrument() {
			return instrument;
		}
		
		// are these necessary?
		public int beats() { return Measure.this.beats(); }
		public NoteDuration beatValue() { return Measure.this.beatValue(); }
//		public double length() { return Measure.this.length(); }
		
		public List<MidiAction> getActions(double time) {
			List<MidiAction> list = notes.get(new Double(time));
			return list == null ? new ArrayList<>() : new ArrayList<>(list);
		}
		
		/**
		 * @param start first time to return played actions (inclusive)
		 * @param end last time to return played a (exclusive)
		 * @return all notes played in the given time window, sorted by time played
		 */
		public List<MidiAction> getActions(double start, double end) {
			List<MidiAction> allNotes = new ArrayList<>();
			List<Double> keySet = new ArrayList<>(notes.keySet());
			Collections.sort(keySet);
//			System.out.println("Key set size: " + keySet.size());
			for (Double dub : keySet) {
				if ((dub >= start && dub < end) || dub == start)
					allNotes.addAll(notes.get(dub));
			}
			// TODO sort?
//			System.out.println("All notes found for " + instrument + ": " + allNotes.size());
			return allNotes;
		}
		
		public List<MidiNote> getNotes(double time) {
			List<MidiNote> notes = new ArrayList<>();
			for (MidiAction action : getActions(time))
				if (action instanceof MidiNote)
					notes.add((MidiNote) action);
			return notes;
		}
		
		/**
		 * @param start first time to return played notes (inclusive)
		 * @param end last time to return played notes (exclusive)
		 * @return all notes played in the given time window, sorted by time played
		 */
		public List<MidiNote> getNotes(double start, double end) {
			List<MidiNote> notes = new ArrayList<>();
			for (MidiAction action : getActions(start, end))
				if (action instanceof MidiNote)
					notes.add((MidiNote) action);
			return notes;
		}
		
		public void add(MidiAction note) {
			add(note, latestNoteEnd());
		}
		
		public void add(MidiAction note, double offset) {
			if (offset < 0)
				throw new IllegalArgumentException("Cannot add notes before start of measure.");
			if (offset + note.duration() > length())
				throw new IllegalArgumentException("Note would end after end of measure."
						+ " Note of duration " + note.getDuration() + " at time " + offset);
			
//			System.out.println("Adding note to " + instrument);
			
			List<MidiAction> list = notes.get(new Double(offset));
			if (list == null) {
				list = new ArrayList<>();
				notes.put(new Double(offset), list);
			}
			list.add(note);
//			System.out.println("Now has " + list.size() + " notes");
		}
		
		public void add(Phrase phrase) {
//			System.out.println("Adding phrase at time: " + latestNoteEnd());
			add(phrase, latestNoteEnd());
		}
		
		public void add(Phrase phrase, double offset) {
			if (offset + phrase.getStart() < 0)
				throw new IllegalArgumentException("No part of the phrase can start before the start of the measure.");
			if (offset + phrase.getEnd() > length())
				throw new IllegalArgumentException("Phrase would end after end of measure.");

			Map<Double, List<MidiAction>> phraseNotes = phrase.getNotes();
			for (Double time : phraseNotes.keySet())
				for (MidiAction note : phraseNotes.get(time))
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
				for (MidiAction note : notes.get(dub)) {
					latest = Math.max(latest, dub + note.duration()); // FIXME likely still prone to errors
//					if (latest >= .25 && latest <= .75)
//						System.out.println("math: " + dub + " + " + note.duration() + " = " + latest);
				}
			}
			return latest;
		}
	}
}
