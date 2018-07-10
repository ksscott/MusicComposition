package performance.instrument;

import performance.Timbre;

public class Instrument { // TODO decide on an architecture surrounding instruments
	
	private String name;
	private Timbre timbre;
	
	public Instrument(String name, Timbre timbre) {
		this.name = name;
		this.timbre = timbre;
	}
	
	public Timbre getTimbre() {
		return timbre;
	}

	@Override
	public String toString() {
		return "Instrument [name=" + name + "]";
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
