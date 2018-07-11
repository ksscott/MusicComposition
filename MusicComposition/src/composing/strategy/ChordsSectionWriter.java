package composing.strategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import composing.IncompleteComposition;
import theory.ChordProgressions;
import theory.ChordSpec;
import theory.Key;
import theory.Measure;
import theory.ChordProgressions.ChordProgression;
import theory.ChordProgressions.KeyChange;
import theory.ChordProgressions.KeyChordProgression;
import theory.analysis.Analysis;
import theory.analysis.Section;

abstract class ChordsSectionWriter implements ComposingStrategy {
		
		protected Key key;
		protected ChordSpec firstChord;
		
		public ChordsSectionWriter(Key key) {
			this.key = key;
			this.firstChord = key.chordSpec(1);
		}
		
		protected abstract int getNextSectionSize(IncompleteComposition composition);
		
		protected abstract ChordProgression nextSectionProgression(Analysis analysis);
		
		/**
		 * Allows subclasses to extend this method and write additional music on top of the base line. e.g.
		 * <p>
		 * Note: Assumes one chord per bar
		 * 
		 * @param lastMeasure immediately before the measure to be composed, used for voice leading
		 * @param nextChordSpec chord for writing background chords
		 * @return the next Measure, composed
		 */
		protected abstract Measure composeBar(Measure lastMeasure, ChordSpec nextChordSpec);
		
		/**
		 * Called when the measures composed fill all sections, and perhaps spill over (but not likely)
		 * 
		 * @param composition the composition currently being composed
		 */
		protected abstract void onSectionsFilled(IncompleteComposition composition);
		
		@Override
		public boolean iterate(IncompleteComposition composition) {
			int sectionSize = getNextSectionSize(composition);
			
			final Queue<Measure> future = composition.getFuture();
			if (future.size() > 2*sectionSize)
				return true; // no need to iterate now
			Analysis analysis = composition.getAnalysis();
			int measuresWithoutSection = composition.size() - analysis.lastEndOfSection();
			int missingMeasures = -measuresWithoutSection;
//			System.out.println("Missing measures: " + missingMeasures);
			
			if (missingMeasures <= 0 && future.size() <= sectionSize) {
				// sections are full, write a new one
				ChordsSection nextSection = new ChordsSection(sectionSize);
				ChordSpec precedingChord;
				if (measuresWithoutSection > 0) {
					// measures at end of piece without a section
					List<Measure> rogueMeasures = composition.getMeasures(
							composition.size()-measuresWithoutSection+1, composition.size());
					if (rogueMeasures.size() != 1)
						throw new IllegalStateException("Somehow have the wrong number of rogue measures...");
					nextSection.putKey(1, key); // relies on first-measure behavior of generateFirstMeasure() -> composeBar()
					nextSection.putChord(1, firstChord); // making some assumptions here
					precedingChord = null; // can pass null because nextSection is not empty
				} else {
					// get last chord
					List<Section> sections = analysis.getSections();
					ChordsSection section = (ChordsSection) sections.get(sections.size() - 1);
					precedingChord = section.getChord(section.size());
				}
				fillSection(nextSection, nextSectionProgression(analysis), precedingChord);
				analysis.addSection(nextSection);
//				System.out.println("Adding new section.");
			} else if (missingMeasures > 0) {
//				System.out.println("Writing measure.");
				// fill out the latest section with written measures
				// compose measures to fill section
				List<Section> sections = analysis.getSections();
//				System.out.println("Sections: " + sections.size());
				ChordsSection lastSection = (ChordsSection) sections.get(sections.size() - 1); // enforced softly in this class
				if (lastSection.size() < missingMeasures)
					throw new IllegalStateException("Something is wrong... must have miscounted.");
				ChordSpec nextChord = lastSection.getChord(lastSection.size() - missingMeasures + 1);
				Measure nextMeasure = composeBar(composition.getMeasure(composition.size()), nextChord);
				Set<Key> keys = lastSection.getKeys(lastSection.size() - missingMeasures + 1);
				String keyString =  "[";
				for (Key key : keys)
					keyString += " " + key + " ";
				keyString += "] ";
				nextMeasure.setMetaInfo(keyString + nextMeasure.getMetaInfo()); // flipped
				future.add(nextMeasure); // XXX
//				future.add(composeBar(composition)); // old implementation
				int lastEndOfSection = analysis.lastEndOfSection();
				if (composition.size() >= lastEndOfSection) {
					onSectionsFilled(composition);
//					// melody
//					try {
//						List<Measure> measuresWithoutMelody = future.stream()
//								.filter(measure -> measure.getMeasureNumber() <= lastEndOfSection)
//								.filter(measure -> !measure.getMetaInfo().contains("melody"))
//								.collect(Collectors.toList());
//						if (measuresWithoutMelody.size() >= 8) {
//							writeMelody(measuresWithoutMelody);
//						}
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
				}
			}
			return future.size() > 16;
		}
		
