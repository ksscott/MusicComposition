package composing.writer;

import java.util.List;

import theory.Measure;
import theory.analysis.Phrase;

public interface MelodyWriter {
	
	public Phrase writeMelody(List<Measure> measures);

}
