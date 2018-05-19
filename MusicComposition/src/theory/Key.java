package theory;

import static theory.Mode.*;

import java.util.Arrays;

public class Key {
	
	public static final Scale MAJOR = IONIAN;
	public static final Scale MINOR = AEOLIAN;
	public static final Scale HARMONIC_MINOR		 = new ScaleImpl(new int[]{2,1,2,2,1,3,1},	"Harmonic Minor");
	public static final Scale MELODIC_MINOR			 = new ScaleImpl(new int[]{2,1,2,2,2,2,1},	"Melodic Minor");
	public static final Scale STANDARD_PENTATONIC	 = new ScaleImpl(new int[]{2,2,3,2},		"Pentatonic");
	public static final Scale BLUES_SCALE			 = new ScaleImpl(new int[]{3,2,1,1,3},		"Blues");
	
	private Note tonic;
	private Scale scale;
	
	public Key(Note tonic, Scale scale) {
		this.tonic = tonic;
		this.scale = scale;
	}
	
	public Note getTonic() {
		return tonic;
	}
	
	public Scale getScale() {
		return new ScaleImpl(scale); // TODO return original?
	}
	
	public boolean contains(Note note) {
		return Arrays.asList(scale.intervalsFromRoot()).contains(tonic.halfStepsTo(note));
	}

}
