package net.sympower.parser.sdv;

import java.io.IOException;

interface SupplierWithIOException<T> {

  T get() throws IOException;

}
