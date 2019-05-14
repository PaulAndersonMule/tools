package com.mulesoft.services.mulecodechecker;

import com.pa.xpathutils.NamespaceResolverImpl;
import com.pa.xpathutils.XPathUtils;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import javax.xml.xpath.XPathExpressionException;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmItem;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author kun.li
 */
public class MuleCodeChecker {

  protected static final Logger log = LogManager.getLogger(MuleCodeChecker.class);
  private static final String USAGE = "usage is 4 arguments: absolute path to well-formed Mule project, main APIKit XML file name|default, absolute path to XQuery rules file|default, RAW|JAVA";

  public MuleCodeChecker() {
  }

  public static CheckResult runCodeChecker(String inputPath, String mainFileName, String xQueriesFilePath, String outputFormat) {

    List<Path> muleFiles = FileOperations.getMuleXMLFiles(inputPath);
    XQueries.initialize(xQueriesFilePath);

    // use project name as mainFileName if default is specified.
    String projectName = FileOperations.getProjectName(inputPath);
    if (mainFileName.equals("default")) {
      mainFileName = projectName;
    }

    // run the check
    return check(muleFiles, mainFileName);
  }

  private static void runCodeChecker(String[] args) {
    if (args.length != 4) {
      System.out.println(USAGE);
    }
    CheckResult result = runCodeChecker(args[0], args[1], args[2], args[3]);
    String outputFormat = args[3];
    switch (outputFormat) {
      case "RAW":
        System.out.println(result.toCSV());
        break;

      case "JAVA":
        System.out.println(result.asJava());
        break;
    }
  }

  /**
   * currently not used
   *
   * @param args
   * @return
   */
  private static List<String> validateOptions(String args[]) {
    Options options = buildOptions();
    // create the parser
    CommandLineParser parser = new DefaultParser();

    // parse the command line arguments
    CommandLine line = null;

    try {
      line = parser.parse(options, args);
    } catch (ParseException x) {
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp(" ", options);
      throw new RuntimeException("see usage messages above");
    }

    String inputPath = line.getOptionValue("inputPath");
    String xQueriesFilePath = line.getOptionValue("xQueries");
    String outputFormat;
    try {
      outputFormat = OutputFormat.valueOf(line.getOptionValue("outputFormat")).name();
    } catch (IllegalArgumentException e) {
      HelpFormatter formatter = new HelpFormatter();
      System.out.println("invalid outputFormat: " + line.getOptionValue("outputFormat"));
      formatter.printHelp(" ", options);
      return null;
    }

    List<String> argsL = new ArrayList<>();
    argsL.add(inputPath);
    argsL.add(xQueriesFilePath);
    argsL.add(outputFormat);

    return null;
  }

  private static Options buildOptions() {
    Option inputPathOption = Option.builder()
            .longOpt("inputPath")
            .hasArg()
            .type(java.lang.String.class)
            .desc("Absolute path to a well-formed Mule project, pointing at the project root")
            .required()
            .build();

    Option xQueriesOption = Option.builder()
            .longOpt("xQueries")
            .hasArg()
            .type(java.lang.String.class)
            .desc("Input path to a file containing XQuery expressions")
            .required()
            .build();

    Option outputFormatOption = Option.builder()
            .longOpt("outputFormat")
            .hasArg()
            .desc("Output format. Must be RAW or JAVA")
            .required()
            .build();

    Options options = new Options();
    options.addOption(inputPathOption);
    options.addOption(xQueriesOption);
    options.addOption(outputFormatOption);

    return options;
  }

  /**
   *
   * @param muleFiles
   * @param xQueries
   * @return <pre>
   * LinkedHashMap
   *  key = fileName
   *  value = LinkedHashMap
   *      key humanReadableName
   *      value List<String> of results from running xQuery against fileName
   * </pre>
   */
  private static CheckResult check(List<Path> muleFiles, String mainFileName) {
    CheckResult checkResult = new CheckResult();

    for (Path muleFile : muleFiles) {
      
      LinkedHashMap<String, List<String>> singleResult = new LinkedHashMap<>();
      String xmlContents = FileOperations.readFileContents(muleFile);
      log.debug("checking file: " + muleFile.toAbsolutePath());

      xqueryResults(checkResult, xmlContents, muleFile, mainFileName);
    }
    return checkResult;
  }

  private static void xqueryResults(CheckResult checkResult, String xmlContents, Path muleFile, String mainFileName) {
    List<CheckResult> results = new ArrayList<>();

    NamespaceResolverImpl nsrImpl;
    try {
      nsrImpl = new NamespaceResolverImpl(xmlContents);
    } catch (XPathExpressionException ex) {
      throw new RuntimeException(String.format("Error during processing file '%s' - invalid XML, missing namespace-declaration etc.)", muleFile), ex);
    }
    
    for (String xQueryName : XQueries.keySet()) {

      String xQuery = XQueries.get(xQueryName);
      // pre-process the XQuery to replace the file name
      String fileNameWithoutExt = StringUtils.removeEndIgnoreCase(muleFile.getFileName().toString(), ".xml");
      xQuery = XQueries.format(xQuery, fileNameWithoutExt, mainFileName);
      log.debug("pre-processed XQuery: " + xQuery);

      // run the XQuery against the XML file content
      List<XdmItem> xdmItems;
      try {
        xdmItems = XPathUtils.evaluate(xQuery, xmlContents, nsrImpl);
      } catch (SaxonApiException ex) {
        throw new RuntimeException(String.format("Error occurred when running XQuery check for query '%s' (%s) ", xQueryName, XQueries.get(xQueryName)), ex);
      }
      List<String> xqueryResult = new ArrayList<>();

      xdmItems.forEach((item) -> {
        xqueryResult.add(item.toString());
      });
      checkResult.addResult(muleFile.getFileName().toString(), xQueryName, xqueryResult);
    }
  }

  public static void main(String[] args) {

    try {
//      MuleCodeChecker codeChecker = new MuleCodeChecker();
      if (args[3].equals("default")) {
        args[3] = "RAW";
      }
      MuleCodeChecker.runCodeChecker(args);
    } catch (Exception e) {
      e.printStackTrace();
      log.debug(e.getMessage(), e);
    }
  }
}
