package ccushnahan.allureFailComparison;

import java.util.ArrayList;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class FailedTest {
	
	private String testName;
	private String testID;
	private String testStatus;
	private boolean flaky;
	private boolean newFail;
	private String previousRunResult;
	private String failedStep;
	private String failedBaseStep;
	private String imageURLs;
	private String baseURL;
	private JsonObject jsonObject;
	
	public FailedTest(JsonObject jsonObject, String baseURL) {
		this.jsonObject = jsonObject;
		this.baseURL = baseURL;
		this.setTestName();
		this.setTestID();
		this.setTestStatus();
		this.setFlakyStatus();
		this.setNewFailStatus();
		this.setPreviousRunResult();
		this.setFailedStep();
		this.setFailedBaseStep();
		this.setImageURLs();
	}
	
	/**
	 * Extracts relevant data from the JSON response and returns an array of data.
	 * @param jsonResp
	 * @param baseURL
	 * @return
	 */
	public ArrayList<String> getFailedTestArray() {
		ArrayList<String> filteredData = new ArrayList<>();

		filteredData.add(this.testID);
		filteredData.add(this.testName);
		filteredData.add(this.testStatus);
		filteredData.add(Boolean.toString(this.flaky));
		filteredData.add(Boolean.toString(newFail));
		filteredData.add(this.previousRunResult);
		filteredData.add(this.failedStep);
		filteredData.add(this.failedBaseStep);
		filteredData.add(this.imageURLs);
		
		return filteredData;
	}
	
	
	/***
	 * Takes JsonObject and extracts name of test
	 */
	private void setTestName() {
		this.testName = this.jsonObject.get("fullName").getAsString();
	}
	
	/**
	 * Takes JsonObject and extracts tag to get OPTSPENDQA num
	 */
	private void setTestID() {
		JsonArray labels = this.jsonObject.get("labels").getAsJsonArray();
		String val = ""; 
		for(JsonElement label: labels) {
			if (label.getAsJsonObject().get("value").toString().contains("OPTSPENDQA")) {
				val = label.getAsJsonObject().get("value").getAsString();
				break;
			}
		}
		this.testID = val;
	}
	
	/***
	 * Takes JSON object and extracts test run status.
	 */
	private void setTestStatus() {
		this.testStatus = this.jsonObject.get("status").getAsString();
	}
	
	/***
	 * Takes JSON object and extracts flaky status
	 */
	private void setFlakyStatus() {
		this.flaky = this.jsonObject.get("flaky").getAsBoolean();
	}
	
	/***
	 * Takes Json Object and extracts New Fail status
	 */
	private void setNewFailStatus() {
		this.newFail = this.jsonObject.get("newFailed").getAsBoolean();
	}
	
	/**
	 * Finds the failed step in the list of steps
	 */
	private void setFailedStep() {
		JsonObject resp = this.jsonObject;
		JsonObject testStage = resp.get("testStage").getAsJsonObject();
		JsonArray steps = testStage.get("steps").getAsJsonArray();
		for(JsonElement step: steps) {
			if (step.getAsJsonObject().get("status").getAsString().equals("broken")) {
				this.failedStep =  step.getAsJsonObject().get("name").getAsString();
				break;
			}
			if (step.getAsJsonObject().get("status").getAsString().equals("failed")) {
				this.failedStep =  step.getAsJsonObject().get("name").getAsString();
			}
			if (step.getAsJsonObject().get("status").getAsString().equals("unknown")) {
				this.failedStep = step.getAsJsonObject().get("name").getAsString();
				break;
			}
		}
	}
	
	/***
	 * Replaces step specifics to find the generic base step.
	 */
	private void setFailedBaseStep() {
		this.failedBaseStep = this.getFailedStep().replaceAll("\".*\"", "([^\\\"]*)");
	}
	
	/***
	 * Finds the status of the previous run of this test. Can either be "Passed", "Failed"
	 * "Broken" or "No History Found"
	 */
	private void setPreviousRunResult() {
		JsonObject history = this.jsonObject.get("extra").getAsJsonObject().get("history").getAsJsonObject();
		JsonArray items = history.get("items").getAsJsonArray();
		try {
			this.previousRunResult =  items.get(0).getAsJsonObject().get("status").getAsString();
		} catch (Exception e) {
			this.previousRunResult =  "No History Found";
		}
	}
	
	/***
	 * Extracts the Image identifiers from the JSON object and then creates
	 * a image URL based on the baseURL for the test run and the image identifier.
	 * Sets string of imageURLs separated by ', '
	 */
	private void setImageURLs() {
		ArrayList<String> imageURLs = new ArrayList<>();
		
		JsonArray afterStages = this.jsonObject.get("afterStages").getAsJsonArray();
		
		for (JsonElement afterStage: afterStages) {
			if (afterStage.getAsJsonObject().keySet().contains("attachments")) {
				JsonArray attachments = afterStage.getAsJsonObject().get("attachments").getAsJsonArray();
				for (JsonElement attachment: attachments) {
					String source = attachment.getAsJsonObject().get("source").getAsString();
					String imageURL = this.baseURL + "data/attachements/" + source;
					imageURLs.add(imageURL);					
				}
			}
		}
		
		this.imageURLs =  String.join(", ", imageURLs);
	}

	public String getPreviousRunResult() {
		return previousRunResult;
	}

	public String getTestName() {
		return testName;
	}

	public String getTestID() {
		return testID;
	}

	public String getTestStatus() {
		return testStatus;
	}

	public boolean isFlaky() {
		return flaky;
	}

	public boolean isNewFail() {
		return newFail;
	}

	public String getFailedStep() {
		return failedStep;
	}

	public String getFailedBaseStep() {
		return failedBaseStep;
	}

	public String getImageURLs() {
		return imageURLs;
	}

	public String getBaseURL() {
		return baseURL;
	}

	public JsonObject getJsonObject() {
		return jsonObject;
	}
	
}
