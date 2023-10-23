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

    Set<Asyncapi.Channel> channels = parseChannels((Map<String, Object>) obj.get("channels"));

    Set<Asyncapi.Component> components =
        parseComponents("#/components", (Map<String, Object>) obj.get("components"));

    return new Asyncapi(null, null, null, channels, components);
  }

  private Set<Asyncapi.Channel> parseChannels(Map<String, Object> node) {
    if (node == null) {
      return null;
    }

    return node.entrySet().stream()
        .map(
            stringObjectEntry ->
                parseChannel(
                    stringObjectEntry.getKey(), (Map<String, Object>) stringObjectEntry.getValue()))
        .collect(Collectors.toSet());
  }

  private Asyncapi.Channel parseChannel(String key, Map<String, Object> node) {
    return new Asyncapi.Channel(
        key,
        parseChannelItem((Map<String, Object>) node.get("subscribe")),
        parseChannelItem((Map<String, Object>) node.get("publish")));
  }

  private Asyncapi.ChannelItem parseChannelItem(Map<String, Object> node) {
    if (node == null) {
      return null;
    }

    Map<String, Object> bindings = (Map<String, Object>) node.get("bindings");
    return new Asyncapi.ChannelItem(
        (String) node.get("operationId"),
        parseKafkaChannelBinding(
            bindings != null ? (Map<String, Object>) bindings.get("kafka") : null),
        parseMessage((Map<String, Object>) node.get("message")));
  }

  private Asyncapi.KafkaChannelBinding parseKafkaChannelBinding(Map<String, Object> node) {
    if (node == null) {
      return null;
    }
    return new Asyncapi.KafkaChannelBinding((String) node.get("groupId"));
  }

  private Asyncapi.Message parseMessage(Map<String, Object> node) {
    String fqdn = (String) ((Map<String, Object>) node.get("payload")).get("$ref");
    return new Asyncapi.Message(
        new Asyncapi.Component(
            fqdn, fqdn.substring(fqdn.lastIndexOf('/') + 1), null),
        parseKafkaMessageBinding(
            (Map<String, Object>) ((Map<String, Object>) node.get("bindings")).get("kafka")));
  }

  private Asyncapi.KafkaMessageBinding parseKafkaMessageBinding(Map<String, Object> node) {
    if (node == null) {
      return null;
    }
    String fqdn = (String) ((Map<String, Object>) node.get("key")).get("$ref");
    return new Asyncapi.KafkaMessageBinding(
        new Asyncapi.Component(
            fqdn, fqdn.substring(fqdn.lastIndexOf('/') + 1), null));
  }

  private Set<Asyncapi.Component> parseComponents(String prefix, Map<String, Object> node) {
    if (node == null) {
      return null;
    }

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
