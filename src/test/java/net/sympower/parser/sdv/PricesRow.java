package net.sympower.parser.sdv;

import java.math.BigDecimal;
import java.time.LocalDate;

@SdvRow("PR")
public class PricesRow {

  public final String code;
  public final int year;
  public final int week;
  public final int day;
  public final LocalDate date;
  public final String alias;
  public final String unit;
  public final BigDecimal hour1;
  public final BigDecimal hour2;
  public final BigDecimal hour3a;
  public final BigDecimal hour3b;
  public final BigDecimal hour4;
  public final BigDecimal hour5;
  public final BigDecimal hour6;
  public final BigDecimal hour7;
  public final BigDecimal hour8;
  public final BigDecimal hour9;
  public final BigDecimal hour10;
  public final BigDecimal hour11;
  public final BigDecimal hour12;
  public final BigDecimal hour13;
  public final BigDecimal hour14;
  public final BigDecimal hour15;
  public final BigDecimal hour16;
  public final BigDecimal hour17;
  public final BigDecimal hour18;
  public final BigDecimal hour19;
  public final BigDecimal hour20;
  public final BigDecimal hour21;
  public final BigDecimal hour22;
  public final BigDecimal hour23;
  public final BigDecimal hour24;
  public final BigDecimal total;

  public PricesRow(String code, int year, int week, int day,
                   @SdvColumnFormat("dd.MM.yyyy") LocalDate date, String alias, String unit,
                   BigDecimal hour1, BigDecimal hour2, BigDecimal hour3a, BigDecimal hour3b,
                   BigDecimal hour4, BigDecimal hour5, BigDecimal hour6, BigDecimal hour7, BigDecimal hour8,
                   BigDecimal hour9, BigDecimal hour10, BigDecimal hour11, BigDecimal hour12, BigDecimal hour13,
                   BigDecimal hour14, BigDecimal hour15, BigDecimal hour16, BigDecimal hour17, BigDecimal hour18,
                   BigDecimal hour19, BigDecimal hour20, BigDecimal hour21, BigDecimal hour22, BigDecimal hour23,
                   BigDecimal hour24, BigDecimal total) {
    this.code = code;
    this.year = year;
    this.week = week;
    this.day = day;
    this.date = date;
    this.alias = alias;
    this.unit = unit;
    this.hour1 = hour1;
    this.hour2 = hour2;
    this.hour3a = hour3a;
    this.hour3b = hour3b;
    this.hour4 = hour4;
    this.hour5 = hour5;
    this.hour6 = hour6;
    this.hour7 = hour7;
    this.hour8 = hour8;
    this.hour9 = hour9;
    this.hour10 = hour10;
    this.hour11 = hour11;
    this.hour12 = hour12;
    this.hour13 = hour13;
    this.hour14 = hour14;
    this.hour15 = hour15;
    this.hour16 = hour16;
    this.hour17 = hour17;
    this.hour18 = hour18;
    this.hour19 = hour19;
    this.hour20 = hour20;
    this.hour21 = hour21;
    this.hour22 = hour22;
    this.hour23 = hour23;
    this.hour24 = hour24;
    this.total = total;
  }

  public BigDecimal[] getHours() {
    return new BigDecimal[] {
      hour1,
      hour2,
      hour3a,
      hour3b,
      hour4,
      hour5,
      hour6,
      hour7,
      hour8,
      hour9,
      hour10,
      hour11,
      hour12,
      hour13,
      hour14,
      hour15,
      hour16,
      hour17,
      hour18,
      hour19,
      hour20,
      hour21,
      hour22,
      hour23,
      hour24
    };
  }

}
