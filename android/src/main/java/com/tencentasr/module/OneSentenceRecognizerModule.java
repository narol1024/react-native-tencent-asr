// @see SDK doc: https://cloud.tencent.com/document/product/1093/35723

package com.tencentasr.module;
import android.Manifest;
import android.app.Activity;
import android.os.Build;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.tencent.cloud.qcloudasrsdk.onesentence.QCloudOneSentenceRecognizer;
import com.tencent.cloud.qcloudasrsdk.onesentence.QCloudOneSentenceRecognizerAudioPathListener;
import com.tencent.cloud.qcloudasrsdk.onesentence.QCloudOneSentenceRecognizerListener;
import com.tencent.cloud.qcloudasrsdk.onesentence.common.QCloudAudioFrequence;
import com.tencent.cloud.qcloudasrsdk.onesentence.common.QCloudSourceType;
import com.tencent.cloud.qcloudasrsdk.onesentence.network.QCloudOneSentenceRecognitionParams;
import com.tencentasr.util.ConfigParameterUtils;
import java.io.File;
import java.io.FileInputStream;
import java.util.LinkedList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

public class OneSentenceRecognizerModule extends ReactContextBaseJavaModule
    implements QCloudOneSentenceRecognizerListener {
  public static final String NAME = "OneSentenceRecognizerModule";
  private String _appId;
  private String _secretId;
  private String _secretKey;
  private String _token;
  private QCloudOneSentenceRecognizer _recognizer;
  private ReactContext _reactContext;
  private boolean _isRecording = false;
  private QCloudOneSentenceRecognitionParams _requestParams;

  public OneSentenceRecognizerModule(ReactApplicationContext reactContext) {
    super(reactContext);
    _reactContext = reactContext;
  }

  private void sendEvent(ReactContext reactContext, String eventName,
                         WritableMap params) {
    reactContext
        .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
        .emit(eventName, params);
  }
  @ReactMethod
  public void addListener(String eventName) {}

  @ReactMethod
  public void removeListeners(Integer count) {}

  @Override
  @NonNull
  public String getName() {
    return NAME;
  }

  // 初始化Recognizer
  private void initializeRecognizer() {
    Activity currentActivity = getCurrentActivity();

    _recognizer =
        _token != null
            ? new QCloudOneSentenceRecognizer(currentActivity, _appId,
                                              _secretId, _secretKey, _token)
            : new QCloudOneSentenceRecognizer(currentActivity, _appId,
                                              _secretId, _secretKey);
    _recognizer.setCallback(this);
  }

  @ReactMethod
  public void configure(final ReadableMap configParams) {
    Log.d("一句话识别模块", "配置AppID、SecretID、SecretKey, Token参数: " +
                                configParams.toString());

    _appId = configParams.getString("appId");
    _secretId = configParams.getString("secretId");
    _secretKey = configParams.getString("secretKey");
    _token = configParams.getString("token");

    _requestParams =
        (QCloudOneSentenceRecognitionParams)
            QCloudOneSentenceRecognitionParams.defaultRequestParams();
    _requestParams.setVoiceFormat(ConfigParameterUtils.getStringOrDefault(
        configParams, "voiceFormat", "aac"));
    _requestParams.setFilterDirty(
        ConfigParameterUtils.getIntOrDefault(configParams, "filterDirty", 0));
    _requestParams.setFilterModal(
        ConfigParameterUtils.getIntOrDefault(configParams, "filterModal", 0));
    _requestParams.setFilterPunc(
        ConfigParameterUtils.getIntOrDefault(configParams, "filterPunc", 0));
    _requestParams.setConvertNumMode(ConfigParameterUtils.getIntOrDefault(
        configParams, "convertNumMode", 1));
    _requestParams.setEngSerViceType(ConfigParameterUtils.getStringOrDefault(
        configParams, "engineModelType", "16k_zh"));
  }

  @ReactMethod
  public void recognizeWithUrl(ReadableMap configParams) {
    Log.d("一句话识别模块:", configParams.toString());
    try {
      String url = configParams.getString("url");
      _requestParams.setSourceType(
          QCloudSourceType.QCloudSourceTypeUrl); // 调用方式:URL
      _requestParams.setUrl(
          url); // 设置音频文件的URL下载地址(请替换为您自己的地址)
      initializeRecognizer();
      _recognizer.recognize(_requestParams);
    } catch (Exception e) {
      e.printStackTrace();
      System.out.println("exception msg" + e.getMessage());
    }
  }

  @ReactMethod
  public void recognizeWithParams(ReadableMap configParams) {
    Log.d("一句话识别模块, 完整参数:", configParams.toString());
    String audioFilePath = configParams.getString("audioFilePath");

    if (audioFilePath == null) {
      System.out.println("audioFilePath参数缺失");
      return;
    }

    File audioFile = new File(audioFilePath);
    if (!audioFile.exists()) {
      System.out.println("音频文件不存在");
      return;
    }

    try {
      FileInputStream fs = new FileInputStream(audioFile);
      byte[] audioData = new byte[fs.available()];
      fs.read(audioData);
      _requestParams.setSourceType(QCloudSourceType.QCloudSourceTypeData);
      _requestParams.setData(audioData);
      initializeRecognizer();
      _recognizer.recognize(_requestParams);
    } catch (Exception e) {
      System.out.println("An error occurred while reading the audio file.");
    }
  }

  @ReactMethod
  public void recognizeWithRecorder(ReadableMap configParams) {
    try {
      Log.d("一句话识别模块", "recognizeWithRecorder");
      initializeRecognizer();
      _recognizer.recognizeWithRecorder();
    } catch (Exception e) {
      e.printStackTrace();
      System.out.println("exception msg" + e.getMessage());
    }
  }

  @ReactMethod
  public void stopRecognizeWithRecorder() {
    try {
      _recognizer.stopRecognizeWithRecorder();
    } catch (Exception e) {
      e.printStackTrace();
      System.out.println("exception msg" + e.getMessage());
    }
  }

  // 一句话识别结果回调
  public void recognizeResult(QCloudOneSentenceRecognizer recognizer,
                              String result, Exception exception) {

    try {
      if (exception == null) {
        JSONObject jsonObject = new JSONObject(result);
        JSONObject response = jsonObject.getJSONObject("Response");
        WritableMap resultBody = Arguments.createMap();
        if (response.has("Error")) {
          WritableMap errorMap = Arguments.createMap();
          JSONObject errorObject = response.getJSONObject("Error");
          errorMap.putString("code", errorObject.getString("Code"));
          errorMap.putString("message", errorObject.getString("Message"));
          resultBody.putMap("error", errorMap);
        } else {
          resultBody.putString("data", response.toString());
        }
        sendEvent(_reactContext, "DidRecognize", resultBody);
      } else {
        WritableMap errorMap = Arguments.createMap();
        WritableMap resultBody = Arguments.createMap();
        errorMap.putString("message", exception.getMessage());
        resultBody.putMap("error", errorMap);
        sendEvent(_reactContext, "DidRecognize", resultBody);
      }

    } catch (Exception e) {
      e.printStackTrace();
      System.out.println("exception msg" + e.getMessage());
    }
  }

  public void didStartRecord() { Log.d("一句话识别模块", "didStartRecord"); }
  public void didStopRecord() { Log.d("一句话识别模块", "didStopRecord"); }
}
