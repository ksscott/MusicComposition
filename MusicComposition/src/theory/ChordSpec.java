package theory;

public class ChordSpec {
	
	private Note tonic;
	private Quality quality;
	private Degree degree;
	private boolean add2; // overlap with degree, change implementation?
	private boolean add4;
	private boolean add6;
	private int inversion;
	
	public ChordSpec(Note tonic) {
		this(tonic, Quality.MAJOR);
	}
	
	public ChordSpec(Note tonic, Quality quality) {
		this(tonic, quality, Degree.NONE);
	}
	
	public ChordSpec(Note tonic, Quality quality, Degree degree) {
		this(tonic, quality, degree, 0);
	}
	
	public ChordSpec(Note tonic, Quality quality, Degree degree, int inversion) {
		this.tonic = tonic;
		this.quality = quality;
		this.degree = degree;
		this.inversion = inversion;
	}
	
	// getters and setters:
	public Note getTonic() { return tonic.clone(); }
	public void setTonic(Note newTonic) { this.tonic = newTonic; } 
	public Quality getQuality() { return quality; }
	public void setQuality(Quality newQuality) { this.quality = newQuality; }
	public Degree getDegree() { return degree; }
	public void setDegree(Degree newDegree) { this.degree = newDegree; }
	public void setAdd2(boolean add2OrNot) { this.add2 = add2OrNot; }
	public void setAdd4(boolean add4OrNot) { this.add2 = add4OrNot; }
	public void setAdd6(boolean add6OrNot) { this.add2 = add6OrNot; }
	public int getInversion() { return inversion; }
	public void setInversion(int newInversion) { this.inversion = newInversion; }
	
	public Chord build() {
		return builder().build();
	}
	
	public ChordBuilder builder() {
		ChordBuilder builder = new ChordBuilder();
		builder.setRoot(tonic);
		switch (quality) {
			case DIMINISHED:
				builder.removePitch(5).addPitch(5, Accidental.FLAT);
				// fall through
			case MINOR:
				builder.removePitch(3).addPitch(3, Accidental.FLAT);
				break;
			case AUGMENTED:
				builder.removePitch(5).addPitch(5, Accidental.SHARP);
				// fall through
			default:
			case MAJOR:
				break;
		}
		switch(degree) {
		case THIRTEENTH:
			builder.addPitch(13);
			// fall through
		case ELEVENTH:
			builder.addPitch(11);
			// fall through
		case NINTH:
			builder.addPitch(9);
			// fall through
		case SEVENTH:
			builder.addPitch(7);
			// fall through
		default:
		case NONE:
			break;
		}
		
		builder.invert(inversion); // TODO invert further? e.g. do re mi sol -> mi sol do re
		// maybe don't invert further
		
//		System.out.println("Building spec: ");
//		for (MidiPitch pitch : builder.build().get())
//			System.out.println(pitch);
		
		return builder;
	}
	
	@Override
	public String toString() {
		String name = "";
		
		name += tonic; // TODO Note lacks a proper toString for valid reasons
		name += quality;
		name += degree;
		name += add2 ? "add2" : "";
		name += add4 ? "add4" : "";
		name += add6 ? "add6" : "";
		
		// TODO handle inversion
		
		return name;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (add2 ? 1231 : 1237);
		result = prime * result + (add4 ? 1231 : 1237);
		result = prime * result + (add6 ? 1231 : 1237);
		result = prime * result + ((degree == null) ? 0 : degree.hashCode());
		result = prime * result + inversion;
		result = prime * result + ((quality == null) ? 0 : quality.hashCode());
		result = prime * result + ((tonic == null) ? 0 : tonic.hashCode());
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
		ChordSpec other = (ChordSpec) obj;
		if (add2 != other.add2)
			return false;
		if (add4 != other.add4)
			return false;
		if (add6 != other.add6)
			return false;
		if (degree != other.degree)
			return false;
		if (inversion != other.inversion)
			return false;
		if (quality != other.quality)
			return false;
		if (tonic == null) {
			if (other.tonic != null)
				return false;
		} else if (!tonic.equals(other.tonic))
			return false;
		return true;
	}

	public enum Quality {
		
		AUGMENTED("+"), MAJOR("M"), MINOR("m"), DIMINISHED("°");
		
		private String notation;
		
		Quality(String notation) {
			this.notation = notation;
		}
		
		@Override
		public String toString() {
			return notation;
		}
	}
	
	public enum Degree {
		
		NONE(""), SEVENTH("7"), NINTH("9"), ELEVENTH("11"), THIRTEENTH("13");
		
		private String notation;
		
		Degree(String notation) {
			this.notation = notation;
		}
		
		@Override
		public String toString() {
			return notation;
		}
		
	}
	
}
