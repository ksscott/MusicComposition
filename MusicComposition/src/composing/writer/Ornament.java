package composing.writer;

import theory.Key;
import theory.MidiNote;
import theory.MidiPitch;
import theory.analysis.Phrase;

public class Ornament {

	private Ornament(){} // util class
	
//	TRILL, // TODO
//	UPPER_MORDENT,
//	LOWER_MORDENT,
//	TURN,
//	APPOGGIATURA,
//	ACCIACCATURA, // TODO
//	GLISSANDO, // TODO
//	SCHLEIFER, // TODO
	;
	
	public static Phrase upperMordent(MidiNote note, Key key) {
		return upperMordent(note, key, note.getDuration()/2.0);
	}
	
	public static Phrase upperMordent(MidiNote note, Key key, double mordentDuration) {
		Phrase phrase = new Phrase();
		
		if (mordentDuration >= note.getDuration() || mordentDuration <= 0)
			throw new IllegalArgumentException("Mordent duration must be greater than zero but less than the length of the adorned note.");
		MidiPitch pitch = new MidiPitch(note.getPitch());
		requirePitchInKey(pitch, key);
		
		MidiPitch upperPitch = key.stepsAbove(1, pitch); // FIXME
		
		phrase.add(new MidiNote(note.getPitch(), mordentDuration/2.0));
		phrase.add(new MidiNote(upperPitch, mordentDuration/2.0));
		phrase.add(new MidiNote(note.getPitch(), note.getDuration() - mordentDuration));
		
		return phrase;
	}
	
	public static Phrase lowerMordent(MidiNote note, Key key) {
		return lowerMordent(note, key, note.getDuration()/2.0);
	}
	
	public static Phrase lowerMordent(MidiNote note, Key key, double mordentDuration) {
		Phrase phrase = new Phrase();
		
		if (mordentDuration >= note.getDuration() || mordentDuration <= 0)
			throw new IllegalArgumentException("Mordent duration must be greater than zero but less than the length of the adorned note.");
		MidiPitch pitch = new MidiPitch(note.getPitch());
		requirePitchInKey(pitch, key);
		
		MidiPitch lowerPitch = key.stepsAbove(-1, pitch); // FIXME
		
		phrase.add(new MidiNote(note.getPitch(), mordentDuration/2.0));
		phrase.add(new MidiNote(lowerPitch, mordentDuration/2.0));
		phrase.add(new MidiNote(note.getPitch(), note.getDuration() - mordentDuration));
		
		return phrase;
	}
	
//	public static Phrase turn(MidiNote note, Key key, double beatValue) {
//		Phrase phrase = new Phrase();
//		MidiPitch pitch = new MidiPitch(note.getPitch());
//		requirePitchInKey(pitch, key);
//		return phrase;
//	}
	
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
		requirePitchInKey(pitch, key);
		
		MidiPitch appoPitch = key.stepsAbove(1, pitch); // FIXME
		
		phrase.add(new MidiNote(appoPitch, appoDuration));
		phrase.add(new MidiNote(note.getPitch(), note.getDuration() - appoDuration));
		
		return phrase;
	}
	
	private static void requirePitchInKey(MidiPitch pitch, Key key) {
		if (!key.contains(pitch))
			throw new IllegalArgumentException("Only notes in the key are currently supported.");
	}
	
}
