package gupiao.china;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import org.jsoup.Jsoup;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import gupiao.general.Stock;
import gupiao.general.StockDealRecord;

public class LoadDealRecordToDB {
	private Connection conn;
	private String dealRecordURL;
	private String currentDate;
	public LoadDealRecordToDB(String propertiesFile) {
		Properties p = Utils.loadProperties(propertiesFile);
		if (p == null)
			System.exit(1);
		
		dealRecordURL=p.getProperty("dealRecordURL");
		
		conn = Utils.connectLocal(p);
		if (conn == null) {
			System.exit(0);
		}
	}
	
    public static void main(String[] args) throws InterruptedException {
    	LoadDealRecordToDB loadHistoricalDataToDB=new LoadDealRecordToDB(args[0]);
    	loadHistoricalDataToDB.startLoadData(args[0]);
        System.out.println("done!");
    }

	private void startLoadData(String propertiesFile) throws InterruptedException {
		WebDriver driver = GUWebDriver.getInstance(propertiesFile);
		driver.get(dealRecordURL);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		currentDate=sdf.format(new Date());
		WebElement element = driver.findElement(By.id("tbl_wrap"));
		String tableSource=element.getAttribute("innerHTML");
		getDataRow(tableSource);
        
		List<WebElement> pages;
       	pages=driver.findElement(By.id("tbl_wrap")).findElements(By.xpath("//div//div//div//div//div//a"));
       	for (int i=0;i<pages.size();i++) {
       		if (pages.get(i).getText().equals("下一页") && !pages.get(i).getAttribute("class").equalsIgnoreCase("pagedisabled")) {
       			pages.get(i).click();
       			Thread.sleep(2000);
       			element = driver.findElement(By.id("tbl_wrap"));
       			tableSource=element.getAttribute("innerHTML");
       			getDataRow(tableSource);
       	        pages=driver.findElement(By.id("tbl_wrap")).findElements(By.xpath("//div//div//div//div//div//a"));
       	        i=0;
       		}
       	}
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
        return records;
    }

	private void saveStockToDB(String record) {
		try {
			String[] tokens=record.replaceAll("　", "").replaceAll(" ", "").split(";");
			StockDealRecord stockDR= new StockDealRecord();
			stockDR.setCode(tokens[0].substring(2));
			stockDR.setName(tokens[1]);
			stockDR.setDate(currentDate);
			stockDR.setClosePrice(tokens[2].trim());
			stockDR.setOpenPrice(tokens[8]);
			stockDR.setHighestPrice(tokens[9]);
			stockDR.setLowestPrice(tokens[10]);
			stockDR.setDealNumber(tokens[11]);
			stockDR.setDealAmount(tokens[12]);
			
			PreparedStatement psL = conn.prepareStatement("SELECT * FROM stocks WHERE code=?");
			psL.setString(1, stockDR.getCode());
			ResultSet rsL = psL.executeQuery();
			if (!rsL.next()) {
				psL = conn.prepareStatement("INSERT INTO stocks"
						+ " (code,name,closePrice,dealNumber) "
						+ "VALUES (?,?,?,?)");
	
				psL.setString(1, stockDR.getCode());
				psL.setString(2, stockDR.getName());
				psL.setFloat(3, stockDR.getClosePrice());
				psL.setBigDecimal(4, stockDR.getDealNumber());
	
				psL.execute();
			} else {
				psL = conn.prepareStatement("UPDATE stocks SET "
						+ " code=?,name=?,closePrice=?,dealNumber=? WHERE code=? ");
	
				psL.setString(1, stockDR.getCode());
				psL.setString(2, stockDR.getName());
				psL.setFloat(3, stockDR.getClosePrice());
				psL.setBigDecimal(4, stockDR.getDealNumber());
				psL.setString(5, stockDR.getCode());
	
				psL.execute();
			}
			rsL.close();
			
			if (stockDR.getOpenPrice()>0) {
				LoadHistoricalDataToDB.saveStockDealToDB(conn, stockDR, true);
			}
			
			psL.close();
		} catch (SQLException e) {
			System.err.println("Insert/Update data error: " + e);
			e.printStackTrace();
		}
		
	}
}
