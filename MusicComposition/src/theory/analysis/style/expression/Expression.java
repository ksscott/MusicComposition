package theory.analysis.style.expression;

import theory.analysis.characteristic.Dimension;

public interface Expression<T extends Dimension> {
	
	public void describe(); // TODO I haven't yet designed the manner in which an Expression will communicate its opinions on its dimension
	
}
