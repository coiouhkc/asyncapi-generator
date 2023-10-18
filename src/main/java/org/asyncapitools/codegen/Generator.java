package org.asyncapitools.codegen;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.TemplateLoader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;

public class Generator {

  Handlebars handlebars = new Handlebars();

  public void generate(CodegenConfig config) throws IOException {
    final Asyncapi asyncapi = parse(config.getPathToSpec());

    TemplateLoader loader = new ClassPathTemplateLoader();
    loader.setPrefix("/" + config.getLanguage() + "/libraries/" + config.getLibrary());
    loader.setSuffix(".mustache");
    Handlebars handlebars = new Handlebars(loader);

    // generate model
    asyncapi.getComponents().forEach(component -> generateModel(handlebars, config, component));
    // generate api
    // generate supporting files
  }

  public Asyncapi parse(String pathToSpec) throws IOException {
    ObjectMapper om = new ObjectMapper(new YAMLFactory());
    Map<String, Object> obj = om.readValue(new File(pathToSpec), Map.class);

    Set<Asyncapi.Component> components =
        parseComponents("#", (Map<String, Object>) obj.get("components"));

    return new Asyncapi(null, null, null, null, components);
  }

  private Set<Asyncapi.Component> parseComponents(String prefix, Map<String, Object> node) {
    if (node.get("type") != null && node.get("type").equals("object")) {
      Set<Asyncapi.Property> properties =
          Optional.ofNullable(node.get("properties"))
              .map(o -> (Map<String, Object>) o)
              .map(Map::entrySet)
              .orElse(Set.of())
              .stream()
              .map(
                  entry ->
                      new Asyncapi.Property(
                          entry.getKey(),
                          ((Map<String, String>) entry.getValue()).get("type"),
                          ((Map<String, String>) entry.getValue()).get("format")))
              .collect(Collectors.toSet());

      return Set.of(
          new Asyncapi.Component(
              prefix, prefix.substring(prefix.lastIndexOf('/') + 1), properties));
    }

    return node.entrySet().stream()
        .flatMap(
            stringMapEntry ->
                parseComponents(
                    prefix + "/" + stringMapEntry.getKey(),
                    (Map<String, Object>) stringMapEntry.getValue())
                    .stream())
        .collect(Collectors.toSet());
  }

  @SneakyThrows
  private void generateModel(
      Handlebars handlebars, CodegenConfig config, Asyncapi.Component component) {
    Template modelTemplate = handlebars.compile("model");
    Context context = Context.newContext(component).combine("package", config.getOutputPackage());
    FileUtils.writeStringToFile(
        new File(
            config.getPathToOutputDirectory()
                + "src/gen/java/"
                + config.getOutputPackage().replaceAll("\\.", "/")
                + "/model/"
                + component.getName()
                + ".java"),
        modelTemplate.apply(context),
        StandardCharsets.UTF_8);
  }
}
