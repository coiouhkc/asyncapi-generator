package org.asyncapitools.codegen;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.TemplateLoader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;

public class Generator {

  private Parser parser = new Parser();

  private Writer writer = new Writer();

  private Handlebars handlebars = new Handlebars();

  public void generate(CodegenConfig config) throws IOException {
    // parse asyncapi spec
    final Asyncapi asyncapi = parser.parse(config.getPathToSpec());

    // prepare template engine
    TemplateLoader loader = new ClassPathTemplateLoader();
    loader.setPrefix("/" + config.getLanguage() + "/libraries/" + config.getLibrary());
    loader.setSuffix(".mustache");
    Handlebars handlebars = new Handlebars(loader);

    // generate models
    Set<Pair<String, String>> models =
        asyncapi.getComponents().stream()
            .map(component -> generateModel(handlebars, config, component))
            .collect(Collectors.toSet());
    // generate apis
    // generate supporting files

    // write
    Map<String, String> filesWithContent = new HashMap<>();
    models.forEach(stringStringPair -> filesWithContent.put(stringStringPair.getKey(), stringStringPair.getValue()));
    writer.write(filesWithContent);
  }

  @SneakyThrows
  public Pair<String, String> generateModel(
      Handlebars handlebars, CodegenConfig config, Asyncapi.Component component) {
    Template modelTemplate = handlebars.compile("model");
    Context context = Context.newContext(component).combine("package", config.getOutputPackage());
    String content = modelTemplate.apply(context);

    return Pair.of(
        config.getPathToOutputDirectory()
            + "src/gen/java/"
            + config.getOutputPackage().replaceAll("\\.", "/")
            + "/model/"
            + component.getName()
            + ".java",
        content);
  }
}
