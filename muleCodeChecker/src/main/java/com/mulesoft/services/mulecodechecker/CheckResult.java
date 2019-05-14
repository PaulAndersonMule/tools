package com.mulesoft.services.mulecodechecker;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

/**
 *
 * @author kun.li
 */
public class CheckResult {

  /*
     * key: file name
     * value: a map of rule result:
     *   key: rule name
     *   value: rule result
   */
  private final LinkedHashMap<String, LinkedHashMap<String, List<String>>> result;

  public CheckResult() {
    this.result = new LinkedHashMap<>();

  }

  void addResult(String fileName, String xqueryName, List<String> result) {
    LinkedHashMap<String, List<String>> resultPerFile;
    if (!this.result.containsKey(fileName)) {
      // initiate the result of this file
      resultPerFile = new LinkedHashMap<>();
    } else {
      resultPerFile = this.result.get(fileName);
    }
    resultPerFile.put(xqueryName, result);
    this.result.put(fileName, resultPerFile);
  }

  public String toCSV() {
    boolean passed = true;
    StringBuilder sb = new StringBuilder();

    for (Entry<String, LinkedHashMap<String, List<String>>> resultPerFile : this.result.entrySet()) {
      sb.append("\n");
      // for each check results per XML file
      Set<Entry<String, List<String>>> results = resultPerFile.getValue().entrySet();

      for (Entry<String, List<String>> resultPerXquery : results) {
        for (String singleResult : resultPerXquery.getValue()) {
          String pass = singleResult.substring(singleResult.lastIndexOf(",") + 1).trim();
          boolean passThisOne = Boolean.valueOf(pass);
          passed = passed && passThisOne;
          sb.append(String.format("%s,%s,%s\n", resultPerFile.getKey(), resultPerXquery.getKey(), singleResult));
        }
      }
    }

    return String.format("%s\n^ ^ ^ final result\n%s", passed, sb.toString());
  }

  protected Object asJava() {
    return Collections.unmodifiableMap(result);
  }
}
