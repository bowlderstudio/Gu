package gupiao.general;

public class StockDealRecord {
    private String code;
    private String name;
    private String date;
    private float openPrice;
    private float highestPrice;
    private float lowestPrice;
    private float closePrice;
    private long dealNumber;
    private long dealAmount;
    private float ema12;
    private float ema26;
    private float diff;
    private float signal;
    private float histogram;

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
		this.highestPrice = Float.parseFloat(highestPrice);
	}

	public float getLowestPrice() {
		return lowestPrice;
	}

	public void setLowestPrice(float lowestPrice) {
		this.lowestPrice = lowestPrice;
	}
	
	public void setLowestPrice(String lowestPrice) {
		this.lowestPrice = Float.parseFloat(lowestPrice);
	}

	public long getDealNumber() {
		return dealNumber;
	}

	public void setDealNumber(long dealNumber) {
		this.dealNumber = dealNumber;
	}
	
	public void setDealNumber(String dealNumber) {
		this.dealNumber = Long.parseLong(dealNumber);
	}

	public long getDealAmount() {
		return dealAmount;
	}

	public void setDealAmount(long dealAmount) {
		this.dealAmount = dealAmount;
	}
	
	public void setDealAmount(String dealAmount) {
		this.dealAmount = Long.parseLong(dealAmount);
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
        this.closePrice = Float.parseFloat(closePrice);
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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
