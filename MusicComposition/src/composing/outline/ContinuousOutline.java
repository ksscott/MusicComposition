package composing.outline;

import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import theory.MathUtil;
import theory.MathUtil.Curve;

/**
 * An {@code Outline} which accepts a continuous {@link Curve} across points in time. 
 * Implementers must provide a {@link Resolver} via the abstract method {@link #getResolver()} 
 * which decides what a {@code Curve} looks like in {@code <T>} space.
 * 
 * @param <T> the type of dimension being outlined
 */
public abstract class ContinuousOutline<T> implements Outline<T> {

	private ArrayDeque<OutlineRegion> regions;
	
	public ContinuousOutline() {
		regions = new ArrayDeque<>();
	}
	
	protected abstract Resolver<T> getResolver();
	
	public T get(Double time) {
		ContinuousOutline<T>.OutlineRegion dominantRegion = 
				regions.stream()
					   .filter(region -> region.contains(time))
					   .findFirst()
					   .orElse(null);
		return dominantRegion == null ? null : dominantRegion.getValue(time, getResolver());
	}
	
	public T getLast(Double time) {
		T t = get(time);
		if (t != null)
			return t;
		OutlineRegion latestRegion = regions.stream().max(new Comparator<OutlineRegion>() {
			@Override
			public int compare(ContinuousOutline<T>.OutlineRegion o1, ContinuousOutline<T>.OutlineRegion o2) {
				return o1.endTime.compareTo(o2.endTime);
			}
		}).orElse(null);
		return latestRegion == null ? null : latestRegion.getValue(time, getResolver());
	}
	
	// TODO remove?
	public List<OutlineRegion> getAll(Double time) {
		return regions.stream()
					  .filter(region -> region.contains(time))
					  .collect(Collectors.toList());
	}
	
	// TODO remove?
	public List<OutlineRegion> getAll(Double startTime, Double endTime) {
		return regions.stream()
					  .filter(region -> region.affectsRange(startTime, endTime))
					  .collect(Collectors.toList());
	}
	
	/** Note: Depending on your {@link Resolver}, other intermediate values are possible */
	public Set<T> getAllPossibleEndpoints(Double startTime, Double endTime) {
		Set<T> set = new HashSet<>();
		getAll(startTime, endTime).stream()
								  .peek(region -> set.add(region.startValue))
								  .forEach(region -> set.add(region.endValue));
		return set;
	}
	
	public void add(T constantValue, Double startTime, Double endTime) {
		add(constantValue, startTime, constantValue, endTime);
	}
	
	/** defaults to a sinusoidal curve */
	public void add(T startValue, Double startTime, T endValue, Double endTime) {
		add(startValue, startTime, endValue, endTime, MathUtil.SINUSOIDAL);
	}
	
	/** The region described will mask all other conflicting regions already stored. */
	public void add(T startValue, Double startTime, T endValue, Double endTime, Curve curve) {
		regions.push(new OutlineRegion(startValue, startTime, endValue, endTime, curve));
	}
	
	/** The region described will be masked by all other conflicting regions already stored. */
	public void addUnderneath(T startValue, Double startTime, T endValue, Double endTime, Curve curve) {
		regions.add(new OutlineRegion(startValue, startTime, endValue, endTime, curve));
	}
	
	public void remove(OutlineRegion region) {
		regions.remove(region);
	}
	
	/**
	 * A {@code Resolver} gets to decide what values exist in {@code <T>} space between two values.
	 * 
	 * @see #resolve(Object, Object, Double)
	 * @param <T> the type to be resolved
	 */
	public interface Resolver<T> {
		
		/**
		 * @param startValue value on one side ({@code distance=0})
		 * @param endValue value on the other side ({@code distance=1})
		 * @param distance fraction of the distance from given {@code startValue} (at {@code distance=0}) 
		 * to given {@code endValue} (at {@code distance=1}).
		 * @return a value representing the value at a {@code distance} from {@code startValue} to {@code endValue}
		 */
		public T resolve(T startValue, T endValue, Double distance);
		
	}
	
	private class OutlineRegion {
		
		// store information about our place within the outline
		private Double startTime;
		private Double endTime;
		
		private T startValue;
		private T endValue;
		private Curve curve;
		
		/**
		 * @param startValue
		 * @param startTime
		 * @param endValue
		 * @param endTime
		 * @param curve
		 */
		public OutlineRegion(T startValue, Double startTime, T endValue, Double endTime, Curve curve) {
			if (startTime > endTime)
				throw new IllegalArgumentException("Start time cannot be after end time");
			
			this.startTime = startTime;
			this.endTime = endTime;
			this.startValue = startValue;
			this.endValue = endValue;
			this.curve = curve;
		}
		
		public boolean contains(Double time) {
			return (time >= startTime) && (time <= endTime); // inclusive on both sides
		}
		
		public boolean affectsRange(Double start, Double end) {
			return !(startTime > end || endTime < start);
		}
		
		public T getValue(Double time, Resolver<T> resolver) {
			if (!this.contains(time))
				throw new IllegalArgumentException("Time " + time + " does not fall in range (" + startTime + "," + endTime + ").");
			
			double normalizedTime = (time - startTime) / (endTime - startTime);
			return resolver.resolve(startValue, endValue, curve.apply(normalizedTime));
		}
	}
	
}
