package net.sympower.parser.sdv;

public class SdvParsingException extends RuntimeException {

  public SdvParsingException(String message) {
    super(message);
  }

  public SdvParsingException(String message, Throwable cause) {
    super(message, cause);
  }

  public SdvParsingException(Throwable cause) {
    super(cause);
  }

}
