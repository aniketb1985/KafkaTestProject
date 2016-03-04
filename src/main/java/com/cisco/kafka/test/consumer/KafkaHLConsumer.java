package com.cisco.kafka.test.consumer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import kafka.consumer.ConsumerConfig;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;

public class KafkaHLConsumer {
  private final ConsumerConnector consumer;
  private final String topic;
  private ExecutorService executor;

  public KafkaHLConsumer(String zookeeper, String groupId, String topic) {
    consumer = kafka.consumer.Consumer
        .createJavaConsumerConnector(createConsumerConfig(zookeeper, groupId));
    this.topic = topic;
  }

  public void shutdown() {
    if (consumer != null)
      consumer.shutdown();
    if (executor != null)
      executor.shutdown();
  }

  public void run(int numThreads) {
    Map<String, Integer> topicCountMap = new HashMap<>();
    topicCountMap.put(topic, new Integer(numThreads));
    Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = consumer
        .createMessageStreams(topicCountMap);
    List<KafkaStream<byte[], byte[]>> streams = consumerMap.get(topic);

    // now launch all the threads
    //
    executor = Executors.newFixedThreadPool(numThreads);

    // now create an object to consume the messages
    //
    int threadNumber = 0;
    for (final KafkaStream<byte[], byte[]> stream : streams) {
      executor.submit(new ConsumerThread(stream, threadNumber));
      threadNumber++;
    }
  }

  private static ConsumerConfig createConsumerConfig(String zookeeper, String groupId) {
    Properties props = new Properties();
    props.put("zookeeper.connect", zookeeper);
    props.put("group.id", groupId);
    props.put("zookeeper.session.timeout.ms", "400");
    props.put("zookeeper.sync.time.ms", "200");
    props.put("auto.commit.interval.ms", "1000");
    props.put("auto.offset.reset", "smallest");

    return new ConsumerConfig(props);
  }

  public static void main(String[] args) {
    String zooKeeper = args[0];
    String groupId = args[1];
    String topic = args[2];
    int threads = Integer.parseInt(args[3]);

    KafkaHLConsumer example = new KafkaHLConsumer(zooKeeper, groupId, topic);
    example.run(threads);

    try {
      Thread.sleep(10000);
    } catch (InterruptedException ie) {
      // do nothing
    }
    example.shutdown();
  }
}
