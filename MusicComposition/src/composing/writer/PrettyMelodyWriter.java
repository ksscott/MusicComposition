package composing.writer;

import static composing.RandomUtil.*;
import static composing.writer.Ornament.appoggiatura;
import static composing.writer.Ornament.lowerMordent;
import static composing.writer.Ornament.upperMordent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import theory.Dynamic;
import theory.Key;
import theory.Measure;
import theory.MidiNote;
import theory.MidiPitch;
import theory.Note;
import theory.Scale;
import theory.ScaleImpl;
import theory.analysis.Phrase;

public class PrettyMelodyWriter implements MelodyWriter {

	@Override
	public Phrase writeMelody(List<Measure> measures) {
		Phrase phrase = new Phrase();
		
		// what follows is a crude POC
		
		Set<MidiPitch> allPitchesInAllMeasures = measures.stream()
				.flatMap(measure -> measure.getNotes(0, measure.beats()*measure.beatValue()).stream())
				.map(MidiNote::getPitch)
				.map(MidiPitch::new)
				.collect(Collectors.toSet());
		Key key;
		try {
			key = Key.inferDiatonic(allPitchesInAllMeasures);
			System.out.println("Detecting key " + key);
		} catch (IllegalArgumentException e) {
			// too many or too few pitches to decide on a single key
			// let's just work with what we know
			// TODO probably incorporate this into inferDiatonic()
			List<Integer> pitchList = allPitchesInAllMeasures.stream()
					.map(MidiPitch::get)
					.map(integer -> modPos(integer, 12)) // causes arbitrary ordering
					.distinct()
					.sorted()
					.collect(Collectors.toList());
			int[] intervals = new int[pitchList.size()];
//			System.out.println("tonicWhyNot: " + newMidiPitch.get());
			for (int i=0; i<intervals.length-1; i++)
				intervals[i] = pitchList.get(i+1) - pitchList.get(i);
			int last = intervals.length-1;
			intervals[last] = 12 - (pitchList.get(last) - pitchList.get(0));
//			System.out.println("CREATED INTERVALS:");
//			for (int interval : intervals)
//				System.out.println("" + interval);
			Note tonicWhyNot = Key.toFlatNote(new MidiPitch(pitchList.get(0)));
			Scale scaleSureOk = new ScaleImpl(intervals);
			key = new Key(tonicWhyNot, scaleSureOk);
		}
		
		for (Measure measure : measures) {
			Phrase measurePhrase = new Phrase();
			Note startingNote = key.note(random(key.getScale().intervals().length) + 1);
			MidiPitch startingPitch = new MidiPitch(startingNote, 4);
			for (int i=0; i<measure.beats(); i++) {
				MidiNote midiNote = new MidiNote(key.stepsAbove(i, startingPitch), measure.beatValue());
				midiNote.setDynamic(Dynamic.MEZZO_PIANO);
				// FIXME I suspect something is wrong with the pitches chosen while writing...
				// the pitches skip around and takes the wrong shape
				// and this happened:
				// Notes at time 0.00: 55 58 62 70 69 
				// Notes at time 0.13: 69 
				// Notes at time 0.25: 55 58 62 70 
				if (roll(40)) {
					if (roll(70))
						measurePhrase.add(appoggiatura(midiNote, key));
					else if (roll(50))
						measurePhrase.add(lowerMordent(midiNote, key));
					else
						measurePhrase.add(upperMordent(midiNote, key));
				} else {
					measurePhrase.add(midiNote);
				}
			}
			phrase.add(measurePhrase);
		}
//		System.out.println("Wrote a melody of length " + phrase.length() + " onto measures of length " 
//				+ measures.stream().map(Measure::length).reduce((a,b) -> a+b).orElse(0.0));
		return phrase;
	}

}
