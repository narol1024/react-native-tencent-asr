package org.narol.reactnative.tencentasrutil;
import com.facebook.react.bridge.ReadableMap;

public class ConfigParameterUtils {

  private ConfigParameterUtils() {}

  public static String getStringOrDefault(ReadableMap map, String key,
                                          String defaultValue) {
    return map.hasKey(key) ? map.getString(key) : defaultValue;
  }

  public static int getIntOrDefault(ReadableMap map, String key,
                                    int defaultValue) {
    return map.hasKey(key) ? map.getInt(key) : defaultValue;
  }
}
