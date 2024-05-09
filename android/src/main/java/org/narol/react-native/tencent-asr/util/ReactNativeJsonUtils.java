package org.narol.reactnative.tencentasrutil;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.facebook.react.bridge.ReadableType;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.json.*;
public class ReactNativeJsonUtils {
  public static WritableMap convertJsonToMap(JSONObject jsonObject)
      throws JSONException {
    WritableMap map = new WritableNativeMap();

    Iterator<String> iterator = jsonObject.keys();
    while (iterator.hasNext()) {
      String key = iterator.next();
      Object value = jsonObject.get(key);
      if (value instanceof JSONObject) {
        map.putMap(key, convertJsonToMap((JSONObject)value));
      } else if (value instanceof JSONArray) {
        map.putArray(key, convertJsonToArray((JSONArray)value));
        if (("option_values").equals(key)) {
          map.putArray("options", convertJsonToArray((JSONArray)value));
        }
      } else if (value instanceof Boolean) {
        map.putBoolean(key, (Boolean)value);
      } else if (value instanceof Integer) {
        map.putInt(key, (Integer)value);
      } else if (value instanceof Double) {
        map.putDouble(key, (Double)value);
      } else if (value instanceof String) {
        map.putString(key, (String)value);
      } else {
        map.putString(key, value.toString());
      }
    }
    return map;
  }
  public static WritableArray convertJsonToArray(JSONArray jsonArray)
      throws JSONException {
    WritableArray array = new WritableNativeArray();

    for (int i = 0; i < jsonArray.length(); i++) {
      Object value = jsonArray.get(i);
      if (value instanceof JSONObject) {
        array.pushMap(convertJsonToMap((JSONObject)value));
      } else if (value instanceof JSONArray) {
        array.pushArray(convertJsonToArray((JSONArray)value));
      } else if (value instanceof Boolean) {
        array.pushBoolean((Boolean)value);
      } else if (value instanceof Integer) {
        array.pushInt((Integer)value);
      } else if (value instanceof Double) {
        array.pushDouble((Double)value);
      } else if (value instanceof String) {
        array.pushString((String)value);
      } else {
        array.pushString(value.toString());
      }
    }
    return array;
  }
}
