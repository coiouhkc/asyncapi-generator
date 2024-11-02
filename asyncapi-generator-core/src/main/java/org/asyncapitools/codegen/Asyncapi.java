package org.asyncapitools.codegen;

import java.util.ArrayList;
import java.util.Set;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Builder(toBuilder = true)
@Data
public class Asyncapi {
  private String version;
  private Info info;
  private Set<Server> servers;
  private Set<Channel> channels;
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
  public static class Channel {
    private String name;
    private ChannelItem subscribe;
    private ChannelItem publish;
    private KafkaChannelBinding kafkaChannelBinding;

    public String getServiceName() {
      return StringUtils.capitalize(name) + "Service";
    }

    public String getDelegateInterfaceName() {
      return StringUtils.capitalize(name) + "DelegateI";
    }
  }

  @Builder(toBuilder = true)
  @Data
  public static class KafkaChannelBinding {
    private String topic;
    private int partitions;
    private int replicas;
    private TopicConfiguration topicConfiguration;
  }

  @Builder(toBuilder = true)
  @Data
  public static class TopicConfiguration {
    private Set<String> cleanupPolicy;
    private long retentionMs;
    private long retentionBytes;
    private long deleteRetentionMs;
    private long maxMessageBytes;
  }

  @Builder(toBuilder = true)
  @Data
  public static class ChannelItem {
    private String operationId;
    private KafkaChannelItemBinding kafkaChannelItemBinding;
    private Message message;
  }

  @Builder(toBuilder = true)
  @Data
  public static class KafkaChannelItemBinding {
    private String groupId;
  }

  @Builder(toBuilder = true)
  @Data
  public static class Message {
    private Component payload;
    private KafkaMessageBinding kafkaMessageBinding;
  }

  @Builder(toBuilder = true)
  @Data
  public static class KafkaMessageBinding {
    private Component key;
  }

  @Builder(toBuilder = true)
  @Data
  public static class Component {
    private String fqdn;
    private String name;
    private Set<Property> properties;

    public String getJavaDataType() {
      // either a real object or an alias for a type
      return name != null ? name : new ArrayList<>(properties).get(0).getJavaDataType();
    }
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
              case "date":
                return "Date";
              case "date-time":
                return "OffsetDateTime";
              case "password":
                return "String";
              case "byte":
                return "String";
              case "binary":
                return "File";
              case "uuid":
                return "UUID";
            }
          }
        case "boolean":
          return "Boolean";
        case "number":
          return "BigDecimal";
        case "integer":
          if (format == null) {
            return "Integer";
          } else {
            switch (format) {
              case "int64":
                return "Long";
              case "int32":
                return "Integer";
            }
          }
        case "array":
          return "List";
        case "object":
          return "Map";
      }

      throw new RuntimeException("Datatype " + type + "/" + format + " is not supported");
    }
  }
}
