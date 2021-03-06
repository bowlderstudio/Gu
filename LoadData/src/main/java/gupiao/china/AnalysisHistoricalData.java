package gupiao.china;

import gupiao.general.StockDealRecord;
import gupiao.general.Stock;
import gupiao.general.StockAnalysisResult;
import gupiao.general.StockAnalysisResult.LineHead;
import gupiao.general.StockAnalysisResult.PriceTrend;
import gupiao.general.StockComparator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class AnalysisHistoricalData {
	private Connection conn;
	private float lowestPrice;
	private float highestPrice;
	// start date for analysis
	private String macdStartDate;
	private String lastDate;
	private String analysisPriceStartDate;
	private String loadStockListFile;
	private String analysisResultFile;
	private String diagramFold;
	private String stockRecordFold;
	private String diagramURL;
	private BigDecimal highestMarketPrice;
	private float rateToHigh;
	private float rateToLow;
	private float priceRate;
	private boolean downloadDiagram;
	private HashMap<String, Stock> stockMap;
	private int priceTrendDays=5;
	private int minCloseDays;

	public AnalysisHistoricalData(String propertiesFile) {
		Properties p = Utils.loadProperties(propertiesFile);
		if (p == null)
			System.exit(1);

		analysisPriceStartDate = p.getProperty("analysisPriceStartDate");
		macdStartDate = p.getProperty("MACDStartDate");
		lowestPrice = Float.parseFloat(p.getProperty("lowestPrice"));
		highestPrice = Float.parseFloat(p.getProperty("highestPrice"));
		loadStockListFile = p.getProperty("loadStockListFile");
		analysisResultFile = p.getProperty("analysisResultFile");
		lastDate = p.getProperty("lastDate");
		diagramFold = p.getProperty("diagramFold");
		diagramURL = p.getProperty("diagramURL");
		stockRecordFold = p.getProperty("stockRecordFold");
		highestMarketPrice = new BigDecimal(p.getProperty("highestMarketPrice"));
		downloadDiagram = Boolean.parseBoolean(p.getProperty("downloadDiagram"));
		rateToHigh = Float.parseFloat(p.getProperty("rateToHigh"));
		rateToLow = Float.parseFloat(p.getProperty("rateToLow"));
		priceRate = Float.parseFloat(p.getProperty("priceRate"));
		minCloseDays = Integer.parseInt(p.getProperty("minCloseDays", "3"));
		conn = Utils.connectLocal(p);
		if (conn == null) {
			System.exit(0);
		}
	}

	public static void main(String[] args) {
		AnalysisHistoricalData analysisData = new AnalysisHistoricalData(args[0]);
		analysisData.cleanOldData();
		analysisData.loadStockListFromDB();
		analysisData.analysisDataList();
		System.out.println("done!");
	}

	private void cleanOldData() {
		Utils.removeFile(analysisResultFile);
	}

	private void loadStockListFromDB() {
		stockMap = new HashMap<String, Stock>();
		ArrayList<Stock> stockList=LoadHistoricalDataToDB.getStockListFromDB(conn);
		for (Stock stock:stockList) {
			stockMap.put(stock.getCode(), stock);
		}
	}
	private void loadStockList() {
		stockMap = new HashMap<String, Stock>();
		BufferedReader fileReader = null;

		// Delimiter used in CSV file
		final String DELIMITER = ";";
		try {
			String line = "";
			// Create the file reader
			fileReader = new BufferedReader(new FileReader(loadStockListFile));
			// Read the file line by line
			while ((line = fileReader.readLine()) != null) {
				// Get all tokens available in line
				if (line.startsWith("#"))
					continue;
				String[] tokens = line.split(DELIMITER);
				Stock stock = new Stock(tokens[0], tokens[1]);
				stock.setClosePrice(Float.parseFloat(tokens[2]));
				stock.setIncreaseRate(Float.parseFloat(tokens[3].replaceAll("%", "")));
				stock.setDealNumber(new BigDecimal(tokens[4]));
				stock.setChangeRate(Float.parseFloat(tokens[5].replaceAll("%", "")));
				stock.setCurrentMarketPrice(tokens[6]);
				stock.setTotalMarketPrice(tokens[7]);
				stockMap.put(stock.getCode(), stock);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				fileReader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void analysisDataList() {
		Utils.saveDataToFile(analysisResultFile, StockAnalysisResult.getTitles()+"\r\n");
		for (Map.Entry<String, Stock> entry : stockMap.entrySet()) {
			analysisData(entry.getValue());
		}
	}

	private void analysisData(Stock stock) {
		String stockCode = stock.getCode();
		// TODO for debug
		if (stockCode.equals("600432")) {
			System.out.print("");
		}
		List<StockDealRecord> stockRecord = getDealRecordFromDB(stockCode);
		//List<StockDealRecord> stockRecord = getDealRecordFromFile(stockCode);
		if (stockRecord.size() == 0) {
			return;
		}
		// check if the stock has been closed
		if (isClosed(stockRecord)) {
			return;
		}
		float closePrice = 0;
		float highestPrice = 0;
		float lowestPrice = 10000;
		int daysFromLastOpen=0;
		int daysFromLowestPrice=0;
		StockDealRecord sr;
		StockAnalysisResult sAnalysis;
		for (int i = 1; i < stockRecord.size(); i++) {
			sr = stockRecord.get(i);
			if (sr.getDate().compareTo(analysisPriceStartDate) < 0) {
				continue;
			}
			if (reOpenStock(stockRecord.get(i - 1), stockRecord.get(i))) {
				daysFromLastOpen=0;
			}
			if (isFuquan(stockRecord.get(i - 1), stockRecord.get(i))) {
				highestPrice = stockRecord.get(i).getClosePrice();
				lowestPrice = stockRecord.get(i).getClosePrice();
				daysFromLastOpen=0;
				daysFromLowestPrice=0;
			} else {
				daysFromLastOpen++;
			}
			closePrice = sr.getClosePrice();
			if (closePrice > highestPrice) {
				highestPrice = closePrice;
			}
			if (closePrice < lowestPrice) {
				lowestPrice = closePrice;
				daysFromLowestPrice=0;
			} else {
				daysFromLowestPrice++;
			}
		}

		calculateMACD(stockRecord);

		if (isExpectedStock(stockRecord) && isExpectedPriceRate(highestPrice, lowestPrice, closePrice)) {
			sAnalysis = new StockAnalysisResult();
			sAnalysis.setCode(stock.getCode());
			sAnalysis.setName(stock.getName());
			sAnalysis.setIndustrySector(stock.getIndustrySector());
			sAnalysis.setIndustrySubSector(stock.getIndustrySubSector());
			sAnalysis.setCurrentPrice(closePrice);
			sAnalysis.setHighestPrice(highestPrice);
			sAnalysis.setLowestPrice(lowestPrice);
			sAnalysis.setDaysFromLowestPrice(daysFromLowestPrice);
			sAnalysis.setDaysFromLastOpen(daysFromLastOpen);
			sAnalysis.setPriceTrend(getPriceTrend(stockRecord));
			sAnalysis.setRedKline(getRedKline(stockRecord));
			sAnalysis.setRedSoldier(getRedSoldier(stockRecord));
			sAnalysis.setAverageLineSlope(getAverageLineSlope(stockRecord));
			sAnalysis.setLineHead(getLineHead(stockRecord));
			sAnalysis.setPriceVSAverageLine(getPriceToAverageLine(stockRecord));
			// System.out.println("step1");
			String data = sAnalysis.toString() + "\r\n";

			Utils.saveDataToFile(analysisResultFile, data);
			// download diagram
			if (downloadDiagram) {
				String imageUrl;
				if (stockCode.startsWith("0") || stockCode.startsWith("3")) {
					imageUrl = diagramURL.replaceAll("CODE%", stockCode).replaceAll("TYPE%", "2");
				} else {
					imageUrl = diagramURL.replaceAll("CODE%", stockCode).replaceAll("TYPE%", "1");
				}

				if (sAnalysis.getLineHead()==LineHead.HEADUP) {
					saveImage(imageUrl, diagramFold + "HEADUP" + stockCode + ".png");
				} else if (sAnalysis.getLineHead()==LineHead.HEADDOWN) {
					saveImage(imageUrl, diagramFold + "HEADDOWN" + stockCode + ".png");
				}
			}
		}
	}

	private float getPriceToAverageLine(List<StockDealRecord> stockRecord) {
		float average20=0;
		if (stockRecord.size()<20) {
			return 0;
		}
		int averageDays=20;
		for (int i=0;i<averageDays;i++) {
			average20+=stockRecord.get(stockRecord.size()-i-1).getClosePrice();
		}
		average20=average20/averageDays;
		float currentPrice=stockRecord.get(stockRecord.size()-1).getClosePrice();
		return (currentPrice-average20)/currentPrice;
	}

	private LineHead getLineHead(List<StockDealRecord> stockRecord) {
		float average5=0,average10=0,average20=0;
		if (stockRecord.size()<20) {
			return LineHead.HEADUNKNOWN;
		}
		int averageDays=20;
		for (int i=0;i<averageDays;i++) {
			if (i<5) {
				average5+=stockRecord.get(stockRecord.size()-i-1).getClosePrice();
			}
			if (i<10) {
				average10+=stockRecord.get(stockRecord.size()-i-1).getClosePrice();
			}
			average20+=stockRecord.get(stockRecord.size()-i-1).getClosePrice();
		}
		average5=average5/5;
		average10=average10/10;
		average20=average20/20;
		if (stockRecord.get(stockRecord.size()-1).getClosePrice() >=average5 
				&& average5>=average10 && average10>=average20) {
			return LineHead.HEADUP;
		} else if (stockRecord.get(stockRecord.size()-1).getClosePrice() <=average5 
				&& average5<=average10 && average10<=average20){
			return LineHead.HEADDOWN;
		} else {
			return LineHead.HEADUNKNOWN;
		}
	}

	private float getAverageLineSlope(List<StockDealRecord> stockRecord) {
		int internalDays=3;
		int averageDays=20;
		if (stockRecord.size()<averageDays+internalDays) {
			System.out.println("Too little to calculate average line slop");
			return 0;
		}
		float average1=0.0f;
		for (int i=0;i<averageDays;i++) {
			average1+=stockRecord.get(stockRecord.size()-i-1).getClosePrice();
		}
		average1=average1/averageDays;
		
		float average2=0;
		for (int i=0;i<averageDays;i++) {
			average2+=stockRecord.get(stockRecord.size()-i-internalDays-1).getClosePrice();
		}
		average2=average2/averageDays;
		float slope=(average1-average2)/internalDays;
		return slope;
	}

	private int getRedKline(List<StockDealRecord> stockRecord) {
		int days=5;
		if (stockRecord.size()<days) {
			return 0;
		}
		int redK=0;
		StockDealRecord sd;
		for (int i=0;i<days;i++) {
			sd=stockRecord.get(stockRecord.size()-i-1);
			if (sd.getClosePrice()>sd.getOpenPrice() && sd.getOpenPrice()!=0) {
				redK++;
			} else {
				break;
			}
		}
		return redK;
	}
	
	private boolean getRedSoldier(List<StockDealRecord> stockRecord) {
		if (stockRecord.size()<3) {
			return false;
		}
		boolean redS=false;
		if (stockRecord.get(1).getDealNumber().compareTo(stockRecord.get(2).getDealNumber())<=0
				|| stockRecord.get(0).getDealNumber().compareTo(stockRecord.get(2).getDealNumber())<=0
				|| stockRecord.get(0).getDealNumber().compareTo(stockRecord.get(1).getDealNumber())<=0) {
			redS=true;
		}
		return redS;
	}

	private boolean reOpenStock(StockDealRecord stockDealRecord, StockDealRecord stockDealRecord2) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		try {

			Date date1 = formatter.parse(stockDealRecord.getDate());
			Date date2 = formatter.parse(stockDealRecord2.getDate());
			long diff = date2.getTime() - date1.getTime();
			if (TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS)>=minCloseDays) {
				return true;
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return false;
	}

	private PriceTrend getPriceTrend(List<StockDealRecord> stockRecord) {
		if (stockRecord.size()<this.priceTrendDays) {
			StockDealRecord record=stockRecord.get(stockRecord.size()-1);
			if (record.getHistogram()<=0) {
				return StockAnalysisResult.PriceTrend.GODOWN;
			} else {
				return StockAnalysisResult.PriceTrend.GOUPRED; 
			}
		} else {
			StockDealRecord startRecord=stockRecord.get((stockRecord.size()-1-priceTrendDays)>=0?(stockRecord.size()-1-priceTrendDays):0);
			StockDealRecord endRecord=stockRecord.get(stockRecord.size()-1);
			if (startRecord.getHistogram()>0 && endRecord.getHistogram()>0) {
				return StockAnalysisResult.PriceTrend.GOUPRED;
			} else if (startRecord.getHistogram()<0 && endRecord.getHistogram()>0
					&& stockRecord.get(stockRecord.size()-2).getHistogram()>0
					&& stockRecord.get(stockRecord.size()-3).getHistogram()>0){
				return StockAnalysisResult.PriceTrend.GOUPMIX;
			} else {
				if (startRecord.getHistogram()<endRecord.getHistogram()) {
					return StockAnalysisResult.PriceTrend.GOUPGREEN;
				} else {
					return StockAnalysisResult.PriceTrend.GODOWN;
				}
			}
		}
	}

	private boolean isClosed(List<StockDealRecord> stockRecord) {
		if (!stockRecord.get(stockRecord.size() - 1).getDate().equals(lastDate)
				|| stockRecord.get(stockRecord.size() - 1).getDealNumber().compareTo(new BigDecimal(0))==0)
			return true;
		else
			return false;
	}

	private boolean isExpectedStock(List<StockDealRecord> stockRecord) {
		return isExpectedMarketPrice(stockRecord.get(0).getCode())
				&& isExceptedPrice(stockRecord.get(stockRecord.size() - 1).getClosePrice());
	}

	private boolean isExpectedPriceRate(float highestPrice, float lowestPrice, float currentPrice) {
		if (currentPrice/highestPrice <= this.rateToHigh
				&& lowestPrice/currentPrice >= this.rateToLow
				&& (currentPrice-lowestPrice)/(highestPrice-lowestPrice) <=this.priceRate)
			return true;
		return false;
	}

	private boolean isExpectedMarketPrice(String code) {
		if (stockMap.get(code)!=null && stockMap.get(code).getTotalMarketPrice()!=null)
			return stockMap.get(code).getTotalMarketPrice().compareTo(highestMarketPrice) <=0 ;
		else {
			System.out.println("missing market price "+code);
			return true;
		}
	}

	private boolean isFuquan(StockDealRecord record1, StockDealRecord record2) {
		if (Math.abs(record1.getClosePrice() - record2.getClosePrice()) / record1.getClosePrice() >= 0.15) {
			return true;
		}
		return false;
	}

	private List<StockDealRecord> getDealRecordFromDB(String code) {
		List<StockDealRecord> records = new ArrayList<StockDealRecord>();
		try {
			PreparedStatement psL = conn
					.prepareStatement("SELECT * FROM stockDealRecord WHERE code=? and date>=? ORDER BY date");
			psL.setString(1, code);
			psL.setString(2, macdStartDate);
			ResultSet rsL = psL.executeQuery();
			StockDealRecord sr;
			while (rsL.next()) {
				sr = new StockDealRecord();
				sr.setCode(rsL.getString("code"));
				sr.setName(rsL.getString("name"));
				sr.setDate(rsL.getString("date"));
				sr.setOpenPrice(rsL.getFloat("openPrice"));
				sr.setClosePrice(rsL.getFloat("closePrice"));
				sr.setHighestPrice(rsL.getFloat("highestPrice"));
				sr.setLowestPrice(rsL.getFloat("lowestPrice"));
				sr.setDealAmount(rsL.getBigDecimal("dealAmount"));
				sr.setDealNumber(rsL.getBigDecimal("dealNumber"));
				//the stock is closed if the price is 0
				if (sr.getClosePrice()>0) {
					records.add(sr);
				}
			}
			rsL.close();
			psL.close();
		} catch (SQLException e) {
			System.err.println("fetch data for " + code + " error: " + e);
			e.printStackTrace();
		}
		return records;
	}

	private List<StockDealRecord> getDealRecordFromFile(String stockCode) {
		List<StockDealRecord> records = new ArrayList<StockDealRecord>();
		
		String fileName=stockRecordFold+"/" + stockCode + ".txt";
        
        File f = new File(fileName);
        if(!f.exists()) {
        	System.out.println("Data for stock " + stockCode +" does not exist!");
        	return records;
        }
        BufferedReader fileReader = null;
        // Delimiter used in CSV file
        final String DELIMITER = ",";
        try {
        	//Check file exist
            String line = "";
            // Create the file reader
            fileReader = new BufferedReader(new FileReader(fileName));
            while ((line = fileReader.readLine()) != null) {
                // Get all tokens available in line
                String[] tokens = line.split(DELIMITER);
                if (tokens[0].startsWith("#"))
                    continue;
                StockDealRecord sr=new StockDealRecord();
                sr.setCode(stockCode);
				sr.setName(stockCode);
				sr.setDate(tokens[0]);
				sr.setOpenPrice(tokens[1]);
				sr.setHighestPrice(tokens[2]);
				sr.setClosePrice(tokens[3]);
				sr.setLowestPrice(tokens[4]);
				sr.setDealNumber(tokens[5]);
				sr.setDealAmount(tokens[6]);
				if (sr.getClosePrice()>0) {
					records.add(sr);
				}
            }
            // Sort data by date first
    		Collections.sort(records, new StockComparator());
    		
		} catch (Exception e) {
			System.err.println("fetch data for " + stockCode + " error: " + e);
			e.printStackTrace();
		}
		return records;
	}
	
	private static float calculateMACDIncreaseRate(String field, List<StockDealRecord> stocks) {
		float lastDay = 0, last2Day = 0;
		if (field.equalsIgnoreCase("HISTOGRAM")) {
			lastDay = stocks.get(stocks.size() - 1).getHistogram();
			last2Day = stocks.get(stocks.size() - 2).getHistogram();
		} else if (field.equalsIgnoreCase("DIFF")) {
			lastDay = stocks.get(stocks.size() - 1).getDiff();
			last2Day = stocks.get(stocks.size() - 2).getDiff();
		}

		if (lastDay < 0 && last2Day < 0) {
			return lastDay / last2Day;
		} else if (lastDay > 0 && last2Day >= 0) {
			return last2Day / lastDay;
		} else if (lastDay > 0 && last2Day < 0) {
			return -last2Day / (lastDay - last2Day);
		}
		return 0;
	}

	private static boolean isMacdMinus(List<StockDealRecord> stocks) {
		int days = 5;
		for (int i = 0; i < days; i++) {
			if (stocks.get(stocks.size() - 1 - i).getHistogram() > 0) {
				return false;
			}
		}
		return true;
	}

	private static boolean isMacdDown(List<StockDealRecord> stocks) {
		if (stocks.get(stocks.size() - 4).getHistogram() < 0
				&& stocks.get(stocks.size() - 1).getHistogram() < stocks.get(stocks.size() - 2).getHistogram()
				&& stocks.get(stocks.size() - 2).getHistogram() < stocks.get(stocks.size() - 3).getHistogram()
				&& stocks.get(stocks.size() - 3).getHistogram() < stocks.get(stocks.size() - 4).getHistogram()) {
			return true;
		}
		return false;
	}

	// Analysis MACD and filter good ones
	private static boolean isMACDRaise(List<StockDealRecord> stocks) {
		int i = stocks.size() - 1;
		if ((stocks.get(i).getHistogram() > stocks.get(i - 1).getHistogram())
				&& (stocks.get(i - 1).getHistogram() > stocks.get(i - 2).getHistogram())
				&& (stocks.get(i - 2).getHistogram() < 0) && (stocks.get(i - 1).getHistogram() < 0)
				// && (stocks.get(i).getDiff() > stocks.get(i - 1).getDiff())
				// && (stocks.get(i - 1).getDiff() > stocks.get(i -
				// 2).getDiff())
				&& (stocks.get(i - 2).getDiff() < 0) && (stocks.get(i - 1).getDiff() < 0)) {
			return true;
		}
		return false;
	}

	public static void saveImage(String imageUrl, String destinationFile) {
		try {
			System.out.println("Save image " + imageUrl);
			URL url = new URL(imageUrl);
			InputStream is = url.openStream();
			OutputStream os = new FileOutputStream(destinationFile);

			byte[] b = new byte[2048];
			int length;

			while ((length = is.read(b)) != -1) {
				os.write(b, 0, length);
			}

			is.close();
			os.close();
		} catch (IOException e) {
			System.out.println("Fail to save image " + imageUrl);
		}
	}

	private static boolean calculateMACD(List<StockDealRecord> stocks) {
		int shortdays = 12;
		int longdays = 26;
		int signaldays = 9;
		if (stocks.size() < 40) {
			System.out.println("Too little date to calculate MACD for "+stocks.get(0).getCode());
			return false;
		}
		float average12 = getAveragePriceForDays(stocks, shortdays);
		float average26 = getAveragePriceForDays(stocks, longdays);
		stocks.get(shortdays - 1).setEma12(average12);
		stocks.get(longdays - 1).setEma26(average26);
		// calculate short ema
		for (int i = shortdays; i < stocks.size(); i++) {
			stocks.get(i).setEma12(stocks.get(i).getClosePrice() * (2.0f / (shortdays + 1))
					+ stocks.get(i - 1).getEma12() * (shortdays - 1) / (shortdays + 1));
		}
		// calculate long ema
		for (int i = longdays; i < stocks.size(); i++) {
			stocks.get(i).setEma26(stocks.get(i).getClosePrice() * (2.0f / (longdays + 1))
					+ stocks.get(i - 1).getEma26() * (longdays - 1) / (longdays + 1));
		}
		// calculate MACD
		for (int i = longdays - 1; i < stocks.size(); i++) {
			stocks.get(i).setDiff(stocks.get(i).getEma12() - stocks.get(i).getEma26());
		}
		// calculate signal
		float averageMACD = 0;
		for (int i = 0; i < signaldays; i++) {
			averageMACD += stocks.get(longdays + i - 1).getDiff();
		}
		averageMACD = averageMACD / signaldays;
		stocks.get(longdays + signaldays - 2).setSignal(averageMACD);
		for (int i = longdays + signaldays - 1; i < stocks.size(); i++) {
			stocks.get(i).setSignal(stocks.get(i).getDiff() * (2.0f / (signaldays + 1))
					+ stocks.get(i - 1).getSignal() * (signaldays - 1) / (signaldays + 1));
		}
		// calculate histogram
		for (int i = longdays + signaldays - 2; i < stocks.size(); i++) {
			stocks.get(i).setHistogram((stocks.get(i).getDiff() - stocks.get(i).getSignal()) * 2);
		}

//		printStocks(stocks);
		return true;
	}

	private static float getAveragePriceForDays(List<StockDealRecord> stocks, int days) {
		float average = 0;
		for (int i = 0; i < stocks.size(); i++) {
			if (i < days)
				average += stocks.get(i).getClosePrice();
			else
				break;
		}
		return average / days;
	}

	private static void printStocks(List<StockDealRecord> stocks) {
		for (StockDealRecord s : stocks) {
			System.out.println(s.getDate() + ", " + s.getClosePrice() + ", " + s.getEma12() + ", " + s.getEma26() + ", "
					+ s.getDiff() + "," + s.getSignal() + ", " + s.getHistogram());
		}
	}

	private boolean isExceptedPrice(float price) {
		if (price != 0 && price >= this.lowestPrice && price <= this.highestPrice) {
			return true;
		} else
			return false;
	}
}
