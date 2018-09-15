package composing.writer;

import java.util.List;

import performance.Dynamic;
import performance.MidiNote;
import theory.Key;
import theory.MidiPitch;
import theory.NoteDuration;
import theory.analysis.Phrase;

public class Ornament {
	
	// Ornaments taken from Wikipedia: "Ornament (music)"

	private Ornament(){} // util class
	
//	TRILL,
//	UPPER_MORDENT,
//	LOWER_MORDENT,
//	TURN,
//	APPOGGIATURA,
//	ACCIACCATURA, // TODO
//	GLISSANDO, // TODO
//	SCHLEIFER, // TODO
	;
	
	/**
	 * Defaults to half the duration of the adorned note.
	 * @see #trill(MidiNote, Key, double)
	 */
	public static Phrase trill(MidiNote note, Key key) {
		return trill(note, key, NoteDuration.quarter());
	}
	
	public static Phrase trill(MidiNote note, Key key, NoteDuration durationOfFourTrills) {
		Phrase phrase = new Phrase();
				
		MidiPitch pitch = new MidiPitch(note.getPitch());
		Dynamic dynamic = note.getDynamic();
		
		requirePitchInKey(pitch, key);
		
		MidiPitch upperPitch = key.stepsAbove(1, pitch);
		NoteDuration durationOfTwoNotes = durationOfFourTrills.expand(-2);
		
		double d = note.duration();
//		while (d > durationOfTwoNotes) {
//			MidiNote lowerNote = new MidiNote(pitch, durationOfTwoNotes.halved());
//			MidiNote upperNote = new MidiNote(upperPitch, durationOfTwoNotes.halved());
//			lowerNote.setDynamic(dynamic);
//			upperNote.setDynamic(Dynamic.below(dynamic));
//			phrase.add(lowerNote);
//			phrase.add(upperNote);
//			d -= durationOfTwoNotes;
//		}
//		phrase.add(new MidiNote(pitch, d)); // artistic choice. unsure how to resolve in the remaining time
		
		// HACK:
		double trillDuration = 0.0;
		while(trillDuration < note.duration()) {
			MidiNote lowerNote = new MidiNote(pitch, durationOfTwoNotes.halved());
			MidiNote upperNote = new MidiNote(upperPitch, durationOfTwoNotes.halved());
			lowerNote.setDynamic(dynamic);
			upperNote.setDynamic(Dynamic.below(dynamic));
			phrase.add(lowerNote);
			phrase.add(upperNote);
			trillDuration += durationOfTwoNotes.duration();
		}
		
		return phrase;
	}
	
	/**
	 * Defaults to half the duration of the adorned note.
	 * @see #upperMordent(MidiNote, Key, double)
	 */
	public static Phrase upperMordent(MidiNote note, Key key) {
		return upperMordent(note, key, note.getDuration().halved());
	}
	
	public static Phrase upperMordent(MidiNote note, Key key, NoteDuration mordentDuration) {
		Phrase phrase = new Phrase();
		
		if (mordentDuration.duration() >= note.duration() || mordentDuration.duration() <= 0)
			throw new IllegalArgumentException("Mordent duration must be greater than zero but less than the length of the adorned note.");
		
		MidiPitch pitch = new MidiPitch(note.getPitch());
		Dynamic dynamic = note.getDynamic();
		
		requirePitchInKey(pitch, key);
		
		MidiPitch upperPitch = key.stepsAbove(1, pitch);
		
		MidiNote firstNote = new MidiNote(pitch, mordentDuration.halved());
		firstNote.setDynamic(dynamic);
		phrase.add(firstNote);
		
		MidiNote secondNote = new MidiNote(upperPitch, mordentDuration.halved());
		secondNote.setDynamic(Dynamic.below(dynamic));
		phrase.add(secondNote);
		
		phrase.add(subtract(note, mordentDuration));
		
		return phrase;
	}
	
	/**
	 * Defaults to half the duration of the adorned note.
	 * @see #lowerMordent(MidiNote, Key, double)
	 */
	public static Phrase lowerMordent(MidiNote note, Key key) {
		return lowerMordent(note, key, note.getDuration().halved());
	}
	
	public static Phrase lowerMordent(MidiNote note, Key key, NoteDuration mordentDuration) {
		Phrase phrase = new Phrase();
		
		if (mordentDuration.duration() >= note.duration() || mordentDuration.duration() <= 0)
			throw new IllegalArgumentException("Mordent duration must be greater than zero but less than the length of the adorned note.");
		
		MidiPitch pitch = new MidiPitch(note.getPitch());
		Dynamic dynamic = note.getDynamic();
		
		requirePitchInKey(pitch, key);
		
		MidiPitch lowerPitch = key.stepsAbove(-1, pitch);
		
		MidiNote firstNote = new MidiNote(pitch, mordentDuration.halved());
		firstNote.setDynamic(dynamic);
		phrase.add(firstNote);
		
		MidiNote secondNote = new MidiNote(lowerPitch, mordentDuration.halved());
		secondNote.setDynamic(Dynamic.below(dynamic));
		phrase.add(secondNote);
		
		phrase.add(subtract(note, mordentDuration));
		
		return phrase;
	}
	
