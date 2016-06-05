package org.mule.modules.excel;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * this class reuses some (refactored) code from https://svn.apache.org/repos/asf/poi/trunk/src/examples/src/org/apache/poi/xssf/eventusermodel/XLSX2CSV.java
 * @author paul.anderson
 *
 */
public abstract class ExcelSheetStreamer {

	private ExcelSheetStreamer() {
	}

	/**
	 * streams CSV-formattetd data from the named sheet (contained in the InputStream parameter) to an OutpuStream
	 * @param sheetName the name of the worksheet to convert to CSV
	 * @param excelFileStream the InputStream of the Excel-file to be converted
	 * @param sink the OutputStream to which the CSV-data is streamed.
	 */
	public static void processSheetByName(String sheetName, InputStream excelFileStream, OutputStream sink) {
		processOneSheet(-99, sheetName, false, excelFileStream, sink);
	}

  /**
   * streams CSV-formattetd data from the sheet at sheetIndex (contained in the InputStream parameter) to an OutpuStream
   * @param sheetIndex the index of the worksheet to convert to CSV.
   * @param excelFileStream the InputStream of the Excel-file to be converted
   * @param sink the OutputStream to which the CSV-data is streamed.
   */
	public static void processSheetBySheetIndex(int sheetIndex, InputStream excelFileStream, OutputStream sink) {
		processOneSheet(sheetIndex, "unused", true, excelFileStream, sink);
	}

	/**
	 * utility multifunction method which does something a bit ugly becuase of how xssfReader and sharedStringsTable are so tied together.
	 * @param sheetIndex the index of the sheet to be read, converted, streamed
	 * @param sheetName the name of the sheet to be read, converted, streamed
	 * @param isBySheetIndex indicates whether the user requested by sheet-index or not
	 * @param excelFileStream this is a stream of the Excel file. Creation and mgt of the stream is done by Mule
	 * @param sink the OutputStream to which data is streamed
	 */
	private static void processOneSheet(int sheetIndex, String sheetName, boolean isBySheetIndex, InputStream excelFileStream, OutputStream sink) {
		OPCPackage pkg;
		try {
			pkg = OPCPackage.open(excelFileStream);
		} catch (InvalidFormatException | IOException ex) {
			throw new RuntimeException(ex);
		}
		XSSFReader xssfReader;
		SharedStringsTable sharedStringsTable = null;
		try {
			xssfReader = new XSSFReader(pkg);
			sharedStringsTable = xssfReader.getSharedStringsTable();
		} catch (IOException | OpenXML4JException ex) {
			throw new RuntimeException(ex);
		}

		InputStream sheetInputStream = null;

		sheetInputStream = getInputStreamForSheet(xssfReader, sheetName, sheetIndex, isBySheetIndex, excelFileStream);

		if (sheetInputStream == null) {
			throw new RuntimeException("sheet " + sheetName + " does not exist in the Excel file");
		}

		XMLReader parser;
		try {
			parser = fetchSheetParser(sharedStringsTable, sink);
		} catch (SAXException ex) {
			throw new RuntimeException(ex);
		}

		// To look up the Sheet Name / Sheet Order / rID,
		//  you need to process the core Workbook stream.
		// Normally it's of the form rId# or rSheet#
		InputSource sheetSource;
		try {
			sheetSource = new InputSource(sheetInputStream);
			parser.parse(sheetSource);
		} catch (IOException | SAXException ex) {
			throw new RuntimeException(ex);
		} finally {
			try {
				sheetInputStream.close();
			} catch (IOException ex) {
				Logger.getLogger(ExcelSheetStreamer.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

	/**
	 * utility method to get the InputStream which allows iteration through the sheets in the workbook
	 * @param xssfReader the reader that streams the workbook itself
	 * @return iterator allowing iteration over all the sheets
	 */
	private static XSSFReader.SheetIterator getSheetsIterator(XSSFReader xssfReader) {
		try {
			return (XSSFReader.SheetIterator) xssfReader.getSheetsData();
		} catch (InvalidFormatException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * locates the input-stream of the desired sheet
   * @param xssfReader the reader that streams the workbook itself
	 * @param sheetName the name of the requested sheet - may be empty: see other parameters
	 * @param sheetIndex the index of the requested sheet - may be -99: see other parameters
	 * @param isBySheetIndex indicates whether the user asked for sheet by its index
	 * @return the inputstream which will stream the contents of the requested sheet.
	 */
	private static InputStream getInputStreamForSheet(XSSFReader xssfReader, String sheetName, int sheetIndex, boolean isBySheetIndex) {

		XSSFReader.SheetIterator iter = getSheetsIterator(xssfReader);

		InputStream sheetInputStream;
		int index = 0;

		while (iter.hasNext()) {
			sheetInputStream = iter.next();
			index++;
			String thisSheetName = iter.getSheetName();

			if ((!isBySheetIndex && sheetName.equals(thisSheetName)) || (index == sheetIndex)) {
				return sheetInputStream;
			}
		}
		throw new RuntimeException("sheet named " + sheetName + " does not exist");
	}

	private static XMLReader fetchSheetParser(SharedStringsTable sst, OutputStream sink) throws SAXException {
		XMLReader parser = XMLReaderFactory.createXMLReader();
		ContentHandler handler = new SheetHandler(sst, sink);
		parser.setContentHandler(handler);
		return parser;
	}

//	public static void main(String[] args) throws Exception {
//		String loc = "/Users/paul.anderson/projects/archive/McDonalds/doc/MCD_Middleware_APIDev_ResourceList.xlsx";
////		String loc = "/Users/paul.anderson/asd.xlsx";
//		OutputStream os = new ByteArrayOutputStream();
//		ExcelSheetStreamer.processSheetByName("v1", new FileInputStream(new File(loc)), os);
//		System.out.println(os.toString());
////		ExcelSheetStreamer.processSheetBySheetIndex(1, new FileInputStream(new File(loc)), os);
//	}
}
