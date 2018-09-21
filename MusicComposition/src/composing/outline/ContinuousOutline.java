package composing.outline;

import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import theory.MathUtil;
import theory.MathUtil.Curve;

public abstract class ContinuousOutline<T> implements Outline<T> {

	private ArrayDeque<OutlineRegion> regions;
	
	public ContinuousOutline() {
		regions = new ArrayDeque<>();
	}
	
	protected abstract Resolver getResolver();
	
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
	
	public List<OutlineRegion> getAll(Double time) {
		return regions.stream()
					  .filter(region -> region.contains(time))
					  .collect(Collectors.toList());
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
	
	public abstract class Resolver {
		
		/**
		 * @param startValue value on one side
		 * @param endValue value on the other side
		 * @param distance fraction of the distance from given startValue to given endValue, 
		 * should always be between 0 and 1 inclusive
		 * @return
		 */
		public abstract T resolve(T startValue, T endValue, Double distance);
		
	}
	
	private class OutlineRegion {
		
		// store information about our place within the outline
		private Double startTime;
		private Double endTime;
		
		private T startValue;
		private T endValue;
		private Curve curve;
		
		public OutlineRegion(T startValue, double startTime, T endValue, double endTime, Curve curve) {
			this.startValue = startValue;
			this.endValue = endValue;
			this.curve = curve;
		}
		
		public boolean contains(Double time) {
			return (time >= startTime) && (time <= endTime); // inclusive on both sides
		}
		
		public T getValue(Double time, Resolver resolver) {
			if (time < startTime || time > endTime)
				throw new IllegalArgumentException();
			
			double normalizedTime = (time - startTime) / (endTime - startTime);
			return resolver.resolve(startValue, endValue, curve.apply(normalizedTime));
		}
	}
	
}
