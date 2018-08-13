package theory.progression;

import static composing.RandomUtil.modPos;
import static composing.RandomUtil.roll;

import java.util.ArrayList;
import java.util.List;

import theory.Chord;
import theory.ChordSpec;
import theory.Key;
import theory.MidiPitch;
import theory.Note;

public class VoiceLeading {

	// util class
	private VoiceLeading() {}
	
	/**
	 * Only respects inversions 0,1,2 and higher order equivalents thereof.
	 * <p>
	 * TODO respect degree, add2, etc.
	 * 
	 * @param lastChord
	 * @param nextChordSpec
	 * @return
	 */
	public static Chord voiceLead(Chord lastChord, ChordSpec nextChordSpec, int bassMin, int bassMax) {
		Chord retval = new Chord();
		
		Chord nextChord = nextChordSpec.build();
//		System.out.println("Voice leading to pitches: ");
//		for (MidiPitch pitch : nextChord.get())
//			System.out.println(pitch);
		if (lastChord.isEmpty() || nextChord.isEmpty())
			return retval;
		List<MidiPitch> lastChordPitches = lastChord.get();
		MidiPitch previousBassPitch = lastChordPitches.get(0);
		
		Note bassNote = Key.toFlatNote(nextChord.get().get(0));
		
		// decide which way to take the bass note
		MidiPitch bassPitch = downFiveOrUpFour(previousBassPitch, bassNote); // TODO maybe revisit
		if (bassPitch.get() < bassMin)
			bassPitch = bassPitch.above(12);
		if (bassPitch.get() > bassMax)
			bassPitch = bassPitch.below(12);
		retval.add(bassPitch);
		int lastPitchAdded = bassPitch.get();
//		boolean bassNoteUp = bassPitch.compareTo(previousBassPitch) < 0; // TODO maybe use this
		
		for (MidiPitch pitch : lastChordPitches.subList(1, lastChordPitches.size())) {
			// for remaining pitches, attempt to step downward
			boolean found = false;
			for (int i=pitch.get()-4; i<=pitch.get()+12; i++) {
				if (i <= lastPitchAdded)
					continue;
				
				for (MidiPitch acceptable : nextChord) {
					if (modPos(acceptable.get() - i, 12) == 0) {
						found = true;
						break;
					}
				}
				if (found) {
					retval.add(new MidiPitch(i));
					lastPitchAdded = i;
					break;
				}
			}
			if (!found)
				throw new IllegalArgumentException("Could not find a pitch to voice lead to!");
		}

		return retval;
	}
	
	public static Chord voiceLeadPolyphony(Chord lastChord, ChordSpec nextChordSpec, int bassMin, int bassMax) {
//		if (true) // temporary
//			return voiceLead(lastChord, nextChordSpec, bassMin, bassMax);
		Chord retval = new Chord();
		
		Chord nextChord = nextChordSpec.build();
		List<MidiPitch> nonCommons = new ArrayList<>(); // collection of non-common pitches
		// assign common pitches:
		for (MidiPitch pitch : lastChord) {
			boolean found = false;
			for (MidiPitch acceptable : nextChord) {
				if (modPos(acceptable.halfStepsTo(pitch), 12) == 0) {
					retval.add(pitch.clone());
					found = true;
					break;
				}
			}
			if (!found)
				nonCommons.add(pitch);
		}
		int lastChordCenter = (int) lastChord.get().stream().mapToInt(MidiPitch::get).average().orElse(0);
		
		for (MidiPitch pitch : nonCommons) {
			// attempt to stay as close to the original pitch as possible
			// favor moving toward center
			int towardCenter = lastChordCenter > pitch.get() ? 1 : -1;
			int awayFromCenter = -towardCenter;
			boolean found = false;
			for (int i=1; i<=12; i++) {
				MidiPitch toward = pitch.above(i*towardCenter);
				if (!retval.contains(toward)) { // have it already?
					for (MidiPitch acceptable : nextChord) { // in next chord?
						if (modPos(acceptable.halfStepsTo(toward),12) == 0) {
							retval.add(toward);
							found = true;
							break;
						}
					}
					if (found) break;
				}
				MidiPitch away = pitch.above(i*awayFromCenter);
				if (!retval.contains(away)) { // have it already?
					for (MidiPitch acceptable : nextChord) { // in next chord?
						if (modPos(acceptable.halfStepsTo(away),12) == 0) {
							retval.add(away);
							found = true;
							break;
						}
					}
					if (found) break;
				}
			}
			if (!found)
				throw new IllegalArgumentException("Could not find a pitch to voice lead to!");
		}
		
		return retval;
	}
	
	/**
	 * @param startingPitch
	 * @param targetNote
	 * @return
	 */
	public static MidiPitch downFiveOrUpFour(MidiPitch startingPitch, Note targetNote) {
		int halfStepsTo = Key.toFlatNote(startingPitch).halfStepsTo(targetNote);
		if (halfStepsTo >= 6)
			halfStepsTo -= 12;
		if (halfStepsTo == 5 && roll(50))
			halfStepsTo -= 12;
		return startingPitch.above(halfStepsTo);
	}
	
}
