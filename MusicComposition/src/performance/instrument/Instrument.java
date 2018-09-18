package performance.instrument;

import java.util.ArrayList;
import java.util.List;

public class Instrument implements Comparable<Instrument> {
	
	public static final Instrument SOPRANO_VOICE = new Instrument ("Soprano");
	public static final Instrument ALTO_VOICE = new Instrument ("Alto");
	public static final Instrument TENOR_VOICE = new Instrument ("Tenor");
	public static final Instrument BASS_VOICE = new Instrument ("Bass");
	
	public static final Instrument VIOLIN = new Instrument("Violin");
	public static final Instrument VIOLA = new Instrument("Viola");
	public static final Instrument CELLO = new Instrument("Cello");
	public static final Instrument CONTRABASS = new Instrument("Contrabass");
	public static final Instrument PIZZICATO_STRINGS = new Instrument("Pizzicato Strings");
	public static final Instrument HARP = new Instrument("Harp");
	
	public static final Instrument FLUTE = new Instrument("Flute");
	
	public static final Instrument TRUMPET = new Instrument("Trumpet");
	public static final Instrument TROMBONE = new Instrument("Trombone");
	public static final Instrument TUBA = new Instrument("Tuba");
	public static final Instrument MUTED_TRUMPET = new Instrument("Muted Trumpet");
	public static final Instrument FRENCH_HORN = new Instrument("French Horn");
	
	public static final Instrument BARITONE_SAXOPHONE = new Instrument("Baritone Saxophone");
	public static final Instrument OBOE = new Instrument("Oboe");
	public static final Instrument BASSOON = new Instrument("Bassoon");
	public static final Instrument CLARINET = new Instrument("Clarinet");
	
	public static final Instrument NYLON_GUITAR = new Instrument("Nylon Guitar");
	public static final Instrument ACOUSTIC_BASS = new Instrument("Bass");
	
	public static final Instrument CELESTA = new Instrument("Celesta");
	public static final Instrument XYLOPHONE = new Instrument("Xylophone");
	public static final Instrument TIMPANI = new Instrument("Timpani");
	
	public static final Instrument SITAR = new Instrument("Sitar");
	public static final Instrument BAGPIPE = new Instrument("Bagpipe");
	
	public static final Instrument STEEL_DRUMS = new Instrument("Steel Drums");
	
	public static final Instrument PIANO = new Instrument("Piano");
	public static final Instrument HARPSICHORD = new Instrument("Harpsichord");
	
	private static List<Instrument> knownInstruments;
	
	private String name;
	
	public Instrument(String name) {
		knownInstruments = new ArrayList<>();
		knownInstruments.add(SOPRANO_VOICE);
		knownInstruments.add(ALTO_VOICE);
		knownInstruments.add(TENOR_VOICE);
		knownInstruments.add(BASS_VOICE);
		knownInstruments.add(VIOLIN);
		knownInstruments.add(VIOLA);
		knownInstruments.add(CELLO);
		knownInstruments.add(PIZZICATO_STRINGS);
		knownInstruments.add(HARP);
		knownInstruments.add(FLUTE);
		knownInstruments.add(TRUMPET);
		knownInstruments.add(MUTED_TRUMPET);
		knownInstruments.add(FRENCH_HORN);
		knownInstruments.add(BARITONE_SAXOPHONE);
		knownInstruments.add(NYLON_GUITAR);
		knownInstruments.add(ACOUSTIC_BASS);
		knownInstruments.add(XYLOPHONE);
		knownInstruments.add(SITAR);
		knownInstruments.add(BAGPIPE);
		knownInstruments.add(STEEL_DRUMS);
		knownInstruments.add(PIANO);
		this.name = name;
	}
	
	@Override
	public int compareTo(Instrument o) {
		boolean thisKnown = knownInstruments.contains(this);
		boolean thatKnown = knownInstruments.contains(o);
		
		// default ordering of known instruments
		if (thisKnown && thatKnown)
			return knownInstruments.indexOf(this) - knownInstruments.indexOf(o);
		
		// all known instruments come first
		else if (thisKnown)
			return -1;
		else if (thatKnown)
			return 1;
		
		// sort the rest alphabetically
		return this.name.compareTo(o.name);
	}

	@Override
	public String toString() {
		return "Instrument[" + name + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Instrument other = (Instrument) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}


}
