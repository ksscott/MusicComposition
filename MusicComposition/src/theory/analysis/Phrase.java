package theory.analysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import performance.MidiNote;

public class Phrase implements Cloneable {
	
	private Map<Double,List<MidiNote>> notes;

	public Phrase() {
		this.notes = new HashMap<>();
	}
	
	/** @return starting time of the first note in this phrase */
	public double getStart() {
		if (notes.isEmpty())
			return 0;
		double earliest = Integer.MAX_VALUE;
		for (Double dub : notes.keySet()) {
			if (!notes.get(dub).isEmpty())
				earliest = Math.min(earliest, dub);
		}
		return earliest;
	}
	
	/** @return ending time of the last note in this phrase */
	public double getEnd() {
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
	
	/** @return duration of the phrase from {@link #getStart()} to {@link #getEnd()} */
	public double length() {
		return getEnd() - getStart();
	}
	
	/** @return a copy of this phrase's map of times and notes */
	public Map<Double,List<MidiNote>> getNotes() {
		return new HashMap<>(notes);
	}
	
	/** @param note to be added to the end of this phrase */
	public void add(MidiNote note) {
		add(note, getEnd());
	}
	
	/**
	 * @param note to be added to this phrase
	 * @param offset time in this phrase to add the given note
	 */
	public void add(MidiNote note, double offset) {
		List<MidiNote> list = notes.get(new Double(offset));
		if (list == null) {
			list = new ArrayList<>();
			notes.put(new Double(offset), list);
		}
		list.add(note);
	}
	
	/** 
	 * Convenience method for absorb(other, getEnd())
	 * @see #absorb(Phrase, double)
	 */
	public void add(Phrase other) {
		absorb(other, getEnd());
	}
	
	/** 
	 * Convenience method for absorb(other, offset)
	 * @see #absorb(Phrase, double)
	 */
	public void add(Phrase other, double offset) {
		absorb(other, offset);
	}
	
	/** 
	 * Convenience method for absorb(other, 0)
	 * @see #absorb(Phrase, double)
	 */
	public void absorb(Phrase other) {
		absorb(other, 0);
	}
	
	/**
	 * Absorb another phrase into this one. 
	 * Notes in the given phrase will be incorporated into this one 
	 * as though they were added to this one.
	 * 
	 * @param other phrase to be absorbed
	 * @param offset time in this phrase to set the given phrase's 0 time mark
	 */
	public void absorb(Phrase other, double offset) {
		Map<Double, List<MidiNote>> otherNotes = other.notes;
		for (Double time : otherNotes.keySet())
			for (MidiNote note : otherNotes.get(time))
				add(note, time + offset);
	}

	/**
	 * Stretches or compresses this phrase about the center: time = 0
	 * <p>
	 * All notes will be lengthened by a factor of timeRatio. 
	 * All note offsets will be shifted away from 0 by a factor of timeRatio.
	 * <p>
	 * Example: expand(2.0)
	 * <p>
	 * | q q q q |  -->  | h h | h h |
	 * 
	 * <p>
	 * WARNING: Note ties will not be preserved
	 * 
	 * @param timeRatio ratio of expansion, ratios less than 1 will compress the phrase, 
	 * non-positive ratios not supported
	 * @return the expanded phrase
	 */
	public Phrase expand(double timeRatio) {
		if (timeRatio <= 0)
			throw new IllegalArgumentException("Only positive ratios accepted for expansions.");
		Phrase phrase = new Phrase();
		for (Double time : notes.keySet()) {
			List<MidiNote> list = notes.get(time);
			List<MidiNote> newList = new ArrayList<>();
			for (MidiNote note : list)
				newList.add(note.expand(timeRatio));
			phrase.notes.put(time*timeRatio, newList);
		}
		return phrase;
	}
	
	public String timesAndNotes() {
		String phraseString = "";
		Map<Double, List<MidiNote>> phraseNotes = getNotes();
		ArrayList<Double> times = new ArrayList<>(phraseNotes.keySet());
		Collections.sort(times);
		for (Double time : times) {
			phraseString += String.format("(%.2f,", time);
			List<MidiNote> list = phraseNotes.get(time);
			for (MidiNote phraseNote : list) {
				phraseString += phraseNote.getPitch() + ",";
			}
			phraseString = phraseString.substring(0, phraseString.length() - 1);
			phraseString += ") ";
		}
		return phraseString;
	}
	
	@Override
	public Phrase clone() {
		return expand(1);
	}
	
}
