package com.mulesoft.services.mulecodechecker;

import com.pa.xpathutils.NamespaceResolverImpl;
import com.pa.xpathutils.XPathUtils;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;
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

  private static final Logger log = LogManager.getLogger(MuleCodeChecker.class);
  private static final String FILE_SEPARATOR = System.getProperty("file.separator");

  public MuleCodeChecker() {
  }

  public static CheckResult runCodeChecker(String inputPath, String xQueriesFilePath, String outputFormat) {

    List<Path> muleFiles = getMuleXMLFiles(inputPath);
    XQueries.initialize(xQueriesFilePath);

    // run the check
    return check(muleFiles);
  }

  private static void runCodeChecker(String[] args) {
    if (args.length != 3) {
      System.out.println("usage is 3 arguments: absolute path to well-formed Mule project, absolute path to XQuery rules file, RAW|JAVA");
    }
    CheckResult result = runCodeChecker(args[0], args[1], args[2]);
    String outputFormat = args[2];
    switch (outputFormat) {
      case "RAW":
        System.out.println(result.toCSV());
        break;

      case "JAVA":
        System.out.println(result.asJava());
        break;
    }
  }

  private static List<String> validateOptions(String args[]) {
    Options options = buildOptions();
    // create the parser
    CommandLineParser parser = new DefaultParser();

    // parse the command line arguments
    CommandLine line = null;

    try {
      parser.parse(options, args);
    } catch (ParseException x) {
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp(" ", options);
      return null;
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
  private static CheckResult check(Collection<Path> muleFiles) {
    CheckResult result = new CheckResult();
    muleFiles.forEach((muleFile) -> {
      LinkedHashMap<String, List<String>> singleResult = new LinkedHashMap<>();
      String xmlContents = readFileContents(muleFile);
      log.debug("checking file: " + muleFile.toAbsolutePath());

      NamespaceResolverImpl nsrImpl;
      try {
        nsrImpl = new NamespaceResolverImpl(xmlContents);
      } catch (XPathExpressionException ex) {
        throw new RuntimeException(String.format("Error during processing file '%s' - invalid XML, missing namespace-declaration etc.)", muleFile), ex);
      }

      XQueries.keySet().forEach((xQueryName) -> {
        try {
          String xQuery = XQueries.get(xQueryName);
          // pre-process the XQuery to replace the file name
          String fileNameWithoutExt = StringUtils.removeEndIgnoreCase(muleFile.getFileName().toString(), ".xml");
          xQuery = XQueries.format(xQuery, fileNameWithoutExt);
          log.debug("pre-processed XQuery: " + xQuery);

          // run the XQuery against the XML file content
          List<XdmItem> xdmItems = XPathUtils.evaluate(xQuery, xmlContents, nsrImpl);
          List<String> xqueryResult = new ArrayList<>();

          xdmItems.forEach((item) -> {
            xqueryResult.add(item.toString());
          });
          result.addResult(muleFile.getFileName().toString(), xQueryName, xqueryResult);
        } catch (SaxonApiException ex) {
          throw new RuntimeException(String.format("Error occurred when running XQuery check for query '%s' (%s) ", xQueryName, XQueries.get(xQueryName)), ex);
        }
      });
    });
    return result;
  }

  private static List<Path> getMuleXMLFiles(String inputPath) {
    Path inputDir = Paths.get(inputPath);
    if (!Files.exists(inputDir)) {
      throw new IllegalArgumentException("Specified path does not exist: " + inputPath);
    }
    if (!Files.isDirectory(inputDir)) {
      throw new IllegalArgumentException("Not a directory: " + inputPath);
    }
    List<Path> results = null;
    try {
      results = Files.find(Paths.get(inputPath),
          Integer.MAX_VALUE,
          (filePath, fileAttr) -> {
            boolean isXml = fileAttr.isRegularFile() && filePath.getFileName().toString().toLowerCase().matches(".*\\.xml");
            Path dir = filePath.getParent();
            // check in both Mule3 and Mule4 dir structure
            boolean isInMuleDir = dir.endsWith("src/main/mule") || dir.endsWith("src/main/app");
            boolean isTarget = dir.toString().contains(FILE_SEPARATOR + "target" + FILE_SEPARATOR);
            return isXml && isInMuleDir && !isTarget;
          }, FileVisitOption.FOLLOW_LINKS)
          .sorted()
          .collect(Collectors.toList());
    } catch (IOException ex) {
      throw new RuntimeException("Error finding Mule files. Please check your arguments.", ex);
    }
    log.debug("found Mule XML files to be examined: " + results);
    return results;
  }

  private static String readFileContents(Path muleFile) {
    try {
      List<String> lines = Files.readAllLines(muleFile);
      return String.join("", lines);
    } catch (IOException ioe) {
      throw new RuntimeException("Cannot read file: " + muleFile, ioe);
    }
  }

  public static void main(String[] args) {

    try {
//      MuleCodeChecker codeChecker = new MuleCodeChecker();
      MuleCodeChecker.runCodeChecker(args);
    } catch (Exception e) {
      System.out.println(e);
      log.debug(e.getMessage(), e);
    }
  }
}
