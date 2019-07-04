package de.fraunhofer.aisec.crymlin.utils;

public class Builtins {
  public static String _split(String s, String regex, int index) {
    return s.split(regex)[index];
  }

  public static boolean _receives_value_from() {
    // TODO implement
    return true;
  }
}
