package io.getstream.logging.format;

public class MessageFormatter {

  public static String formatMessage(String message, Object[] args) {
    Object[] converted = ArgsConverter.INSTANCE.convertArgs(args);
    return String.format(message, converted);
  }
}
