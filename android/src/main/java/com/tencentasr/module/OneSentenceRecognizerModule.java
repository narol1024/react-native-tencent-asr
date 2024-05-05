// @see SDK doc: https://cloud.tencent.com/document/product/1093/35723

package com.tencentasr.module;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
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
import org.json.JSONObject;
public class OneSentenceRecognizerModule extends ReactContextBaseJavaModule
    implements QCloudOneSentenceRecognizerListener {
  public static final String NAME = "OneSentenceRecognizerModule";
  private QCloudOneSentenceRecognizer _recognizer;
  private ReactContext _reactContext;
  private boolean _isRecording = false;
  private MediaRecorder _mediaRecorder;
  private QCloudOneSentenceRecognitionParams _customParams;

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

  @ReactMethod
  public void configure(final ReadableMap configParams) {
    Log.d("一句话识别模块", "配置AppID、SecretID、SecretKey, Token参数: " +
                                configParams.toString());

    String appId = configParams.getString("appId");
    String secretId = configParams.getString("secretId");
    String secretKey = configParams.getString("secretKey");
    String token = configParams.getString("token");

    _customParams =
        (QCloudOneSentenceRecognitionParams)
            QCloudOneSentenceRecognitionParams.defaultRequestParams();
    _customParams.setVoiceFormat(ConfigParameterUtils.getStringOrDefault(
        configParams, "voiceFormat", "aac"));
    _customParams.setFilterDirty(
        ConfigParameterUtils.getIntOrDefault(configParams, "filterDirty", 0));
    _customParams.setFilterModal(
        ConfigParameterUtils.getIntOrDefault(configParams, "filterModal", 0));
    _customParams.setFilterPunc(
        ConfigParameterUtils.getIntOrDefault(configParams, "filterPunc", 0));
    _customParams.setConvertNumMode(ConfigParameterUtils.getIntOrDefault(
        configParams, "convertNumMode", 1));
    _customParams.setEngSerViceType(ConfigParameterUtils.getStringOrDefault(
        configParams, "engineModelType", "16k_zh"));

    _recognizer =
        token != null
            ? new QCloudOneSentenceRecognizer(appId, secretId, secretKey, token)
            : new QCloudOneSentenceRecognizer(appId, secretId, secretKey);
    _recognizer.setCallback(this);
  }

  @ReactMethod
  public void recognizeWithUrl(ReadableMap configParams) {
    Log.d("一句话识别模块:", configParams.toString());
    String url = configParams.getString("url");

    try {
      _customParams.setSourceType(
          QCloudSourceType.QCloudSourceTypeUrl); // 调用方式:URL
      _customParams.setUrl(
          url); // 设置音频文件的URL下载地址(请替换为您自己的地址)
      _recognizer.recognize(_customParams);
    } catch (Exception e) {
      e.printStackTrace();
      System.out.println("exception msg" + e.getMessage());
    }
  }

  @ReactMethod
  public void recognizeWithParams(ReadableMap configParams) {
    Log.d("一句话识别模块, 完整参数:", configParams.toString());
    String audioFilePath = configParams.getString("filePath");

    if (audioFilePath == null) {
      System.out.println("Missing filePath parameter..");
      return;
    }

    File audioFile = new File(audioFilePath);
    if (!audioFile.exists()) {
      System.out.println("Audio file does not exist.");
      return;
    }

    try {
      FileInputStream fs = new FileInputStream(audioFile);
      byte[] audioData = new byte[fs.available()];
      fs.read(audioData);
      _customParams.setSourceType(QCloudSourceType.QCloudSourceTypeData);
      _customParams.setData(audioData);
      _recognizer.recognize(_customParams);
    } catch (Exception e) {
      System.out.println("An error occurred while reading the audio file.");
    }
  }

  @ReactMethod
  public void startRecognizeWithRecorder(ReadableMap configParams) {
    try {
      Log.d("一句话识别模块", "recognizeWithRecorder");
      // TODO: 内置录音器
      _recognizer.recognizeWithRecorder();
      _recognizer.setQCloudOneSentenceRecognizerAudioPathListener(
          new QCloudOneSentenceRecognizerAudioPathListener() {
            public void callBackAudioPath(String audioPath) {
              Log.d("一句话识别模块",
                    "callBackAudioPath: audioPath=" + audioPath);
            }
          });
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
      WritableMap params = Arguments.createMap();

      JSONObject jsonObject = new JSONObject(result);
      JSONObject response = jsonObject.getJSONObject("Response");

      params.putString("requestId", response.getString("RequestId"));
      params.putString("text", response.getString("Result"));
      params.putInt("audioDuration", response.getInt("AudioDuration"));
      params.putInt("wordSize", response.getInt("WordSize"));

      sendEvent(_reactContext, "DidRecognize", params);
    } catch (Exception e) {
      e.printStackTrace();
      System.out.println("exception msg" + e.getMessage());
    }
  }

  public void didStartRecord() { Log.d("一句话识别模块", "didStartRecord"); }

  public void didStopRecord() { Log.d("一句话识别模块", "didStopRecord"); }
}
