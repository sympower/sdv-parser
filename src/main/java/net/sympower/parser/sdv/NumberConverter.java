package net.sympower.parser.sdv;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.function.Function;

public class NumberConverter<T extends Number> extends AbstractNumberConverter<T> {

  private final Function<String, T> defaultConverter;
  private final Function<Number, T> numberConverter;

  public NumberConverter(Function<String, T> defaultConverter, Function<Number, T> numberConverter) {
    this.defaultConverter = defaultConverter;
    this.numberConverter = numberConverter;
  }

  @Override
  protected T parseDefault(String value) {
    return defaultConverter.apply(value);
  }

  protected T parse(String value, DecimalFormat fmt) throws ParseException {
    return numberConverter.apply(fmt.parse(value));
  }

}
