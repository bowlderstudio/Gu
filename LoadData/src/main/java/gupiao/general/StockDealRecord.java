package gupiao.general;

import java.math.BigDecimal;

public class StockDealRecord {
    private String code;
    private String name;
    private String date;
    private float openPrice;
    private float highestPrice;
    private float lowestPrice;
    private float closePrice;
    private float increaseRate;
    private BigDecimal dealNumber;
    private BigDecimal dealAmount;
    private float ema12;
    private float ema26;
    private float diff;
    private float signal;
    private float histogram;

    public StockDealRecord(String code, String date, float price) {
    	this.code=code;
    	this.date=date;
    	this.closePrice=price;
    	this.ema12 = -1;
        this.ema26 = -1;
        this.signal = -1;
        this.histogram = -1;
    }
    
    public StockDealRecord() {
        this.ema12 = -1;
        this.ema26 = -1;
        this.signal = -1;
        this.histogram = -1;
    }

    public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public float getOpenPrice() {
		return openPrice;
	}

	public void setOpenPrice(float openPrice) {
		this.openPrice = openPrice;
	}
	
	public void setOpenPrice(String openPrice) {
		this.openPrice = Float.parseFloat(openPrice);
	}

	public float getHighestPrice() {
		return highestPrice;
	}

	public void setHighestPrice(float highestPrice) {
		this.highestPrice = highestPrice;
	}
	
	public void setHighestPrice(String highestPrice) {
		try{
			this.highestPrice = Float.parseFloat(highestPrice);
		} catch (java.lang.NumberFormatException e) {
			this.highestPrice = 0;
		}
	}

	public float getLowestPrice() {
		return lowestPrice;
	}

	public void setLowestPrice(float lowestPrice) {
		this.lowestPrice = lowestPrice;
	}
	
	public void setLowestPrice(String lowestPrice) {
		try{
			this.lowestPrice = Float.parseFloat(lowestPrice);
		} catch (java.lang.NumberFormatException e) {
			this.lowestPrice = 0;
		}
	}

	public BigDecimal getDealNumber() {
		return dealNumber;
	}

	public void setDealNumber(BigDecimal dealNumber) {
		this.dealNumber = dealNumber;
	}
	
	public void setDealNumber(String dealNumber) {
		try{
			this.dealNumber = new BigDecimal(dealNumber);
		} catch (java.lang.NumberFormatException e) {
			this.dealNumber = new BigDecimal(0);
		}
	}

	public BigDecimal getDealAmount() {
		return dealAmount;
	}

	public void setDealAmount(BigDecimal dealAmount) {
		this.dealAmount = dealAmount;
	}
	
	public void setDealAmount(String dealAmount) {
		try{
			this.dealAmount = new BigDecimal(dealAmount);
		} catch (java.lang.NumberFormatException e) {
			this.dealAmount = new BigDecimal(0);
		}
	}

	public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public float getClosePrice() {
        return closePrice;
    }

    public void setClosePrice(float closePrice) {
        this.closePrice = closePrice;
    }
    
    public void setClosePrice(String closePrice) {
    	try{
    		this.closePrice = Float.parseFloat(closePrice);
		} catch (java.lang.NumberFormatException e) {
			this.closePrice = 0;
		}
    }

    public float getEma12() {
        return ema12;
    }

    public void setEma12(float ema12) {
        this.ema12 = ema12;
    }

    public float getEma26() {
        return ema26;
    }

    public void setEma26(float ema26) {
        this.ema26 = ema26;
    }

    public float getDiff() {
        return diff;
    }

    public void setDiff(float diff) {
        this.diff = diff;
    }

    public float getSignal() {
        return signal;
    }

    public void setSignal(float signal) {
        this.signal = signal;
    }

    public float getHistogram() {
        return histogram;
    }

    public void setHistogram(float histogram) {
        this.histogram = histogram;
    }

    public float getIncreaseRate() {
		return increaseRate;
	}

	public void setIncreaseRate(float increaseRate) {
		this.increaseRate = increaseRate;
	}
	
	public void setIncreaseRate(String increaseRate) {
		try{
			this.increaseRate = Float.parseFloat(increaseRate);
		} catch (java.lang.NumberFormatException e) {
			this.increaseRate = 0;
		}
	}

	public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
