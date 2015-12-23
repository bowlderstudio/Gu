package gupiao.china;

/**
 * Load from http://www.shdjt.com/flsort.asp?lb=
 */
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

public class LoadIndustrySubSectorToDB {
	private Connection conn;
	private String industrySectorURL;
	private String[] sectors;
	public LoadIndustrySubSectorToDB(String propertiesFile) {
		Properties p = Utils.loadProperties(propertiesFile);
		if (p == null)
			System.exit(1);
		
		industrySectorURL=p.getProperty("industrySubSectorURL");
		sectors=p.getProperty("subSectors").split(";");
		
		conn = Utils.connectLocal(p);
		if (conn == null) {
			System.exit(0);
		}
	}
	
    public static void main(String[] args) throws InterruptedException {
    	LoadIndustrySubSectorToDB loadHistoricalDataToDB=new LoadIndustrySubSectorToDB(args[0]);
    	loadHistoricalDataToDB.startLoadData(args[0]);
        System.out.println("done!");
    }

	private void startLoadData(String propertiesFile) throws InterruptedException {
		WebDriver driver = GUWebDriver.getInstance(propertiesFile);
		for (int i=0;i<sectors.length;i++) {
			driver.get(industrySectorURL+sectors[i]);
			Thread.sleep(5000);
			WebElement element = driver.findElement(By.xpath("//table//tbody//tr//td//div//form//font"));
			String sector=element.getText().replaceAll("板块个股.历史数据回放：", "");
			element = driver.findElement(By.id("senfe"));
			String tableSource=element.getAttribute("innerHTML");
	        getDataRow(sector,tableSource);
		}
	}
	
	public void getDataRow(String sector,String content) {
	    content=content.replaceAll("</td>", ";</td>");
        String dataStart = "<tr height=";
        String dataEnd = "</tr>";
        int startIndex = content.indexOf(dataStart);
        int endIndex;
        while (startIndex >= 0) {
            content = content.substring(startIndex);
            endIndex = content.indexOf(dataEnd);
            String dataString = content.substring(0, endIndex);
            dataString = Jsoup.parse(dataString).text()+"\r\n";
            saveSectorToDB(sector, dataString);
            
            content = content.substring(endIndex);
            startIndex = content.indexOf(dataStart);
        }
    }

	private void saveSectorToDB(String sector, String record) {
		try {
			String[] tokens=record.split(";");
			String code=tokens[1].trim();
			
			PreparedStatement psL = conn.prepareStatement("UPDATE stocks SET "
					+ " industrySubSector=? WHERE code=? ");

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
