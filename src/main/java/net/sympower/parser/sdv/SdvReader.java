package net.sympower.parser.sdv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.annotation.Annotation;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class SdvReader {

  private static final String COMMENT_PREFIX = "#";
  private static final String COLUMN_DELIMITER = ";";

  private final HashMap<String, Class<?>> rowBeans = new HashMap<>();
  private final HashMap<Class<?>, SdvTypeConverter<?>> converters = new HashMap<>();
  private final HashMap<Class<?>, SdvTypeConverterWithFormat<?, ? extends Annotation>> convertersWithFormat = new HashMap<>();

  private Charset charset = StandardCharsets.UTF_8;
  private boolean ignoreUnknownRows = true;
  private final Pattern columnDelimiterPattern;
  private Locale defaultLocale = Locale.getDefault();
  private DecimalFormat defaultDecimalFormat = new DecimalFormat();

  public SdvReader() {
    this.columnDelimiterPattern = Pattern.compile(Pattern.quote(COLUMN_DELIMITER));
    registerConverter(String.class, value -> value);
    registerConverter(Short.class, Short::valueOf);
    registerConverter(Short.TYPE, Short::valueOf);
    registerConverter(Integer.class, Integer::valueOf);
    registerConverter(Integer.TYPE, Integer::valueOf);
    registerConverter(Long.class, Long::valueOf);
    registerConverter(Long.TYPE, Long::valueOf);
    registerConverter(Byte.class, Byte::valueOf);
    registerConverter(Byte.TYPE, Byte::valueOf);
    registerConverter(Float.class, new NumberConverter<>(Float::valueOf, Number::floatValue));
    registerConverter(Float.TYPE, new NumberConverter<>(Float::valueOf, Number::floatValue));
    registerConverter(Double.class, new NumberConverter<>(Double::valueOf, Number::doubleValue));
    registerConverter(Double.TYPE, new NumberConverter<>(Double::valueOf, Number::doubleValue));
    registerConverter(Boolean.class, Boolean::valueOf);
    registerConverter(Boolean.TYPE, Boolean::valueOf);
    registerConverter(Character.class, value -> Character.valueOf(value.charAt(0)));
    registerConverter(Character.TYPE, value -> Character.valueOf(value.charAt(0)));
    registerConverter(BigInteger.class, BigInteger::new);
    registerConverter(BigDecimal.class, new BigDecimalConverter());
    registerConverter(LocalDate.class, new DateTimeConverter<>(LocalDate::parse, LocalDate::parse));
    registerConverter(LocalTime.class, new DateTimeConverter<>(LocalTime::parse, LocalTime::parse));
    registerConverter(LocalDateTime.class, new DateTimeConverter<>(LocalDateTime::parse, LocalDateTime::parse));
    registerConverter(ZonedDateTime.class, new DateTimeConverter<>(ZonedDateTime::parse, ZonedDateTime::parse));
    registerConverter(OffsetDateTime.class, new DateTimeConverter<>(OffsetDateTime::parse, OffsetDateTime::parse));
  }

  public void registerRowType(Class<?> klass) {
    SdvRow sdvRow = klass.getAnnotation(SdvRow.class);
    if (sdvRow == null) {
      throw new IllegalArgumentException(
        String.format("Row class %s has to be annotated with @%s!", klass.getName(), SdvRow.class.getSimpleName()));
    }
    rowBeans.put(sdvRow.value(), klass);
  }

  public <T> void registerConverter(Class<T> klass, SdvTypeConverter<T> converter) {
    this.converters.put(klass, converter);
  }

  public <T> void registerConverter(Class<T> klass, SdvTypeConverterWithFormat<T,?> converter) {
    this.convertersWithFormat.put(klass, converter);
  }

  public <T> T parseDocument(Path path, Class<T> documentType) throws IOException {
    return parseDocument(pathToUrl(path), documentType);
  }

  public <T> T parseDocument(Path path, T document) throws IOException {
    return parseDocument(pathToUrl(path), document);
  }

  public <T> T parseDocument(URL url, Class<T> documentType) throws IOException {
    return parseDocument(url, new SdvRowCollector<>(documentType));
  }

  public <T> T parseDocument(URL url, T document) throws IOException {
    return parseDocument(url, new SdvRowCollector<>(document));
  }

  public <T> T parseDocument(InputStream is, Class<T> documentType) throws IOException {
    return parseDocument(() -> iterate(is, Object.class), new SdvRowCollector<>(documentType));
  }

  public <T> T parseDocument(InputStream is, T document) throws IOException {
    return parseDocument(() -> iterate(is, Object.class), new SdvRowCollector<>(document));
  }

  public <T> T parseDocument(Reader reader, Class<T> documentType) throws IOException {
    return parseDocument(() -> iterate(reader, Object.class), new SdvRowCollector<>(documentType));
  }

  public <T> T parseDocument(Reader reader, T document) throws IOException {
    return parseDocument(() -> iterate(reader, Object.class), new SdvRowCollector<>(document));
  }

  private <T> T parseDocument(URL url, SdvRowCollector<T> collector) throws IOException {
    return parseDocument(() -> iterate(url, Object.class), collector);
  }

  private <T> T parseDocument(SupplierWithIOException<SdvRowIterator<?>> iteratorSupplier, SdvRowCollector<T> collector) throws IOException {
    collector.registerRowBeanTypes(this);
    try (SdvRowIterator<?> iter = iteratorSupplier.get()) {
      while (iter.hasNext()) {
        collector.newRow(iter.next());
      }
    }
    return collector.getDocument();
  }

  public List<?> parse(Path path) throws IOException {
    return parse(path, Object.class);
  }

  public <T> List<T> parse(Path path, Class<T> rowFilterType) throws IOException {
    return parse(pathToUrl(path), rowFilterType);
  }

  public List<?> parse(URL url) throws IOException {
    return parse(url, Object.class);
  }

  public <T> List<T> parse(URL url, Class<T> rowFilterType) throws IOException {
    return parse(() -> iterate(url, rowFilterType));
  }

  public <T> List<T> parse(InputStream is, Class<T> rowFilterType) throws IOException {
    return parse(() -> iterate(is, rowFilterType));
  }

  public <T> List<T> parse(Reader reader, Class<T> rowFilterType) throws IOException {
    return parse(() -> iterate(reader, rowFilterType));
  }

  private <T> List<T> parse(SupplierWithIOException<SdvRowIterator<T>> iteratorSupplier) throws IOException {
    ArrayList<T> rows = new ArrayList<>();
    try (SdvRowIterator<T> iter = iteratorSupplier.get()) {
      iter.forEachRemaining(rows::add);
    }
    return rows;
  }

  public SdvRowIterator<?> iterate(URL url) throws IOException {
    return iterate(url, Object.class);
  }

  public <T> SdvRowIterator<T> iterate(URL url, Class<T> rowFilterType) throws IOException {
    return iterate(url.openStream(), rowFilterType);
  }

  public <T> SdvRowIterator<T> iterate(InputStream is, Class<T> rowFilterType) throws IOException {
    return iterate(new InputStreamReader(is, this.charset), rowFilterType);
  }

  public <T> SdvRowIterator<T> iterate(Reader reader, Class<T> rowFilterType) throws IOException {
    setConverterDefaults();
    return new SdvRowIterator<>(this, COMMENT_PREFIX, columnDelimiterPattern, new BufferedReader(reader), rowFilterType);
  }

  private void setConverterDefaults() {
    for (SdvTypeConverterWithFormat converter : convertersWithFormat.values()) {
      if (converter instanceof AbstractNumberConverter) {
        AbstractNumberConverter numberConverter = (AbstractNumberConverter) converter;
        numberConverter.setDefaultDecimalFormat(this.defaultDecimalFormat);
      }
    }
  }

  Class<?> getRowBeanType(String type) {
    return rowBeans.get(type);
  }

  public void setCharset(Charset charset) {
    this.charset = charset;
  }

  public void setIgnoreUnknownRows(boolean ignoreUnknownRows) {
    this.ignoreUnknownRows = ignoreUnknownRows;
  }

  public void setDefaultLocale(Locale defaultLocale) {
    this.defaultLocale = defaultLocale;
  }

  public void setDefaultDecimalFormat(DecimalFormat defaultDecimalFormat) {
    this.defaultDecimalFormat = defaultDecimalFormat;
  }

  public boolean isIgnoreUnknownRows() {
    return ignoreUnknownRows;
  }

  public Locale getDefaultLocale() {
    return defaultLocale;
  }

  public SdvTypeConverter<?> getConverter(Class<?> type) {
    return this.converters.get(type);
  }

  public SdvTypeConverterWithFormat<?, ? extends Annotation> getConverterWithFormat(Class<?> type) {
    return this.convertersWithFormat.get(type);
  }

  private static URL pathToUrl(Path path) {
    try {
      return path.toUri().toURL();
    }
    catch (MalformedURLException e) {
      throw new SdvParsingException(e);
    }
  }

}
