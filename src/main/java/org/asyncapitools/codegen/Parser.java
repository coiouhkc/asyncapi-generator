package org.asyncapitools.codegen;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class Parser {
  public Asyncapi parse(String pathToSpec) throws IOException {
    ObjectMapper om = new ObjectMapper(new YAMLFactory());
    Map<String, Object> obj = om.readValue(new File(pathToSpec), Map.class);

    Set<Asyncapi.Component> components =
        parseComponents("#/components", (Map<String, Object>) obj.get("components"));

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
}
