package org.mule.modules.excel.config;

import org.mule.api.annotations.components.Configuration;
import org.mule.api.annotations.Configurable;
import org.mule.api.annotations.param.Default;

@Configuration(friendlyName = "Configuration")
public class ConnectorConfig {

    /**
     * Greeting message
     */
    @Configurable
    @Default("Sheet1")
    private String sheetIdentifier;

    /**
     * Set sheet identifier
     *
     * @param sheetIdentifier the greeting message
     */
    public void setSheetIdentifier(String sheetIdentifier) {
        this.sheetIdentifier = sheetIdentifier;
    }

    /**
     * Get sheetIdentifier
     */
    public String getSheetIdentifier() {
        return this.sheetIdentifier;
    }

}