package gupiao.china;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;
import org.jsoup.Jsoup;
import gupiao.general.Stock;
import gupiao.general.StockDealRecord;

public class LoadHistoricalDataToDB {
	private Connection conn;
	String loadStartDate;
	String loadStockListFile;
	private boolean LoadURLByWebClient;
	private static String season;
	String errorFile;
	private String loadURL;
	private boolean loadAllData;
	
	public LoadHistoricalDataToDB(String propertiesFile) {
		Properties p = Utils.loadProperties(propertiesFile);
		if (p == null)
			System.exit(1);
		
		loadStartDate = p.getProperty("loadStartDate");
		loadStockListFile= p.getProperty("loadStockListFile");
		errorFile=p.getProperty("errorFile");
		season=p.getProperty("season");
		loadURL=p.getProperty("loadURL")+season;
		loadAllData=Boolean.parseBoolean(p.getProperty("loadAllData"));
		LoadURLByWebClient=Boolean.parseBoolean(p.getProperty("LoadURLByWebClient"));
		
		conn = Utils.connectLocal(p);
		if (conn == null) {
			System.exit(0);
		}
	}
	
    public static void main(String[] args) {
    	LoadHistoricalDataToDB loadHistoricalDataToDB=new LoadHistoricalDataToDB(args[0]);
    	loadHistoricalDataToDB.cleanOldData();
    	loadHistoricalDataToDB.startLoadData();
        System.out.println("done!");
    }

	private void cleanOldData() {
		Utils.removeFile(this.errorFile);
	}

	private void startLoadData() {
		ArrayList<Stock> stockList=getStockListFromDB(conn);//getStockList();
		StockDealRecord stockDeal;
		String url;
		for (Stock stock:stockList) {
			if (this.hasLoaded(stock.getCode(), season)) {
				continue;
			} else {
				url=loadURL.replaceAll("CODE%", stock.getCode());
				String content=LoadURLByWebClient?Utils.getUrlSourceByWebClient(url)
						:Utils.getUrlSourceByReader(url);
				if (content.isEmpty()) {
					continue;
				}
				System.out.println("Start to load data for code " + stock.getCode());
				String dataStart = "<a target='_blank' href='http://vip.stock.finance.sina.com.cn/quotes_service/view/vMS_tradehistory.php?";
		        String dataEnd = "</tr>";
		        int startIndex = content.indexOf(dataStart);
		        int endIndex;
		        while (startIndex >= 0) {
		        	content = content.substring(startIndex);
		            endIndex = content.indexOf(dataEnd);
		            String dataString = content.substring(0, endIndex);
		            dataString = Jsoup.parse(dataString).text().replaceAll(" ", ",");
		            String[] token=Jsoup.parse(dataString).text().split(",");
		            stockDeal=new StockDealRecord();
		            stockDeal.setCode(stock.getCode());
		            stockDeal.setName(stock.getName());
		            stockDeal.setDate(token[0]);
		            stockDeal.setOpenPrice(token[1]);
		            stockDeal.setHighestPrice(token[2]);
		            stockDeal.setClosePrice(token[3]);
		            stockDeal.setLowestPrice(token[4]);
		            stockDeal.setDealNumber(token[5]);
		            stockDeal.setDealAmount(token[6]);
		            
		            if (loadAllData) {
		            	saveStockDealToDB(conn, stockDeal, true);
		            } else if (dataString.substring(0, 10).compareTo(loadStartDate) >=0) {
		            	saveStockDealToDB(conn, stockDeal, true);
		            }
		            content = content.substring(endIndex);
		            startIndex = content.indexOf(dataStart);
		        }
		        addLoaded(conn, stock.getCode(),season);
			}
		}
	}
	
	private boolean hasLoaded(String code, String season) {
		boolean loaded=false;
		try {
			PreparedStatement psL = conn.prepareStatement("SELECT * FROM stockLoadRecord WHERE code=? AND season=?");
			psL.setString(1, code);
			psL.setString(2, season);
			ResultSet rsL = psL.executeQuery();
			if (rsL.next()) {
				loaded=true;
			}
		} catch (SQLException e) {
			System.err.println("Fetch loaded data error: " + e);
			e.printStackTrace();
		}
		return loaded;
	}

