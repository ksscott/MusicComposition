package composing.writer;

import static composing.RandomUtil.random;
import static composing.RandomUtil.roll;
import static composing.writer.Ornament.appoggiatura;
import static composing.writer.Ornament.lowerMordent;
import static composing.writer.Ornament.trill;
import static composing.writer.Ornament.upperMordent;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import theory.Dynamic;
import theory.Key;
import theory.Measure;
import theory.MidiNote;
import theory.MidiPitch;
import theory.Note;
import theory.analysis.Phrase;

public class PrettyMelodyWriter implements MelodyWriter {

	@Override
	public Phrase writeMelody(List<Measure> measures) {
		if (measures.isEmpty())
			return new Phrase();
		
		Set<MidiPitch> allPitchesInAllMeasures = measures.stream()
				.flatMap(measure -> measure.getNotes(0, measure.beats()*measure.beatValue()).stream())
				.map(MidiNote::getPitch)
				.map(MidiPitch::new)
				.collect(Collectors.toSet());
		Key key = Key.inferKey(allPitchesInAllMeasures);
		
		return writeMelody(measures, key);
	}
	
	public Phrase writeMelody(List<Measure> measures, Key key) {
		Phrase phrase = new Phrase();
		
		if (measures.isEmpty())
			return phrase;
		
		Set<MidiPitch> allPitchesInAllMeasures = measures.stream()
				.flatMap(measure -> measure.getNotes(0, measure.beats()*measure.beatValue()).stream())
				.map(MidiNote::getPitch)
				.map(MidiPitch::new)
				.collect(Collectors.toSet());
		
		Optional<Integer> highest = allPitchesInAllMeasures.stream().map(MidiPitch::get).reduce((a,b) -> a < b ? b : a);
		
		int ornamentChance = 33;
		int appoggiaturaChance = 70; // = (ornamentChance * XX)% 
		int mordentChance = 90; // = (ornamentChance * (100-appoggiaturaChance) * XX)%
//		int trillChance = 70; // = (ornamentChance * (100-appoggiaturaChance) * (100-mordentChance) * XX)%
//		int turnChance = 100; // = (ornamentChance * (100-appoggiaturaChance) * (100-mordentChance) * (100-trillChance))%
		
		for (Measure measure : measures) {
			Phrase measurePhrase = new Phrase();
			boolean risingMelody = roll(50);
			Note startingNote = key.note(random(key.getScale().intervals().length) + 1);
			MidiPitch startingPitch = new MidiPitch(startingNote, 1);
			int halfStepsBelowHighest = risingMelody ? 5 : 0;
			while (startingPitch.get() < highest.get() - halfStepsBelowHighest)
				startingPitch = startingPitch.above(12);
			
			for (int i=0; i<measure.beats(); i++) {
				int steps = risingMelody ? i : -i;
				MidiNote midiNote = new MidiNote(key.stepsAbove(steps, startingPitch), measure.beatValue());
				midiNote.setDynamic(Dynamic.MEZZO_PIANO);
				measure.setMetaInfo(measure.getMetaInfo() + " " + midiNote.getPitch());
				if (roll(ornamentChance)) {
					if (roll(appoggiaturaChance)) {
						measurePhrase.add(appoggiatura(midiNote, key));
						measure.setMetaInfo(measure.getMetaInfo() + "ap");
					}
					else if (roll(mordentChance)) {
						if (roll(50))
							measurePhrase.add(lowerMordent(midiNote, key));
						else
							measurePhrase.add(upperMordent(midiNote, key));
						measure.setMetaInfo(measure.getMetaInfo() + "md");
					} else if (i==measure.beats()-1){
						measurePhrase.add(trill(midiNote, key));
						measure.setMetaInfo(measure.getMetaInfo() + "tr");
					} else {
//						measurePhrase.add(turn(midiNote, key)); // removing turns for now
//						measure.setMetaInfo(measure.getMetaInfo() + "tn");
						measurePhrase.add(midiNote);
						measure.setMetaInfo(measure.getMetaInfo() + "__");
					}
				} else {
					measurePhrase.add(midiNote);
					measure.setMetaInfo(measure.getMetaInfo() + "__");
				}
			}
			phrase.add(measurePhrase);
			
//			measure.setMetaInfo(measure.getMetaInfo() + " " + measurePhrase.timesAndNotes());
			
		}
//		System.out.println("Wrote a melody of length " + phrase.length() + " onto measures of length " 
//				+ measures.stream().map(Measure::length).reduce((a,b) -> a+b).orElse(0.0));
		return phrase;
	}

}
