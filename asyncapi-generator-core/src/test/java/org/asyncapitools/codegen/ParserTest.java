package org.asyncapitools.codegen;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Set;
import org.junit.jupiter.api.Test;

public class ParserTest {

  private Parser parser = new Parser();

  @Test
  void wip() throws IOException {
    Asyncapi asyncapi = parser.parse("src/test/resources/asyncapi.yaml");
    assertThat(asyncapi).isNotNull();
    assertThat(asyncapi.getChannels()).hasSize(1);
    Asyncapi.Component testKey =
        new Asyncapi.Component(
            "#/components/schemas/TestKey",
            "TestKey",
            Set.of(new Asyncapi.Property("key", "string", null)));
    Asyncapi.Component testPayload =
        new Asyncapi.Component(
            "#/components/schemas/TestPayload",
            "TestPayload",
            Set.of(
                new Asyncapi.Property("prop1", "integer", null),
                new Asyncapi.Property("prop2", "string", "date-time"),
                new Asyncapi.Property("prop3", "number", "double"),
                new Asyncapi.Property("prop4", "string", null)));
    assertThat(asyncapi.getChannels())
        .contains(
            new Asyncapi.Channel(
                "inout",
                new Asyncapi.ChannelItem(
                    "read",
                    new Asyncapi.KafkaChannelItemBinding("in-group"),
                    new Asyncapi.Message(testPayload, new Asyncapi.KafkaMessageBinding(testKey))),
                new Asyncapi.ChannelItem(
                    "write",
                    new Asyncapi.KafkaChannelItemBinding("out-group"),
                    new Asyncapi.Message(testPayload, new Asyncapi.KafkaMessageBinding(testKey))),
                new Asyncapi.KafkaChannelBinding(
                    "my-specific-topic-name", 20, 3, new Asyncapi.TopicConfiguration(Set.of("compact", "delete"), 604800000, 1000000000, 86400000, 1048588))));
    assertThat(asyncapi.getComponents()).hasSize(3);
    assertThat(asyncapi.getComponents())
        .contains(new Asyncapi.Component("#/components/schemas/TestEmpty", "TestEmpty", Set.of()));
  }
}
