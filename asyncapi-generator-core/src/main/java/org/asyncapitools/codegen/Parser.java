package org.asyncapitools.codegen;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Parser {
  public Asyncapi parse(String pathToSpec) throws IOException {
    ObjectMapper om = new ObjectMapper(new YAMLFactory());
    Map<String, Object> obj = om.readValue(new File(pathToSpec), Map.class);

    Set<Asyncapi.Channel> channels = parseChannels((Map<String, Object>) obj.get("channels"));

    Set<Asyncapi.Component> components =
        parseComponents("#/components", (Map<String, Object>) obj.get("components"));

    channels.forEach(
        channel ->
            Stream.of(channel.getSubscribe(), channel.getPublish())
                .filter(Objects::nonNull)
                .forEach(
                    channelItem -> {
                      if (channelItem.getMessage() != null
                          && channelItem.getMessage().getKafkaMessageBinding() != null
                          && channelItem.getMessage().getKafkaMessageBinding().getKey() != null) {
                        Asyncapi.Component key =
                            channelItem.getMessage().getKafkaMessageBinding().getKey();
                        channelItem
                            .getMessage()
                            .getKafkaMessageBinding()
                            .setKey(
                                components.stream()
                                    .filter(component -> component.getFqdn().equals(key.getFqdn()))
                                    .findFirst()
                                    .orElse(key));
                      }

                      if (channelItem.getMessage() != null
                          && channelItem.getMessage().getPayload() != null) {
                        Asyncapi.Component payload = channelItem.getMessage().getPayload();
                        channelItem
                            .getMessage()
                            .setPayload(
                                components.stream()
                                    .filter(
                                        component -> component.getFqdn().equals(payload.getFqdn()))
                                    .findFirst()
                                    .orElse(payload));
                      }
                    }));

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
    Map<String, Object> bindings = (Map<String, Object>) node.get("bindings");
    return new Asyncapi.Channel(
        key,
        parseChannelItem((Map<String, Object>) node.get("subscribe")),
        parseChannelItem((Map<String, Object>) node.get("publish")),
        parseKafkaChannelBinding(
            bindings != null ? (Map<String, Object>) bindings.get("kafka") : null));
  }

  private Asyncapi.ChannelItem parseChannelItem(Map<String, Object> node) {
    if (node == null) {
      return null;
    }

    Map<String, Object> bindings = (Map<String, Object>) node.get("bindings");
    return new Asyncapi.ChannelItem(
        (String) node.get("operationId"),
        parseKafkaChannelItemBinding(
            bindings != null ? (Map<String, Object>) bindings.get("kafka") : null),
        parseMessage((Map<String, Object>) node.get("message")));
  }

  private Asyncapi.KafkaChannelBinding parseKafkaChannelBinding(Map<String, Object> node) {
    if (node == null) {
      return null;
    }
    return new Asyncapi.KafkaChannelBinding(
        (String) node.get("topic"),
        (Integer) node.get("partitions"),
        (Integer) node.get("replicas"),
        parseTopicConfiguration((Map<String, Object>) node.get("topicConfiguration")));
  }

  private Asyncapi.TopicConfiguration parseTopicConfiguration(Map<String, Object> node) {
    if (node == null) {
      return null;
    }

    return new Asyncapi.TopicConfiguration(
        new HashSet<>((List<String>) node.get("cleanup.policy")),
        (Integer) node.get("retention.ms"),
        (Integer) node.get("retention.bytes"),
        (Integer) node.get("delete.retention.ms"),
        (Integer) node.get("max.message.bytes"));
  }

  private Asyncapi.KafkaChannelItemBinding parseKafkaChannelItemBinding(Map<String, Object> node) {
    if (node == null) {
      return null;
    }
    return new Asyncapi.KafkaChannelItemBinding((String) node.get("groupId"));
  }

  private Asyncapi.Message parseMessage(Map<String, Object> node) {
    String fqdn = (String) ((Map<String, Object>) node.get("payload")).get("$ref");
    return new Asyncapi.Message(
        new Asyncapi.Component(fqdn, fqdn.substring(fqdn.lastIndexOf('/') + 1), null),
        parseKafkaMessageBinding(
            (Map<String, Object>) ((Map<String, Object>) node.get("bindings")).get("kafka")));
  }

  private Asyncapi.KafkaMessageBinding parseKafkaMessageBinding(Map<String, Object> node) {
    if (node == null) {
      return null;
    }
    String fqdn = (String) ((Map<String, Object>) node.get("key")).get("$ref");
    return new Asyncapi.KafkaMessageBinding(
        new Asyncapi.Component(fqdn, fqdn.substring(fqdn.lastIndexOf('/') + 1), null));
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
              .map(entry -> parseProperty(entry.getKey(), (Map<String, Object>) entry.getValue()))
              .collect(Collectors.toSet());

      return Set.of(
          new Asyncapi.Component(
              prefix, prefix.substring(prefix.lastIndexOf('/') + 1), properties));
    } else if (node.get("type") != null && !node.get("type").equals("object")) {
      return Set.of(new Asyncapi.Component(prefix, null, Set.of(parseProperty(null, node))));
    } else {
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

  private static Asyncapi.Property parseProperty(String name, Map<String, Object> values) {
    return new Asyncapi.Property(name, (String) values.get("type"), (String) values.get("format"));
  }
}
