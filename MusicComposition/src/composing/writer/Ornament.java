package composing.writer;

import performance.Dynamic;
import performance.MidiNote;
import theory.Key;
import theory.MidiPitch;
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
		return trill(note, key, 1/4.0);
	}
	
	public static Phrase trill(MidiNote note, Key key, double durationOfFourTrills) {
		Phrase phrase = new Phrase();
				
		MidiPitch pitch = new MidiPitch(note.getPitch());
		Dynamic dynamic = note.getDynamic();
		
		requirePitchInKey(pitch, key);
		
		MidiPitch upperPitch = key.stepsAbove(1, pitch);
		double durationOfTwoNotes = durationOfFourTrills / 4.0;
		
		double d = note.getDuration();
		while (d > durationOfTwoNotes) {
			MidiNote upperNote = new MidiNote(pitch, durationOfTwoNotes / 2.0);
			MidiNote lowerNote = new MidiNote(upperPitch, durationOfTwoNotes / 2.0);
			upperNote.setDynamic(dynamic);
			lowerNote.setDynamic(Dynamic.below(dynamic));
			phrase.add(upperNote);
			phrase.add(lowerNote);
			d -= durationOfTwoNotes;
		}
		phrase.add(new MidiNote(pitch, d)); // artistic choice. unsure how to resolve in the remaining time
		
		return phrase;
	}
	
	/**
	 * Defaults to half the duration of the adorned note.
	 * @see #upperMordent(MidiNote, Key, double)
	 */
	public static Phrase upperMordent(MidiNote note, Key key) {
		return upperMordent(note, key, note.getDuration()/2.0);
	}
	
	public static Phrase upperMordent(MidiNote note, Key key, double mordentDuration) {
		Phrase phrase = new Phrase();
		
		if (mordentDuration >= note.getDuration() || mordentDuration <= 0)
			throw new IllegalArgumentException("Mordent duration must be greater than zero but less than the length of the adorned note.");
		
		MidiPitch pitch = new MidiPitch(note.getPitch());
		Dynamic dynamic = note.getDynamic();
		
		requirePitchInKey(pitch, key);
		
		MidiPitch upperPitch = key.stepsAbove(1, pitch);
		
		MidiNote firstNote = new MidiNote(note.getPitch(), mordentDuration/2.0);
		MidiNote secondNote = new MidiNote(upperPitch, mordentDuration/2.0);
		MidiNote lastNote = new MidiNote(note.getPitch(), note.getDuration() - mordentDuration);
		firstNote.setDynamic(dynamic);
		secondNote.setDynamic(Dynamic.below(dynamic));
		lastNote.setDynamic(Dynamic.below(dynamic));
		phrase.add(firstNote);
		phrase.add(secondNote);
		phrase.add(lastNote);
		
		return phrase;
	}
	
	/**
	 * Defaults to half the duration of the adorned note.
	 * @see #lowerMordent(MidiNote, Key, double)
	 */
	public static Phrase lowerMordent(MidiNote note, Key key) {
		return lowerMordent(note, key, note.getDuration()/2.0);
	}
	
	public static Phrase lowerMordent(MidiNote note, Key key, double mordentDuration) {
		Phrase phrase = new Phrase();
		
		if (mordentDuration >= note.getDuration() || mordentDuration <= 0)
			throw new IllegalArgumentException("Mordent duration must be greater than zero but less than the length of the adorned note.");
		
		MidiPitch pitch = new MidiPitch(note.getPitch());
		Dynamic dynamic = note.getDynamic();
		
		requirePitchInKey(pitch, key);
		
		MidiPitch lowerPitch = key.stepsAbove(-1, pitch);
		
		MidiNote firstNote = new MidiNote(note.getPitch(), mordentDuration/2.0);
		MidiNote secondNote = new MidiNote(lowerPitch, mordentDuration/2.0);
		MidiNote lastNote = new MidiNote(note.getPitch(), note.getDuration() - mordentDuration);
		firstNote.setDynamic(dynamic);
		secondNote.setDynamic(Dynamic.below(dynamic));
		lastNote.setDynamic(Dynamic.below(dynamic));
		phrase.add(firstNote);
		phrase.add(secondNote);
		phrase.add(lastNote);
		
		return phrase;
	}
	
	/**
	 * Defaults to half the duration of the adorned note.
	 * @see #turn(MidiNote, Key, double)
	 */
	public static Phrase turn(MidiNote note, Key key) {
		return turn(note, key, note.getDuration() / 2.0);
	}
	
	public static Phrase turn(MidiNote note, Key key, double turnDuration) {
		Phrase phrase = new Phrase();
		
		if (turnDuration >= note.getDuration() || turnDuration <= 0)
			throw new IllegalArgumentException("Turn duration must be greater than zero but less than the length of the adorned note.");
		int notePitch = note.getPitch();
		
		MidiPitch pitch = new MidiPitch(notePitch);
		Dynamic dynamic = note.getDynamic();
		
		requirePitchInKey(pitch, key);
		
		MidiPitch upperPitch = key.stepsAbove(1, pitch);
		MidiPitch lowerPitch = key.stepsAbove(-1, pitch);
		
		MidiNote firstNote = new MidiNote(notePitch, turnDuration/2.0);
		MidiNote secondNote = new MidiNote(upperPitch, turnDuration/6.0);
		MidiNote thirdNote = new MidiNote(notePitch, turnDuration/6.0);
		MidiNote fourthNote = new MidiNote(lowerPitch, turnDuration/6.0);
		MidiNote lastNote = new MidiNote(notePitch, note.getDuration() - turnDuration);
		firstNote.setDynamic(dynamic);
		secondNote.setDynamic(Dynamic.below(dynamic));
		thirdNote.setDynamic(Dynamic.below(dynamic));
		fourthNote.setDynamic(Dynamic.below(dynamic));
		lastNote.setDynamic(Dynamic.below(dynamic));
		phrase.add(firstNote);
		phrase.add(secondNote);
		phrase.add(thirdNote);
		phrase.add(fourthNote);
		phrase.add(lastNote);
		
		return phrase;
	}
	
	/**
	 * Defaults to half the duration of the adorned note.
	 * @see #appoggiatura(MidiNote, Key, double)
	 */
	public static Phrase appoggiatura(MidiNote note, Key key) {
		return appoggiatura(note, key, note.getDuration()/2.0);
	}
	
	/**
	 * @param note the note to be adorned
	 * @param key Key of the piece is required to know how high to raise the appoggiatura note
	 * @param appoDuration requested duration of the appoggiatura note
	 * @return
	 */
	public static Phrase appoggiatura(MidiNote note, Key key, double appoDuration) { // TODO maybe revisit this
		Phrase phrase = new Phrase();
		
		if (appoDuration >= note.getDuration() || appoDuration <= 0)
			throw new IllegalArgumentException("Appoggiatura duration must be greater than zero but less than the length of the adorned note.");
		
		MidiPitch pitch = new MidiPitch(note.getPitch());
		Dynamic dynamic = note.getDynamic();
		
		requirePitchInKey(pitch, key);
		
		MidiPitch appoPitch = key.stepsAbove(1, pitch);
		
		MidiNote upperNote = new MidiNote(appoPitch, appoDuration);
		MidiNote lowerNote = new MidiNote(note.getPitch(), note.getDuration() - appoDuration);
		upperNote.setDynamic(dynamic);
		lowerNote.setDynamic(Dynamic.below(dynamic));
		phrase.add(upperNote);
		phrase.add(lowerNote);
		
		return phrase;
	}
	
	private static void requirePitchInKey(MidiPitch pitch, Key key) {
		if (!key.contains(pitch))
			throw new IllegalArgumentException("Only notes in the key are currently supported.");
	}
	
}
