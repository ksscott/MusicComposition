package theory.analysis;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Analysis {

	private Set<Phrase> motifs;
	private List<PieceSection> sections;
	
	public Analysis() {
		this.motifs = new HashSet<>();
		this.sections = new ArrayList<>();
	}
	
	public Set<Phrase> getMotifs() {
		return motifs;
	}
	
	public List<Section> getSections() {
		return sections.stream().sorted().map(PieceSection::get).collect(Collectors.toList());
	}
	
	public void addSection(Section section) {
		addSection(lastEndOfSection() + 1, section);
	}
	
	public void addSection(int firstMeasureNumber, Section section) {
		sections.add(new PieceSection(firstMeasureNumber, section));
	}
	
	int firstStartOfSection() {
		return sections.stream().mapToInt(PieceSection::firstMeasureNumber).max().orElse(0);
	}
	
	int lastEndOfSection() {
		return sections.stream().mapToInt(PieceSection::lastMeasureNumber).max().orElse(0);
	}
	
	private class PieceSection implements Comparable<PieceSection> {
		
		/** measure number of the first measure of this section */
		private int start;
		private Section section;
		
		private PieceSection(int startingMeasureNumber, Section mapped) {
			this.section = mapped;
		}
		
		public Section get() {
			return section;
		}
		
		public int firstMeasureNumber() {
			return start;
		}
		
		public int lastMeasureNumber() {
			return start + section.size() - 1;
		}

		@Override
		public int compareTo(PieceSection o) {
			int thisFirst = firstMeasureNumber();
			int thisLast = lastMeasureNumber();
			int otherFirst = o.firstMeasureNumber();
			int otherLast = o.lastMeasureNumber();
			if (thisFirst == otherFirst)
				return new Integer(thisLast).compareTo(new Integer(otherLast));
			return new Integer(thisFirst).compareTo(new Integer(otherFirst));
		}
		
	}
}
