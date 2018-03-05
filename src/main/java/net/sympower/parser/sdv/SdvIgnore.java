package net.sympower.parser.sdv;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Ignore fields (in a document) annotated with this annotation.
 */
@Retention(RUNTIME)
public @interface SdvIgnore {
}
