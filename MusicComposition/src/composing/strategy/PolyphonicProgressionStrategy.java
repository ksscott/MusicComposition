package composing.strategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import composing.IncompleteComposition;
import performance.MidiNote;
import performance.Tempo;
import performance.instrument.Instrument;
import theory.Chord;
import theory.ChordSpec;
import theory.Key;
import theory.Measure;
import theory.MidiPitch;
import theory.analysis.Analysis;
import theory.progression.ChordProgressions;
import theory.progression.ChordProgressions.ChordProgression;
import theory.progression.VoiceLeading;

public class PolyphonicProgressionStrategy extends ChordsSectionWriter {

	private Tempo tempo;
	private List<Instrument> voices;
	private Map<Instrument,MidiNote> heldNotes;
	
	public PolyphonicProgressionStrategy(Key key) {
		super(key);
		this.tempo = Tempo.ANDANTE;
		this.voices = new ArrayList<>();
		voices.add(Instrument.SOPRANO_VOICE);
		voices.add(Instrument.ALTO_VOICE);
		voices.add(Instrument.TENOR_VOICE);
		voices.add(Instrument.BASS_VOICE);
		this.heldNotes = new HashMap<>();
	}

	@Override
	public Measure generateFirstMeasure() {
		return composeBar(null, key.chordSpec(1));
	}
	
	@Override
	protected int getNextSectionSize(IncompleteComposition composition) {
		return 8;
	}

	@Override
	protected ChordProgression nextSectionProgression(Analysis analysis) {
		// FIXME This needs to be changed. This progression is not suitable for good polyphonic motion.
		return ChordProgressions.standardMajorProgression(key.getTonic()); // TODO elaborate on this
	}

	@Override
	protected Measure composeBar(Measure lastMeasure, ChordSpec nextChordSpec) {
		// TODO significant work is required to do anything other than play one chord per bar
		// first off, the analysis has assigned exactly one chordspec to each bar
		Measure measure = new Measure(4, 1/4.0);
		
		List<MidiNote> lastNotes = new ArrayList<>();
		
		for (Instrument voice : voices) {
			measure.addInstrument(voice);
			
			if (lastMeasure != null) {
				List<MidiNote> notes = lastMeasure.getNotes(voice, 0, lastMeasure.length());
				if (notes.size() != 1)
					throw new IllegalStateException("Expect to have exactly one note per voice per measure, but had: " + notes.size());
				lastNotes.add(notes.get(0));
			} else {
				// dumb, but we only do it once (x4)
				ChordSpec chordSpec = key.chordSpec(1);
				Chord chord = chordSpec.builder().addPitch(8).setOctave(3).build();
				MidiPitch pitch = chord.get().get(voices.indexOf(voice));
				lastNotes.add(new MidiNote(pitch, 1));
			}
		}
		
		Chord lastChord = new Chord(lastNotes.stream()
											 .map(MidiNote::getPitch)
											 .map(MidiPitch::new)
											 .collect(Collectors.toList()));
		
		// FIXME This needs to be changed. This voice leading is not suitable for good polyphonic motion.
		Chord nextChord = VoiceLeading.voiceLeadPolyphony(lastChord, nextChordSpec, 
				MidiPitch.inOctave(key.getTonic(), 2), MidiPitch.inOctave(key.getTonic(), 4));
		
		int index = 0;
		for (MidiPitch pitch : nextChord) { // assume sorted
			MidiNote nextNote = new MidiNote(pitch, measure.length());
			if (lastMeasure != null) {
				MidiNote.tieOver(lastNotes.get(index), nextNote); // assume sorted
				heldNotes.put(voices.get(index), lastNotes.get(index));
			} else {
				nextNote.setTiesOver(true);
			}
			measure.add(voices.get(index++), nextNote);
		}
		
		measure.setBpm(tempo.getBpm());
		
		return measure;
	}

	@Override
	protected ComposingStage onSectionsFilled(IncompleteComposition composition) {
		// TODO maybe do nothing?
		return null;
	}

	@Override
	public String toString() {
		return "Polyphonic Progression";
	}
}
