package gupiao.china;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class Utils {

	public static Properties loadProperties(String filename) {
		Properties p = new Properties();
		InputStream fin = null;
		try {
			fin = new FileInputStream(filename);
			p.load(fin);
		} catch (IOException ioe) {
			System.err.println("IO exception while reading properties:" + ioe);
			return null;
		} finally {
			if (fin != null) {
				try {
					fin.close();
				} catch (IOException ioe) {
					System.err.println("IO exception while closing:" + ioe);
					return null;
				}
			}
		}
		return p;
	}
	
	public static Connection connectLocal(Properties p) {
		try {
			Class.forName("org.sqlite.JDBC");
			Connection connDb = DriverManager.getConnection(p.getProperty("local_conn"),
					p.getProperty("local_user", ""), p.getProperty("local_password", ""));
			connDb.setAutoCommit(true);
			
			Statement s = connDb.createStatement();
			s.execute("CREATE TABLE IF NOT EXISTS stocks (code VARCHAR(20),name VARCHAR(100),changeRate REAL,closePrice REAL,increastRate REAL,"
					+ "currentMarketPrice NUMERIC,totalMarketPrice NUMERIC,dealNumber BIGINT,PRIMARY KEY (code))");
			
			s.execute("CREATE TABLE IF NOT EXISTS stockDealRecord ("
					+ " code VARCHAR(20),"
					+ " name VARCHAR(100),"
					+ " date VARCHAR(20),"
					+ " openPrice REAL,"
					+ " closePrice REAL,"
					+ " highestPrice REAL,"
					+ " lowestPrice REAL,"
					+ " increastRate REAL,"
					+ " diff REAL,"
					+ " ema12 REAL,"
					+ " ema26 REAL,"
					+ " signal REAL,"
					+ " histogram REAL,"
					+ " dealAmount NUMERIC,"
					+ " dealNumber BIGINT,"
					+ " PRIMARY KEY (code, date));");
			
			s.execute("CREATE INDEX IF NOT EXISTS idx_stocks ON stocks (code)");
			s.execute("CREATE INDEX IF NOT EXISTS idx_stockDealRecord ON stockDealRecord (code, date)");
			s.execute("CREATE TABLE IF NOT EXISTS stockLoadRecord ("
					+ " code VARCHAR(20),"
					+ " season VARCHAR(5), PRIMARY KEY (code, season));");
			return connDb;
		} catch (SQLException e) {
			System.err.println("conn localdb error: " + e);
			e.printStackTrace();
			return null;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static String getUrlSourceByWebClient(String url) {

		java.util.logging.Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(java.util.logging.Level.OFF);
	    java.util.logging.Logger.getLogger("org.apache.http").setLevel(java.util.logging.Level.OFF);

        @SuppressWarnings("resource")
		final WebClient webclient = new WebClient(BrowserVersion.CHROME);
        HtmlPage page;
		try {
			page = webclient.getPage(url);
			//System.out.println("PULLING LINKS:"+page.asXml());
			return page.asXml();
		} catch (FailingHttpStatusCodeException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		return "";
        
    }
	
	public static String getUrlSourceByReader(String url) {
        StringBuilder a = new StringBuilder();
        try {
            URL u = new URL(url);
            URLConnection uc = u.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream(), "gb2312"));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
            	//System.out.println(inputLine);
                a.append(inputLine);
            }
            in.close();

        } catch (Exception e) {
        	System.err.println("Error when get url source!");
        }
        return a.toString();
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
	
	public static void removeFile(String filename) {
		try {

			File file = new File(filename);

			if (file.delete()) {
				System.out.println("Old " + file.getName() + " is deleted!");
			} else {
				System.out.println("Delete operation is failed.");
			}

		} catch (Exception e) {

			e.printStackTrace();

		}
	}
}
