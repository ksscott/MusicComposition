package theory.analysis;

import java.util.HashSet;
import java.util.Set;

public class Analysis {

	private Set<Phrase> motifs;
	
	
	public Analysis() {
		this.motifs = new HashSet<>();
	}
	
	public Set<Phrase> getMotifs() {
		return motifs;
	}
	
}
