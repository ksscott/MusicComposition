package composing.writer;

import static composing.RandomUtil.random;
import static composing.RandomUtil.roll;
import static composing.writer.Ornament.appoggiatura;
import static composing.writer.Ornament.lowerMordent;
import static composing.writer.Ornament.trill;
import static composing.writer.Ornament.upperMordent;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import performance.Dynamic;
import performance.MidiAction.MidiRest;
import performance.MidiNote;
import theory.Key;
import theory.Measure;
import theory.MidiPitch;
import theory.Note;
import theory.analysis.Phrase;

public class PrettyMelodyWriter implements MelodyWriter {

	@Override
	public Phrase writeMelody(List<Measure> measures) {
		if (measures.isEmpty())
			return new Phrase();
		
		Set<MidiPitch> allPitchesInAllMeasures = measures.stream().flatMap(
					  measure -> measure.getInstruments().stream().flatMap(
						 inst -> measure.getNotes(inst, 0, measure.length()).stream()))
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
		
		Set<MidiPitch> allPitchesInAllMeasures = measures.stream().flatMap(
					  measure -> measure.getInstruments().stream().flatMap(
						 inst -> measure.getNotes(inst, 0, measure.length()).stream()))
				.map(MidiNote::getPitch)
				.map(MidiPitch::new)
				.collect(Collectors.toSet());
		
		int highest = allPitchesInAllMeasures.stream().map(MidiPitch::get).reduce((a,b) -> a < b ? b : a).orElse(60);
		
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
			while (startingPitch.get() < highest - halfStepsBelowHighest)
				startingPitch = startingPitch.above(12);
			
			double measureLength = measure.length();
			double time = 0;
			int steps = 0;
			while (time < measureLength) {
				boolean noteOrRest = roll(97); // false means we rest; determination can/should be changed
				double duration = 0; // length of note or rest
				if (noteOrRest && roll(10)) // don't rest for a half note
					duration = 2/4.0; // half note
				else if (roll(15))
					duration = 1/8.0; // eighth note
				else
					duration = 1/4.0; // quarter note
				// else, eighth notes or something
				duration = Math.min(duration, measureLength-time); // truncate at end of measure
				
				if (!noteOrRest) { // rest
					measurePhrase.add(new MidiRest(duration));
					measure.setMetaInfo(measure.getMetaInfo() + " rest");
					time += duration;
					continue;
				}
				
				MidiNote midiNote = new MidiNote(key.stepsAbove(steps, startingPitch), duration);
				steps += risingMelody ? 1 : -1;
				
//				midiNote.setDynamic(Dynamic.MEZZO_PIANO);
				measure.setMetaInfo(measure.getMetaInfo() + " " + midiNote.getPitch());
				if (roll(ornamentChance)) {
					double ornamentLength = Math.min(1/8.0, duration/2.0);
					if (roll(appoggiaturaChance)) {
						measurePhrase.add(appoggiatura(midiNote, key, ornamentLength), time);
						measure.setMetaInfo(measure.getMetaInfo() + "ap");
					}
					else if (roll(mordentChance)) {
						if (roll(50))
							measurePhrase.add(lowerMordent(midiNote, key, ornamentLength), time);
						else
							measurePhrase.add(upperMordent(midiNote, key, ornamentLength), time);
						measure.setMetaInfo(measure.getMetaInfo() + "md");
					} else if (time >= measureLength-measure.beatValue()){ // last beat
						measurePhrase.add(trill(midiNote, key), time);
						measure.setMetaInfo(measure.getMetaInfo() + "tr");
					} else {
//						measurePhrase.add(turn(midiNote, key)); // removing turns for now
//						measure.setMetaInfo(measure.getMetaInfo() + "tn");
						measurePhrase.add(midiNote, time);
						measure.setMetaInfo(measure.getMetaInfo() + "__");
					}
				} else {
					measurePhrase.add(midiNote, time);
					measure.setMetaInfo(measure.getMetaInfo() + "__");
				}
				time += duration;
			}
			
			phrase.add(measurePhrase);
			
//			measure.setMetaInfo(measure.getMetaInfo() + " " + measurePhrase.timesAndNotes());
			
		}
//		System.out.println("Wrote a melody of length " + phrase.length() + " onto measures of length " 
//				+ measures.stream().map(Measure::length).reduce((a,b) -> a+b).orElse(0.0));
		return phrase;
	}

}
