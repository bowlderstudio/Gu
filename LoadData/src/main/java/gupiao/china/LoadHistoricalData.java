package gupiao.china;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import org.jsoup.Jsoup;

public class LoadHistoricalData {
    public static final String STARTDATE="2015-10-13";
    public static String getUrlSource(String url) {
        StringBuilder a = new StringBuilder();
        try {
            URL yahoo = new URL(url);
            URLConnection yc = yahoo.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream(), "gb2312"));
            String inputLine;
            while ((inputLine = in.readLine()) != null)
                a.append(inputLine);
            in.close();

        } catch (Exception e) {
            System.out.println("Error when get url source!");
        }
        return a.toString();
    }

    public static String getDataRow(String content, boolean getAllData) {
        String dataStart = "<a target='_blank' href='http://vip.stock.finance.sina.com.cn/quotes_service/view/vMS_tradehistory.php?";
        String dataEnd = "</tr>";
        int startIndex = content.indexOf(dataStart);
        int endIndex;
        String rowData = "";
        while (startIndex >= 0) {
            content = content.substring(startIndex);
            endIndex = content.indexOf(dataEnd);
            String dataString = content.substring(0, endIndex);
            dataString = Jsoup.parse(dataString).text().replaceAll(" ", ",");
            if (getAllData) {
            	rowData += Jsoup.parse(dataString).text() + "\r\n";
            } else if (dataString.substring(0, 10).compareTo(STARTDATE) >=0) {
                rowData += Jsoup.parse(dataString).text() + "\r\n";
            }
            content = content.substring(endIndex);
            startIndex = content.indexOf(dataStart);
        }
        return rowData;
    }

    public static void saveDataToFile(String filename, String data) {
        if (data.isEmpty())
            return;
        try {
            File newTextFile = new File(filename);

            FileWriter fw = new FileWriter(newTextFile, true);
            fw.write(data);
            fw.close();

        } catch (IOException iox) {
            // do stuff with exception
            iox.printStackTrace();
        }
    }

    public static void loadDataList(String listFile) {
        BufferedReader fileReader = null;

        // Delimiter used in CSV file
        final String DELIMITER = ";";
        try {
            String line = "";
            // Create the file reader
            fileReader = new BufferedReader(new FileReader(listFile));

            // Read the file line by line
            while ((line = fileReader.readLine()) != null) {
            	if (line.startsWith("#"))
            		continue;
                // Get all tokens available in line
                String[] tokens = line.split(DELIMITER);
                //Skip B stock
//                if (tokens[0].contains("B")) {
//                	System.out.println("Skip load "+tokens[0]);
//                	continue;
//                }
                loadData(tokens[0]);
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
    }

    public static void loadData(String code) {
        if (code.isEmpty())
            return;
        String fileName= code + ".txt";
        //Check file exist
        File f = new File(fileName);
        String sourceCode;
        if(!f.exists()) {
        	System.out.println("Start to load data for new code " + code);
            
        	String header="#date, start price, highest price, close price, lowest price, deal number, deal amount\r\n";
            saveDataToFile(fileName, header);
            
//            sourceCode = LoadHistoricalData
//                    .getUrlSource("http://money.finance.sina.com.cn/corp/go.php/vMS_MarketHistory/stockid/" + code
//                            + ".phtml?year=2014&jidu=4");
//            saveDataToFile("/disk1/gupiao/" + code + ".txt", getDataRow(sourceCode,true));
            sourceCode = LoadHistoricalData.getUrlSource("http://money.finance.sina.com.cn/corp/go.php/vMS_MarketHistory/stockid/"
                    + code + ".phtml?year=2015&jidu=1");
            saveDataToFile(fileName, getDataRow(sourceCode,true));
            sourceCode = LoadHistoricalData.getUrlSource("http://money.finance.sina.com.cn/corp/go.php/vMS_MarketHistory/stockid/"
                    + code + ".phtml?year=2015&jidu=2");
            saveDataToFile(fileName, getDataRow(sourceCode,true));
            sourceCode = LoadHistoricalData.getUrlSource("http://money.finance.sina.com.cn/corp/go.php/vMS_MarketHistory/stockid/"
                    + code + ".phtml?year=2015&jidu=3");
            saveDataToFile(fileName, getDataRow(sourceCode,true));
            sourceCode = LoadHistoricalData.getUrlSource("http://money.finance.sina.com.cn/corp/go.php/vMS_MarketHistory/stockid/"
                    + code + ".phtml?year=2015&jidu=4");
            saveDataToFile(fileName, getDataRow(sourceCode,true));
        } else {
        	System.out.println("Start to load data for code " + code);
            
	        sourceCode = LoadHistoricalData.getUrlSource("http://money.finance.sina.com.cn/corp/go.php/vMS_MarketHistory/stockid/"
	                + code + ".phtml?year=2015&jidu=4");
	        saveDataToFile(fileName, getDataRow(sourceCode,false));
        }
    }

    public static void main(String[] args) {
        LoadHistoricalData.loadDataList(args[0]);
        System.out.println("done!");
    }
}
