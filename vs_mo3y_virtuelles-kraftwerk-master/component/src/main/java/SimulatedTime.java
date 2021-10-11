package src.main.java;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.Month;

//  Each minute one simulated day passes -> 1min = 24 simulated hours, 1sec = 24 simulated minutes
public class SimulatedTime {

  private static final LocalDateTime initTime = LocalDateTime.of(2021, Month.JANUARY, 1, 0, 0, 0);

  public static LocalDateTime now() {
    LocalDateTime realTime = LocalDateTime.now();
    int seconds = realTime.getSecond();
    long simulatedSeconds = realSecondsToSimulatedSeconds(seconds);
    int minutes = realTime.getMinute();
    int hours = realTime.getHour();
    int simulatedDays = minutes + hours * 60;
    LocalDateTime simulatedDateTime = initTime.plusDays(simulatedDays)
        .plusSeconds(simulatedSeconds);
    return simulatedDateTime;
  }

  public static long realSecondsToSimulatedSeconds(int seconds) {
    return seconds * 1440;
  }
}