package org.asyncapitools.codegen;

import java.util.Set;
import lombok.Builder;
import lombok.Data;

@Builder(toBuilder = true)
@Data
public class Asyncapi {
  private String version;
  private Info info;
  private Set<Server> servers;
  private Channels channels;
  private Set<Component> components;

  @Builder(toBuilder = true)
  @Data
  public static class Info {
    private String title;
    private String version;
    private String description;
  }

  @Builder(toBuilder = true)
  @Data
  public static class Server {
    private String name;
    private String url;
    private String protocol;
  }

  @Builder(toBuilder = true)
  @Data
  public static class Channels {
    private String name;
    private Channel subscribe;
    private Channel publish;
  }

  @Builder(toBuilder = true)
  @Data
  public static class Channel {
    private String name;
    private String operationId;
    private KafkaChannelBinding kafkaChannelBinding;
    private Message message;
  }

  @Builder(toBuilder = true)
  @Data
  public static class KafkaChannelBinding {
    private String groupId;
    private String key;
  }

  @Builder(toBuilder = true)
  @Data
  public static class Message {
    private Component payload;
  }

  @Builder(toBuilder = true)
  @Data
  public static class Component {
    private String fqdn;
    private String name;
    private Set<Property> properties;
  }

  @Builder(toBuilder = true)
  @Data
  public static class Property {
    private String name;
    private String type;
    private String format;

    public String getJavaDataType() {
      switch (type) {
        case "string":
          if (format == null) {
            return "String";
          } else {
            switch (format) {
              case "date": return "Date";
              case "date-time": return "OffsetDateTime";
              case "password": return "String";
              case "byte": return "String";
              case "binary": return "File";
              case "uuid": return "UUID";
            }
          }
        case "boolean":
          return "Boolean";
        case "number":
          return "Integer";
        case "integer":
          return "Integer";
        case "array":
          return "List";
        case "object":
          return "Map";
      }

      throw new RuntimeException("Datatype " + type + "/" + format + " is not supported");
    }
  }
}
