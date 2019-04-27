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
    private String inputPath;
    private String xQueriesFilePath;
    private OutputFormat outputFormat;

    public MuleCodeChecker() {
    }

    public MuleCodeChecker(String inputPath, String xQueriesFilePath) {
        this.inputPath = inputPath;
        this.xQueriesFilePath = xQueriesFilePath;
    }

    public static void main(String[] args) {
        try {
            MuleCodeChecker codeChecker = new MuleCodeChecker();
            codeChecker.runCodeChecker(args);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            log.debug(e.getMessage(), e);
        }
    }

    public CheckResult runCodeChecker() {

        List<Path> muleFiles = getMuleXMLFiles(inputPath);
        XQueries xQueries = getXQueries(xQueriesFilePath);

        // run the check
        return check(muleFiles, xQueries);
    }

    private void runCodeChecker(String[] args) {
        if (!validateOptions(args)) {
            // options validation failed
            return;
        }

        CheckResult result = this.runCodeChecker();

        switch (outputFormat) {
            case RAW:
                System.out.println(result.toCSV());
                break;

            case JAVA:
                System.out.println(result.stringify());
                break;
        }
    }

    private boolean validateOptions(String args[]) {
        Options options = buildOptions();
        // create the parser
        CommandLineParser parser = new DefaultParser();
        try {
            // parse the command line arguments
            CommandLine line = parser.parse(options, args);

            this.inputPath = line.getOptionValue("inputPath");
            this.xQueriesFilePath = line.getOptionValue("xQueries");
            try {
                this.outputFormat = OutputFormat.valueOf(line.getOptionValue("outputFormat"));
            } catch (java.lang.IllegalArgumentException e) {
                throw new ParseException("invalid outputFormat: " + line.getOptionValue("outputFormat"));
            }

            return true;
        } catch (ParseException exp) {
            // missing required arguments
            System.err.println(exp.getMessage());

            // print usage
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(" ", options);

            return false;
        }
    }

    private Options buildOptions() {
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
    private CheckResult check(Collection<Path> muleFiles, XQueries xQueries) {
        CheckResult result = new CheckResult();
        muleFiles.forEach((muleFile) -> {
            LinkedHashMap<String, List<String>> singleResult = new LinkedHashMap<>();
            String xmlContents = readFileContents(muleFile);
            try {
                log.debug("checking file: " + muleFile.toAbsolutePath());
                NamespaceResolverImpl nsrImpl = new NamespaceResolverImpl(xmlContents);
                xQueries.keySet().forEach((xQueryName) -> {
                    try {
                        String xquery = xQueries.get(xQueryName);
                        // pre-process the XQuery to replace the file name
                        String fileNameWithoutExt = StringUtils.removeEndIgnoreCase(muleFile.getFileName().toString(), ".xml");
                        xquery = xQueries.format(xquery, fileNameWithoutExt);
                        log.debug("pre-processed XQuery: " + xquery);

                        // run the XQuery against the XML file content
                        List<XdmItem> xdmItems = XPathUtils.evaluate(xquery, xmlContents, nsrImpl);
                        List<String> xqueryResult = new ArrayList<>();

                        xdmItems.forEach((item) -> {
                            xqueryResult.add(item.toString());
                        });
                        result.addResult(muleFile.getFileName().toString(), xQueryName, xqueryResult);
                    } catch (SaxonApiException ex) {
                        throw new RuntimeException("Error occurred when running XQuery check.", ex);
                    }
                });
            } catch (XPathExpressionException xpe) {
                throw new RuntimeException("Error occurred when running XQuery check.", xpe);
            }
        });
        return result;
    }

    private List<Path> getMuleXMLFiles(String inputPath) {
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
                        return isXml && isInMuleDir;
                    }, FileVisitOption.FOLLOW_LINKS)
                    .sorted()
                    .collect(Collectors.toList());
        } catch (IOException ex) {
            throw new RuntimeException("Error finding Mule files. Please check your arguments.", ex);
        }
        log.debug("found Mule XML files to be examined: " + results);
        return results;
    }

    private XQueries getXQueries(String xQueriesFilePath) {
        // TODO implement - use two hard coded XQueries for now
        XQueries result = XQueries.getInstance();
        result.loadFromFile(xQueriesFilePath);
        return result;
    }

    private String readFileContents(Path muleFile) {
        try {
            List<String> lines = Files.readAllLines(muleFile);
            return String.join("", lines);
        } catch (IOException ioe) {
            throw new RuntimeException("Cannot read file: " + muleFile, ioe);
        }
    }
}
