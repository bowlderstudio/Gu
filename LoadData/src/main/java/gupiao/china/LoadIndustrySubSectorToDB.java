package gupiao.china;

/**
 * Load from http://www.shdjt.com/flsort.asp?lb=
 */
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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
	public LoadIndustrySubSectorToDB(String propertiesFile) {
		Properties p = Utils.loadProperties(propertiesFile);
		if (p == null)
			System.exit(1);
		
		industrySectorURL=p.getProperty("industrySubSectorURL");
		
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
		driver.get(industrySectorURL);
		Thread.sleep(2000);
		WebElement element = driver.findElement(By.id("bklist"));
		if (element==null) {
			System.out.println("Cannot find bklist");
			return;
		}
		List<WebElement> bkList=element.findElements(By.xpath("//tr//td//a"));
		List<String> bkListUrl=new ArrayList<String>();
		for (WebElement e:bkList) {
			if (e.getAttribute("href").contains("list.html")) {
				bkListUrl.add(e.getText()+";"+e.getAttribute("href"));
			}
		}

		String[] banKuai;
		for (String bk:bkListUrl) {
			banKuai=bk.split(";");
			readBanKuai(driver, banKuai[0],banKuai[1]);
		}

	}
	
	public void readBanKuai(WebDriver driver, String sector,String url) throws InterruptedException {
		driver.get("http://127.0.0.1/");
		//driver.get(url);
		driver.get("http://quote.eastmoney.com/center/list.html#28002465_0_2");
		Thread.sleep(2000);
		WebElement element = driver.findElement(By.id("fixed"));
		if (element==null) {
			System.out.println("unable find fixed for sector "+sector+", URL="+url);
			return;
		}

		String tableSource=element.getAttribute("innerHTML");
        getDataRow(sector,tableSource);
        
        element=driver.findElement(By.id("pagenav"));
        List<WebElement> aElements=element.findElements(By.xpath("//a"));
        for (WebElement e:aElements) {
        	if (e.getText().equalsIgnoreCase("下一页") && e.getAttribute("class").equalsIgnoreCase("disable")) {
        		break;
        	} else if (e.getText().equalsIgnoreCase("下一页")) {
        		e.click();
        		Thread.sleep(2000);
        		element = driver.findElement(By.id("fixed"));
        		if (element==null) {
        			System.out.println("unable find fixed for sector "+sector+", URL="+url);
        			return;
        		}

        		tableSource=element.getAttribute("innerHTML");
                getDataRow(sector,tableSource);
                aElements=element.findElements(By.xpath("//a"));
        	} 
        }
    }
	
	public void getDataRow(String sector,String content) {
	    content=content.replaceAll("</td>", ";</td>");
        String dataStart = "<tr>";
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
			if (tokens.length<2)
				return;
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
