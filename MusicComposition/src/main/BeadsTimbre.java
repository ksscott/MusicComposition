package main;

import net.beadsproject.beads.data.Buffer;
import performance.Timbre;
import performance.instrument.Instrument;

public class BeadsTimbre implements Timbre {
	
	int peakMillis;
	Buffer waveform;
	
	private static BeadsTimbre sineTimbre;
	private static BeadsTimbre instrumentTimbre;
	private static BeadsTimbre voiceTimbre;
	
	public BeadsTimbre(int attackTimeMillis, Buffer waveform) {
		this.peakMillis = attackTimeMillis;
		this.waveform = waveform;
	}
	public int getPeakMillis() { return peakMillis; }
	public Buffer getWaveform() { return waveform; }
	
	public static BeadsTimbre getTimbre(Instrument instrument) {
		if (instrument == Instrument.SOPRANO_VOICE
				|| instrument == Instrument.ALTO_VOICE
				|| instrument == Instrument.TENOR_VOICE
				|| instrument == Instrument.BASS_VOICE
				)
			return getSineTimbre();
		
		if (instrument == Instrument.FLUTE) 
			return getInstrumentTimbre();
		if (instrument == Instrument.PIANO) 
			return getInstrumentTimbre();
		
		if (instrument == Instrument.TRUMPET) 
			return getSineTimbre();
		if (instrument == Instrument.ACOUSTIC_BASS) 
			return getInstrumentTimbre();
		
		return getSineTimbre();
	}
	
	public static BeadsTimbre getSineTimbre() {
		if (sineTimbre == null)
			sineTimbre = new BeadsTimbre(150, Buffer.SINE);
		return sineTimbre;
	}
	
	public static BeadsTimbre getInstrumentTimbre() {
		if (instrumentTimbre == null) {
			int resolution = 10000;
			int harmonics = Math.min(1000, resolution/4/20);
			// decay envelope on harmonics:
			double nToThe = -2.7;
			// sinusoidal envelope on harmonics:
			double nthHarmonicIsMinimum = 2;
			double minimumHarmonicRatio = .6;
			double rangeHalf = (1.0 - minimumHarmonicRatio)/2.0;
			double rangeCenter = 1.0 - rangeHalf;
			// add noisy neighbors:
			double neighborDist = .05;
			double neighborRatio = .15;
			
			Buffer buffer = new Buffer(resolution);
			{
				for (int i=0; i<resolution; i++) {
					float value = 0;
					for (int j=1; j<harmonics+1; j++) {
						double harmonic = Math.sin(i*2*Math.PI/resolution*j);
						double upperNeighborRatio = j*(1+neighborDist);
						double lowerNeighborRatio = j*(1-neighborDist);
						double harmonicUpperNeighbor = Math.sin(i*2*Math.PI/resolution*upperNeighborRatio);
						double harmonicLowerNeighbor = Math.sin(i*2*Math.PI/resolution*lowerNeighborRatio);
						double decayEnv = Math.pow(j, nToThe);
						double sinusoidalEnv = rangeCenter+rangeHalf*Math.cos((j-1.2)*Math.PI/nthHarmonicIsMinimum);
//					double sinusoidalEnv2 = Math.cos(rangeCenter+rangeHalf*Math.cos((j-1.2)*Math.PI/nthHarmonicIsMinimum/2.0));
						double envFinal = decayEnv 
								* sinusoidalEnv 
//									* sinusoidalEnv2
								;
						value += harmonic * envFinal
								+ harmonicUpperNeighbor * envFinal * neighborRatio
								+ harmonicLowerNeighbor * envFinal * neighborRatio
								;
					}
					buffer.buf[i] = value;
				}
			}
			instrumentTimbre = new BeadsTimbre(120, buffer);
		}
		return instrumentTimbre;
	}
	
	public static BeadsTimbre getVoiceTimbre() {
		if (voiceTimbre == null) {
			int resolution = 10000;
			int harmonics = Math.min(1000, resolution/4/20);
			// decay envelope on harmonics:
			double nToThe = -2.3;
			// sinusoidal envelope on harmonics:
			double nthHarmonicIsMinimum = 6;
			double minimumHarmonicRatio = .6;
			double rangeHalf = (1.0 - minimumHarmonicRatio)/2.0;
			double rangeCenter = 1.0 - rangeHalf;
			// add noisy neighbors:
			double neighborDist = .07;
			double neighborRatio = .7;
			
			Buffer buffer = new Buffer(resolution);
			{
				for (int i=0; i<resolution; i++) {
					float value = 0;
					for (int j=1; j<harmonics+1; j++) {
						double harmonic = Math.sin(i*2*Math.PI/resolution*j);
						double upperNeighborRatio = j*(1+neighborDist);
						double lowerNeighborRatio = j*(1-neighborDist);
						double harmonicUpperNeighbor = Math.sin(i*2*Math.PI/resolution*upperNeighborRatio);
						double harmonicLowerNeighbor = Math.sin(i*2*Math.PI/resolution*lowerNeighborRatio);
						double decayEnv = Math.pow(j, nToThe);
						double sinusoidalEnv = rangeCenter+rangeHalf*Math.cos((j-1)*Math.PI/nthHarmonicIsMinimum);
						double sinusoidalEnv2 = Math.cos(rangeCenter+rangeHalf*Math.cos((j-1)*Math.PI/nthHarmonicIsMinimum/3.0));
						double envFinal = decayEnv 
								* sinusoidalEnv 
								* sinusoidalEnv2
								;
						value += harmonic * envFinal
								+ harmonicUpperNeighbor * envFinal * neighborRatio
								+ harmonicLowerNeighbor * envFinal * neighborRatio
								;
					}
					buffer.buf[i] = value;
				}
			}
			voiceTimbre = new BeadsTimbre(200, buffer);
		}
		return voiceTimbre;
	}
}