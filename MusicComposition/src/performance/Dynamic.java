package performance;

public class Dynamic {

	public static final Dynamic PIANISSIMO = new Dynamic(-3);
	public static final Dynamic PIANO = new Dynamic(-2);
	public static final Dynamic MEZZO_PIANO = new Dynamic(-1);
	public static final Dynamic MEZZO_FORTE = new Dynamic(0);
	public static final Dynamic FORTE = new Dynamic(1);
	public static final Dynamic FORTISSIMO = new Dynamic(2);
	
	private final int value;
	
	private Dynamic(int value) {
		this.value = value;
	}
	
	public int getValue() {
		return value;
	}
	
	public static Dynamic of(Dynamic other) {
		return new Dynamic(other.value);
	}
	
	public static Dynamic above(Dynamic other) {
		return new Dynamic(other.value + 1);
	}
	
	public static Dynamic below(Dynamic other) {
		return new Dynamic(other.value - 1);
	}
	
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
			if (value > 3) {
				retval = "ff";
				for (int i=4; i<=value; i++)
					retval += "f";
			} else { // assume value < -2
				retval = "pp";
				for (int i=-3; i>= value; i--)
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
