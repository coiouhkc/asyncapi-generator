package org.asyncapitools.codegen;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.apache.commons.io.FileUtils;

public class Writer {
  public void write(Map<String, String> files) {
    files.forEach(
        (pathToFile, fileContent) -> {
          try {
            FileUtils.writeStringToFile(new File(pathToFile), fileContent, StandardCharsets.UTF_8);
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        });
  }
}
