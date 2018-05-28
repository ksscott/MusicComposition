package theory;

public class ChordBuilder {

	private Chord chord;
	private Note root;
	private int octave;
	private boolean major;
	
	public ChordBuilder() {
		this.chord = new Chord();
		root = new Note(Letter.A);
		octave = 4;
		major = true;
	}
	
	public Chord build() {
		MidiPitch rootPitch = new MidiPitch(root, octave);
		chord.add(rootPitch);
		chord.add(rootPitch.above(major ? 4 : 3)); // third FIXME
		chord.add(rootPitch.above(7)); // fifth FIXME
		return new Chord(chord);
	}
	
	public ChordBuilder setRoot(Note root) {
		this.root = root;
		return this;
	}
	
	public ChordBuilder setOctave(int octave) {
		this.octave = octave;
		return this;
	}
	
	public ChordBuilder major() {
		major = true;
		return this;
	}
	
	public ChordBuilder minor() {
		major = false;
		return this;
	}
	
	public ChordBuilder addPitch(int scaleDegree, Accidental adjustment) {
		// TODO
		throw new RuntimeException("Unimplemented");
//		root.
//		MidiPitch pitch = new MidiPitch(scaleDegree);
//		chord.add(pitch);
//		return this;
	}
	
	public ChordBuilder removePitch(int scaleDegree, Accidental adjustment) {
		// TODO
		throw new RuntimeException("Unimplemented");
//		return this;
	}
	
}
