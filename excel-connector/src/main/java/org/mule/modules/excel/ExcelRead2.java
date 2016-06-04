package org.mule.modules.excel;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;

import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public abstract class ExcelRead2 {

	private ExcelRead2() {
	}

	public static void processSheetByName(String sheetName, InputStream excelFileStream, OutputStream sink) {
		processOneSheet(-99, sheetName, false, excelFileStream, sink);
	}

	public static void processSheetBySheetIndex(int sheetIndex, InputStream excelFileStream, OutputStream sink) {
		processOneSheet(sheetIndex, "unused", true, excelFileStream, sink);
	}

	private static void processOneSheet(int sheetIndex, String sheetName, boolean isBySheetIndex, InputStream excelFileStream, OutputStream sink) {

//		InputStream sheetInputStream = null;
//		while (iter.hasNext()){
//			iter.next();
//      String thisSheetName = iter.getSheetName();
//      if (sheetName.equals(thisSheetName)){
//        sheetInputStream = iter.next();
//        break;
//      }
//		}
		OPCPackage pkg;
		try {
			pkg = OPCPackage.open(excelFileStream);
		} catch (InvalidFormatException | IOException ex) {
			throw new RuntimeException(ex);
		}
		XSSFReader xssfReader;
		SharedStringsTable sst = null;
		try {
			xssfReader = new XSSFReader(pkg);
			sst = xssfReader.getSharedStringsTable();
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
			parser = fetchSheetParser(sst, sink);
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
				Logger.getLogger(ExcelRead2.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

	private static XSSFReader.SheetIterator getSheetsIterator(XSSFReader xssfReader) {
		XSSFReader.SheetIterator iter;
		try {
			return (XSSFReader.SheetIterator) xssfReader.getSheetsData();
		} catch (InvalidFormatException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static InputStream getInputStreamForSheet(XSSFReader xssfReader, String sheetName, int sheetIndex, boolean isBySheetIndex, InputStream excelFileStream) {

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

	public static void main(String[] args) throws Exception {
		String loc = "/Users/paul.anderson/projects/archive/McDonalds/doc/MCD_Middleware_APIDev_ResourceList.xlsx";
//		String loc = "/Users/paul.anderson/asd.xlsx";
		OutputStream os = new ByteArrayOutputStream();
		ExcelRead2.processSheetByName("v1", new FileInputStream(new File(loc)), os);
		System.out.println(os.toString());
//		ExcelRead2.processSheetBySheetIndex(1, new FileInputStream(new File(loc)), os);
	}
}
