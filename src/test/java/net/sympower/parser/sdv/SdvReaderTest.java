package net.sympower.parser.sdv;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class SdvReaderTest {

  @Test
  public void parseAllRowsIgnored() throws IOException {
    SdvReader sut = new SdvReader();
    List<?> results = sut.parse(getClass().getResource("prices.sdv"));
    assertThat(results.size()).isEqualTo(0);
  }

  @Test
  public void parseAllRowsNoneIgnored() throws IOException {
    SdvReader sut = new SdvReader();
    sut.setIgnoreUnknownRows(false);
    assertThatThrownBy(() -> sut.parse(getClass().getResource("prices.sdv")))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Row type (ST) not registered, on row 'ST;2017;12;7;23;167;11:58;22.03.2017'");
  }

  @Test
  public void parseLastUpdated() throws IOException {
    SdvReader sut = new SdvReader();
    sut.registerRowType(LastUpdatedRow.class);
    sut.registerRowType(AreaDescriptionRow.class);
    List<LastUpdatedRow> results = sut.parse(getClass().getResource("lastUpdated.sdv"), LastUpdatedRow.class);
    Iterator<LastUpdatedRow> iterator = results.iterator();
    assertLastUpdatedRow(iterator);
    assertThat(iterator.hasNext()).as("Should not have more rows").isFalse();
  }

  private void assertLastUpdatedRow(Iterator<?> iterator) {
    assertThat(iterator.hasNext()).as("Should have last updated row").isTrue();
    LastUpdatedRow result = (LastUpdatedRow) iterator.next();
    assertThat(result.year).isEqualTo(2017);
    assertThat(result.week).isEqualTo(12);
    assertThat(result.day).isEqualTo(7);
    assertThat(result.hour).isEqualTo(23);
    assertThat(result.totalHours).isEqualTo(167);
    assertThat(result.time).isEqualTo(LocalTime.of(11, 58));
    assertThat(result.date).isEqualTo(LocalDate.of(2017, 3, 22));
  }

  @Test
  public void parseLastAreaDescriptor() throws IOException {
    SdvReader sut = new SdvReader();
    sut.registerRowType(AreaDescriptionRow.class);
    List<?> results = sut.parse(getClass().getResource("areaDescription.sdv"));
    Iterator<?> iterator = results.iterator();
    assertAreaEquals("SP1", "SYSTEMPRICE", iterator);
    assertAreaEquals("BG", "Bulgaria", iterator);
    assertAreaEquals("FI", "Finland", iterator);
    assertThat(iterator.hasNext()).as("Should not have more rows").isFalse();
  }

  private void assertAreaEquals(String alias, String desc, Iterator<?> iterator) {
    assertThat(iterator.hasNext()).as("Should have area with alias '%s' and description '%s'", alias, desc).isTrue();
    AreaDescriptionRow result = (AreaDescriptionRow) iterator.next();
    assertThat(result.alias).isEqualTo(alias);
    assertThat(result.description).isEqualTo(desc);
  }

  @Test
  public void parseLineCount() throws IOException {
    SdvReader sut = new SdvReader();
    sut.registerRowType(LineCountRow.class);
    List<?> results = sut.parse(getClass().getResource("lineCount.sdv"));
    Iterator<?> iterator = results.iterator();
    assertLineCountRow(223, iterator);
    assertThat(iterator.hasNext()).as("Should not have more rows").isFalse();
  }

  private void assertLineCountRow(int lineCount, Iterator<?> iterator) {
    assertThat(iterator.hasNext()).as("Should have line count row with value %s", lineCount).isTrue();
    LineCountRow result = (LineCountRow) iterator.next();
    assertThat(result.count).isEqualTo(lineCount);
  }

  @Test
  public void parseLastUpdatedAreaPricesAndCount() throws IOException {
    SdvReader sut = new SdvReader();
    sut.setDefaultLocale(new Locale("fi"));
    sut.registerRowType(LastUpdatedRow.class);
    sut.registerRowType(AreaDescriptionRow.class);
    sut.registerRowType(PricesRow.class);
    sut.registerRowType(LineCountRow.class);
    List<?> results = sut.parse(getClass().getResource("prices.sdv"));
    Iterator<?> iterator = results.iterator();
    assertLastUpdatedRow(iterator);
    assertAreaEquals("FRE", "Finnish-Russian Exchange Bidding Area", iterator);
    assertAreaEquals("FI", "Finland", iterator);
    assertPriceRows(iterator);
    assertLineCountRow(223, iterator);
    assertThat(iterator.hasNext()).as("Should not have more rows").isFalse();
  }

  @Test
  public void parseSpotPriceDocument() throws IOException {
    SdvReader sut = new SdvReader();
    sut.setDefaultLocale(new Locale("fi"));
    SpotPriceDocument doc = sut.parseDocument(getClass().getResource("prices.sdv"), SpotPriceDocument.class);
    assertThat(doc.lastUpdated).as("Should have last updated row").isNotNull();
    assertLastUpdatedRow(Arrays.asList(doc.lastUpdated).iterator());
    assertThat(doc.lastUpdatedSetViaMethod).as("Last updated should have been set via a method call").isTrue();
    assertThat(doc.areas).as("Should have areas").isNotNull();
    Iterator<AreaDescriptionRow> areaIter = doc.areas.iterator();
    assertAreaEquals("FRE", "Finnish-Russian Exchange Bidding Area", areaIter);
    assertAreaEquals("FI", "Finland", areaIter);
    assertThat(areaIter.hasNext()).as("Should not have more area rows").isFalse();
    Iterator<PricesRow> priceIter = doc.prices.iterator();
    assertPriceRows(priceIter);
    assertThat(doc.priceAddedViaMethod).as("Prices should have been added via a method call").isTrue();
    assertThat(priceIter.hasNext()).as("Should not have more price rows").isFalse();
    assertThat(doc.lineCount).as("Should have line count row").isNotNull();
    assertLineCountRow(223, Arrays.asList(doc.lineCount).iterator());
  }

  private void assertPriceRows(Iterator<?> iterator) {
    assertPriceRowEquals("SO", 2017, 12, 3, LocalDate.of(2017, 3, 22), "FRE", "NOK",
      new BigDecimal[] {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
      iterator);
    assertPriceRowEquals("SO", 2017, 12, 3, LocalDate.of(2017, 03, 22), "FI", "NOK",
      new BigDecimal[] { new BigDecimal("-289.70"), new BigDecimal("213.17"), new BigDecimal("196.84"), null, new BigDecimal("235.61"), new BigDecimal("235.43"), new BigDecimal("244.91"), new BigDecimal("267.72"), new BigDecimal("316.06"), new BigDecimal("280.39"), new BigDecimal("251.57"), new BigDecimal("263.52"), new BigDecimal("247.56"), new BigDecimal("257.32"), new BigDecimal("244.73"), new BigDecimal("201.49"), new BigDecimal("204.50"), new BigDecimal("206.97"), new BigDecimal("203.04"), new BigDecimal("202.50"), new BigDecimal("205.05"), new BigDecimal("208.06"), new BigDecimal("199.03"), new BigDecimal("191.55"), new BigDecimal("188.63")}, iterator);
    assertPriceRowEquals("SO", 2017, 12, 3, LocalDate.of(2017, 03, 22), "FI", "SEK",
      new BigDecimal[] { new BigDecimal("301.63"), new BigDecimal("221.95"), new BigDecimal("204.95"), null, new BigDecimal("245.32"), new BigDecimal("245.13"), new BigDecimal("255.00"), new BigDecimal("278.75"), new BigDecimal("329.08"), new BigDecimal("291.95"), new BigDecimal("261.94"), new BigDecimal("274.38"), new BigDecimal("257.76"), new BigDecimal("267.92"), new BigDecimal("254.81"), new BigDecimal("209.80"), new BigDecimal("212.93"), new BigDecimal("215.49"), new BigDecimal("211.41"), new BigDecimal("210.84"), new BigDecimal("213.50"), new BigDecimal("216.63"), new BigDecimal("207.23"), new BigDecimal("199.44"), new BigDecimal("196.40")}, iterator);
    assertPriceRowEquals("SO", 2017, 12, 3, LocalDate.of(2017, 03, 22), "FI", "DKK",
      new BigDecimal[] { new BigDecimal("236.12"), new BigDecimal("173.75"), new BigDecimal("160.44"), null, new BigDecimal("192.04"), new BigDecimal("191.89"), new BigDecimal("199.62"), new BigDecimal("218.21"), new BigDecimal("257.61"), new BigDecimal("228.54"), new BigDecimal("205.05"), new BigDecimal("214.79"), new BigDecimal("201.78"), new BigDecimal("209.73"), new BigDecimal("199.47"), new BigDecimal("164.23"), new BigDecimal("166.68"), new BigDecimal("168.69"), new BigDecimal("165.49"), new BigDecimal("165.05"), new BigDecimal("167.13"), new BigDecimal("169.58"), new BigDecimal("162.22"), new BigDecimal("156.13"), new BigDecimal("153.75")}, iterator);
    assertPriceRowEquals("SO", 2017, 12, 3, LocalDate.of(2017, 03, 22), "FI", "EUR",
      new BigDecimal[] { new BigDecimal("31.76"), new BigDecimal("23.37"), new BigDecimal("21.58"), null, new BigDecimal("25.83"), new BigDecimal("25.81"), new BigDecimal("26.85"), new BigDecimal("29.35"), new BigDecimal("34.65"), new BigDecimal("30.74"), new BigDecimal("27.58"), new BigDecimal("28.89"), new BigDecimal("27.14"), new BigDecimal("28.21"), new BigDecimal("26.83"), new BigDecimal("22.09"), new BigDecimal("22.42"), new BigDecimal("22.69"), new BigDecimal("22.26"), new BigDecimal("22.20"), new BigDecimal("22.48"), new BigDecimal("22.81"), new BigDecimal("21.82"), new BigDecimal("21.00"), new BigDecimal("20.68")}, iterator);
    assertPriceRowEquals("SF", 2017, 12, 7, LocalDate.of(2017, 3, 26), "FRE", "NOK",
      new BigDecimal[] {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
      iterator);
    assertPriceRowEquals("SF", 2017, 12, 7, LocalDate.of(2017, 03, 26), "FI", "NOK",
      new BigDecimal[] { new BigDecimal("155.70"), new BigDecimal("156.06"), null, null, new BigDecimal("176.40"), new BigDecimal("175.23"), new BigDecimal("175.41"), new BigDecimal("176.04"), new BigDecimal("178.92"), new BigDecimal("181.35"), new BigDecimal("182.07"), new BigDecimal("180.81"), new BigDecimal("184.14"), new BigDecimal("190.53"), new BigDecimal("189.63"), new BigDecimal("157.68"), new BigDecimal("156.78"), new BigDecimal("156.87"), new BigDecimal("157.86"), new BigDecimal("158.85"), new BigDecimal("158.76"), new BigDecimal("159.03"), new BigDecimal("160.92"), new BigDecimal("206.55"), new BigDecimal("72.63")}, iterator);
    assertPriceRowEquals("SF", 2017, 12, 7, LocalDate.of(2017, 03, 26), "FI", "SEK",
      new BigDecimal[] { new BigDecimal("166.08"), new BigDecimal("166.46"), null, null, new BigDecimal("188.16"), new BigDecimal("186.91"), new BigDecimal("187.10"), new BigDecimal("187.78"), new BigDecimal("190.85"), new BigDecimal("193.44"), new BigDecimal("194.21"), new BigDecimal("192.86"), new BigDecimal("196.42"), new BigDecimal("203.23"), new BigDecimal("202.27"), new BigDecimal("168.19"), new BigDecimal("167.23"), new BigDecimal("167.33"), new BigDecimal("168.38"), new BigDecimal("169.44"), new BigDecimal("169.34"), new BigDecimal("169.63"), new BigDecimal("171.65"), new BigDecimal("220.32"), new BigDecimal("77.47")}, iterator);
    assertPriceRowEquals("SF", 2017, 12, 7, LocalDate.of(2017, 03, 26), "FI", "DKK",
      new BigDecimal[] { new BigDecimal("128.59"), new BigDecimal("128.89"), null, null, new BigDecimal("145.69"), new BigDecimal("144.72"), new BigDecimal("144.87"), new BigDecimal("145.39"), new BigDecimal("147.77"), new BigDecimal("149.78"), new BigDecimal("150.37"), new BigDecimal("149.33"), new BigDecimal("152.08"), new BigDecimal("157.36"), new BigDecimal("156.62"), new BigDecimal("130.23"), new BigDecimal("129.49"), new BigDecimal("129.56"), new BigDecimal("130.38"), new BigDecimal("131.20"), new BigDecimal("131.12"), new BigDecimal("131.34"), new BigDecimal("132.91"), new BigDecimal("170.59"), new BigDecimal("59.99")}, iterator);
    assertPriceRowEquals("SO", 2017, 12, 7, LocalDate.of(2017, 03, 26), "FI", "EUR",
      new BigDecimal[] { new BigDecimal("17.30"), new BigDecimal("17.34"), null, null, new BigDecimal("19.60"), new BigDecimal("19.47"), new BigDecimal("19.49"), new BigDecimal("19.56"), new BigDecimal("19.88"), new BigDecimal("20.15"), new BigDecimal("20.23"), new BigDecimal("20.09"), new BigDecimal("20.46"), new BigDecimal("21.17"), new BigDecimal("21.07"), new BigDecimal("17.52"), new BigDecimal("17.42"), new BigDecimal("17.43"), new BigDecimal("17.54"), new BigDecimal("17.65"), new BigDecimal("17.64"), new BigDecimal("17.67"), new BigDecimal("17.88"), new BigDecimal("22.95"), new BigDecimal("8.07")}, iterator);
  }

  private void assertPriceRowEquals(String code, int year, int week, int day, LocalDate date, String alias, String unit,
                                    BigDecimal[] hours, Iterator<?> iterator) {
    assertThat(iterator.hasNext()).as(
        "Should have price row with code '%s', year %s, week %s, day %s, date %s, alias '%s', unit '%s'",
        code, year, week, day, date, alias, unit
    ).isTrue();
    PricesRow result = (PricesRow) iterator.next();
    assertThat(result.code).isEqualTo(code);
    assertThat(result.year).isEqualTo(year);
    assertThat(result.week).isEqualTo(week);
    assertThat(result.day).isEqualTo(day);
    assertThat(result.date).isEqualTo(date);
    assertThat(result.alias).isEqualTo(alias);
    assertThat(result.unit).isEqualTo(unit);
    assertThat(result.getHours()).containsExactly(hours);
  }

}
