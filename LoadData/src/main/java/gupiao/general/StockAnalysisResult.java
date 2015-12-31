package gupiao.general;

public class StockAnalysisResult {	
	private String code;
	private String name;
	private String industrySector;
	private String industrySubSector;
	private float highestPrice;
	private float lowestPrice;
	private float currentPrice;
	//days from the last open day
	private int daysFromLastOpen;
	private int daysFromLowestPrice;
	private PriceTrend priceTrend;
	private int redKline;
	private boolean redSoldier;

	public enum PriceTrend {
		GODOWN("GoDown"),GOUPGREEN("GoUpGreen"),GOUPMIX("GoUpMix"),GOUPRED("GoUpRed");
		private String code;
        private PriceTrend(String code) {
            this.code = code;
        }
        @Override
        public String toString() {
            return String.valueOf(code);
        }
	};
	
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
	public float getPriceRate() {
		return (this.currentPrice-this.lowestPrice)/(this.highestPrice-this.lowestPrice);
	}
	
	public int getDaysFromLastOpen() {
		return daysFromLastOpen;
	}
	public void setDaysFromLastOpen(int daysFromLastOpen) {
		this.daysFromLastOpen = daysFromLastOpen;
	}
	public int getDaysFromLowestPrice() {
		return daysFromLowestPrice;
	}
	public void setDaysFromLowestPrice(int daysFromLowestPrice) {
		this.daysFromLowestPrice = daysFromLowestPrice;
	}
	public static String getTitles() {
		return "code,name,current price, highest price, lowest price, rate to high, rate to low, price rate,days from last open, days from lowest price, priceTrend, redKline, redSoldier, industrySector, industrySubSector";
	}
	
	public PriceTrend getPriceTrend() {
		return priceTrend;
	}
	public void setPriceTrend(PriceTrend priceTrend) {
		this.priceTrend = priceTrend;
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
	public int getRedKline() {
		return redKline;
	}
	public void setRedKline(int redKline) {
		this.redKline = redKline;
	}
	public boolean isRedSoldier() {
		return redSoldier;
	}
	public void setRedSoldier(boolean redSoldier) {
		this.redSoldier = redSoldier;
	}
	public String toString() {
		return this.code+","+this.name+","+this.currentPrice+","+this.highestPrice+","
				+this.lowestPrice+","+getRateToHigh()+","+getRateToLow()+","+getPriceRate()+","
				+this.daysFromLastOpen+","+this.daysFromLowestPrice+","+this.priceTrend.toString()+","
				+this.redKline+","+this.redSoldier+","+this.industrySector+","+this.industrySubSector;
	}
}
