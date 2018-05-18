package theory;

import static theory.Mode.*;

public class Key {
	
	public static final Scale MAJOR = IONIAN;
	public static final Scale MINOR = AEOLIAN;
	public static final Scale HARMONIC_MINOR		 = new ScaleImpl(new int[]{2,1,2,2,1,3,1});
	public static final Scale MELODIC_MINOR			 = new ScaleImpl(new int[]{2,1,2,2,2,2,1});
	public static final Scale STANDARD_PENTATONIC	 = new ScaleImpl(new int[]{2,2,3,2});
	public static final Scale BLUES_SCALE			 = new ScaleImpl(new int[]{3,2,1,1,3});
	
	private Note tonic;
	private Scale scale;
	
	public Key(Note tonic, Scale scale) {
		this.tonic = tonic;
		this.scale = scale;
	}
	
	public boolean contains(Note note) {
		return false; // FIXME
	}

}
