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

public class LoadIndustrySectorToDB {
	private Connection conn;
	private String industrySectorURL;
	private String[] sectors;
	public LoadIndustrySectorToDB(String propertiesFile) {
		Properties p = Utils.loadProperties(propertiesFile);
		if (p == null)
			System.exit(1);
		
		industrySectorURL=p.getProperty("industrySectorURL");
		sectors=p.getProperty("sectors").split(";");
		
		conn = Utils.connectLocal(p);
		if (conn == null) {
			System.exit(0);
		}
	}
	
    public static void main(String[] args) throws InterruptedException {
    	LoadIndustrySectorToDB loadHistoricalDataToDB=new LoadIndustrySectorToDB(args[0]);
    	loadHistoricalDataToDB.startLoadData(args[0]);
        System.out.println("done!");
    }

	private void startLoadData(String propertiesFile) throws InterruptedException {
		WebDriver driver = GUWebDriver.getInstance(propertiesFile);
		for (int i=0;i<sectors.length;i++) {
			driver.get(industrySectorURL+"?"+sectors[i]);
			Thread.sleep(5000);
			WebElement element = driver.findElement(By.xpath("//div//div//div//span"));
			String sector=element.getText();
			element = driver.findElement(By.id("StockListPage"));
			String tableSource=element.getAttribute("innerHTML");
	        getDataRow(sector,tableSource);
	        
	        String[] pageNumber=driver.findElement(By.id("pagenum")).getText().split("/");
			List<WebElement> pageDivs;
	        while (!pageNumber[0].equals(pageNumber[1])) {
	        	pageDivs=driver.findElements(By.xpath("//body//div//div//div//a"));
	        	
	        	for (WebElement e:pageDivs) {
	        		if (e.getAttribute("title").equals("下一页")) {
	        			e.click();
	        			Thread.sleep(2000);
	        			driver.findElement(By.id("StockListPage"));
	        			tableSource=element.getAttribute("innerHTML");
	        	        getDataRow(sector,tableSource);
	        			break;
	        		}
	        	}

	        	pageNumber=driver.findElement(By.id("pagenum")).getText().split("/");
	        }
		}
	}
	
	public void getDataRow(String sector,String content) {
	    content=content.replaceAll("</td>", " </td>");
        String dataStart = "<tr>";
        String dataEnd = "</tr>";
        int startIndex = content.indexOf(dataStart);
        int endIndex;
        while (startIndex >= 0) {
            content = content.substring(startIndex);
            endIndex = content.indexOf(dataEnd);
            String dataString = content.substring(0, endIndex);
            dataString = Jsoup.parse(dataString).text()+"\r\n";
            if (!dataString.startsWith("代码")) {
            	saveSectorToDB(sector, dataString);
            }
            
            content = content.substring(endIndex);
            startIndex = content.indexOf(dataStart);
        }
    }

	private void saveSectorToDB(String sector, String record) {
		try {
			String[] tokens=record.split(" ");
			String code=tokens[0];
			
			PreparedStatement psL = conn.prepareStatement("UPDATE stocks SET "
					+ " industrySector=? WHERE code=? ");

			psL.setString(1, sector);
			psL.setString(2, code);
			psL.execute();
			
			psL.close();
		} catch (SQLException e) {
			System.err.println("Insert/Update data error: " + e);
			e.printStackTrace();
		}
		
	}
}
