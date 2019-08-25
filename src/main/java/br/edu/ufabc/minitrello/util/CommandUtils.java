package br.edu.ufabc.minitrello.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

public abstract class CommandUtils {

  public static boolean argIsNumber(Object value) {
    return !(value instanceof String);
  }

  public static boolean argIsString(Object value) {
    return value instanceof String;
  }

  public static String substringAfterFirstDigit(String str) {
    return str.substring(str.length() - 10, str.length());
  }

  public static int getQueueNumber(String str) {
    return Integer.parseInt(substringAfterFirstDigit(str));
  }

  public static String getUser() {
    try {
      return InetAddress.getLocalHost().getCanonicalHostName().toString()
        + "-" + System.currentTimeMillis();
    } catch (UnknownHostException e) {
      return String.valueOf(System.currentTimeMillis());
    }
  }

}
