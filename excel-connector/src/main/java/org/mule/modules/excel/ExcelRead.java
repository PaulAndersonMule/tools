package org.mule.modules.excel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

/**
 *
 * @author Apache POI project documentation
 */
public class ExcelRead {

  public static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");

  public static void streamExcelAsCSV(InputStream is, OutputStream bos) throws InvalidFormatException, EncryptedDocumentException, IOException {

    Workbook wb = WorkbookFactory.create(is);
    Sheet sheet = wb.getSheetAt(0);
    streamExcelAsCSV(is, wb, sheet, bos);
  }

  public static void streamExcelAsCSV(InputStream is, int sheetIndex, OutputStream bos) throws InvalidFormatException, EncryptedDocumentException, IOException {

    Workbook wb = WorkbookFactory.create(is);
    Sheet sheet = wb.getSheetAt(sheetIndex);
    streamExcelAsCSV(is, wb, sheet, bos);
  }

  public static void streamExcelAsSCV(InputStream is, String sheetName, OutputStream bos) throws InvalidFormatException, EncryptedDocumentException, IOException {

    Workbook wb = WorkbookFactory.create(is);
    Sheet sheet = wb.getSheet(sheetName);
    streamExcelAsCSV(is, wb, sheet, bos);
  }

  private static void streamExcelAsCSV(InputStream is, Workbook wb, Sheet sheet, OutputStream bos) throws InvalidFormatException, EncryptedDocumentException, IOException {

    try {
      for (Row row : sheet) {
        StringBuilder sb = new StringBuilder();
        for (Cell cell : row) {
          processCell(sb, cell);
        }
        sb.append('\n');
        try {
          bos.write(sb.toString().getBytes());
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    } finally {
      wb.close();
    }
  }

  private static void processCell(StringBuilder sb, Cell cell) {
    switch (cell.getCellType()) {
    case Cell.CELL_TYPE_BLANK:
      sb.append(",");
      break;
    case Cell.CELL_TYPE_BOOLEAN:
      sb.append(cell.getBooleanCellValue()).append(",");
      break;
    case Cell.CELL_TYPE_FORMULA:
      switch (cell.getCachedFormulaResultType()) {
      case Cell.CELL_TYPE_NUMERIC:
        if (HSSFDateUtil.isCellDateFormatted(cell)) {
          LocalDate ldate = cell.getDateCellValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
          sb.append(ldate.format(dtf));
        } else {
          sb.append(cell.getNumericCellValue()).append(",");
        }
        break;
      case Cell.CELL_TYPE_STRING:
        sb.append(cell.getStringCellValue()).append(",");
        break;
      }

      break;
    case Cell.CELL_TYPE_NUMERIC:
      sb.append(cell.getNumericCellValue()).append(",");
      break;
    case Cell.CELL_TYPE_STRING:
      sb.append(cell.getStringCellValue()).append(",");
    default:
      System.out.println("impossible");
    }
  }
}
