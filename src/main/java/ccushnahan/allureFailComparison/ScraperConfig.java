package ccushnahan.allureFailComparison;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class ScraperConfig {
	
	private Properties props;
	private String[] baseURLs;
	private String[] runNames;
	private boolean writeToExcel;
	
	public ScraperConfig(String propFile) throws FileNotFoundException, IOException{
		this.setProperties(propFile);
		this.setBaseURLs();
		this.setRunNames();
		this.setWriteToExcel();
	}
	
	
	public String[] getBaseURLs() {
		return baseURLs;
	}


	public String[] getRunNames() {
		return runNames;
	}
	
	public boolean isWriteToExcel() {
		return writeToExcel;
	}

	/**
	 * Loads properties from config file
	 * @param propFile
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void setProperties(String propFile) throws FileNotFoundException, IOException {
		String path = System.getProperty("user.dir") + propFile;
		Properties props = new Properties();
		props.load(new FileInputStream(path));
		this.props = props;
	}
	
	/***
	 * Loads base URL property
	 * @return baseURL
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void setBaseURLs() throws FileNotFoundException, IOException {
		this.baseURLs = props.getProperty("baseURLs").split(",");
	}
	
	/**
	 * Loads run name property
	 * @return runName
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void setRunNames() throws FileNotFoundException, IOException {
		this.runNames = props.getProperty("runNames").split(",");
	}
	
	public void setWriteToExcel() {
		this.writeToExcel = props.getProperty("writeToExcel").equals("true");
	}

}
