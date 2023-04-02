package ccushnahan.allureFailComparison;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import io.github.bonigarcia.wdm.WebDriverManager;

/***
 * Uses selenium webdriver to scrape allure page for json files.
 * @author cush
 *
 */
public class AllureScraper {
	
	private String baseURL;
	private WebDriver driver;
	
	public AllureScraper(String baseURL) {
		this.setBaseURL(baseURL);
		this.driver = driverSetup();
	}
	
	/**
	 * Sets up WebDriver for application
	 * @return driver
	 */
	private static WebDriver driverSetup() {
		System.setProperty("webdriver.http.factory", "jdk-http-client");
		System.setProperty("webdriver.chrome.silentOutput", "true");
		java.util.logging.Logger.getLogger("org.openqa.selenium").setLevel(Level.OFF); 
		WebDriverManager.chromedriver().setup();
		WebDriver driver = new ChromeDriver();
		driver.manage().timeouts().implicitlyWait(Duration.ofMillis(500));
		return driver;
	}
	
	
	/**
	 * Gets the title of the test run.
	 * 
	 * Opens the URL from through the webdriver and uses xpath to find and extract
	 * the test run title from the base page.
	 * 
	 * @param driver
	 * @param URL
	 * @return title
	 * @throws InterruptedException
	 */
	public String getRunTitle() throws InterruptedException {
		// Open Url
		driver.get(baseURL);
		TimeUnit.SECONDS.sleep(3);

		// Use xpath to find the element with title text
		String titleXpath = "//*[@class='widget__title']";
		WebElement titleElement = driver.findElement(By.xpath(titleXpath));

		// Get and return text from title element
		String title = titleElement.getText();
		return title;
	}

	/**
	 * Gets the date of the Test Run from the base page.
	 * 
	 * Passes driver to getRunTitle method to return the title from the base page.
	 * Uses a date regex to find the matching date from the title and return it.
	 * 
	 * @param driver
	 * @param URL
	 * @return date
	 * @throws InterruptedException
	 */
	public String getRunDate() throws InterruptedException {
		// Get the title string
		String title = this.getRunTitle();

		// Set regex to look for Allure Date format and match it against the title
		Pattern pattern = Pattern.compile("[0-9]+[\\/][0-9]+[\\/][0-9]+");
		Matcher matcher = pattern.matcher(title);

		// Find extract and return the match
		matcher.find();
		String date = matcher.group();
		return date;
	}
	
	/**
	 * Gets the Number of the test run.
	 * @param driver
	 * @param baseURL
	 * @return testRunNumber
	 */
	public String getRunNumber() {
		return driver.getCurrentUrl().replace(baseURL, "").replace("/", "");
	}
	
	/**
	 * Gets a list of urls that point to failed test json data.
	 * 
	 * Opens suites page and finds elements that fall into failed, broken or unknown tests and
	 * then extracts the hrefs of those elements to find the testcase UID and creates a url string
	 * for the json data associated with the test. Returns list of json urls.
	 * 
	 * @param driver
	 * @param URL
	 * @return failedTestJsonList
	 * @throws InterruptedException
	 */
	public List<String> getFailedTestJsonList() throws InterruptedException {
		// Navigate to suites page
		String currentURL = driver.getCurrentUrl();
		String suitesURL = currentURL + "#suites";
		String dataURL = currentURL + "data/test-cases/";
		driver.get(suitesURL);
		TimeUnit.SECONDS.sleep(3);

		// Get failed tests with xpath
		String failedTestLinkXpath = "(//*[@data-tooltip='Broken'] | //*[@data-tooltip='Failed'])/../../..";
		List<WebElement> failedTestElements = driver.findElements(By.xpath(failedTestLinkXpath));

		// Turn list of elements into json urls
		// Rewrite this using a regex to ident correct part of href as it can change depending on hosting context		
		List<String> failedTestJson = failedTestElements.stream()
				.map(failedEle -> dataURL 
								 + failedEle.getAttribute("href")
													 .split("/")[failedEle.getAttribute("href").split("/").length -1] 
			            		 + ".json")
				.toList();
		return failedTestJson;
	}
	
	public void closeDriver() {
		this.driver.close();
	}
	
	public void quitDriver() {
		this.driver.quit();
		this.driver = null;
	}

	public String getBaseURL() {
		return baseURL;
	}

	public void setBaseURL(String baseURL) {
		this.baseURL = baseURL;
	}
}
