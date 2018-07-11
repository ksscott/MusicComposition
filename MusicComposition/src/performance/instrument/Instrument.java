package performance.instrument;

import java.util.ArrayList;
import java.util.List;

import main.BeadsTimbre;
import performance.Timbre;

public class Instrument implements Comparable<Instrument> { // TODO decide on an architecture surrounding instruments
	
	public static final Instrument SOPRANO_VOICE = new Instrument ("Soprano", BeadsTimbre.getVoiceTimbre());
	public static final Instrument ALTO_VOICE = new Instrument ("Alto", BeadsTimbre.getVoiceTimbre());
	public static final Instrument TENOR_VOICE = new Instrument ("Tenor", BeadsTimbre.getVoiceTimbre());
	public static final Instrument BASS_VOICE = new Instrument ("Bass", BeadsTimbre.getVoiceTimbre());
	
	// hint for the uninitiated: "solo" isn't really an instrument
	public static final Instrument SOLO = new Instrument("Solo", BeadsTimbre.getInstrumentTimbre());
	public static final Instrument PIANO = new Instrument("Piano", BeadsTimbre.getSineTimbre());
	
	public static final Instrument TRUMPET = new Instrument("Trumpet", BeadsTimbre.getSineTimbre());
	public static final Instrument BASS = new Instrument("Bass", BeadsTimbre.getInstrumentTimbre());
	
	private static List<Instrument> knownInstruments;
	
	private String name;
	private Timbre timbre;
	
	public Instrument(String name, Timbre timbre) {
		knownInstruments = new ArrayList<>();
		knownInstruments.add(SOPRANO_VOICE);
		knownInstruments.add(ALTO_VOICE);
		knownInstruments.add(TENOR_VOICE);
		knownInstruments.add(BASS_VOICE);
		knownInstruments.add(SOLO);
		knownInstruments.add(PIANO);
		knownInstruments.add(TRUMPET);
		knownInstruments.add(BASS);
		this.name = name;
		this.timbre = timbre;
	}
	
	public Timbre getTimbre() {
		return timbre;
	}

	@Override
	public int compareTo(Instrument o) {
		if (knownInstruments.contains(this) && knownInstruments.contains(o))
			return knownInstruments.indexOf(this) - knownInstruments.indexOf(o);
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
		result = prime * result + ((timbre == null) ? 0 : timbre.hashCode());
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
		if (timbre == null) {
			if (other.timbre != null)
				return false;
		} else if (!timbre.equals(other.timbre))
			return false;
		return true;
	}


}
