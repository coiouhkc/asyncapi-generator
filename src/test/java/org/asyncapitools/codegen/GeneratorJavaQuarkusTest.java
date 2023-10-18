package org.asyncapitools.codegen;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.TemplateLoader;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class GeneratorJavaQuarkusTest {

  Generator generator = new Generator();

  Handlebars handlebars;

  CodegenConfig config;

  @BeforeEach
  void setUp() {
    config = new CodegenConfig(
        "src/test/resources/asyncapi.yaml", "target/generated-sources/asyncapi/", "org.acme", "Java", "quarkus");

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
    Pair<String, String> pair = generator.generateModel(
        handlebars,
        config,
        new Asyncapi.Component(
            "#/components/schemas/Test", "Test", Set.of(new Asyncapi.Property("ts", "string", "date-time"))));

    assertThat(pair).isNotNull();
    assertThat(pair.getKey()).isEqualTo("target/generated-sources/asyncapi/src/gen/java/org/acme/model/Test.java");
    assertThat(pair.getValue()).isEqualToIgnoringWhitespace("""
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

@Builder(toBuilder = true)
@Data
public class Test {
  
    private OffsetDateTime ts;
  
}""");
  }
}
