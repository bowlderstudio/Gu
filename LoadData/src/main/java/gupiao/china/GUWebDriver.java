package gupiao.china;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.firefox.FirefoxBinary;

import cucumber.api.Scenario;

public class GUWebDriver {
	private static final Logger LOGGER = Logger.getLogger(GUWebDriver.class.getName());
	//private static final String testPropertiesResource = "/test.properties";
    protected static String FIREFOX_BIN;
    
    ///FIXME: adjust version strings
    private static final String MS_FIREFOX_UA = "Mozilla/5.0 (Windows NT 6.2; rv:21.0) Gecko/20130326 Firefox/21.0";
    private static final String UBUNTU_FIREFOX_UA = "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:23.0) Gecko/20100101 Firefox/23.0";
    
    private static FirefoxDriver driver;
    private static FirefoxProfile profile;
    
    static {
    	
        profile = new FirefoxProfile();
        profile.setPreference("general.useragent.override", UBUNTU_FIREFOX_UA);
    }
    
    /**
     * Should not be able to create an instance of this class.
     */
    private GUWebDriver() {
    }

    public static FirefoxDriver getInstance(String propertyFile) {
    	Properties p = Utils.loadProperties(propertyFile);
    	FIREFOX_BIN=p.getProperty("firefox.bin");
    	
        if (driver == null) {
            driver = new FirefoxDriver(new FirefoxBinary(new File(FIREFOX_BIN)), profile);
            driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
        }
        return driver;
    }

    public static void closeDriver(Scenario result) {
        if (driver != null) {
            if(result.isFailed()) {
                byte[] screenshot = ((TakesScreenshot)driver).getScreenshotAs(OutputType.BYTES);
                result.embed(screenshot, "image/png");
            }
            driver.close();
            driver = null;
        }
    }

}
