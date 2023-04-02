package ccushnahan.allureFailComparison;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CSVWriter {

	/***
	 * Writes the scraped data to a csv file.
	 * @param name
	 * @param date
	 * @param data
	 * @throws IOException
	 */
	public static void saveResultsToCsv(String name, String date, String testRunNum, List<List> data) throws IOException {
		
		String headers = String.join("|", "Test ID", "Test Name", "Test Status", "Flaky", "New Fail", "Prev Run Result", "Failed Step", "Failed Base Step", "Image URLs");
		String fileName = "" + name + "_" + String.join("-", date.split("/")) + ".csv";
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
}