	/**
	 * Defaults to half the duration of the adorned note.
	 * @see #turn(MidiNote, Key, double)
	 */
	public static Phrase turn(MidiNote note, Key key) {
		return turn(note, key, note.getDuration().halved());
	}
	
	public static Phrase turn(MidiNote note, Key key, NoteDuration turnDuration) {
		Phrase phrase = new Phrase();
		
		if (turnDuration.duration() >= note.duration() || turnDuration.duration() <= 0)
			throw new IllegalArgumentException("Turn duration must be greater than zero but less than the length of the adorned note.");
		int notePitch = note.getPitch();
		
		MidiPitch pitch = new MidiPitch(notePitch);
		Dynamic dynamic = note.getDynamic();
		
		requirePitchInKey(pitch, key);
		
		MidiPitch upperPitch = key.stepsAbove(1, pitch);
		MidiPitch lowerPitch = key.stepsAbove(-1, pitch);
		
		MidiNote firstNote = new MidiNote(notePitch, turnDuration.halved());
		firstNote.setDynamic(dynamic);
		phrase.add(firstNote);
		
		MidiNote secondNote = new MidiNote(upperPitch, turnDuration.expand(-2).toTriplet(true));
		secondNote.setDynamic(Dynamic.below(dynamic));
		phrase.add(secondNote);
		
		MidiNote thirdNote = new MidiNote(notePitch, turnDuration.expand(-2).toTriplet(true));
		thirdNote.setDynamic(Dynamic.below(dynamic));
		phrase.add(thirdNote);
		
		MidiNote fourthNote = new MidiNote(lowerPitch, turnDuration.expand(-2).toTriplet(true));
		fourthNote.setDynamic(Dynamic.below(dynamic));
		phrase.add(fourthNote);
		
		phrase.add(subtract(note, turnDuration));
		
		return phrase;
	}
	
	/**
	 * Defaults to half the duration of the adorned note.
	 * @see #appoggiatura(MidiNote, Key, double)
	 */
	public static Phrase appoggiatura(MidiNote note, Key key) {
		return appoggiatura(note, key, note.getDuration().halved());
	}
	
	/**
	 * @param note the note to be adorned
	 * @param key Key of the piece is required to know how high to raise the appoggiatura note
	 * @param appoDuration requested duration of the appoggiatura note
	 * @return
	 */
	public static Phrase appoggiatura(MidiNote note, Key key, NoteDuration appoDuration) { // TODO maybe revisit this
		Phrase phrase = new Phrase();
		
		if (appoDuration.duration() >= note.duration() || appoDuration.duration() <= 0)
			throw new IllegalArgumentException("Appoggiatura duration must be greater than zero but less than the length of the adorned note.");
		
		MidiPitch pitch = new MidiPitch(note.getPitch());
		Dynamic dynamic = note.getDynamic();
		
		requirePitchInKey(pitch, key);
		
		MidiPitch appoPitch = key.stepsAbove(1, pitch);
		
		MidiNote upperNote = new MidiNote(appoPitch, appoDuration);
		upperNote.setDynamic(dynamic);
		phrase.add(upperNote);
		
		phrase.add(subtract(note, appoDuration));
		
		return phrase;
	}
	
	private static void requirePitchInKey(MidiPitch pitch, Key key) {
		if (!key.contains(pitch))
			throw new IllegalArgumentException("Only notes in the key are currently supported.");
	}
	
	/**
	 * Automatically reduces Dynamic of remaining note by one level
	 * 
	 * @param minuend original, unornamented note
	 * @param subtrahend duration of the ornament
	 * @return A phrase of tied notes representing the remaining time after an ornament
	 */
	private static Phrase subtract(MidiNote minuend, NoteDuration subtrahend) {
		Phrase lastNotePhrase = new Phrase();
		Dynamic lastNoteDynamic = Dynamic.below(minuend.getDynamic());
		List<NoteDuration> difference = NoteDuration.subtract(minuend.getDuration(), subtrahend);
		MidiNote last = null;
		for (NoteDuration duration : difference) {
			MidiNote lastPhraseNote = new MidiNote(minuend.getPitch(), duration);
			lastPhraseNote.setDynamic(lastNoteDynamic);
			lastNotePhrase.add(lastPhraseNote);
			if (!(last == null))
				MidiNote.tieOver(last, lastPhraseNote);
			last = lastPhraseNote;
		}
		return lastNotePhrase;
	}
	
}
