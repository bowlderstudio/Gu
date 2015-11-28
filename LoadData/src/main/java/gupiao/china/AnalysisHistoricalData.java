package gupiao.china;

import gupiao.general.StockDealRecord;
import gupiao.general.Stock;
import gupiao.general.StockAnalysisResult;
import gupiao.general.StockComparator;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

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
	private String diagramURL;
	private float highestMarketPrice;
	private boolean downloadDiagram;
	private HashMap<String, Stock> stockMap;

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
		highestMarketPrice = Float.parseFloat(p.getProperty("highestMarketPrice"));
		downloadDiagram = Boolean.parseBoolean(p.getProperty("downloadDiagram"));
		conn = Utils.connectLocal(p);
		if (conn == null) {
			System.exit(0);
		}
	}

	public static void main(String[] args) {
		AnalysisHistoricalData analysisData = new AnalysisHistoricalData(args[0]);
		analysisData.loadStockList();
		analysisData.analysisDataList();
		System.out.println("done!");
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
				stock.setIncreaseRate(Float.parseFloat(tokens[3]));
				stock.setDealNumber(Long.parseLong(tokens[4]));
				stock.setChangeRate(Float.parseFloat(tokens[5]));
				stock.setCurrentMarketPrice(Float.parseFloat(tokens[6]));
				stock.setTotalMarketPrice(Float.parseFloat(tokens[7]));
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
		for (Map.Entry<String, Stock> entry : stockMap.entrySet()) {
			analysisData(entry.getValue());
		}
	}

	private void analysisData(Stock stock) {
		String stockCode = stock.getCode();
		// TODO for debug
		// if (!stockCode.equals("600735"))
		// return;
		List<StockDealRecord> stockRecord = getDealRecord(stockCode);
		if (stockRecord.size() == 0) {
			return;
		}
		// check if the stock has been closed
		if (!stockRecord.get(stockRecord.size() - 1).getDate().equals(lastDate)) {
			return;
		}
		float closePrice = 0;
		float highestPrice = 0;
		float lowestPrice = 10000;

		StockDealRecord sr;
		StockAnalysisResult sAnalysis;
		for (int i = 1; i < stockRecord.size(); i++) {
			sr = stockRecord.get(i);
			if (sr.getDate().compareTo(analysisPriceStartDate) < 0) {
				continue;
			}
			if (fuquan(stockRecord.get(i - 1), stockRecord.get(i))) {
				highestPrice = stockRecord.get(i).getClosePrice();
				lowestPrice = stockRecord.get(i).getClosePrice();
			}
			closePrice = sr.getClosePrice();
			if (closePrice > highestPrice) {
				highestPrice = closePrice;
			}
			if (closePrice < lowestPrice) {
				lowestPrice = closePrice;
			}
		}

		if (!calculateMACD(stockRecord))
			return;

		if (isExpectedStock(stockRecord)) {
			sAnalysis = new StockAnalysisResult();
			sAnalysis.setCode(stock.getCode());
			sAnalysis.setName(stock.getName());
			sAnalysis.setCurrentPrice(closePrice);
			sAnalysis.setHighestPrice(highestPrice);
			sAnalysis.setLowestPrice(lowestPrice);
			// System.out.println("step1");
			String data = sAnalysis.toString() + "\r\n";

			LoadHistoricalData.saveDataToFile(analysisResultFile, data);
			// download diagram
			if (downloadDiagram) {
				String imageUrl;
				if (stockCode.startsWith("0") || stockCode.startsWith("3")) {
					imageUrl = diagramURL.replaceAll("CODE%", stockCode).replaceAll("TYPE%", "2");
				} else {
					imageUrl = diagramURL.replaceAll("CODE%", stockCode).replaceAll("TYPE%", "1");
				}

				if (isMACDRaise(stockRecord)) {
					saveImage(imageUrl, diagramFold + "HistogramUp" + stockCode + ".png");
				} else if (isMacdMinus(stockRecord)) {
					saveImage(imageUrl, diagramFold + "HistogramMinus" + stockCode + ".png");
				} else if (isMacdDown(stockRecord)) {
					saveImage(imageUrl, diagramFold + "HistogramDown" + stockCode + ".png");
				}
			}
		}
	}

	private boolean isExpectedStock(List<StockDealRecord> stockRecord) {
		return isExpectedMarketPrice(stockRecord.get(0).getCode())
				&& isExceptedPrice(stockRecord.get(stockRecord.size() - 1).getClosePrice());
	}

	private boolean isExpectedMarketPrice(String code) {
		return stockMap.get(code).getTotalMarketPrice() <= highestMarketPrice;
	}

	private boolean fuquan(StockDealRecord record1, StockDealRecord record2) {
		if (Math.abs(record1.getClosePrice() - record2.getClosePrice()) / record1.getClosePrice() >= 0.15) {
			return true;
		}
		return false;
	}

	private List<StockDealRecord> getDealRecord(String code) {
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
				sr.setOpenPrice(rsL.getFloat("closePrice"));
				sr.setOpenPrice(rsL.getFloat("highestPrice"));
				sr.setOpenPrice(rsL.getFloat("lowestPrice"));
				sr.setDealAmount(rsL.getFloat("dealAmount"));
				sr.setDealNumber(rsL.getLong("dealNumber"));
				records.add(sr);
			}
			rsL.close();
			psL.close();
		} catch (SQLException e) {
			System.err.println("fetch data for " + code + " error: " + e);
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
		// Sort data by date first
		Collections.sort(stocks, new StockComparator());
		if (stocks.size() < 40) {
			System.out.println("Too little date to calculate MACD");
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

		// printStocks(stocks);
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
