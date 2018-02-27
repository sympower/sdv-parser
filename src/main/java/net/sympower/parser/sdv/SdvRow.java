package net.sympower.parser.sdv;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
public @interface SdvRow {

  /**
   * Row type identifier in the SDV file (the first column). This is used to find corresponding Java class at runtime.
   */
  String value();

}
