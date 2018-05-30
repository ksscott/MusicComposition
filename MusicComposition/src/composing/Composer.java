package composing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import composing.strategy.ComposingStrategy;
import composing.strategy.PrettyProgressionStrategy;
import composing.strategy.TwelveBarImprovStrategy;
import theory.Key;
import theory.Letter;
import theory.Measure;
import theory.Note;

public class Composer {
	
	private ComposerThread thread;
	private List<Composition> works = new ArrayList<>();
	
	private ComposingStrategy[] oldTricks = new ComposingStrategy[] { 
					new TwelveBarImprovStrategy(new Note(Letter.C)),
					new PrettyProgressionStrategy(new Key(new Note(Letter.C), Key.MAJOR)),
			};
	
	public Measure beginComposing() {
//		return beginComposing(randomRepertoire());
		return beginComposing(oldTricks[1]);
	}
	
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
			case SWITCH:
				strategy = thread.getStrategy();
				ComposingStrategy newStrategy;
				do {
					newStrategy = randomRepertoire();
				} while (newStrategy.equals(strategy));
				finishComposing();
				return beginComposing(newStrategy);
			default:
				return null;
		}
	}

	public Composition finishComposing() {
		Composition work = thread.stopComposing();
		works.add(work);
		return work;
	}
	
	@SuppressWarnings("unused")
	private ComposingStrategy randomRepertoire() {
		return oldTricks[(int) (Math.random() * oldTricks.length)];
	}
	
	private enum UserInput {
		RESTART("restart"),
		SWITCH("switch"),
		;
		
		private Predicate<String> test;
		
		private UserInput(String... commands) {
			final List<String> commandList = Arrays.asList(commands);
			this.test = string -> string == null ? false : commandList.contains(string.toLowerCase());
		}
		
		public boolean matches(String inputString) {
			return test.test(inputString);
		}
		
		public static UserInput get(String inputString) {
			for (UserInput command : values())
				if (command.matches(inputString))
					return command;
			return null;
		}
	}
}
