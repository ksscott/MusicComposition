package instrument;

public class Instrument {
	
	private String name;
	
	public Instrument(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "Instrument [name=" + name + "]";
	}

}
