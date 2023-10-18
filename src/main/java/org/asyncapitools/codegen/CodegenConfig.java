package org.asyncapitools.codegen;

import lombok.Builder;
import lombok.Data;

@Builder(toBuilder = true)
@Data
public class CodegenConfig {
  private final String pathToSpec;
  private final String pathToOutputDirectory;
  private final String outputPackage;
  @Builder.Default private final String language = "Java";
  @Builder.Default private final String library = "quarkus";
}
