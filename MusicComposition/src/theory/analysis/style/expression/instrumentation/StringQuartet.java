package theory.analysis.style.expression.instrumentation;

import java.util.ArrayList;
import java.util.List;

import performance.instrument.Instrument;
import static performance.instrument.Instrument.*;
import theory.analysis.characteristic.Instrumentation;
import theory.analysis.style.expression.Expression;

public class StringQuartet implements Expression<Instrumentation> {

	private List<Instrument> quartet;
	
	{
		quartet = new ArrayList<>();
		
		quartet.add(VIOLIN);
//		quartet.add(VIOLIN); // TODO two violins, how to represent?
		quartet.add(VIOLA);
		quartet.add(CELLO);
	}
	
	@Override
	public void describe() {
		// TODO Auto-generated method stub
		
	}

}
