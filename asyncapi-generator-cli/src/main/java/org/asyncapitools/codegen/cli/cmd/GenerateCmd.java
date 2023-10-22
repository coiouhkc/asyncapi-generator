package org.asyncapitools.codegen.cli.cmd;

import org.asyncapitools.codegen.CodegenConfig;
import org.asyncapitools.codegen.Generator;
import picocli.CommandLine;

import java.io.IOException;

@CommandLine.Command(name = "generate", mixinStandardHelpOptions = true)
public class GenerateCmd implements Runnable {
  @CommandLine.Option(
      names = {"-i", "--input-spec"},
      required = true)
  private String inputSpec;

  @CommandLine.Option(
      names = {"-o", "--output"},
      required = true)
  private String output;

  @CommandLine.Option(
      names = {"-p", "--package-name"},
      required = true)
  private String packageName;

  @CommandLine.Option(
      names = {"-l", "--language"},
      required = false)
  private String language;

  @CommandLine.Option(
      names = {"-y", "--library"},
      required = false)
  private String library;

  @CommandLine.Option(
      names = {"-t", "--template-directory"},
      required = false)
  private String templateDirectory;

  @Override
  public void run() {
      try {
          final CodegenConfig config =
                  new CodegenConfig(inputSpec, output, packageName, language, library, templateDirectory);

          final Generator generator = new Generator();
          generator.generate(config);

      } catch (IOException e) {
      throw new RuntimeException("Generation failed", e);
      }
  }
}
