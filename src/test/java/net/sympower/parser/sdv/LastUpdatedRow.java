package net.sympower.parser.sdv;

import java.time.LocalDate;
import java.time.LocalTime;

@SdvRow("ST")
public class LastUpdatedRow {

  public final int year;
  public final int week;
  public final int day;
  public final int hour;
  public final int totalHours;
  public final LocalTime time;
  public final LocalDate date;

  public LastUpdatedRow(int year, int week, int day, int hour, int totalHours,
                        @SdvColumnFormat("HH:mm") LocalTime time,
                        @SdvColumnFormat("dd.MM.yyyy") LocalDate date) {
    this.year = year;
    this.week = week;
    this.day = day;
    this.hour = hour;
    this.totalHours = totalHours;
    this.time = time;
    this.date = date;
  }

  @Override
  public String toString() {
    return "LastUpdatedRow{" + ", year=" + year +
      ", week=" + week +
      ", day=" + day +
      ", hour=" + hour +
      ", totalHours=" + totalHours +
      ", time=" + time +
      ", date=" + date +
      '}';
  }

}
