package gupiao.china;

import gupiao.general.Stock;
import gupiao.general.StockComparator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AnalysisHistoricalData {
    public static float LOWESTPRICE = 1;
    public static float HIGHESTPRICE = 20;
    public static float RATETOHIGH = 0.6f;
    // used to find the latest price
    public static String currentDate = "2015-11-20";
    // start date for analysis
    public static String startMACDDate = "2015-01-01";
    public static String startAnalysisPriceDate = "2015-05-01";
    public static String ANALYSISRESULTFILE = "/disk1/gupiao/analysisresult"+currentDate+".txt";
    public static String DIAGRAMFOLD = "/disk1/gupiao/diagram/";

    public static void main(String[] args) {
        AnalysisHistoricalData.analysisDataList("/disk1/gupiao/stockList.txt");
        System.out.println("done!");
    }

    public static void analysisDataList(String listFile) {
        BufferedReader fileReader = null;

        // Delimiter used in CSV file
        final String DELIMITER = ";";
        try {
            String line = "";
            // Create the file reader
            fileReader = new BufferedReader(new FileReader(listFile));
            LoadHistoricalData.saveDataToFile(ANALYSISRESULTFILE,
                    "#name, #code, currentPrice, highestPrice, lowestPrice, rate, difference, histogram rate, diff rate\r\n");
            // Read the file line by line
            while ((line = fileReader.readLine()) != null) {
                // Get all tokens available in line
                String[] tokens = line.split(DELIMITER);
                analysisData(tokens[0], tokens[1]);
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

	private static void analysisData(String stockCode, String codeName) {
		//TODO for debug
//		if (!stockCode.equals("600735"))
//        	return;
        BufferedReader fileReader = null;
        List<Stock> stocks = new ArrayList<>();
        String fileName="/disk1/gupiao/" + stockCode + ".txt";
        
        File f = new File(fileName);
        if(!f.exists()) {
        	System.out.println("Data for stock " + stockCode +" does not exist!");
        	return;
        }
        
        // Delimiter used in CSV file
        final String DELIMITER = ",";
        try {
        	//Check file exist
            String line = "";
            // Create the file reader
            fileReader = new BufferedReader(new FileReader(fileName));

            // Read the file line by line
            float closePrice, currentPrice, highestPrice, lowestPrice;
            currentPrice = 0;
            highestPrice = 0;
            lowestPrice = 10000;
            while ((line = fileReader.readLine()) != null) {
                // Get all tokens available in line
                String[] tokens = line.split(DELIMITER);
                if (tokens[0].startsWith("#"))
                    continue;
                closePrice = Float.parseFloat(tokens[3]);
                if (tokens[0].compareTo(startMACDDate) <= 0) {
                    continue;
                }
                stocks.add(new Stock(stockCode, tokens[0], closePrice));
                if (tokens[0].equalsIgnoreCase(currentDate)) {
                    currentPrice = Float.parseFloat(tokens[3]);
                }
                if (tokens[0].compareTo(startAnalysisPriceDate) >= 0 && closePrice > highestPrice) {
                    highestPrice = closePrice;
                }
                if (tokens[0].compareTo(startAnalysisPriceDate) >= 0 && closePrice < lowestPrice) {
                    lowestPrice = closePrice;
                }
            }
            // TODO remove old filter
            // if (isPotencialGood(currentPrice, highestPrice, lowestPrice)) {
            // String
            // data=codeName+","+dataFile+","+currentPrice+","+highestPrice+","+lowestPrice+","+(currentPrice/highestPrice)+","+(currentPrice-lowestPrice)+"\r\n";
            // ReadUrlTest.saveDataToFile(ANALYSISRESULTFILE,data);
            // }
            if (stocks.size() == 0)
                return;

            if (!calculateMACD(stocks))
                return;

            if (stocks.get(stocks.size() - 1).getDate().equals(currentDate) && isExceptedPrice(currentPrice)) {

            	//System.out.println("step1");
            	float histogramRate=calculateMACDIncreaseRate("HISTOGRAM",stocks);
            	float diffRate=calculateMACDIncreaseRate("DIFF",stocks);
                String data = codeName + "," + stockCode + "," + currentPrice + "," + highestPrice + "," + lowestPrice
                        + "," + (currentPrice / highestPrice) + "," + (currentPrice - lowestPrice) + ","+histogramRate+","+diffRate+"\r\n";

                //System.out.println("step2");
                //System.out.println(stockCode);
                String imageUrl;
                // download diagram
                if (stockCode.startsWith("0") || stockCode.startsWith("3")) {
                    imageUrl = "http://hqpick.eastmoney.com/EM_Quote2010PictureProducter/Index.aspx?ImageType=KXL&ID="
                            + stockCode + "2&EF=&Formula=MACD&UnitWidth=6&FA=&BA=&type=&0.9095650463773457";
                } else {
                    imageUrl = "http://hqpick.eastmoney.com/EM_Quote2010PictureProducter/Index.aspx?ImageType=KXL&ID="
                            + stockCode + "1&EF=&Formula=MACD&UnitWidth=6&FA=&BA=&type=&0.9095650463773457";
                }
                //Remove, need to be test 
//                if (diffRate<0.4) {
//                	LoadHistoricalData.saveDataToFile(ANALYSISRESULTFILE, data);
//                    saveImage(imageUrl, DIAGRAMFOLD +"DiffUp"+ stockCode + ".png");
//                } else 
                if (isMACDRaise(stocks)) {
                	LoadHistoricalData.saveDataToFile(ANALYSISRESULTFILE, data);
                    saveImage(imageUrl, DIAGRAMFOLD +"HistogramUp"+ stockCode + ".png");
                } else if (isMacdMinus(stocks)) {
                    LoadHistoricalData.saveDataToFile(ANALYSISRESULTFILE, data);
                    saveImage(imageUrl, DIAGRAMFOLD + "HistogramMinus" + stockCode + ".png");
                } else if (isMacdDown(stocks)) {
                	LoadHistoricalData.saveDataToFile(ANALYSISRESULTFILE, data);
                    //saveImage(imageUrl, DIAGRAMFOLD + "HistogramDown" + stockCode + ".png");
                }
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

	private static float calculateMACDIncreaseRate(String field, List<Stock> stocks) {
		float lastDay=0,last2Day=0;
		if (field.equalsIgnoreCase("HISTOGRAM")) {
			lastDay=stocks.get(stocks.size()-1).getHistogram();
			last2Day=stocks.get(stocks.size()-2).getHistogram();
		} else if (field.equalsIgnoreCase("DIFF")){
			lastDay=stocks.get(stocks.size()-1).getDiff();
			last2Day=stocks.get(stocks.size()-2).getDiff();
		}
		
		if (lastDay <0 && last2Day <0) {
			return lastDay/last2Day;
		} else if(lastDay >0 && last2Day >=0) {
			return last2Day/lastDay;
		} else if (lastDay>0 && last2Day<0) {
			return -last2Day/(lastDay-last2Day);
		}
		return 0;
	}

	private static boolean isMacdMinus(List<Stock> stocks) {
        int days = 5;
        for (int i = 0; i < days; i++) {
            if (stocks.get(stocks.size() - 1 - i).getHistogram() > 0) {
                return false;
            }
        }
        return true;
    }

    private static boolean isMacdDown(List<Stock> stocks) {
        if (stocks.get(stocks.size() - 4).getHistogram() < 0
                && stocks.get(stocks.size() - 1).getHistogram() < stocks.get(stocks.size() - 2).getHistogram()
                && stocks.get(stocks.size() - 2).getHistogram() < stocks.get(stocks.size() - 3).getHistogram()
                && stocks.get(stocks.size() - 3).getHistogram() < stocks.get(stocks.size() - 4).getHistogram()) {
            return true;
        }
        return false;
    }

    // Analysis MACD and filter good ones
    private static boolean isMACDRaise(List<Stock> stocks) {
        int i = stocks.size() - 1;
        if ((stocks.get(i).getHistogram() > stocks.get(i - 1).getHistogram())
                && (stocks.get(i - 1).getHistogram() > stocks.get(i - 2).getHistogram())
                && (stocks.get(i - 2).getHistogram() < 0) && (stocks.get(i - 1).getHistogram() < 0)
                // && (stocks.get(i).getDiff() > stocks.get(i - 1).getDiff())
                // && (stocks.get(i - 1).getDiff() > stocks.get(i - 2).getDiff())
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

    private static boolean calculateMACD(List<Stock> stocks) {
        int stordays = 12;
        int longdays = 26;
        int signaldays = 9;
        // Sort data by date first
        Collections.sort(stocks, new StockComparator());
        if (stocks.size() < 40) {
            System.out.println("Too little date to calculate MACD");
            return false;
        }
        float average12 = getAveragePriceForDays(stocks, stordays);
        float average26 = getAveragePriceForDays(stocks, longdays);
        stocks.get(stordays - 1).setEma12(average12);
        stocks.get(longdays - 1).setEma26(average26);
        // calculate short ema
        for (int i = stordays; i < stocks.size(); i++) {
            stocks.get(i).setEma12(
                    stocks.get(i).getClosePrice() * (2.0f / (stordays + 1)) + stocks.get(i - 1).getEma12()
                            * (stordays - 1) / (stordays + 1));
        }
        // calculate long ema
        for (int i = longdays; i < stocks.size(); i++) {
            stocks.get(i).setEma26(
                    stocks.get(i).getClosePrice() * (2.0f / (longdays + 1)) + stocks.get(i - 1).getEma26()
                            * (longdays - 1) / (longdays + 1));
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
            stocks.get(i).setSignal(
                    stocks.get(i).getDiff() * (2.0f / (signaldays + 1)) + stocks.get(i - 1).getSignal()
                            * (signaldays - 1) / (signaldays + 1));
        }
        // calculate histogram
        for (int i = longdays + signaldays - 2; i < stocks.size(); i++) {
            stocks.get(i).setHistogram((stocks.get(i).getDiff() - stocks.get(i).getSignal()) * 2);
        }

        //printStocks(stocks);
        return true;
    }

    private static float getAveragePriceForDays(List<Stock> stocks, int days) {
        float average = 0;
        for (int i = 0; i < stocks.size(); i++) {
            if (i < days)
                average += stocks.get(i).getClosePrice();
            else
                break;
        }
        return average / days;
    }

    private static void printStocks(List<Stock> stocks) {
        for (Stock s : stocks) {
            System.out.println(s.getDate() + ", " + s.getClosePrice() + ", " + s.getEma12() + ", " + s.getEma26()
                    + ", " + s.getDiff() + "," + s.getSignal() + ", " + s.getHistogram());
        }
    }

    private static boolean isExceptedPrice(float price) {
        if (price != 0 && price >= LOWESTPRICE && price <= HIGHESTPRICE) {
            return true;
        } else
            return false;
    }

    private static boolean isPotencialGood(float currentPrice, float highestPrice, float lowestPrice) {
        if (isExceptedPrice(currentPrice) && (currentPrice / highestPrice) < RATETOHIGH)
            return true;
        else
            return false;
    }

}
