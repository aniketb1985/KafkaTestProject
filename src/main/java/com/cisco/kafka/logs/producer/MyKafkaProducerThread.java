package com.cisco.kafka.logs.producer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;

public class MyKafkaProducerThread implements Runnable {

  final String topic;
  Producer<String, String> producer;

  public MyKafkaProducerThread(String topic, Producer<String, String> producer) {
    this.topic = topic;
    this.producer = producer;
  }

  private static String fileNameCreator() {
    String minutesAndSeconds = ":00:00";
    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yy HH");
    Calendar previousHour = Calendar.getInstance();
    previousHour.setTime(Calendar.getInstance().getTime());
    previousHour.add(Calendar.HOUR, -1);
    return (sdf.format(previousHour.getTime())) + minutesAndSeconds;
  }

  @Override
  public void run() {
    String formattedPreviousHour = fileNameCreator();
    // List<String> logLevel = Arrays.asList(new String[] { "INFO", "ERROR", "WARN", "FATAL" });
    try {
      File file = new File("/home/vikhyatk/Documents/Logs/log_" + formattedPreviousHour + ".txt");
      if (!file.exists()) {
        System.out.println("File does not exist");
      }
      try (FileReader fr = new FileReader(file.getAbsoluteFile());
          BufferedReader br = new BufferedReader(fr)) {
        String line; // read a line from file
        while ((line = br.readLine()) != null) {
          String[] splicedLine = line.split("~!~");
          String ip = splicedLine[2];
          // String topicByLogLevel = splicedLine[3];
          System.out.println("creating event ");
          KeyedMessage<String, String> data = new KeyedMessage<>(
              /*logLevel.stream().anyMatch(s -> s.equals(topicByLogLevel)) ? topicByLogLevel :*/ topic,
              ip, line);
          producer.send(data);
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    producer.close();
  }
}
