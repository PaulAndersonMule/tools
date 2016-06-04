package org.mule.modules.excel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.mule.api.annotations.Config;
import org.mule.api.annotations.Connector;
import org.mule.api.annotations.Processor;
import org.mule.api.annotations.param.Default;
import org.mule.modules.excel.config.ConnectorConfig;

@Connector(name="excel", friendlyName="Excel")
public class ExcelConnector{

    @Config
    ConnectorConfig config;

     /**
     * Custom processor
     *
	 * @param payloadInputStream
	 * @param sheetName
     * @return A greeting message
	 * @throws org.apache.poi.openxml4j.exceptions.InvalidFormatException
	 * @throws java.io.IOException
     */
    @Processor
    public OutputStream streamDataBySheetName(@Default("#[payload]") InputStream payloadInputStream, @Default("#[flowVars.sheetName]") String sheetName) 
      throws InvalidFormatException, EncryptedDocumentException, IOException {
      
      OutputStream bos = new ByteArrayOutputStream(512);
      
			try {
				ExcelRead2.processSheetByName("v1", payloadInputStream, bos);
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
      return bos; 
    }

    @Processor
    public ByteArrayOutputStream streamDataBySheetIndex(@Default("#[payload]") InputStream payloadInputStream, @Default("#[flowVars.sheetIndex]") String sheetIndex) 
      throws InvalidFormatException, EncryptedDocumentException, IOException {
      
      ByteArrayOutputStream bos = new ByteArrayOutputStream(1024);
      
      ExcelRead2.processSheetBySheetIndex(Integer.parseInt(sheetIndex), payloadInputStream, bos);
      return bos; 
    }

    public ConnectorConfig getConfig() {
        return config;
    }

    public void setConfig(ConnectorConfig config) {
        this.config = config;
    }

		private class DataStreamback implements IRowDataSink{

		private OutputStream os;
		private DataStreamback(){}
		
		public DataStreamback(OutputStream stream){
			os = stream;
		}
		
		@Override
		public void rowDataCallback(String rowAsCSV) {
			throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
		}
			
		}

}