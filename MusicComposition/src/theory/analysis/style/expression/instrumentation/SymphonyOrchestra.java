package theory.analysis.style.expression.instrumentation;

import java.util.ArrayList;
import java.util.List;

import performance.instrument.Instrument;
import static performance.instrument.Instrument.*;
import theory.analysis.characteristic.Instrumentation;
import theory.analysis.style.expression.Expression;

public class SymphonyOrchestra implements Expression<Instrumentation> {

	List<Instrument> orchestra;
	
	{
		orchestra = new ArrayList<>();
		orchestra.add(FLUTE);
		orchestra.add(OBOE);
		orchestra.add(CLARINET);
		orchestra.add(BASSOON);
		
		orchestra.add(FRENCH_HORN);
		orchestra.add(TRUMPET);
		orchestra.add(TROMBONE);
		orchestra.add(TUBA);
		
		orchestra.add(TIMPANI);
		
		orchestra.add(PIANO);
		orchestra.add(CELESTA);
		orchestra.add(CHURCH_ORGAN);
		orchestra.add(HARPSICHORD);
		
		orchestra.add(HARP);
		orchestra.add(VIOLIN);
		orchestra.add(VIOLA);
		orchestra.add(CELLO);
		orchestra.add(CONTRABASS);
		orchestra.add(NYLON_GUITAR);
	}
	
	@Override
	public void describe() {
		// TODO Auto-generated method stub
		
	}

}
