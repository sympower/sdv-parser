package net.sympower.parser.sdv;

import java.lang.annotation.Annotation;
import java.util.Locale;

public interface SdvTypeConverterWithFormat<T, F extends Annotation> {

  Class<F> getAnnotationType();

  T convert(String value, F format, Locale locale);

}
