package theory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class NoteDuration implements Cloneable,Comparable<NoteDuration> {
	
	private final int power;
	private final int dots;
	private final boolean triplet;
	
	public NoteDuration(int power) {
		this(power, 0);
	}
	
	public NoteDuration(int power, int dots) {
		this(power, dots, false);
	}
	
	public NoteDuration(int power, boolean isTriplet) {
		this(power, 0, isTriplet);
	}
	
	/**
	 * @param power NoteDuration = whole*2^power
	 * @param dots non-negative number of dots to add to this note
	 * @param isTriplet whether the note is a triplet, e.g. three triplet eighth notes = one quarter note
	 */
	public NoteDuration(int power, int dots, boolean isTriplet) {
		if (dots < 0)
			throw new IllegalArgumentException("Note duration must have a non-negative number of dots.");
		
		this.power = power;
		this.dots = dots;
		this.triplet = isTriplet;
	}
	
	public int getPower() { return power; }
	public int getDots() { return dots; }
	public boolean isTriplet() { return triplet; }
	
	public double duration() {
		double value = Math.pow(2, power);
		
		double dotAddition = value;
		for (int i=0; i<dots; i++) {
			dotAddition /= 2.0;
			value += dotAddition;
		}
		
		if (triplet)
			value = value * 2 / 3.0;
		
		return value;
	}
	
	/**
	 * @param power a power of 2
	 * @return a NoteDuration equal to (this*2^power)
	 */
	public NoteDuration expand(int power) {
		return new NoteDuration(this.power + power, dots, triplet);
	}
	
	public NoteDuration doubled() {
		return expand(1);
	}
	
	public NoteDuration halved() {
		return expand(-1);
	}

	/**
	 * @return a NoteDuration with one more dot than this one
	 */
	public NoteDuration dotted() {
		return new NoteDuration(power, dots+1);
	}
	
	/**
	 * Will not remove a dot when no dot is present.
	 * 
	 * @return a NoteDuration with one dot removed
	 */
	public NoteDuration unDotted() {
		int reduction = dots == 0 ? 0 : 1;
		return new NoteDuration(power, dots-reduction);
	}
	
	/**
	 * @param isTriplet whether the returned NoteDuration should be a triplet
	 * @return a NoteDuration with the given isTriplet status
	 */
	public NoteDuration toTriplet(boolean isTriplet) {
		NoteDuration duration = new NoteDuration(power, dots, isTriplet);
		return duration;
	}
	
	public static NoteDuration longa() { return new NoteDuration(2); }
	public static NoteDuration breve() { return new NoteDuration(1); }
	public static NoteDuration whole() { return new NoteDuration(0); }
	public static NoteDuration half() { return new NoteDuration(-1); }
	public static NoteDuration quarter() { return new NoteDuration(-2); }
	public static NoteDuration eighth() { return new NoteDuration(-3); }
	public static NoteDuration sixteenth() { return new NoteDuration(-4); }

	public static NoteDuration max(NoteDuration one, NoteDuration two) {
		return one.compareTo(two) < 0 ? two : one;
	}
	
	public static NoteDuration min(NoteDuration one, NoteDuration two) {
		return one.compareTo(two) > 0 ? two : one;
	}
	
	/**
	 * A note is always equal to 2^(d+1)-1 of its units, where 'd' is the number of dots on the note. 
	 * 
	 * @return the smallest note value yielded when breaking up the 
	 * given note into the simplest series of non-dotted notes. 
	 * Equivalent to the largest non-dotted note that divides the given note. 
	 * Triplet status is preserved.
	 */
	public static NoteDuration unit(NoteDuration note) {
		return new NoteDuration(note.getPower()-note.getDots(), note.isTriplet());
	}
	
	/** @see #unit(NoteDuration) */
	public static NoteDuration unit(NoteDuration one, NoteDuration two) {
		NoteDuration unit1 = unit(one);
		NoteDuration unit2 = unit(two);
		
		if (unit1.isTriplet() == unit2.isTriplet())
			return unit1.getPower() > unit2.getPower() ? unit2 : unit1; // return smaller of units
		
		// return a triplet that fits into the duple note
		if (unit1.isTriplet()) { // unit2 not triplet
			return new NoteDuration(Math.min(unit1.getPower(), unit2.getPower()-1), true);
		} else { // unit1 not triplet, unit2 is triplet
			return new NoteDuration(Math.min(unit2.getPower(), unit1.getPower()-1), true);
		}
	}
	
	/** @see #unit(NoteDuration, NoteDuration)) */
	public static NoteDuration unit(Collection<NoteDuration> notes) {
		if (notes.isEmpty())
			throw new IllegalArgumentException("Cannot find common unit of a null set.");
		Set<NoteDuration> set = new HashSet<>(notes);
		Iterator<NoteDuration> it = set.iterator();
		NoteDuration unit = it.next();
		while (it.hasNext())
			unit = unit(unit, it.next());
		return unit;
	}
	
	public static List<NoteDuration> simplify(List<NoteDuration> notes) {
		return simplify(notes, 0);
	}
	
	/**
	 * WARNING: Implementation incomplete
	 * 
	 * @param largestPower power of 2, e.g. 0 = whole note, -2 = quarter note
	 */
	public static List<NoteDuration> simplify(List<NoteDuration> notes, int largestPower) {
		List<NoteDuration> old = new ArrayList<>(notes);
		if (notes.isEmpty())
			return old;
		List<NoteDuration> list = new ArrayList<>();
		
		Collections.sort(old);
		Collections.reverse(old); // big to small
		
		NoteDuration unit = unit(old);
		
		// TODO
		
		return list;
	}
	
	/**
	 * Complement this note up to fill a note one size larger (with no dots and not a triplet).
	 */
	public static List<NoteDuration> complement(NoteDuration note) {
		List<NoteDuration> complement = new ArrayList<>();
		
		// adding a note's "unit" to itself will equal a NoteDuration(notePower+1, noteIsTriplet)
		complement.add(unit(note));
		
		// now note + complement  adds up to one NoteDuration(notePower+1, noteIsTriplet)
		
		if (note.isTriplet())
			complement.add(new NoteDuration(note.getPower(), true));
		
		return complement;
	}
	
	/**
	 * Fill the remainder of a measure after a single note.
	 * <p>
	 * WARNING: Case where given note has a greater base value than a single beat is not yet implemented
	 */
	public static List<NoteDuration> supplement(NoteDuration note, TimeSignature timeSignature) {
		int notePower = note.getPower();
		int noteDots = note.getDots();
		boolean noteIsTriplet = note.isTriplet();
		int beatPower = timeSignature.getBeatValue().getPower();
		
		List<NoteDuration> supplement = new ArrayList<>();
		
		if (notePower == beatPower) { // maybe possible, let's check:
			if (timeSignature.getBeats() == 1) {
				if (noteDots >= 2)
					throw new IllegalArgumentException("Note is too large.");
				if (noteDots == 1) {
					if (noteIsTriplet)
						return supplement; // triplet single-dotted note equals its own value, return nothing
					else // note not triplet, overflows
						throw new IllegalArgumentException("Note is too large.");
				} else { // note has no dots
					if (!noteIsTriplet) {
						return supplement; // note exactly fills entire time signature
					} else { // triplet
						// rather ridiculous situation, but I suppose we'll handle it
						supplement.add(new NoteDuration(notePower-1, true));
						return supplement;
					}
				}
			}
			// otherwise, definitely possible with at least two beats
			
			// fill exactly two beats:
			supplement.addAll(complement(note));
			
			// fill rest of measure:
			NoteDuration temp = new NoteDuration(beatPower);
			for (int i=2; i<timeSignature.getBeats(); i++) // loop carefully defined
				supplement.add(temp.clone());
			
			return supplement;
		} else if (notePower > beatPower) { // maybe possible, check note length vs number of beats
			// TODO
			throw new IllegalArgumentException("Unimplemented.");
		} else { // notePower < beatPower, definitely possible
			// get up to note one size above
			supplement.addAll(complement(note));
			
			// keep increasing in power until we've filled one beat of time signature:
			NoteDuration temp = new NoteDuration(notePower+1);
			for (int i=1; i<(beatPower-notePower); i++) { // loop carefully defined
				supplement.addAll(complement(temp));
				temp = temp.doubled();
			}
			// supplement now adds with the give note to equal one beat of the time signature
			// variable "temp" should have same size as one beat of time signature
			for (int i=1; i<timeSignature.getBeats(); i++) // loop carefully defined
				supplement.add(temp.clone());
			
			return supplement;
		}
	}
	
	/** Currently only supports subracting smaller durations from larger ones. */
	public static List<NoteDuration> subtract(NoteDuration minuend, NoteDuration subtrahend) {
		return supplement(subtrahend, new TimeSignature(1, minuend));
	}
	
	/** 
	 * Currently only supports divisions without remainders. 
	 * <p>
	 * WARNING: Implementation incomplete
	 */
	public static int divide(NoteDuration dividend, NoteDuration divisor) {
		if (divisor.equals(dividend))
			return 1;
		
		int divisorPower = divisor.getPower();
		int dividendPower = dividend.getPower();
		
		int quotient = 0;
		
		if (divisorPower == dividendPower) {
			if (divisor.equals(dividend)
					|| (divisor.getDots() == 0 && !divisor.isTriplet() 
						&& dividend.getDots() == 1 && dividend.isTriplet())
					|| (dividend.getDots() == 0 && !dividend.isTriplet() 
						&& divisor.getDots() == 1 && divisor.isTriplet()))
				return 1;
			else
				throw new IllegalArgumentException();
		} else if (divisorPower > dividendPower) {
			throw new IllegalArgumentException();
		} else { // divisorPower < dividendPower
			if (divisor.isTriplet() == dividend.isTriplet()) {
				
			} else if (divisor.isTriplet()) { // dividend not triplet
				
			} else { // divisor not triplet, dividend is triplet
				
			}
		}
		
		// TODO
		
		return quotient;
	}
	
	@Override
	public NoteDuration clone() {
		return new NoteDuration(power, dots, triplet);
	}
	
	@Override
	public int compareTo(NoteDuration o) {
		// rounding errors shouldn't be a problem here
		return new Double(duration()).compareTo(new Double(o.duration()));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + dots;
		result = prime * result + power;
		result = prime * result + (triplet ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NoteDuration other = (NoteDuration) obj;
		if (dots != other.dots)
			return false;
		if (power != other.power)
			return false;
		if (triplet != other.triplet)
			return false;
		return true;
	}
	
}