		/**
		 * Modifies the given Section object
		 * 
		 * @param nextSection an incomplete section to be filled out
		 * @param progression
		 */
		private void fillSection(ChordsSection nextSection, ChordProgression progression, ChordSpec precedingChord) {
			List<ChordSpec> chordSpecs = nextSection.getAllChords();
			
			ChordSpec lastChordSpec = !chordSpecs.isEmpty() ? chordSpecs.get(chordSpecs.size() - 1) : 
				(precedingChord == null ? firstChord : precedingChord);
			
			int i = chordSpecs.size() + 1;
			if (progression instanceof KeyChange) {
				KeyChange keyChange = (KeyChange) progression;
				Key fromKey = keyChange.getFromKey();
				Key toKey = keyChange.getToKey();
				
				// TODO look further back for lastChordSpec:
				int fromScaleDegree = lastChordSpec == null ? 1 : fromKey.scaleDegree(lastChordSpec.getTonic());
				if (!chordSpecs.isEmpty()) {
					chordSpecs.remove(chordSpecs.size() - 1); // to be replaced below
					i--; // boy, this is messy...
				}
				chordSpecs.addAll(keyChange.progress(fromScaleDegree, 1, 8-chordSpecs.size()));
				
				for (ChordSpec chord : chordSpecs) {
					nextSection.putChord(i, chord);
					if (keyChange.isInFromKey(chord)) {
						nextSection.putKey(i, fromKey);
					}
					if (keyChange.isInToKey(chord)) {
						nextSection.putKey(i, toKey);
					}
					lastChordSpec = chord;
					i++;
				}
				if (i <= nextSection.size())
					progression = ChordProgressions.standardMajorProgression(toKey.getTonic());
			}
			// whether empty or partially full from the KeyChange progression,
			// fill chordSpecs up to 8 chords
//			System.out.print("Progressing to measures:");
			while (i <= nextSection.size()) {
				Key progressionKey = ((KeyChordProgression) progression).getKey();
				ChordSpec nextChordSpec = progression.getNext(lastChordSpec);
				nextSection.putChord(i, nextChordSpec);
				nextSection.putKey(i, progressionKey); // relies on behavior in nextSectionProgression()
				lastChordSpec = nextChordSpec;
				i++;
//				System.out.print(" " + i + ":" + lastChordSpec + "->" + nextChordSpec);
			}
//			System.out.println();
		}
		
		private static class ChordsSection extends Section {

			protected Map<Integer,ChordSpec> chords;
			
			public ChordsSection(int measures) {
				super(measures);
				this.chords = new HashMap<>();
			}
			
			/**
			 * @return all ChordSpecs for all measures, in order as mapped (missing mappings not accounted for)
			 */
			public List<ChordSpec> getAllChords() {
				return new ArrayList<>(chords.keySet()).stream()
						.sorted()
						.map(measure -> chords.get(measure))
						.collect(Collectors.toList());
			}
			
			public ChordSpec getChord(int measure) {
				return chords.get(measure);
			}
			
			public void putChord(int measure, ChordSpec chord) {
				chords.put(measure, chord);
			}
		}
	}