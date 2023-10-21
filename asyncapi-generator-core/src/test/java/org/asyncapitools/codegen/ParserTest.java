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
    assertThat(asyncapi.getChannels()).hasSize(1);
    assertThat(asyncapi.getChannels())
        .contains(
            new Asyncapi.Channel(
                "inout",
                new Asyncapi.ChannelItem(
                    "read",
                    new Asyncapi.KafkaChannelBinding("in-group"),
                    new Asyncapi.Message(
                        new Asyncapi.Component("#/components/schemas/TestPayload", "TestPayload", null),
                        new Asyncapi.KafkaMessageBinding(
                            new Asyncapi.Component("#/components/schemas/TestKey", "TestKey", null)))),
                new Asyncapi.ChannelItem(
                    "write",
                    new Asyncapi.KafkaChannelBinding("out-group"),
                    new Asyncapi.Message(
                        new Asyncapi.Component("#/components/schemas/TestPayload", "TestPayload", null),
                        new Asyncapi.KafkaMessageBinding(
                            new Asyncapi.Component("#/components/schemas/TestKey", "TestKey", null))))));
    assertThat(asyncapi.getComponents()).hasSize(3);
    assertThat(asyncapi.getComponents())
        .contains(new Asyncapi.Component("#/components/schemas/TestEmpty", "TestEmpty", Set.of()));
  }
}
