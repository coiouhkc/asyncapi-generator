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

public class GeneratorExternalTemplateDirTest {
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
            null,
            null,
            "src/main/resources/Java/libraries/quarkus");

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
  }
}
