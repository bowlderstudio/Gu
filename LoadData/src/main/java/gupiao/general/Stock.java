package gupiao.general;

public class Stock {
    private String code;
    private String name;
    private float closePrice;
    private float increaseRate;
    private long dealNumber;
    private float changeRate;
    private float currentMarketPrice;
    private float totalMarketPrice;

    public Stock(String code, String name) {
        this.code = code;
        this.name = name;
    }

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

	public float getClosePrice() {
		return closePrice;
	}

	public void setClosePrice(float closePrice) {
		this.closePrice = closePrice;
	}

	public float getIncreaseRate() {
		return increaseRate;
	}

	public void setIncreaseRate(float increaseRate) {
		this.increaseRate = increaseRate;
	}

	public long getDealNumber() {
		return dealNumber;
	}

	public void setDealNumber(long dealAmount) {
		this.dealNumber = dealAmount;
	}

	public float getChangeRate() {
		return changeRate;
	}

	public void setChangeRate(float changeRate) {
		this.changeRate = changeRate;
	}

	public float getCurrentMarketPrice() {
		return currentMarketPrice;
	}

	public void setCurrentMarketPrice(float currentMarketPrice) {
		this.currentMarketPrice = currentMarketPrice;
	}

	public float getTotalMarketPrice() {
		return totalMarketPrice;
	}

	public void setTotalMarketPrice(float totalMarketPrice) {
		this.totalMarketPrice = totalMarketPrice;
	}


}
