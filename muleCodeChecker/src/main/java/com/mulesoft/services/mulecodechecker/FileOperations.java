package com.mulesoft.services.mulecodechecker;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author p.anderson
 */
class FileOperations {

  private static final String FILE_SEPARATOR = System.getProperty("file.separator");
  private static final String MULE_ROOT_ELEMENT = "<?xml ";

  protected static List<Path> getMuleXMLFiles(String inputPath) {
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
                boolean isInMuleDir = dir.endsWith("src/main/mule") || dir.endsWith("src/main/resources") || dir.endsWith("src/main/app");
                boolean isTarget = dir.toString().contains(FILE_SEPARATOR + "target" + FILE_SEPARATOR);
                return isXml && isInMuleDir && !isTarget;
              }, FileVisitOption.FOLLOW_LINKS)
              .sorted()
              .collect(Collectors.toList());
    } catch (IOException ex) {
      throw new RuntimeException("Error finding Mule files. Please check your arguments.", ex);
    }
    MuleCodeChecker.log.debug("found Mule XML files to be examined: " + results);
    return results;
  }

  protected static String readFileContents(Path muleFile) {
    try {
      List<String> lines = Files.readAllLines(muleFile);
      if (lines.get(0).trim().startsWith(MULE_ROOT_ELEMENT)){
        return String.join("", lines);
      } else {
        return "";
      }
    } catch (IOException ioe) {
      throw new RuntimeException("Cannot read file: " + muleFile, ioe);
    }
  }

  protected static String getProjectName(String inputPath) {
    Path projectDir = Paths.get(inputPath);
    return projectDir.getName(projectDir.getNameCount() - 1).toString();
  }

}
