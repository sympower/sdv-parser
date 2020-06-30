package net.sympower.parser.sdv;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.Locale;

public abstract class AbstractNumberConverter<T extends Number> extends SdvTypeConverterWithFormatBase<T, SdvDecimalFormat> {

  private DecimalFormat defaultDecimalFormat;

  protected AbstractNumberConverter() {
    super(SdvDecimalFormat.class);
  }

  @Override
  public T convert(String value, SdvDecimalFormat pattern, Locale locale) {
    DecimalFormat fmt;
    if (this.defaultDecimalFormat != null) {
      fmt = this.defaultDecimalFormat;
    }
    else {
      fmt = new DecimalFormat(pattern.value());
    }
    if (fmt != null) {
      if (locale != null) {
        fmt.setDecimalFormatSymbols(new DecimalFormatSymbols(locale));
      }
      if (pattern != null) {
        if (pattern.decimalSeparator().length() > 0) {
          fmt.getDecimalFormatSymbols().setDecimalSeparator(pattern.decimalSeparator().charAt(0));
        }
        if (pattern.groupingSeparator().length() > 0) {
          fmt.getDecimalFormatSymbols().setGroupingSeparator(pattern.groupingSeparator().charAt(0));
        }
      }
      // Workaround for similar issue https://bugs.openjdk.java.net/browse/JDK-8189097
      if (value.startsWith("-")) {
        fmt.setNegativePrefix("-");
      }
      try {
        return parse(value, fmt);
      }
      catch (ParseException e) {
        throw new IllegalArgumentException("Can not parse value to number: " + value, e);
      }
    }
    else {
      return parseDefault(value);
    }
  }

  protected abstract T parseDefault(String value);

  protected abstract T parse(String value, DecimalFormat fmt) throws ParseException;

  public void setDefaultDecimalFormat(DecimalFormat defaultDecimalFormat) {
    this.defaultDecimalFormat = defaultDecimalFormat;
  }

}
