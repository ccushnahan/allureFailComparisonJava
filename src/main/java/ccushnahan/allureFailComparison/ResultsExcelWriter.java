package ccushnahan.allureFailComparison;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ResultsExcelWriter {

	/***
	 * Writes the scraped data to a excel file.
	 * @param name
	 * @param date
	 * @param data
	 * @throws IOException
	 */
	public static void saveResultsToExcel(String name, String date, String testRunNum, List<List> data) throws IOException {
		String[] headers = {"Test ID", "Test Name", "Test Status", "Flaky", "New Fail", "Prev Run Result", "Failed Step", "Failed Base Step", "Image URLs"};		
		String fileName = "" + name + "_" + String.join("-", date.split("/")) + ".xlsx";
		
		XSSFWorkbook book = new XSSFWorkbook();
		Sheet sheet = book.createSheet(date.replaceAll("/", "-") + " " + name);
		
		Row header = sheet.createRow(0);
		
		CellStyle headerStyle = book.createCellStyle();
		XSSFFont font = ((XSSFWorkbook) book).createFont();
		font.setFontName("Arial");
		font.setBold(true);
		headerStyle.setFont(font);
		setColumnWidths(sheet);
		
		int colNum = 0;
		for (String headerName: headers) {
			Cell headerCell = header.createCell(colNum);
			headerCell.setCellValue(headerName);
			headerCell.setCellStyle(headerStyle);
			colNum++;
		}
		
		CellStyle style = book.createCellStyle();
		style.setWrapText(true);
		
		int rowNum = 1;
		for (List<String> results: data) {
			colNum = 0;
			Row row = sheet.createRow(rowNum);
			for (String result: results) {
				Cell cell = row.createCell(colNum);
				cell.setCellValue(result);
				cell.setCellStyle(style);
				colNum++;
			}
			rowNum++;
			
		}
		
		FileOutputStream file = new FileOutputStream(new File(fileName));
		book.write(file);
		book.close();
	}
	
	public static void setColumnWidths(Sheet sheet) {
		sheet.setColumnWidth(0, 4000);
		sheet.setColumnWidth(1, 10000);
		sheet.setColumnWidth(2, 4000);
		sheet.setColumnWidth(3, 4000);
		sheet.setColumnWidth(4, 4000);
		sheet.setColumnWidth(5, 4000);
		sheet.setColumnWidth(6, 10000);
		sheet.setColumnWidth(7, 10000);
		sheet.setColumnWidth(8, 30000);

	}
}
