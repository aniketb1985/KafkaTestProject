package com.cisco.kafka.logs.producer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LogCreatorClass {
  private static final SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yy HH");
  private static String minutesAndSeconds = ":00:00";
  private static String currentHour = sdf.format(Calendar.getInstance().getTime()) + minutesAndSeconds;

  private LogCreatorClass() {
  }

  public static void main(String[] args) {
    ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    Random random = new Random();
    SimpleDateFormat sdf1 = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
    Runnable task = () -> {
      currentHour = (sdf.format(Calendar.getInstance().getTime()) + minutesAndSeconds).equals(currentHour)
          ? currentHour : (sdf.format(Calendar.getInstance().getTime()) + minutesAndSeconds);
      try {
        File file = new File("/home/vikhyatk/Documents/Logs/log_" + currentHour + ".txt");
        if (!file.exists()) {
          file.createNewFile();
        }
        try (FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
            BufferedWriter bw = new BufferedWriter(fw)) {
          String[] logLevel = { "INFO", "ERROR", "WARN", "FATAL" };
          String dateAndTime = Arrays
              .asList(sdf1.format(Calendar.getInstance().getTime()).split(" ")).stream()
              .reduce((s1, s2) -> s1 + "~!~" + s2).get();
          Object object = new Object();
          synchronized (object) {
            int randomNumber = random.nextInt(4);
            String line = dateAndTime + "~!~" + "192.168.2." + random.nextInt(255) + "~!~"
                + logLevel[randomNumber] + "~!~This is a test generated message for some "
                + logLevel[randomNumber];
            bw.write(line + "\n"); // write to file
          }
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    };
    int initialDelay = 0;
    int period = 2;
    executor.scheduleAtFixedRate(task, initialDelay, period, TimeUnit.SECONDS);
  }
}
