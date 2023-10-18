package org.asyncapitools.codegen;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class ParserTest {

  private Parser parser = new Parser();
  @Test
  void wip() throws IOException {
    Asyncapi asyncapi = parser.parse("src/test/resources/asyncapi.yaml");
    assertThat(asyncapi).isNotNull();
    assertThat(asyncapi.getComponents()).hasSize(3);
    assertThat(asyncapi.getComponents()).contains(new Asyncapi.Component("#/components/schemas/TestEmpty", "TestEmpty", Set.of()));
  }
}
