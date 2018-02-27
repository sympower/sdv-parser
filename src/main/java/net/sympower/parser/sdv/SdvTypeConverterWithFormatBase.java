package net.sympower.parser.sdv;

import java.lang.annotation.Annotation;

public abstract class SdvTypeConverterWithFormatBase<T, F extends Annotation> implements SdvTypeConverterWithFormat<T, F> {

  private final Class<F> annotationType;

  protected SdvTypeConverterWithFormatBase(Class<F> annotationType) {
    this.annotationType = annotationType;
  }

  @Override
  public Class<F> getAnnotationType() {
    return this.annotationType;
  }

}
