package net.sympower.parser.sdv;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
public @interface SdvColumnFormat {

  String value();

}
