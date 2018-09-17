package theory.analysis.style;

import java.util.List;

import theory.analysis.characteristic.Dimension;
import theory.analysis.style.expression.Expression;

public interface Style {
	
	public<T extends Dimension> List<Expression<T>> getExpressions(); // TODO what next
	
}
