package br.edu.ufabc.minitrello.util;

public abstract class CommandUtils {

  public static boolean argIsNumber(Object value) {
    return !(value instanceof String);
  }

  public static String substringAfterFirstDigit(String str) {
    return str.substring(str.length() - 10, str.length());
  }

  public static int getQueueNumber(String str) {
    return Integer.parseInt(substringAfterFirstDigit(str));
  }

}
