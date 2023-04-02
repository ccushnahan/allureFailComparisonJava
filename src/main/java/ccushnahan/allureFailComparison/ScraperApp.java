package ccushnahan.allureFailComparison;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import io.github.bonigarcia.wdm.WebDriverManager;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ScraperApp {

	public static void main(String[] args) throws IOException, InterruptedException {
		scrapeAllureRun();
	}
	
	public static void scrapeAllureRun() throws IOException, InterruptedException {
		System.out.println("[ Scraper: ] Starting Scraper");
		ScraperConfig config = new ScraperConfig("/config.properties");
		String[] baseURLs = config.getBaseURLs();
		String[] automationNames = config.getRunNames();
		
		for (int i = 0; i < baseURLs.length; i++) {
			String baseURL = baseURLs[i];
			String automationName = automationNames[i];
			System.out.println("[ SETUP: ] WebDriver Setup");
			AllureScraper scraper = new AllureScraper(baseURL);
			System.out.println("[ INFO: ] Getting Test Run Date");
			String testRunDate = scraper.getRunDate();
			System.out.println("[ INFO: ] Getting Test Run Number");
			String testRunNum = scraper.getRunNumber();
			System.out.println("[ INFO: ] Getting Test Data Json URLs");
			List<String> failedTestURLS = scraper.getFailedTestJsonList();
			System.out.println("[ INFO: ] Scraping Data From Json URLs");
			FailedTestJsonScraper jsonScraper = new FailedTestJsonScraper(failedTestURLS, baseURL);
			List<List> failedTestData = jsonScraper.scrapeFailedTestsData();
			
			if (config.isWriteToExcel()) {
				System.out.println("[ INFO: ] Saving Results to Excel");
				ResultsExcelWriter.saveResultsToExcel(automationName, testRunDate, testRunNum, failedTestData);
			} else {
				System.out.println("[ INFO: ] Saving Results to CSV");
				CSVWriter.saveResultsToCsv(automationName, testRunDate, testRunNum, failedTestData);
			}
			scraper.closeDriver();
			scraper.quitDriver();
			System.out.println("[ INFO: ] " + automationName + ": Save Complete");
			System.out.println("[ INFO: ] " + automationName + ": Scraping complete!");
		}
	}

}
