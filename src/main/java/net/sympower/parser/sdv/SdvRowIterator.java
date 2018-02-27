package net.sympower.parser.sdv;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.regex.Pattern;

public class SdvRowIterator<T> implements Iterator<T>, AutoCloseable {

  private final SdvReader reader;
  private final String commentPrefix;
  private final Pattern columnDelimiterPattern;

  private final BufferedReader textReader;
  private final Class<?> rowFilterType;

  private String line;
  private Class<T> rowType;
  private String[] cols;

  SdvRowIterator(SdvReader reader, String commentPrefix, Pattern columnDelimiterPattern, BufferedReader textReader, Class<?> rowFilterType) {
    this.reader = reader;
    this.commentPrefix = commentPrefix;
    this.columnDelimiterPattern = columnDelimiterPattern;
    this.textReader = textReader;
    this.rowFilterType = rowFilterType;
  }

  @Override
  public boolean hasNext() {
    while ((this.line = readNextLine()) != null) {
      this.line = this.line.trim();
      if (!line.startsWith(this.commentPrefix) && line.length() > 0) {
        this.cols = this.columnDelimiterPattern.split(line, -1);
        if (cols.length > 0) {
          String type = cols[0];
          this.rowType = (Class<T>) this.reader.getRowBeanType(type);
          if (rowType == null) {
            if (this.reader.isIgnoreUnknownRows()) {
              continue;
            }
            throw new IllegalArgumentException(
              String.format("Row type (%s) not registered, on row '%s'", type, line));
          }
          if (!rowFilterType.isAssignableFrom(this.rowType)) {
            continue;
          }
          return true;
        }
      }
    }
    try {
      close();
    }
    catch (IOException e) {
      throw new SdvParsingIOException(e);
    }
    return false;
  }

  private String readNextLine() {
    try {
      return textReader.readLine();
    }
    catch (IOException e) {
      throw new SdvParsingIOException(e);
    }
  }

  @Override
  public T next() {
    if (this.line == null || this.cols == null || this.rowType == null) {
      throw new SdvParsingException("Should call hasNext() before calling next()!");
    }
    try {
      int paramCount = cols.length - 1;
      Constructor<T> constr = (Constructor<T>) findConstructor(rowType, paramCount);
      if (constr == null) {
        throw new SdvParsingException(
          String.format("No constructor with suitable number of parameters (%s) found, on class %s for row '%s'", paramCount, rowType, line));
      }
      Object[] params = parseParameters(cols, 1, constr);
      try {
        return constr.newInstance(params);
      }
      catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
        throw new SdvParsingReflectionException(
          String.format("Error while invoking constructor on class %s", rowType), e);
      }
    }
    finally {
      this.line = null;
      this.cols = null;
      this.rowType = null;
    }
  }

  public void close() throws IOException {
    this.textReader.close();
  }

  private Object[] parseParameters(String[] cols, int colStartIndex, Constructor<?> constr) {
    Class<?>[] parameterTypes = constr.getParameterTypes();
    Annotation[][] parameterAnnotations = constr.getParameterAnnotations();
    Object[] params = new Object[parameterTypes.length];
    for (int i = 0; i < parameterTypes.length; i++) {
      params[i] = convertValue(
        cols[colStartIndex + i], parameterTypes[i], parameterAnnotations[i],
        i, constr.getDeclaringClass());
    }
    return params;
  }

  private Object convertValue(String value, Class<?> type, Annotation[] annotations, int index, Class<?> klass) {
    if (value == null || value.length() == 0) {
      return null;
    }
    Object result = null;
    try {
      SdvTypeConverter<?> converter = this.reader.getConverter(type);
      if (converter != null) {
        result = converter.convert(value);
      }
      if (result == null) {
        SdvTypeConverterWithFormat<?, ? extends Annotation> converterWithFormat = this.reader.getConverterWithFormat(type);
        if (converterWithFormat != null) {
          result = converterWithFormat.convert(value, findAnnotation(annotations, converterWithFormat.getAnnotationType()), this.reader.getDefaultLocale());
        }
      }
    }
    catch (Exception e) {
      throw new SdvParsingException(
        String.format("Error while parsing constructor parameter #%s (type %s, value '%s'), on class %s", index+1, type, value, klass), e);
    }
    if (result == null) {
      throw new SdvParsingException(
        String.format("Constructor parameter #%s type %s not supported (value: %s), on class %s", index+1, type, value, klass));
    }
    return result;
  }

  private <T extends Annotation> T findAnnotation(Annotation[] annotations, Class<? extends Annotation> annotationClass) {
    for (Annotation annotation : annotations) {
      if (annotation.annotationType().isAssignableFrom(annotationClass)) {
        return (T) annotation;
      }
    }
    return null;
  }

  private Constructor<?> findConstructor(Class<?> rowType, int paramCount) {
    for (Constructor<?> constr : rowType.getConstructors()) {
      if (constr.getParameterCount() == paramCount) {
        return constr;
      }
    }
    return null;
  }

}
