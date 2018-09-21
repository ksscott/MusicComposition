package performance;

import java.util.HashMap;
import java.util.Map;

public class Dynamic implements Cloneable {

	public static final Dynamic PIANISSIMO = new Dynamic(-3);
	public static final Dynamic PIANO = new Dynamic(-2);
	public static final Dynamic MEZZO_PIANO = new Dynamic(-1);
	public static final Dynamic MEZZO_FORTE = new Dynamic(0);
	public static final Dynamic FORTE = new Dynamic(1);
	public static final Dynamic FORTISSIMO = new Dynamic(2);
	
	public static final Map<Dynamic,Float> volumeCache = new HashMap<>();
	
	private final int value;
	
	private Dynamic(int value) {
		this.value = value;
	}
	
	/** @return value between 0 (silent) and 1 (maximum) */
	public float volume() {
		if (volumeCache.containsKey(this))
			return volumeCache.get(this);
		double y = (Math.atan((value-1)*.5) / Math.PI) + 0.5;
		float volume = (float) (1-Math.pow(1-y, 2));
		volumeCache.put(this, volume);
		return volume;
	}
	
	/**
	 * @param other dynamic to compare to this one
	 * @return number of dynamic levels above this dynamic the given dynamic is 
	 * e.g. MEZZO_FORTE.levelsTo(MEZZO_PIANO) = -1
	 */
	public int levelsTo(Dynamic other) {
		return other.value - this.value;
	}
	
	/** @return the dynamic immediately louder than this one */
	public Dynamic up() {
		return up(1);
	}
	
	public Dynamic up(int levels) {
		return new Dynamic(value + levels);
	}
	
	/** @return the dynamic immediately quieter than this one */
	public Dynamic down() {
		return down(1);
	}
	
	public Dynamic down(int levels) {
		return new Dynamic(value - levels);
	}
	
	/** e.g. "mf" or "pp" */
	public String notation() {
		String retval = "<dynamic>";

		if (equals(PIANISSIMO))
			retval = "pp";
		else if (equals(PIANO))
			retval = "p";
		else if (equals(MEZZO_PIANO))
			retval = "mp";
		else if (equals(MEZZO_FORTE))
			retval = "mf";
		else if (equals(FORTE))
			retval = "f";
		else if (equals(FORTISSIMO))
			retval = "ff";
		else {
			if (value > FORTISSIMO.value) {
				retval = "ff";
				for (int i=FORTISSIMO.value+1; i<=value; i++)
					retval += "f";
			} else { // assume value < -2
				retval = "pp";
				for (int i=PIANISSIMO.value-1; i>= value; i--)
					retval += "p";
			}
		}

		return retval;
	}
	
	@Override
	public String toString() {
		String retval = "<dynamic>";
		
		if (equals(PIANISSIMO))
			retval = "Pianissimo";
		else if (equals(PIANO))
			retval = "Piano";
		else if (equals(MEZZO_PIANO))
			retval = "Mezzo Piano";
		else if (equals(MEZZO_FORTE))
			retval = "Mezzo Forte";
		else if (equals(FORTE))
			retval = "Forte";
		else if (equals(FORTISSIMO))
			retval = "Fortissimo";
		else {
			if (value > FORTISSIMO.value) {
				retval = "Fortiss";
				for (int i=FORTISSIMO.value+1; i<=value; i++)
					retval += "iss";
				retval += "imo";
			} else { // assume value < PIANISSIMO.value
				retval = "Pianiss";
				for (int i=PIANISSIMO.value-1; i>= value; i--)
					retval += "iss";
				retval += "imo";
			}
		}
		
		return retval;
	}
	
	@Override
	public Dynamic clone() {
		return new Dynamic(value);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + value;
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
		Dynamic other = (Dynamic) obj;
		if (value != other.value)
			return false;
		return true;
	}
	
}
