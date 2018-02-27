package net.sympower.parser.sdv;

public interface SdvTypeConverter<T> {

  T convert(String value);

}
