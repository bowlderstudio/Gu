package gupiao.china;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;
import org.jsoup.Jsoup;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import gupiao.general.Stock;
import gupiao.general.StockDealRecord;

public class LoadStockListToDB {
	private Connection conn;
	private String marketPriceURL;
	private String currentDate;
	public LoadStockListToDB(String propertiesFile) {
		Properties p = Utils.loadProperties(propertiesFile);
		if (p == null)
			System.exit(1);
		
		marketPriceURL=p.getProperty("marketPriceURL");
		
		conn = Utils.connectLocal(p);
		if (conn == null) {
			System.exit(0);
		}
	}
	
    public static void main(String[] args) throws InterruptedException {
    	LoadStockListToDB loadHistoricalDataToDB=new LoadStockListToDB(args[0]);
    	loadHistoricalDataToDB.startLoadData(args[0]);
        System.out.println("done!");
    }

	private void startLoadData(String propertiesFile) throws InterruptedException {
		WebDriver driver = GUWebDriver.getInstance(propertiesFile);
		driver.get(marketPriceURL);
		WebElement element = driver.findElement(By.id("spanUpdateTime"));
		currentDate=element.getText();
		element = driver.findElement(By.id("divContainer"));
		String tableSource=element.getAttribute("innerHTML");
        getDataRow(tableSource);
        
		WebElement pageDiv;
        List<WebElement> pages;
        while (true) {
        	pageDiv=driver.findElement(By.id("pageDiv1"));
            pages=pageDiv.findElements(By.tagName("a"));
        	if (pages.get(pages.size()-1).getText().equals("下一页") && pages.get(pages.size()-1).isEnabled()) {
        		pages.get(pages.size()-1).click();
        		Thread.sleep(2000);
        		element = driver.findElement(By.id("divContainer"));
                tableSource=element.getAttribute("innerHTML");
                getDataRow(tableSource);
        	} else {
        		break;
        	}
        }
        Thread.sleep(2000);
		element = driver.findElement(By.id("divContainer"));
        tableSource=element.getAttribute("innerHTML");
        getDataRow(tableSource);
	}
	
	public String getDataRow(String content) {
	    String records="";
	    content=content.replaceAll("</td>", ";</td>").replaceAll("</th>", ";</th>");
        String dataStart = "<tr class=";
        String dataEnd = "</tr>";
        int startIndex = content.indexOf(dataStart);
        int endIndex;
        while (startIndex >= 0) {
            content = content.substring(startIndex);
            endIndex = content.indexOf(dataEnd);
            String dataString = content.substring(0, endIndex);
            dataString = Jsoup.parse(dataString).text().replaceAll(",", "")+"\r\n";
            saveStockToDB(dataString);
            content = content.substring(endIndex);
            startIndex = content.indexOf(dataStart);
        }
        System.out.println(records);
        return records;
    }

	private void saveStockToDB(String record) {
		try {
			String[] tokens=record.split(";");
			Stock stock= new Stock();
			stock.setCode(tokens[0]);
			stock.setName(tokens[1]);
			stock.setClosePrice(tokens[2]);
			stock.setIncreaseRate(tokens[3]);
			stock.setDealNumber(tokens[4]);
			stock.setChangeRate(tokens[5]);
			stock.setCurrentMarketPrice(tokens[6]);
			stock.setTotalMarketPrice(tokens[7]);
			
			PreparedStatement psL = conn.prepareStatement("SELECT * FROM stocks WHERE code=?");
			psL.setString(1, stock.getCode());
			ResultSet rsL = psL.executeQuery();
			if (!rsL.next()) {
				psL = conn.prepareStatement("INSERT INTO stocks"
						+ " (code,name,changeRate,closePrice,increastRate,currentMarketPrice"
						+ ",totalMarketPrice,dealNumber) "
						+ "VALUES (?,?,?,?,?,?,?,?)");
	
				psL.setString(1, stock.getCode());
				psL.setString(2, stock.getName());
				psL.setFloat(3, stock.getChangeRate());
				psL.setFloat(4, stock.getClosePrice());
				psL.setFloat(5, stock.getIncreaseRate());
				psL.setBigDecimal(6, stock.getCurrentMarketPrice());
				psL.setBigDecimal(7, stock.getTotalMarketPrice());
				psL.setBigDecimal(8, stock.getDealNumber());
	
				psL.execute();
			} else {
				psL = conn.prepareStatement("UPDATE stocks SET "
						+ " code=?,name=?,changeRate=?,closePrice=?,increastRate=?,currentMarketPrice=?"
						+ ",totalMarketPrice=?,dealNumber=? WHERE code=? ");
	
				psL.setString(1, stock.getCode());
				psL.setString(2, stock.getName());
				psL.setFloat(3, stock.getChangeRate());
				psL.setFloat(4, stock.getClosePrice());
				psL.setFloat(5, stock.getIncreaseRate());
				psL.setBigDecimal(6, stock.getCurrentMarketPrice());
				psL.setBigDecimal(7, stock.getTotalMarketPrice());
				psL.setBigDecimal(8, stock.getDealNumber());
				psL.setString(9, stock.getCode());
	
				psL.execute();
			}
			rsL.close();
			
			StockDealRecord stockDeal=new StockDealRecord();
			stockDeal.setCode(stock.getCode());
			stockDeal.setName(stock.getName());
			stockDeal.setClosePrice(stock.getClosePrice());
			stockDeal.setDate(currentDate);
			stockDeal.setDealNumber(stock.getDealNumber());
			LoadHistoricalDataToDB.saveStockDealToDB(conn, stockDeal, false);
			
			psL.close();
		} catch (SQLException e) {
			System.err.println("Insert/Update data error: " + e);
			e.printStackTrace();
		}
		
	}
}
