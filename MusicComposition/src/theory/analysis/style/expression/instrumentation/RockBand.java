package theory.analysis.style.expression.instrumentation;

import java.util.ArrayList;
import java.util.List;

import performance.instrument.Instrument;
import static performance.instrument.Instrument.*;
import theory.analysis.characteristic.Instrumentation;
import theory.analysis.style.expression.Expression;

public class RockBand implements Expression<Instrumentation> {

	private List<Instrument> band;
	
	{
		band = new ArrayList<>();
		
		band.add(TENOR_VOICE); // not necessarily a male vocalist
		band.add(NYLON_GUITAR); // TODO electric guitar, usually
		band.add(ACOUSTIC_BASS); // TODO electric bass, usually
//		band.add(); // TODO placeholder for drum set
	}
	
	@Override
	public void describe() {
		// TODO Auto-generated method stub
		
	}

}
