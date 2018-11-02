package theory.analysis.style.expression.instrumentation;

import java.util.ArrayList;
import java.util.List;

import performance.instrument.Instrument;
import static performance.instrument.Instrument.*;
import theory.analysis.characteristic.Instrumentation;
import theory.analysis.style.expression.Expression;

public class Choir implements Expression<Instrumentation> {

	private List<Instrument> choir;
	
	{
		choir = new ArrayList<>();
		
		choir.add(SOPRANO_VOICE);
		choir.add(ALTO_VOICE);
		choir.add(TENOR_VOICE);
		choir.add(BASS_VOICE);
	}
	
	@Override
	public void describe() {
		// TODO Auto-generated method stub
		
	}

}
