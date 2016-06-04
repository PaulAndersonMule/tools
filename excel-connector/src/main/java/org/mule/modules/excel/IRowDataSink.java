package org.mule.modules.excel;

/**
 *
 * @author paul.anderson
 */
public interface IRowDataSink {
	void rowDataCallback(String rowAsCSV);
}
