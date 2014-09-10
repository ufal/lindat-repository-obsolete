package cz.cuni.mff.ufal.test;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import org.openqa.selenium.server.RemoteControlConfiguration;
import org.openqa.selenium.server.SeleniumServer;
import org.testng.ITestContext;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.*;

import com.thoughtworks.selenium.CommandProcessor;
import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.HttpCommandProcessor;
import com.thoughtworks.selenium.Selenium;

import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverBackedSelenium;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebDriver;
import org.testng.Reporter;
import org.testng.ITestResult;
import org.apache.commons.io.FileUtils;
import java.io.File;


public class BaseTestCase {

	protected static SeleniumServer server;
	protected static CommandProcessor proc;
	protected static Selenium selenium;
  protected static WebDriver driver;

	protected static String seleniumHost;
	protected static String seleniumPort;
	protected static String seleniumBrowser;
	protected static String seleniumUrl;
	
	protected static Properties prop = new Properties();

	@BeforeSuite
	public void beforeSuite(ITestContext context) throws IOException {
		
		prop.load(new FileReader(context.getCurrentXmlTest().getParameter("ufal.properties")));
		
		seleniumHost = context.getCurrentXmlTest().getParameter("selenium.host");
		seleniumPort = context.getCurrentXmlTest().getParameter("selenium.port");
		seleniumBrowser = context.getCurrentXmlTest().getParameter("selenium.browser");
		seleniumUrl = prop.getProperty("lr.dspace.baseUrl");
		if(!seleniumUrl.endsWith("/"))
			seleniumUrl += "/";

		RemoteControlConfiguration rcc = new RemoteControlConfiguration();
		rcc.setSingleWindow(true);
		rcc.setPort(Integer.parseInt(seleniumPort));

		try {
			server = new SeleniumServer(false, rcc);
			server.boot();
		} catch (Exception e) {
			throw new IllegalStateException("Can't start selenium server", e);
		}

		//proc = new HttpCommandProcessor(seleniumHost, Integer.parseInt(seleniumPort), seleniumBrowser, seleniumUrl);
		
	}

	@AfterSuite
	public void afterSuite() {
		server.stop();
	}

	@BeforeTest
	public void beforeTest(ITestContext context) {
		//selenium = new DefaultSelenium(proc);
		//selenium.start();
    // should be according to parameter
    driver = new FirefoxDriver();
    selenium = new WebDriverBackedSelenium(driver, seleniumUrl);
	}

  @AfterMethod(alwaysRun = true)
  public void takeScreenshot(ITestResult result) throws IOException {
    System.setProperty("org.uncommons.reportng.escape-output", "false");
    // output gets lost without this
    Reporter.setCurrentTestResult(result);
    try {
        File f = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        File outputDir = new File(result.getTestContext().getOutputDirectory());
        File saved = new File(outputDir.getParent(), result.getName()+".png");
        FileUtils.copyFile(f, saved);
        // this works for the TestNG reporter log but not for ReportNG since the results are under the html/ subdir 
        Reporter.log("screenshot for "+result.getName()+" url="+this.driver.getCurrentUrl()+" <img src=\""+saved.getName()+"\">", 0, false);
    } catch (IOException e) {
        Reporter.log("error generating screenshot for "+result.getName()+": "+e, true);
    }
  }

	@AfterTest
	public void afterTest(ITestContext context) {
		selenium.stop();
	}

}
