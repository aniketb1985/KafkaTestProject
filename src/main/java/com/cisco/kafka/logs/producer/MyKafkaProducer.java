package com.cisco.kafka.logs.producer;

import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import kafka.javaapi.producer.Producer;
import kafka.producer.ProducerConfig;

public class MyKafkaProducer {
  private ScheduledExecutorService executor;

  private MyKafkaProducer(ScheduledExecutorService executor) {
    this.executor = executor;
  }

  public void shutdown() {
    if (executor != null)
      executor.shutdown();
  }

  public static void main(String[] args) {
    String topic = args[0];

    Properties props = new Properties();
    props.put("metadata.broker.list", "localhost:9092,broker1:9092");
    props.put("producer.type", "async");
    props.put("serializer.class", "kafka.serializer.StringEncoder");
    props.put("partitioner.class", "com.cisco.kafka.logs.producer.MyKafkaPartitioner");
    props.put("request.required.acks", "1");

    ProducerConfig config = new ProducerConfig(props);

    Producer<String, String> producer = new Producer<>(config);

    // now create a pool of threads
    //
    MyKafkaProducer kafkaProducer = new MyKafkaProducer(Executors.newScheduledThreadPool(1));

    // now schedule a thread to produce the messages
    //
    kafkaProducer.executor.scheduleAtFixedRate(new MyKafkaProducerThread(topic, producer), 0, 15,
        TimeUnit.MINUTES);
    try {
      Thread.sleep(10000);
    } catch (InterruptedException ie) {
      // do nothing
    }
    kafkaProducer.shutdown();
  }
}
