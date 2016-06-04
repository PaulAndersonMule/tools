package org.mule.modules.excel;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author paul.anderson
 */
class SheetHandler extends DefaultHandler {

		private final SharedStringsTable sst;
		private String lastContents;
		private boolean nextIsString;
		private final Pattern CELL_ADDRESS_PATTERN = Pattern.compile("^([A-Z]*)(\\d{1,20})$", Pattern.CASE_INSENSITIVE);
		private String currentRowAddress;
		private String cellAddress;
		private boolean rowChanged = false;
		StringBuilder contentsEntireRow = new StringBuilder();
		private final OutputStream rowDataCallbackDestination;
		
		protected SheetHandler(SharedStringsTable sst, OutputStream rowDataCallbackDestination) {
			this.sst = sst;
			this.rowDataCallbackDestination = rowDataCallbackDestination;
		}

		@Override
		public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
			// c is the element-name for "cell"

			if (name.equals("c")) {
				cellAddress = attributes.getValue("r");
				Matcher matcher = CELL_ADDRESS_PATTERN.matcher(cellAddress);
				String thisRowAddress = null;
				if (matcher.matches()) {
					thisRowAddress = matcher.group(2);
					if (currentRowAddress == null){
						currentRowAddress = thisRowAddress;
					}
					if (currentRowAddress != null && !thisRowAddress.equals(currentRowAddress)) {
						rowChanged = true;
						currentRowAddress = thisRowAddress;
						try {
						  contentsEntireRow.append('\n');
							rowDataCallbackDestination.write(contentsEntireRow.toString().getBytes(Charset.defaultCharset()));
						} catch (IOException ex) {
							throw new RuntimeException(ex);
						}
						contentsEntireRow.delete(0, contentsEntireRow.length());
					}
				}
				// Figure out if the value is an index in the SST
				String cellType = attributes.getValue("t");
				if (cellType != null && cellType.equals("s")) {
					nextIsString = true;
				} else {
					nextIsString = false;
				}
			}
			// Clear contents cache
			lastContents = "";
		}

		@Override
		public void endElement(String uri, String localName, String name)
						throws SAXException {
			if (nextIsString) {
				int idx = Integer.parseInt(lastContents);
				lastContents = new XSSFRichTextString(sst.getEntryAt(idx)).toString();
				nextIsString = false;
			}

			// v => contents of a cell
			// Output after we've seen the string contents
			if (name.equals("v")) {
				contentsEntireRow.append(lastContents).append(",");
			}
		}

		@Override
		public void characters(char[] ch, int start, int length)
						throws SAXException {
			lastContents += new String(ch, start, length);
		}
	}

