package net.sympower.parser.sdv;

import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.util.function.BiFunction;
import java.util.function.Function;

public class DateTimeConverter<T extends Temporal> extends AbstractDateTimeConverter<T> {

  private final Function<String, T> defaultConverter;
  private final BiFunction<String, DateTimeFormatter, T> withFormatConverter;

  public DateTimeConverter(Function<String, T> defaultConverter, BiFunction<String, DateTimeFormatter, T> withFormatConverter) {
    this.defaultConverter = defaultConverter;
    this.withFormatConverter = withFormatConverter;
  }

  @Override
  protected T parseDefault(String value) {
    return defaultConverter.apply(value);
  }

  protected T parse(String value, DateTimeFormatter fmt) {
    return withFormatConverter.apply(value, fmt);
  }

}
