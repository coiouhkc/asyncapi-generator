package org.asyncapitools.codegen;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.FileTemplateLoader;
import com.github.jknack.handlebars.io.URLTemplateLoader;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import org.apache.commons.lang3.tuple.Pair;

public class Generator {

  private Parser parser = new Parser();

  private Writer writer = new Writer();

  private Handlebars handlebars = new Handlebars();

  public void generate(CodegenConfig config) throws IOException {
    // parse asyncapi spec
    final Asyncapi asyncapi = parser.parse(config.getPathToSpec());

    // prepare template engine
    URLTemplateLoader loader =
        config.isTemplateInClasspath()
            ? new ClassPathTemplateLoader(config.getTemplateDir(), ".mustache")
            : new FileTemplateLoader(config.getTemplateDir(), ".mustache");
    Handlebars handlebars = new Handlebars(loader);

    // generate config
    Set<Pair<String, String>> configs =
        Stream.of("")
            .flatMap(s -> generateConfig(handlebars, config, asyncapi))
            .collect(Collectors.toSet());

    // generate models
    Set<Pair<String, String>> models =
        asyncapi.getComponents().stream()
            .filter(component -> component.getName() != null)
            .flatMap(component -> generateModel(handlebars, config, component))
            .collect(Collectors.toSet());

    // generate apis
    Set<Pair<String, String>> apis =
        asyncapi.getChannels().stream()
            .filter(channel -> channel.getSubscribe() != null)
            .flatMap(channel -> generateApi(handlebars, config, channel))
            .collect(Collectors.toSet());

    // generate supporting files
    Set<Pair<String, String>> supportingFiles =
        Stream.of("application.properties", "create-topics.sh")
            .flatMap(s -> generateSupportingFile(handlebars, config, asyncapi, s))
            .collect(Collectors.toSet());

    // write
    Map<String, String> filesWithContent = new HashMap<>();
    Stream.of(configs, models, apis, supportingFiles)
        .flatMap(Collection::stream)
        .forEach(
            stringStringPair ->
                filesWithContent.put(stringStringPair.getKey(), stringStringPair.getValue()));
    writer.write(filesWithContent);
  }

  @SneakyThrows
  public Stream<Pair<String, String>> generateConfig(
      Handlebars handlebars, CodegenConfig config, Asyncapi asyncapi) {
    Context context = Context.newContext(asyncapi).combine("package", config.getOutputPackage());

    try {
      Template configTemplate = handlebars.compile("configuration");

      String configurationContent = configTemplate.apply(context);

      String configDir =
          config.getPathToOutputDirectory()
              + "src/gen/java/"
              + config.getOutputPackage().replaceAll("\\.", "/")
              + "/config/";

      return Stream.of(
          Pair.of(configDir + "AsyncApiKafkaConfiguration" + ".java", configurationContent));
    } catch (IOException e) {
      return Stream.empty();
    }
  }

  @SneakyThrows
  public Stream<Pair<String, String>> generateModel(
      Handlebars handlebars, CodegenConfig config, Asyncapi.Component component) {
    Context context = Context.newContext(component).combine("package", config.getOutputPackage());

    Template modelTemplate = handlebars.compile("model");
    String content = modelTemplate.apply(context);

    String serializerContent = null;
    try {
      Template serializerTemplate = handlebars.compile("serializer");
      serializerContent = serializerTemplate.apply(context);
    } catch (FileNotFoundException fnfe) {
      // do nothing, filter later
    }

    String deserializerContent = null;
    try {
      Template deserializerTemplate = handlebars.compile("deserializer");
      deserializerContent = deserializerTemplate.apply(context);
    } catch (FileNotFoundException fnfe) {
      // do nothing, filter later
    }

    String modelDir =
        config.getPathToOutputDirectory()
            + "src/gen/java/"
            + config.getOutputPackage().replaceAll("\\.", "/")
            + "/model/";

    return Stream.of(
            Pair.of(modelDir + component.getName() + ".java", content),
            Pair.of(modelDir + component.getName() + "Serializer.java", serializerContent),
            Pair.of(modelDir + component.getName() + "Deserializer.java", deserializerContent))
        .filter(pair -> pair.getRight() != null);
  }

  @SneakyThrows
  public Stream<Pair<String, String>> generateApi(
      Handlebars handlebars, CodegenConfig config, Asyncapi.Channel channel) {
    Context context = Context.newContext(channel).combine("package", config.getOutputPackage());

    Template serviceTemplate = handlebars.compile("service");
    String serviceContent = serviceTemplate.apply(context);

    Template delegateInterfaceTemplate = handlebars.compile("delegate-interface");
    String delegateInterfaceContent = delegateInterfaceTemplate.apply(context);

    String serviceDir =
        config.getPathToOutputDirectory()
            + "src/gen/java/"
            + config.getOutputPackage().replaceAll("\\.", "/")
            + "/service/";

    return Stream.of(
        Pair.of(serviceDir + channel.getServiceName() + ".java", serviceContent),
        Pair.of(
            serviceDir + channel.getDelegateInterfaceName() + ".java", delegateInterfaceContent));
  }

  @SneakyThrows
  public Stream<Pair<String, String>> generateSupportingFile(
      Handlebars handlebars, CodegenConfig config, Asyncapi asyncapi, String fileName) {

    Context context = Context.newContext(asyncapi).combine("package", config.getOutputPackage());

    String content = null;

    try {
      Template template = handlebars.compile(fileName);
      content = template.apply(context);
    } catch (FileNotFoundException fnfe) {
      // do nothing, filter later
    }

    String dir =
        config.getPathToOutputDirectory()
            + "src/gen/java/"
            + config.getOutputPackage().replaceAll("\\.", "/")
            + "/";

    return Stream.of(Pair.of(dir + fileName, content));
  }
}