	public static void saveStockDealToDB(Connection conn, StockDealRecord stockDeal, boolean allowedUpdate) {
		try {
			PreparedStatement psL = conn.prepareStatement("SELECT * FROM stockDealRecord WHERE date=? AND code=?");
			psL.setString(1, stockDeal.getDate());
			psL.setString(2, stockDeal.getCode());
			ResultSet rsL = psL.executeQuery();
			if (!rsL.next()) {
				psL = conn.prepareStatement("INSERT INTO stockDealRecord"
						+ " (code,name,date,openPrice,closePrice,highestPrice,lowestPrice,dealAmount,dealNumber) "
						+ "VALUES (?,?,?,?,?,?,?,?,?)");
	
				psL.setString(1, stockDeal.getCode());
				psL.setString(2, stockDeal.getName());
				psL.setString(3, stockDeal.getDate());
				psL.setFloat(4, stockDeal.getOpenPrice());
				psL.setFloat(5, stockDeal.getClosePrice());
				psL.setFloat(6, stockDeal.getHighestPrice());
				psL.setFloat(7, stockDeal.getLowestPrice());
				psL.setBigDecimal(8, stockDeal.getDealAmount());
				psL.setBigDecimal(9, stockDeal.getDealNumber());
	
				psL.execute();
			} else if (allowedUpdate){
				psL = conn.prepareStatement("UPDATE stockDealRecord SET"
						+ " code=?,name=?,date=?,openPrice=?,closePrice=?,highestPrice=?,lowestPrice=?,dealAmount=?,dealNumber=? "
						+ " WHERE code=? and date=?");
	
				psL.setString(1, stockDeal.getCode());
				psL.setString(2, stockDeal.getName());
				psL.setString(3, stockDeal.getDate());
				psL.setFloat(4, stockDeal.getOpenPrice());
				psL.setFloat(5, stockDeal.getClosePrice());
				psL.setFloat(6, stockDeal.getHighestPrice());
				psL.setFloat(7, stockDeal.getLowestPrice());
				psL.setBigDecimal(8, stockDeal.getDealAmount());
				psL.setBigDecimal(9, stockDeal.getDealNumber());
				psL.setString(10, stockDeal.getCode());
				psL.setString(11, stockDeal.getDate());
	
				psL.execute();
			}
			rsL.close();
			psL.close();
		} catch (SQLException e) {
			System.err.println("Insert/update data error: " + e);
			e.printStackTrace();
		}
		
	}

	private ArrayList<Stock> getStockListFromFile() {
		BufferedReader fileReader = null;
		ArrayList<Stock> stockList=new ArrayList<Stock>();
        // Delimiter used in CSV file
        final String DELIMITER = ";";
        try {
            String line = "";
            fileReader = new BufferedReader(new FileReader(loadStockListFile));
            while ((line = fileReader.readLine()) != null) {
            	if (line.startsWith("#"))
            		continue;
                // Get all tokens available in line
                String[] tokens = line.split(DELIMITER);
                stockList.add(new Stock(tokens[0],tokens[1]));
            }
        } catch (Exception e) {
        	System.err.println("Load data error: " + e);
            e.printStackTrace();
        } finally {
            try {
                fileReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return stockList;
	}
	
	public static ArrayList<Stock> getStockListFromDB(Connection conn) {
		ArrayList<Stock> stocks=new ArrayList<Stock>();
		try {
			PreparedStatement psL = conn.prepareStatement("SELECT * FROM stocks ORDER BY code");
			ResultSet rsL = psL.executeQuery();
			while (rsL.next()) {
				Stock stock=new Stock();
				stock.setCode(rsL.getString("code"));
				stock.setName(rsL.getString("name"));
				stock.setChangeRate(rsL.getFloat("changeRate"));
				stock.setClosePrice(rsL.getFloat("closePrice"));
				stock.setIncreaseRate(rsL.getFloat("increastRate"));
				stock.setCurrentMarketPrice(rsL.getBigDecimal("currentMarketPrice"));
				stock.setTotalMarketPrice(rsL.getBigDecimal("totalMarketPrice"));
				stock.setDealNumber(rsL.getBigDecimal("dealNumber"));
				stock.setIndustrySector(rsL.getString("industrySector"));
				stock.setIndustrySubSector(rsL.getString("industrySubSector"));
				stocks.add(stock);
			}
		} catch (SQLException e) {
			System.err.println("Fetch stock list error: " + e);
			e.printStackTrace();
		}
			
		return stocks;
	}
	
	private static ArrayList<Stock> addLoaded(Connection conn, String code, String season) {
		ArrayList<Stock> stocks=new ArrayList<Stock>();
		try {
			PreparedStatement psL = conn.prepareStatement("INSERT INTO stockLoadRecord (code, season) VALUES (?,?)");
			psL.setString(1, code);
			psL.setString(2, season);
			psL.execute();
		} catch (SQLException e) {
			System.err.println("Fetch stock list error: " + e);
			e.printStackTrace();
		}
			
		return stocks;
	}
}
