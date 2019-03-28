package com.mulesoft.services.mulecodechecker;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;

/**
 *
 * @author kun.li
 */
public class CheckResult {

    private LinkedHashMap<String, LinkedHashMap<String, List<String>>> result;

    public CheckResult() {
        this.result = new LinkedHashMap<>();
    }

    //TODO this needs to throw unchecked Exception if there's every an overwrite-situation.
    //this relieves us of the need to separately validate rule-files - they will just die with "fail"
    // if there are duplicate XQuery-names
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
        StringBuilder sb = new StringBuilder();
        this.result.entrySet().forEach((Entry<String, LinkedHashMap<String, List<String>>> resultPerFile) -> {
            resultPerFile.getValue().entrySet().forEach((Entry<String, List<String>> resultPerXquery) -> {
                resultPerXquery.getValue().forEach((String singleResult) -> {
                    sb.append(String.format("%s,%s,%s\n", resultPerFile.getKey(), resultPerXquery.getKey(), singleResult));
                });
            });
        });
        return sb.toString();
    }

    String stringify() {
        return Objects.toString(this.result);
    }
}
