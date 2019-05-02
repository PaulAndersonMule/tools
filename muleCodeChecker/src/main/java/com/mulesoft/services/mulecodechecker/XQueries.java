package com.mulesoft.services.mulecodechecker;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author kun.li
 */
public abstract class XQueries {

  private static final Logger log = LogManager.getLogger(MuleCodeChecker.class);

  private static final Map<String, String> xqueries = new LinkedHashMap<>();
  
  protected static void initialize(String rulesFilePath){
    xqueries.clear();
    loadFromFile(rulesFilePath);
  }
  
  protected static void put(String name, String xquery) {
    if (xqueries.containsKey(name)) {
      throw new IllegalArgumentException("Duplicate XQuery name is not allowed. Please check this XQuery: " + name);
    }

    xqueries.put(name, xquery);
  }

  protected static String get(String name) {
    return xqueries.get(name);
  }

  protected static Set<String> keySet() {
    return Collections.unmodifiableSet(xqueries.keySet());
  }

  /**
   * Load the XQueries from the specified file. Each line of the file must be in
   * this form:
   * <pre>
   * "I am an XPath. All chars except quotes allowed", (everything after that is XQuery)
   * </pre>
   *
   * @param xQueriesFilePath the absolute path of the xquery file
   */
  private static void loadFromFile(String xQueriesFilePath) {
    log.debug("loading XQueries from file: " + xQueriesFilePath);
    Path path = Paths.get(xQueriesFilePath);
    if (!Files.exists(path)) {
      throw new IllegalArgumentException("The Xquery file does not exist: " + xQueriesFilePath);
    }
    try {
      List<String> lines = Files.readAllLines(path);
      // separate the XQuery name and expression, capture them using regular expression groups.
      String pattern = "(\\\"[^\\\"]*\\\"),(.*)";
      Pattern r = Pattern.compile(pattern);

      lines.forEach(line -> {
        // ignore empty lines
        if (line.trim().length() > 0) {
          Matcher m = r.matcher(line);
          if (m.find()) {
            String name = m.group(1);
            String xquery = m.group(2).trim();
            put(name, xquery);
            log.debug("loaded XQuery. name:" + name + " xquery: " + xquery);
          } else {
            throw new IllegalArgumentException("Each line in the xquery file must be in this format: \"I am an XPath. All chars except quotes allowed\", (everything after that is XQuery)");
          }
        }
      });
    } catch (IOException ioe) {
      throw new RuntimeException("Error loading from XQuery file: " + xQueriesFilePath, ioe);
    }
  }

  protected static String format(String xquery, String fileName) {
    // currently only support replacing the file name.
    if (StringUtils.containsIgnoreCase(xquery, "^fileName^")) {
      return StringUtils.replaceIgnoreCase(xquery, "^fileName^", "\"" + fileName + "\"");
    } else {
      return xquery;
    }
  }
}
