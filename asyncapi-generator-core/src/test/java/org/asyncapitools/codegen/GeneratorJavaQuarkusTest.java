package org.asyncapitools.codegen;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.TemplateLoader;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class GeneratorJavaQuarkusTest {

  Generator generator = new Generator();

  Handlebars handlebars;

  CodegenConfig config;

  @BeforeEach
  void setUp() {
    config =
        new CodegenConfig(
            "src/test/resources/asyncapi.yaml",
            "target/generated-sources/asyncapi/",
            "org.acme",
            "Java",
            "quarkus",
            null);

    TemplateLoader loader = new ClassPathTemplateLoader();
    loader.setPrefix("/" + "Java" + "/libraries/" + "quarkus");
    loader.setSuffix(".mustache");
    handlebars = new Handlebars(loader);
  }

  @Test
  void wip() throws IOException {
    Generator generator = new Generator();
    generator.generate(config);
  }

  @Test
  void generateModel() {
    List<Pair<String, String>> models =
        generator
            .generateModel(
                handlebars,
                config,
                new Asyncapi.Component(
                    "#/components/schemas/Test",
                    "Test",
                    Set.of(new Asyncapi.Property("ts", "string", "date-time"))))
            .collect(Collectors.toList());

    assertThat(models).isNotNull();
    assertThat(models).hasSize(3);
    assertThat(models.get(0).getKey())
        .isEqualTo("target/generated-sources/asyncapi/src/gen/java/org/acme/model/Test.java");
    assertThat(models.get(0).getValue())
        .isEqualToIgnoringWhitespace(
            """
                package org.acme.model;

                import java.util.List;
                import java.util.Map;
                import java.util.Set;

                import java.util.Date;
                import java.util.UUID;

                import java.util.ArrayList;
                import java.util.LinkedHashSet;
                import java.util.HashMap;

                import java.time.LocalDate;
                import java.time.LocalTime;

                import java.time.LocalDateTime;
                import java.time.OffsetDateTime;

                import lombok.Builder;
                import lombok.Data;
                import lombok.extern.jackson.Jacksonized;

                @Builder
                @Data
                @Jacksonized
                public class Test {

                    private final OffsetDateTime ts;

                }""");
  }

  @Test
  void generateApi() {
    List<Pair<String, String>> apis =
        generator
            .generateApi(
                handlebars,
                config,
                new Asyncapi.Channel(
                    "inout",
                    new Asyncapi.ChannelItem(
                        "read",
                        new Asyncapi.KafkaChannelBinding("in-group"),
                        new Asyncapi.Message(
                            new Asyncapi.Component(
                                "#/components/schemas/TestPayload", "TestPayload", null),
                            new Asyncapi.KafkaMessageBinding(
                                new Asyncapi.Component(
                                    "#/components/schemas/TestKey", "TestKey", null)))),
                    new Asyncapi.ChannelItem(
                        "write",
                        new Asyncapi.KafkaChannelBinding("out-group"),
                        new Asyncapi.Message(
                            new Asyncapi.Component(
                                "#/components/schemas/TestPayload", "TestPayload", null),
                            new Asyncapi.KafkaMessageBinding(
                                new Asyncapi.Component(
                                    "#/components/schemas/TestKey", "TestKey", null))))))
            .collect(Collectors.toList());

    assertThat(apis).isNotNull();
    assertThat(apis).hasSize(2);
    assertThat(apis.get(0).getKey())
        .isEqualTo(
            "target/generated-sources/asyncapi/src/gen/java/org/acme/service/InoutService.java");
    assertThat(apis.get(0).getValue())
        .isEqualToIgnoringWhitespace(
            """
package org.acme.service;

import org.acme.model.*;

import org.eclipse.microprofile.reactive.messaging.Incoming;

import io.smallrye.reactive.messaging.kafka.Record;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class InoutService {
  @Inject InoutDelegateI delegate;

  @Incoming("inout")
  public void read(Record<TestKey, TestPayload> record) {
    delegate.read(record);
  }
}""");

    assertThat(apis.get(1).getKey())
        .isEqualTo(
            "target/generated-sources/asyncapi/src/gen/java/org/acme/service/InoutDelegateI.java");
    assertThat(apis.get(1).getValue())
        .isEqualToIgnoringWhitespace(
            """
package org.acme.service;

import org.acme.model.*;

import org.eclipse.microprofile.reactive.messaging.Incoming;

import io.smallrye.reactive.messaging.kafka.Record;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

public interface InoutDelegateI {
    default void read(Record<TestKey, TestPayload> record) {
        throw new RuntimeException("Not implemented!");
    }
}""");
  }
}
