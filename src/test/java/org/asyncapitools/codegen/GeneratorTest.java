package org.asyncapitools.codegen;

import org.junit.jupiter.api.Test;

import java.io.IOException;

public class GeneratorTest {
  @Test
  void wip() throws IOException {
    Generator generator = new Generator();

    generator.generate(
        new CodegenConfig(
            "src/test/resources/asyncapi.yaml", "target/generated-sources/asyncapi/", "org.acme", "Java", "quarkus"));
  }
}
