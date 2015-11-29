package gupiao.general;

public class Stock {
    private String code;
    private String name;
    private float closePrice;
    private float increaseRate;
    private float dealNumber;
    private float changeRate;
    private float currentMarketPrice;
    private float totalMarketPrice;

    public Stock () {
    	
    }
    
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
	
	public void setClosePrice(String closePrice) {
		this.closePrice = Float.parseFloat(closePrice);
	}

	public float getIncreaseRate() {
		return increaseRate;
	}

	public void setIncreaseRate(float increaseRate) {
		this.increaseRate = increaseRate;
	}

	public void setIncreaseRate(String increaseRate) {
		this.increaseRate = Float.parseFloat(increaseRate.replaceAll("%", ""));
	}
	
	public float getDealNumber() {
		return dealNumber;
	}

	public void setDealNumber(long dealAmount) {
		this.dealNumber = dealAmount;
	}
	
	public void setDealNumber(String dealAmount) {
		this.dealNumber = Float.parseFloat(dealAmount.replaceAll(",", ""));
	}

	public float getChangeRate() {
		return changeRate;
	}

	public void setChangeRate(float changeRate) {
		this.changeRate = changeRate;
	}

	public void setChangeRate(String changeRate) {
		this.changeRate = Float.parseFloat(changeRate.replaceAll("%", ""));
	}
	
	public float getCurrentMarketPrice() {
		return currentMarketPrice;
	}

	public void setCurrentMarketPrice(float currentMarketPrice) {
		this.currentMarketPrice = currentMarketPrice;
	}

	public void setCurrentMarketPrice(String currentMarketPrice) {
		this.currentMarketPrice = Float.parseFloat(currentMarketPrice.replaceAll(",", ""));
	}
	
	public float getTotalMarketPrice() {
		return totalMarketPrice;
	}

	public void setTotalMarketPrice(float totalMarketPrice) {
		this.totalMarketPrice = totalMarketPrice;
	}

	public void setTotalMarketPrice(String totalMarketPrice) {
		this.totalMarketPrice = Float.parseFloat(totalMarketPrice.replaceAll(",", ""));
	}
}
