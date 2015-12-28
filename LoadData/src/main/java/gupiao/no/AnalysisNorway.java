//package gupiao.no;
//
//import java.io.BufferedReader;
//import java.io.FileOutputStream;
//import java.io.FileReader;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.io.OutputStream;
//import java.net.URL;
//import java.net.URLConnection;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//
//public class AnalysisNorway {
//    public static float LOWESTPRICE = 1;
//    public static float HIGHESTPRICE = 200;
//    public static float RATETOHIGH = 0.6f;
//    // used to find the latest price
//    public static String currentDate = "20150817";
//    // start date for analysis
//    public static String startMACDDate = "20150101";
//    public static String startAnalysisPriceDate = "20150101";
//    public static String ANALYSISRESULTFILE = "/disk1/gupiao/norwegian/analysisresult20150817.txt";
//    public static String DIAGRAMFOLD = "/disk1/gupiao/diagram/";
//    public static String DATAURL="http://www.netfonds.no/quotes/paperhistory.php?paper=%T.OSE&csv_format=csv";
//    public static String ANAURL="http://www.netfonds.no/quotes/analysis.php?paper=%T.OSE";
//
//    public static void main(String[] args) {
//        AnalysisNorway.analysisDataList("/disk1/gupiao/norwegian/norwegianList.txt");
//        System.out.println("done!");
//    }
//    
//    public static void getDataFromUrl(String code) {
//        String url=DATAURL.replaceAll("%T", code);
//        System.out.println("Start to fetch data for "+code);
//        try {
//            URL yahoo = new URL(url);
//            URLConnection yc = yahoo.openConnection();
//            BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream(),"UTF-8"));
//            String inputLine;
//            String[] data;
//            List<Stock> stocks = new ArrayList<>();
//            float closePrice, currentPrice, highestPrice, lowestPrice;
//            currentPrice = 0;
//            highestPrice = 0;
//            lowestPrice = 10000;;
//            while ((inputLine = in.readLine()) != null) {
//                if (inputLine.startsWith("quote_date"))
//                    continue;
//                data=inputLine.split(",");
//                closePrice=Float.parseFloat(data[6]);
//                if (data[0].compareTo(startMACDDate)>=0) {
//                    stocks.add(new Stock(code, data[0], closePrice));
//                }
//                if (data[0].equalsIgnoreCase(currentDate)) {
//                    currentPrice = closePrice;
//                }
//                if (data[0].compareTo(startAnalysisPriceDate) >= 0 && closePrice > highestPrice) {
//                    highestPrice = closePrice;
//                }
//                if (data[0].compareTo(startAnalysisPriceDate) >= 0 && closePrice < lowestPrice) {
//                    lowestPrice = closePrice;
//                }
//            }
//            in.close();
//
//            if (stocks.size() == 0)
//                return;
//
//            if (!calculateMACD(stocks))
//                return;
//            
//            if (stocks.get(stocks.size() - 1).getDate().equals(currentDate) && isExceptedPrice(currentPrice)) {
//
//                String analysisResult = code + "," + code + "," + currentPrice + "," + highestPrice + "," + lowestPrice
//                        + "," + (currentPrice / highestPrice) + "," + (currentPrice - lowestPrice);
//
//                analysisResult+=","+ANAURL.replaceAll("%T", code);
//                // System.out.println(stockCode);
//                String imageUrl;
//                if (isMACDRaise(stocks)) {
//                    ReadUrlTest.saveDataToFile(ANALYSISRESULTFILE, "RAISE,"+analysisResult + "\r\n");
//                    // saveImage(imageUrl, DIAGRAMFOLD + stockCode + ".png");
//                } else if (isMacdMinus(stocks)) {
//                    ReadUrlTest.saveDataToFile(ANALYSISRESULTFILE, "MINUS,"+analysisResult + "\r\n");
//                    //saveImage(imageUrl, DIAGRAMFOLD + "_" + stockCode + ".png");
//                } else if (isMacdDown(stocks)) {
//                    ReadUrlTest.saveDataToFile(ANALYSISRESULTFILE, "DOWN,"+analysisResult + "\r\n");
//                    //saveImage(imageUrl, DIAGRAMFOLD + "down" + stockCode + ".png");
//                }
//            }
//
//        } catch (Exception e) {
//            System.out.println("Error when get data from url "+url);
//        }
//    }
//
//    public static void analysisDataList(String listFile) {
//        BufferedReader fileReader = null;
//
//        // Delimiter used in CSV file
//        final String DELIMITER = ",";
//        try {
//            String line = "";
//            // Create the file reader
//            fileReader = new BufferedReader(new FileReader(listFile));
//            ReadUrlTest.saveDataToFile(ANALYSISRESULTFILE,
//                    "#name, #code, currentPrice, highestPrice, lowestPrice, rate, difference\r\n");
//            // Read the file line by line
//            while ((line = fileReader.readLine()) != null) {
//                // Get all tokens available in line
//                String[] tokens = line.split(DELIMITER);
//                getDataFromUrl(tokens[1]);
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
//
//    }
//
//    private static void analysisData(String stockCode, String codeName) {
//        // if (!stockCode.equals("600299"))
//        // return;
//        BufferedReader fileReader = null;
//        List<Stock> stocks = new ArrayList<>();
//        // Delimiter used in CSV file
//        final String DELIMITER = ",";
//        try {
//            String line = "";
//            // Create the file reader
//            fileReader = new BufferedReader(new FileReader("/disk1/gupiao/" + stockCode + ".txt"));
//
//            // Read the file line by line
//            float closePrice, currentPrice, highestPrice, lowestPrice;
//            currentPrice = 0;
//            highestPrice = 0;
//            lowestPrice = 10000;
//            while ((line = fileReader.readLine()) != null) {
//                // Get all tokens available in line
//                String[] tokens = line.split(DELIMITER);
//                if (tokens[0].startsWith("#"))
//                    continue;
//                closePrice = Float.parseFloat(tokens[3]);
//                if (tokens[0].compareTo(startMACDDate) <= 0) {
//                    continue;
//                }
//                stocks.add(new Stock(stockCode, tokens[0], closePrice));
//                if (tokens[0].equalsIgnoreCase(currentDate)) {
//                    currentPrice = Float.parseFloat(tokens[3]);
//                }
//                if (tokens[0].compareTo(startAnalysisPriceDate) >= 0 && closePrice > highestPrice) {
//                    highestPrice = closePrice;
//                }
//                if (tokens[0].compareTo(startAnalysisPriceDate) >= 0 && closePrice < lowestPrice) {
//                    lowestPrice = closePrice;
//                }
//            }
//            // TODO remove old filter
//            // if (isPotencialGood(currentPrice, highestPrice, lowestPrice)) {
//            // String
//            // data=codeName+","+dataFile+","+currentPrice+","+highestPrice+","+lowestPrice+","+(currentPrice/highestPrice)+","+(currentPrice-lowestPrice)+"\r\n";
//            // ReadUrlTest.saveDataToFile(ANALYSISRESULTFILE,data);
//            // }
//            if (stocks.size() == 0)
//                return;
//
//            if (!calculateMACD(stocks))
//                return;
//
//            if (stocks.get(stocks.size() - 1).getDate().equals(currentDate) && isExceptedPrice(currentPrice)) {
//
//                String data = codeName + "," + stockCode + "," + currentPrice + "," + highestPrice + "," + lowestPrice
//                        + "," + (currentPrice / highestPrice) + "," + (currentPrice - lowestPrice) + "\r\n";
//
//                // System.out.println(stockCode);
//                String imageUrl;
//                // download diagram
//                if (stockCode.startsWith("0") || stockCode.startsWith("3")) {
//                    imageUrl = "http://hqpick.eastmoney.com/EM_Quote2010PictureProducter/Index.aspx?ImageType=KXL&ID="
//                            + stockCode + "2&EF=&Formula=MACD&UnitWidth=6&FA=&BA=&type=&0.9095650463773457";
//                } else {
//                    imageUrl = "http://hqpick.eastmoney.com/EM_Quote2010PictureProducter/Index.aspx?ImageType=KXL&ID="
//                            + stockCode + "1&EF=&Formula=MACD&UnitWidth=6&FA=&BA=&type=&0.9095650463773457";
//                }
//                if (isMACDRaise(stocks)) {
//                    // ReadUrlTest.saveDataToFile(ANALYSISRESULTFILE, data);
//                    // saveImage(imageUrl, DIAGRAMFOLD + stockCode + ".png");
//                } else if (isMacdMinus(stocks)) {
//                    ReadUrlTest.saveDataToFile(ANALYSISRESULTFILE, data);
//                    saveImage(imageUrl, DIAGRAMFOLD + "_" + stockCode + ".png");
//                } else if (isMacdDown(stocks)) {
//                    //ReadUrlTest.saveDataToFile(ANALYSISRESULTFILE, data);
//                    //saveImage(imageUrl, DIAGRAMFOLD + "down" + stockCode + ".png");
//                }
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                fileReader.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//
//    }
//
//    private static boolean isMacdMinus(List<Stock> stocks) {
//        int days = 5;
//        for (int i = 0; i < days; i++) {
//            if (stocks.get(stocks.size() - 1 - i).getHistogram() > 0) {
//                return false;
//            }
//        }
//        return true;
//    }
//
//    private static boolean isMacdDown(List<Stock> stocks) {
//        if (stocks.get(stocks.size() - 4).getHistogram() < 0
//                && stocks.get(stocks.size() - 1).getHistogram() < stocks.get(stocks.size() - 2).getHistogram()
//                && stocks.get(stocks.size() - 2).getHistogram() < stocks.get(stocks.size() - 3).getHistogram()
//                && stocks.get(stocks.size() - 3).getHistogram() < stocks.get(stocks.size() - 4).getHistogram()) {
//            return true;
//        }
//        return false;
//    }
//
//    // Analysis MACD and filter good ones
//    private static boolean isMACDRaise(List<Stock> stocks) {
//        int i = stocks.size() - 1;
//        if ((stocks.get(i).getHistogram() > stocks.get(i - 1).getHistogram())
//                && (stocks.get(i - 1).getHistogram() > stocks.get(i - 2).getHistogram())
//                && (stocks.get(i - 2).getHistogram() < 0) && (stocks.get(i - 1).getHistogram() < 0)
//                && (stocks.get(i).getDiff() > stocks.get(i - 1).getDiff())
//                // && (stocks.get(i - 1).getDiff() > stocks.get(i - 2).getDiff())
//                && (stocks.get(i - 2).getDiff() < 0) && (stocks.get(i - 1).getDiff() < 0)) {
//            return true;
//        }
//        return false;
//    }
//
//    public static void saveImage(String imageUrl, String destinationFile) {
//        try {
//            URL url = new URL(imageUrl);
//            InputStream is = url.openStream();
//            OutputStream os = new FileOutputStream(destinationFile);
//
//            byte[] b = new byte[2048];
//            int length;
//
//            while ((length = is.read(b)) != -1) {
//                os.write(b, 0, length);
//            }
//
//            is.close();
//            os.close();
//        } catch (IOException e) {
//            System.out.println("Fail to save image " + imageUrl);
//        }
//    }
//
//    private static boolean calculateMACD(List<Stock> stocks) {
//        int stordays = 12;
//        int longdays = 26;
//        int signaldays = 9;
//        // Sort data by date first
//        Collections.sort(stocks, new StockComparator());
//        if (stocks.size() < 40) {
//            System.out.println("Too little date to calculate MACD");
//            return false;
//        }
//        float average12 = getAveragePriceForDays(stocks, stordays);
//        float average26 = getAveragePriceForDays(stocks, longdays);
//        stocks.get(stordays - 1).setEma12(average12);
//        stocks.get(longdays - 1).setEma26(average26);
//        // calculate short ema
//        for (int i = stordays; i < stocks.size(); i++) {
//            stocks.get(i).setEma12(
//                    stocks.get(i).getClosePrice() * (2.0f / (stordays + 1)) + stocks.get(i - 1).getEma12()
//                            * (stordays - 1) / (stordays + 1));
//        }
//        // calculate long ema
//        for (int i = longdays; i < stocks.size(); i++) {
//            stocks.get(i).setEma26(
//                    stocks.get(i).getClosePrice() * (2.0f / (longdays + 1)) + stocks.get(i - 1).getEma26()
//                            * (longdays - 1) / (longdays + 1));
//        }
//        // calculate MACD
//        for (int i = longdays - 1; i < stocks.size(); i++) {
//            stocks.get(i).setDiff(stocks.get(i).getEma12() - stocks.get(i).getEma26());
//        }
//        // calculate signal
//        float averageMACD = 0;
//        for (int i = 0; i < signaldays; i++) {
//            averageMACD += stocks.get(longdays + i - 1).getDiff();
//        }
//        averageMACD = averageMACD / signaldays;
//        stocks.get(longdays + signaldays - 2).setSignal(averageMACD);
//        for (int i = longdays + signaldays - 1; i < stocks.size(); i++) {
//            stocks.get(i).setSignal(
//                    stocks.get(i).getDiff() * (2.0f / (signaldays + 1)) + stocks.get(i - 1).getSignal()
//                            * (signaldays - 1) / (signaldays + 1));
//        }
//        // calculate histogram
//        for (int i = longdays + signaldays - 2; i < stocks.size(); i++) {
//            stocks.get(i).setHistogram((stocks.get(i).getDiff() - stocks.get(i).getSignal()) * 2);
//        }
//
//        //printStocks(stocks);
//        return true;
//    }
//
//    private static float getAveragePriceForDays(List<Stock> stocks, int days) {
//        float average = 0;
//        for (int i = 0; i < stocks.size(); i++) {
//            if (i < days)
//                average += stocks.get(i).getClosePrice();
//            else
//                break;
//        }
//        return average / days;
//    }
//
//    private static void printStocks(List<Stock> stocks) {
//        for (Stock s : stocks) {
//            System.out.println(s.getDate() + ", " + s.getClosePrice() + ", " + s.getEma12() + ", " + s.getEma26()
//                    + ", " + s.getDiff() + "," + s.getSignal() + ", " + s.getHistogram());
//        }
//    }
//
//    private static boolean isExceptedPrice(float price) {
//        if (price != 0 && price >= LOWESTPRICE && price <= HIGHESTPRICE) {
//            return true;
//        } else
//            return false;
//    }
//
//    private static boolean isPotencialGood(float currentPrice, float highestPrice, float lowestPrice) {
//        if (isExceptedPrice(currentPrice) && (currentPrice / highestPrice) < RATETOHIGH)
//            return true;
//        else
//            return false;
//    }
//
//}
