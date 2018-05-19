package theory;

public enum Accidental {
	
	FLAT("b"), NONE(""), SHARP("#");
	
	private final String symbol;
	
	private Accidental(String symbol) {
		this.symbol = symbol;
	}
	
	public int pitchAdjustment() {
		return ordinal() - 1;
	}
	
	public String getSymbol() {
		return new String(symbol);
	}

}
