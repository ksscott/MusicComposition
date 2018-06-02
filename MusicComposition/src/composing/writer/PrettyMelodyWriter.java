package composing.writer;

import static composing.RandomUtil.*;
import static composing.writer.Ornament.*;

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
		Key key = Key.inferKey(allPitchesInAllMeasures);
		
		int ornamentChance = 40;
		int appoggiaturaChance = 70; // = (ornamentChance * XX)% 
		int mordentChance = 90; // = (ornamentChance * (100 - appoggiaturaChance) * XX)%
//		int turnChance = 100; // = (ornamentChance * (100 - appoggiaturaChance) * (100 - mordentChance))%
		
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
				if (roll(ornamentChance)) {
					if (roll(appoggiaturaChance))
						measurePhrase.add(appoggiatura(midiNote, key));
					else if (roll(mordentChance)) {
						if (roll(50))
							measurePhrase.add(lowerMordent(midiNote, key));
						else
							measurePhrase.add(upperMordent(midiNote, key));
					} else {
						measurePhrase.add(turn(midiNote, key));
					}
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
