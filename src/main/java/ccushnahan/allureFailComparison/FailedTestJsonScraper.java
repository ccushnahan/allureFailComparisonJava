package ccushnahan.allureFailComparison;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/***
 * Scrapes Json files
 * @author cush
 *
 */
public class FailedTestJsonScraper {

	private List<String> jsonList;
	private String baseURL;
	
	public FailedTestJsonScraper(List<String> jsonList, String baseURL) {
		this.jsonList = jsonList;
		this.baseURL = baseURL;
	}
	
	/***
	 * Takes list of failed test urls and extracts failed test data.
	 * @throws IOException 
	 * @throws MalformedURLException 
	 * @throws InterruptedException 
	 * @returns failedTestsData
	 */
	public List<List> scrapeFailedTestsData() throws MalformedURLException, IOException, InterruptedException {
		List<List> failedTestsData = new ArrayList<>();
		
		for (String failedTestURL: jsonList) {
			ArrayList<String> failedTestData = scrapeFailedTestData(failedTestURL);
			failedTestsData.add(failedTestData);
			TimeUnit.SECONDS.sleep(3);
		}
		
		return failedTestsData;
	}
	
	/***
	 * Takes url queries for json response and then returns filtered json response data.
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	private ArrayList<String> scrapeFailedTestData(String testURL) throws MalformedURLException, IOException {
		System.out.println("\tScraping Json URL: " + testURL);
		JsonObject jsonResp = scrapeFailedTestJson(testURL);
		ArrayList<String> filteredData = filterRelevantTestData(jsonResp);
		return filteredData;
	}

	/***
	 * Gets response from get query to JSON URL and returns JSON response
	 * @return JsonObject
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	private JsonObject scrapeFailedTestJson(String testURL) throws MalformedURLException, IOException {
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
	 * @return
	 */
	private ArrayList<String> filterRelevantTestData(JsonObject jsonResp) {
		return (new FailedTest(jsonResp, baseURL).getFailedTestArray());
	}
}
