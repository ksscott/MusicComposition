package theory.analysis.style.expression.instrumentation;

import java.util.ArrayList;
import java.util.List;

import performance.instrument.Instrument;
import static performance.instrument.Instrument.*;

import theory.analysis.characteristic.Instrumentation;
import theory.analysis.style.expression.Expression;

public class JazzTrio implements Expression<Instrumentation> {

	private List<Instrument> trio;
	
	{
		trio = new ArrayList<>();
		
		trio.add(PIANO);
		trio.add(ACOUSTIC_BASS);
//		trio.add(); // placeholder for drum set
	}
	
	@Override
	public void describe() {
		// TODO Auto-generated method stub
		
	}

}
