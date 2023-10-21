package org.asyncapitools.codegen;

import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Builder(toBuilder = true)
@Data
public class CodegenConfig {
  private final String pathToSpec;
  private final String pathToOutputDirectory;
  private final String outputPackage;
  @Builder.Default private final String language = "Java";
  @Builder.Default private final String library = "quarkus";

  /**
   * Path to template directory, overrides language + library
   */
  private final String pathToTemplateDirectory;

  public String getTemplateDir() {
    if (StringUtils.isNotBlank(pathToTemplateDirectory)) {
      return pathToTemplateDirectory;
    }

    return "/" + language + "/libraries/" + library;
  }

  public boolean isTemplateInClasspath() {
    return StringUtils.isBlank(pathToTemplateDirectory);
  }
}
