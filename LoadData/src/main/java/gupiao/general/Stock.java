package gupiao.general;

import java.math.BigDecimal;

public class Stock {
    private String code;
    private String name;
    private float closePrice;
    private float increaseRate;
    private BigDecimal dealNumber;
    private float changeRate;
    private BigDecimal currentMarketPrice;
    private BigDecimal totalMarketPrice;
    private String industrySector;
	private String industrySubSector;

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
	
	public BigDecimal getDealNumber() {
		return dealNumber;
	}

	public void setDealNumber(BigDecimal dealAmount) {
		this.dealNumber = dealAmount;
	}
	
	public void setDealNumber(String dealAmount) {
		this.dealNumber = new BigDecimal(dealAmount.replaceAll(",", ""));
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
	
	public BigDecimal getCurrentMarketPrice() {
		return currentMarketPrice;
	}

	public void setCurrentMarketPrice(BigDecimal currentMarketPrice) {
		this.currentMarketPrice = currentMarketPrice;
	}

	public void setCurrentMarketPrice(String currentMarketPrice) {
		this.currentMarketPrice = new BigDecimal(currentMarketPrice.replaceAll(",", ""));
	}
	
	public BigDecimal getTotalMarketPrice() {
		return totalMarketPrice;
	}

	public void setTotalMarketPrice(BigDecimal totalMarketPrice) {
		this.totalMarketPrice = totalMarketPrice;
	}

	public void setTotalMarketPrice(String totalMarketPrice) {
		this.totalMarketPrice = new BigDecimal(totalMarketPrice.replaceAll(",", ""));
	}

	public String getIndustrySector() {
		return industrySector;
	}

	public void setIndustrySector(String industrySector) {
		this.industrySector = industrySector;
	}

	public String getIndustrySubSector() {
		return industrySubSector;
	}

	public void setIndustrySubSector(String industrySubSector) {
		this.industrySubSector = industrySubSector;
	}
}
