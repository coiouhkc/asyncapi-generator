package org.asyncapitools.codegen.plugin;

import java.io.IOException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.asyncapitools.codegen.CodegenConfig;
import org.asyncapitools.codegen.Generator;

@Mojo(name = "generate", defaultPhase = LifecyclePhase.GENERATE_SOURCES, threadSafe = true)
public class CodegenMojo extends AbstractMojo {

  @Parameter(
      name = "inputSpec",
      property = "asyncapi.generator.maven.plugin.inputSpec",
      required = true)
  private String inputSpec;

  @Parameter(name = "output", property = "asyncapi.generator.maven.plugin.output", required = true)
  private String output;

  @Parameter(
      name = "packageName",
      property = "asyncapi.generator.maven.plugin.packageName",
      required = true)
  private String packageName;

  @Parameter(name = "language", property = "asyncapi.generator.maven.plugin.language")
  private String language;

  @Parameter(name = "library", property = "asyncapi.generator.maven.plugin.library")
  private String library;

  @Parameter(
      name = "templateDirectory",
      property = "asyncapi.generator.maven.plugin.templateDirectory")
  private String templateDirectory;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    try {
      final CodegenConfig config =
          new CodegenConfig(inputSpec, output, packageName, language, library, templateDirectory);

      final Generator generator = new Generator();
      generator.generate(config);

    } catch (IOException e) {
      throw new MojoExecutionException("Generation failed", e);
    }
  }
}
