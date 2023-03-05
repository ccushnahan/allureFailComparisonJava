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

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import io.github.bonigarcia.wdm.WebDriverManager;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class App {

	public static void main(String[] args) throws IOException, InterruptedException {
		System.out.println("Starting Scraper");
		String baseURL = loadBaseURL();
		String automationName = loadRunName();
		System.out.println("WebDriver Setup");
		WebDriver driver = driverSetup();
		System.out.println("Getting Test Run Date");
		String testRunDate = getRunDate(driver, baseURL);
		System.out.println("Getting Test Run Number");
		String testRunNum = getRunNumber(driver, baseURL);
		System.out.println("Getting Test Data Json URLs");
		List<String> failedTestURLS = getFailedTestJsonList(driver, baseURL);
		System.out.println("Scraping Data From Json URLs");
		List<List> failedTestData = getFailedTestsData(failedTestURLS, baseURL);
		System.out.println("Saving Results to CSV");
		saveResultsToCsv(automationName, testRunDate, testRunNum, failedTestData);
		driver.close();
		System.out.println("Save Complete");
		System.out.println("Scraping complete!");
	}
	
	/**
	 * Sets up WebDriver for application
	 * @return driver
	 */
	private static WebDriver driverSetup() {
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
	private static String getRunTitle(WebDriver driver, String URL) throws InterruptedException {
		// Open Url
		driver.get(URL);
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
	private static String getRunDate(WebDriver driver, String URL) throws InterruptedException {
		// Get the title string
		String title = getRunTitle(driver, URL);

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
	public static String getRunNumber(WebDriver driver, String baseURL) {
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
	private static List<String> getFailedTestJsonList(WebDriver driver, String URL) throws InterruptedException {
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
		List<String> failedTestJson = failedTestElements.stream()
				.map(failedEle -> dataURL + failedEle.getAttribute("href").split("/")[7] + ".json")
				.toList();
		return failedTestJson;
	}
	
	/***
	 * Takes list of failed test urls and extracts failed test data.
	 * @param failedTestURLS
	 * @throws IOException 
	 * @throws MalformedURLException 
	 * @throws InterruptedException 
	 * @returns failedTestsData
	 */
	private static List<List> getFailedTestsData(List<String> failedTestURLs, String baseURL) throws MalformedURLException, IOException, InterruptedException {
		List<List> failedTestsData = new ArrayList<>();
		
		for (String failedTestURL: failedTestURLs) {
			ArrayList<String> failedTestData = getFailedTestData(failedTestURL, baseURL);
			failedTestsData.add(failedTestData);
			TimeUnit.SECONDS.sleep(3);
		}
		
		return failedTestsData;
	}
	/***
	 * Takes url queries for json response and then returns filtered json response data.
	 * @param testURL
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	private static ArrayList<String> getFailedTestData(String testURL, String baseURL) throws MalformedURLException, IOException {
		System.out.println("\tScraping Json URL: " + testURL);
		JsonObject jsonResp = getFailedTestJson(testURL);
		ArrayList<String> filteredData = filterRelevantTestData(jsonResp, baseURL);
		return filteredData;
	}

	/***
	 * Gets response from get query to JSON URL and returns JSON response
	 * @param testURL
	 * @return JsonObject
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	private static JsonObject getFailedTestJson(String testURL) throws MalformedURLException, IOException {
		String contents;
		try (InputStream is = new URL(
				testURL)
				.openStream()) {
			contents = new String(is.readAllBytes());
		}
		JsonObject element = (JsonObject) JsonParser.parseString(contents);
//	  System.out.println(element.toString());
		return element;
	}
	/**
	 * Extracts relevant data from the JSON response and returns an array of data.
	 * @param jsonResp
	 * @param baseURL
	 * @return
	 */
	private static ArrayList<String> filterRelevantTestData(JsonObject jsonResp, String baseURL) {
		ArrayList<String> filteredData = new ArrayList<>();
		String testID = getTestID(jsonResp);
		String testName = jsonResp.get("fullName").getAsString();
		String testStatus = jsonResp.get("status").getAsString();
		String flaky = jsonResp.get("flaky").getAsString();
		String newFail = jsonResp.get("newFailed").getAsString();
		String failedStep = getFailedStep(jsonResp);
		String failedBaseStep = getFailedBaseStep(failedStep);
		String prevRunResult = getPreviousRunResult(jsonResp);
		String imageURLs = getImageURLs(jsonResp, baseURL);

		filteredData.add(testID);
		filteredData.add(testName);
		filteredData.add(testStatus);
		filteredData.add(flaky);
		filteredData.add(newFail);
		filteredData.add(prevRunResult);
		filteredData.add(failedStep);
		filteredData.add(failedBaseStep);
		filteredData.add(imageURLs);
		
		return filteredData;
	}
	
	/**
	 * Takes JsonObject and extracts tag to get OPTSPENDQA num
	 * @param resp
	 * @return testID
	 */
	private static String getTestID(JsonObject resp) {
		JsonArray labels = resp.get("labels").getAsJsonArray();
		String val = ""; 
		for(JsonElement label: labels) {
			if (label.getAsJsonObject().get("value").toString().contains("OPTSPENDQA")) {
				val = label.getAsJsonObject().get("value").getAsString();
				break;
			}
		}
		return val;
	}
	
	/**
	 * Finds the failed step in the list of steps
	 * @param resp
	 * @return failedStep
	 */
	private static String getFailedStep(JsonObject resp) {
		JsonObject testStage = resp.get("testStage").getAsJsonObject();
		JsonArray steps = testStage.get("steps").getAsJsonArray();
		for(JsonElement step: steps) {
			if (step.getAsJsonObject().get("status").getAsString().equals("broken")) {
				return step.getAsJsonObject().get("name").getAsString();
			}
			if (step.getAsJsonObject().get("status").getAsString().equals("failed")) {
				return step.getAsJsonObject().get("name").getAsString();
			}
			if (step.getAsJsonObject().get("status").getAsString().equals("unknown")) {
				return step.getAsJsonObject().get("name").getAsString();
			}
		}
		return "";
	}
	
	/***
	 * Replaces step specifics to find the generic base step.
	 * @param failedStep
	 * @return baseStep
	 */
	private static String getFailedBaseStep(String failedStep) {
		return failedStep.replaceAll("\".*\"", "([^\\\"]*)");
	}
	
	/***
	 * Finds the status of the previous run of this test. Can either be "Passed", "Failed"
	 * "Broken" or "No History Found"
	 * @param resp
	 * @return
	 */
	private static String getPreviousRunResult(JsonObject resp) {
		JsonObject history = resp.get("extra").getAsJsonObject().get("history").getAsJsonObject();
		JsonArray items = history.get("items").getAsJsonArray();
		try {
			return items.get(0).getAsJsonObject().get("status").getAsString();
		} catch (Exception e) {
			return "No History Found";
		}
	}
	
	/***
	 * Extracts the Image identifiers from the json response and then creates
	 * a image url based on the baseURL for the test run and the image identifier.
	 * Returns string of imageURLs separated by ', '
	 * @param resp
	 * @param baseURL
	 * @return imageURLs
	 */
	private static String getImageURLs(JsonObject resp, String baseURL) {
		ArrayList<String> imageURLs = new ArrayList<>();
		
		JsonArray afterStages = resp.get("afterStages").getAsJsonArray();
		
		for (JsonElement afterStage: afterStages) {
			if (afterStage.getAsJsonObject().keySet().contains("attachments")) {
				JsonObject attachments = afterStage.getAsJsonObject().get("attachments").getAsJsonObject();
				String source = attachments.get("source").getAsString();
				String imageURL = baseURL + "data/attachements/" + source;
				imageURLs.add(imageURL);
			}
		}
		
		return String.join(", ", imageURLs);
	}
	
	/***
	 * Writes the scraped data to a csv file.
	 * @param name
	 * @param date
	 * @param data
	 * @throws IOException
	 */
	private static void saveResultsToCsv(String name, String date, String testRunNum, List<List> data) throws IOException {
		
		String headers = String.join("|", "Test ID", "Test Name", "Test Status", "Flaky", "New Fail", "Prev Run Result", "Failed Step", "Failed Base Step", "Image URLs");
		String fileName = "" + name + "_" + String.join("-", date.split("/")) + "_(Run_" + testRunNum + ").csv";
		File file = new File(fileName);
		FileWriter fileWriter = new FileWriter(file);
		BufferedWriter bw = new BufferedWriter(fileWriter);
		bw.write(headers + "\n");
		for (List<String> entry: data) {
			bw.write(String.join("|", entry) + "\n");
		}
		
		bw.close();
		fileWriter.close();
	}
	
	/**
	 * Loads properties from config file
	 * @param propFile
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private static Properties loadProperties(String propFile) throws FileNotFoundException, IOException {
		String path = System.getProperty("user.dir") + propFile;
		Properties props = new Properties();
		props.load(new FileInputStream(path));
		return props;
	}
	
	/***
	 * Loads base URL property
	 * @return baseURL
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private static String loadBaseURL() throws FileNotFoundException, IOException {
		Properties configProps = loadProperties("/config.properties");
		return configProps.getProperty("baseURL");
	}
	
	/**
	 * Loads run name property
	 * @return runName
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private static String loadRunName() throws FileNotFoundException, IOException {
		Properties configProps = loadProperties("/config.properties");
		return configProps.getProperty("runName");
	}
}
