package gupiao.general;

public class StockAnalysisResult {	
	private String code;
	private String name;
	private float highestPrice;
	private float lowestPrice;
	private float currentPrice;
	private float rateToHigh;
	private float rateToLow;
	
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
		return rateToHigh;
	}
	public void setRateToHigh(float rateToHigh) {
		this.rateToHigh = rateToHigh;
	}
	public float getRateToLow() {
		return rateToLow;
	}
	public void setRateToLow(float rateToLow) {
		this.rateToLow = rateToLow;
	}
}
