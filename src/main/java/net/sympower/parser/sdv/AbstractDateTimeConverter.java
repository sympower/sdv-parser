package net.sympower.parser.sdv;

import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.util.Locale;

public abstract class AbstractDateTimeConverter<T extends Temporal> extends SdvTypeConverterWithFormatBase<T, SdvColumnFormat> {

  protected AbstractDateTimeConverter() {
    super(SdvColumnFormat.class);
  }

  @Override
  public T convert(String value, SdvColumnFormat pattern, Locale locale) {
    if (pattern != null) {
      DateTimeFormatter fmt = DateTimeFormatter.ofPattern(pattern.value(), locale);
      return parse(value, fmt);
    }
    else {
      return parseDefault(value);
    }
  }

  protected abstract T parseDefault(String value);

  protected abstract T parse(String value, DateTimeFormatter fmt);

}
