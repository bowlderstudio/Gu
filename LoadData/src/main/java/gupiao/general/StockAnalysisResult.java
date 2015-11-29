package gupiao.general;

public class StockAnalysisResult {	
	private String code;
	private String name;
	private float highestPrice;
	private float lowestPrice;
	private float currentPrice;
	
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public float getHighestPrice() {
		return highestPrice;
	}
	public void setHighestPrice(float highestPrice) {
		this.highestPrice = highestPrice;
	}
	public float getLowestPrice() {
		return lowestPrice;
	}
	public void setLowestPrice(float lowestPrice) {
		this.lowestPrice = lowestPrice;
	}
	public float getCurrentPrice() {
		return currentPrice;
	}
	public void setCurrentPrice(float currentPrice) {
		this.currentPrice = currentPrice;
	}
	public float getRateToHigh() {
		return this.currentPrice/this.highestPrice;
	}
	public float getRateToLow() {
		return this.lowestPrice/this.currentPrice;
	}
	
	public static String getTitles() {
		return "code,name,current price, highest price, lowest price, rate to high, rate to low";
	}
	
	public String toString() {
		return this.code+","+this.name+","+this.currentPrice+","+this.highestPrice+","+this.lowestPrice+","+getRateToHigh()+","+getRateToLow();
	}
}
