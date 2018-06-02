package composing;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.function.Function;
import java.util.function.Predicate;

import composing.strategy.ComposingStrategy;
import composing.strategy.PrettyProgressionStrategy;
import composing.strategy.TwelveBarImprovStrategy;
import theory.Accidental;
import theory.Key;
import theory.Letter;
import theory.Measure;
import theory.Note;

public class Composer {
	
	private ComposerThread thread;
	private List<Composition> works = new ArrayList<>();
	private Queue<ComposingStrategy> requests = new ArrayDeque<>();
	
	private List<ComposingStrategy> oldTricks = Arrays.asList(new ComposingStrategy[] { 
					new TwelveBarImprovStrategy(new Note(Letter.C)),
					new PrettyProgressionStrategy(new Key(new Note(Letter.B, Accidental.FLAT), Key.MAJOR)),
			});
	
	/**
	 * @param strategy
	 * @return the first measure of the composition
	 */
	public Measure beginComposing() {
//		return beginComposing(randomRepertoire());
		return beginComposing(oldTricks.get(1));
	}
	
	/**
	 * @param strategy
	 * @return the first measure of the composition
	 */
	public Measure beginComposing(ComposingStrategy strategy) {
		Composition composition = new Composition();
		Measure measure = strategy.generateFirstMeasure();
		composition.addMeasure(measure);
		thread = new ComposerThread(composition, strategy);
		thread.start();
		return measure;
	}
	
	public Measure writeNextMeasure() {
		return thread.writeNextMeasure();
	}
	
	public Measure receiveInput(String inputString) {
		UserInput command = UserInput.get(inputString);
		if (command == null)
			return null;
		ComposingStrategy strategy;
		switch (command) {
			case RESTART:
				strategy = thread.getStrategy();
				finishComposing();
				return beginComposing(strategy);
			case REQUEST:
				for (ComposingStrategy trick : oldTricks)
					if (trick.toString().toLowerCase().contains(
							UserInput.REQUEST.getParams(inputString).get(0).toLowerCase())) {
						requests.add(trick);
						System.out.println("Requested " + trick);
					}
				break;
			case SWITCH:
				strategy = thread.getStrategy();
				ComposingStrategy nextStrategy;
				finishComposing();
				// requests queued?
				if (!requests.isEmpty()) {
					nextStrategy = requests.poll();
				} else { // else, pick anything that's not the current piece
					do {
						nextStrategy = randomRepertoire();
					} while (nextStrategy.equals(strategy));
				}
				return beginComposing(nextStrategy);
			default:
				break;
		}
		return null;
	}

	public Composition finishComposing() {
		Composition work = thread.stopComposing();
		works.add(work);
		return work;
	}
	
	private ComposingStrategy randomRepertoire() {
		return oldTricks.get((int) (Math.random() * oldTricks.size()));
	}
	
	private enum UserInput {
		RESTART(startsWithAny("restart", "reset")),
		REQUEST(startsWithAny("request"), 
				string -> Arrays.asList(string.substring("request".length()).trim())),
		SWITCH(startsWithAny("switch")),
		;
		
		private Predicate<String> test;
		private Function<String,List<String>> paramParser;
		
		private UserInput(Predicate<String> matcher) {
			this(matcher, string -> Collections.emptyList());
		}
		
		private UserInput(Predicate<String> matcher, Function<String,List<String>> paramParser) {
			this.test = matcher;
			this.paramParser = paramParser;
		}
		
		public boolean matches(String inputString) {
			return test.test(inputString);
		}
		
		public List<String> getParams(String inputString) { // TODO abstract more
			return paramParser.apply(inputString);
		}
		
		public static UserInput get(String inputString) {
			for (UserInput command : values())
				if (command.matches(inputString))
					return command;
			return null;
		}
		
		private static Predicate<String> startsWithAny(String... strings) {
			final List<String> stringList = Arrays.asList(strings);
			return string -> {
				if (string == null)
					return false;
				for (String known : stringList)
					if (string.toLowerCase().startsWith(known.toLowerCase()))
						return true;
				return false;
			};
		}
	}
}
