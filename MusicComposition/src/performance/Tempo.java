package performance;

public interface Tempo {
	
	public int getBpm();
	
	public static class TempoImpl implements Tempo,Cloneable {
		
		private int bpm;
		
		public TempoImpl(int bpm) {
			if (bpm <= 0)
				throw new IllegalArgumentException("Tempo must be greater than zero.");
			this.bpm = bpm;
		}
		
		public TempoImpl(Tempo other) {
			this(other.getBpm());
		}
		
		@Override
		public int getBpm() {
			return bpm;
		}
		
		@Override
		public TempoImpl clone() {
			return new TempoImpl(bpm);
		}
	}
	
	public enum Tempi implements Tempo {

		// BPM taken from Wikipedia: "Tempo"

		// ordered by average tempo, not lowest as in Wikipedia
		LARGHISSIMO(20, 24), // 20 not listed
		GRAVE(25, 45),
		LARGO(40, 60),
		LENTO(45, 60),
		LARGHETTO(60, 66),
		ADAGIO(66, 76),
		ADAGIETTO(72, 76),
		MARCIA_MODERATO(83, 85), // weirdo, was two slots down
		ANDANTE(76, 108),
		ANDANTINO(80, 108),
		ANDANTE_MODERATO(92, 112),
		MODERATO(108, 120),
		ALLEGRETTO(112, 120),
		ALLEGRO_MODERATO(116, 120),
		ALLEGRO(120, 156),
		VIVACE(156, 176),
		VIVACISSIMO(172, 176), // also "Allegrissimo" or "Allegro Vivace"
		PRESTO(168, 200),
		PRESTISSIMO(200, 220), // 220 not listed
		;

		int minBpm;
		int maxBpm;

		private Tempi(int minBpm, int maxBpm) {
			this.minBpm = minBpm;
			this.maxBpm = maxBpm;
		}

		@Override
		public int getBpm() {
			return (minBpm + maxBpm) / 2;
		}

		public int getMinBpm() {
			return minBpm;
		}

		public int getMaxBpm() {
			return maxBpm;
		}

	}
}