//package no.met.kss.utils;
//
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileReader;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.net.URL;
//import java.net.URLConnection;
//
//import org.jsoup.Jsoup;
//
//public class ReadUrlNorway {
//    public static final String STARTDATE="2015-08-17";
//    public static String getUrlSource(String url) {
//        StringBuilder a = new StringBuilder();
//        try {
//            URL yahoo = new URL(url);
//            URLConnection yc = yahoo.openConnection();
//            BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream(), "gb2312"));
//            String inputLine;
//            while ((inputLine = in.readLine()) != null)
//                a.append(inputLine);
//            in.close();
//
//        } catch (Exception e) {
//            System.out.println("Error when get url source!");
//        }
//        return a.toString();
//    }
//
//    public static String getDataRow(String content) {
//        String dataStart = "<a target='_blank' href='http://vip.stock.finance.sina.com.cn/quotes_service/view/vMS_tradehistory.php?";
//        String dataEnd = "</tr>";
//        int startIndex = content.indexOf(dataStart);
//        int endIndex;
//        String rowData = "";
//        while (startIndex >= 0) {
//            content = content.substring(startIndex);
//            endIndex = content.indexOf(dataEnd);
//            String dataString = content.substring(0, endIndex);
//            dataString = Jsoup.parse(dataString).text().replaceAll(" ", ",");
//            if (dataString.substring(0, 10).compareTo(STARTDATE) >=0)
//                rowData += Jsoup.parse(dataString).text() + "\r\n";
//            content = content.substring(endIndex);
//            startIndex = content.indexOf(dataStart);
//        }
//        return rowData;
//    }
//
//    public static void saveDataToFile(String filename, String data) {
//        if (data.isEmpty())
//            return;
//        try {
//            File newTextFile = new File(filename);
//
//            FileWriter fw = new FileWriter(newTextFile, true);
//            fw.write(data);
//            fw.close();
//
//        } catch (IOException iox) {
//            // do stuff with exception
//            iox.printStackTrace();
//        }
//    }
//
//    public static void loadDataList(String listFile) {
//        BufferedReader fileReader = null;
//
//        // Delimiter used in CSV file
//        final String DELIMITER = ",";
//        try {
//            String line = "";
//            // Create the file reader
//            fileReader = new BufferedReader(new FileReader(listFile));
//
//            // Read the file line by line
//            while ((line = fileReader.readLine()) != null) {
//                // Get all tokens available in line
//                String[] tokens = line.split(DELIMITER);
//                loadData(tokens[1]);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                fileReader.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    public static void loadData(String code) {
//        if (code.isEmpty())
//            return;
//        System.out.println("Start to load data for code " + code);
//        String header="#date, start price, highest price, close price, lowest price, deal number, deal amount\r\n";
//        //saveDataToFile("/disk1/gupiao/" + code + ".txt", header);
//        String sourceCode;
//        //sourceCode = ReadUrlTest
//        //        .getUrlSource("http://money.finance.sina.com.cn/corp/go.php/vMS_MarketHistory/stockid/" + code
//        //                + ".phtml?year=2014&jidu=4");
//        //saveDataToFile("/disk1/gupiao/" + code + ".txt", getDataRow(sourceCode));
////        sourceCode = ReadUrlTest.getUrlSource("http://money.finance.sina.com.cn/corp/go.php/vMS_MarketHistory/stockid/"
////                + code + ".phtml?year=2015&jidu=1");
////        saveDataToFile("/disk1/gupiao/" + code + ".txt", getDataRow(sourceCode));
////        sourceCode = ReadUrlTest.getUrlSource("http://money.finance.sina.com.cn/corp/go.php/vMS_MarketHistory/stockid/"
////                + code + ".phtml?year=2015&jidu=2");
////        saveDataToFile("/disk1/gupiao/" + code + ".txt", getDataRow(sourceCode));
//        sourceCode = ReadUrlNorway.getUrlSource("http://money.finance.sina.com.cn/corp/go.php/vMS_MarketHistory/stockid/"
//                + code + ".phtml?year=2015&jidu=3");
//        saveDataToFile("/disk1/gupiao/" + code + ".txt", getDataRow(sourceCode));
//    }
//
//    public static void main(String[] args) {
//        ReadUrlNorway.loadDataList("/disk1/gupiao/list.txt");
//        System.out.println("done!");
//    }
//}
