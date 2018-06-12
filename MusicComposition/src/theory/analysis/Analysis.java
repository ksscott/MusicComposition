package theory.analysis;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Analysis {

	private Set<Phrase> motifs;
	private List<Section> sections;
	
	
	public Analysis() {
		this.motifs = new HashSet<>();
		this.sections = new ArrayList<>();
	}
	
	public Set<Phrase> getMotifs() {
		return motifs;
	}
	
	public List<Section> getSections() {
		return sections;
	}
	
	public void addSection(Section section) {
		sections.add(section);
	}
	
}
