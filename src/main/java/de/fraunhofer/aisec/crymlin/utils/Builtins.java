package de.fraunhofer.aisec.crymlin.utils;

public class Builtins {
  public static String _split(String s, String regex, int index) {
    String[] splitted = s.split(regex);
    if (index < splitted.length) {
      return splitted[index];
    } else {
      // TODO throw Error?
      return "";
    }
  }

  public static boolean _receives_value_from() {
    // TODO implement
    return true;
  }
}
