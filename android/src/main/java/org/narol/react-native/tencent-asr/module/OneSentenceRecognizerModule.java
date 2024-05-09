// @see SDK doc: https://cloud.tencent.com/document/product/1093/35723
package org.narol.reactnative.tencentasr.module;
import android.app.Activity;
import android.util.Log;
import androidx.annotation.NonNull;
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
import java.io.File;
import java.io.FileInputStream;
import org.json.JSONObject;
import org.narol.reactnative.tencentasrutil.ConfigParameterUtils;
import org.narol.reactnative.tencentasrutil.ErrorTypes;
import org.narol.reactnative.tencentasrutil.ReactNativeJsonUtils;

// 本地模块错误
class OneSentenceRecognizerModuleErrorTypes extends ErrorTypes {
  // 调用内置录音器失败
  public static final String RECOGNIZE_WITH_RECORDER_FAILED =
      "RECOGNIZE_WITH_RECORDER_FAILED";
  // 调用recognizeWithUrl失败
  public static final String RECOGNIZE_WITH_URL_FAILED =
      "RECOGNIZE_WITH_URL_FAILED";
  // 调用recognizeWithParams失败
  public static final String RECOGNIZE_WITH_PARAMS_FAILED =
      "RECOGNIZE_WITH_PARAMS_FAILED";
}

public class OneSentenceRecognizerModule extends ReactContextBaseJavaModule
    implements QCloudOneSentenceRecognizerListener {
  public static final String ModuleName = "OneSentenceRecognizerModule";
  private String _appId;
  private String _secretId;
  private String _secretKey;
  private String _token;
  private Boolean _isRecording = false;
  private QCloudOneSentenceRecognizer _recognizer;
  private final ReactContext _reactContext;
  private QCloudOneSentenceRecognitionParams _requestParams;

  public OneSentenceRecognizerModule(ReactApplicationContext reactContext) {
    super(reactContext);
    _reactContext = reactContext;
  }

  private void sendEvent(ReactContext reactContext, String eventName,
                         WritableMap params) {
    reactContext
        .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
        .emit(ModuleName + "." + eventName, params);
  }
  @ReactMethod
  public void addListener(String eventName) {}

  @ReactMethod
  public void removeListeners(Integer count) {}

  // 模块名称
  @Override
  @NonNull
  public String getName() {
    return ModuleName;
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

  // 统一处理错误事件
  private void sendErrorEvent(String errorCode, String errorMessage) {
    WritableMap errorMap = Arguments.createMap();
    errorMap.putString("code", errorCode);
    errorMap.putString("message", errorMessage);
    Log.i(ModuleName, "errorCoe: " + errorCode + "errorMsg: " + errorMessage);
    sendEvent(_reactContext, "onError", errorMap);
  }

  @ReactMethod
  public void configure(final ReadableMap configParams) {
    Log.i(ModuleName,
          "调用configure方法, 调用参数: " + configParams.toString());

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

  // 通过语音url进行一句话识别的快捷入口
  @ReactMethod
  public void recognizeWithUrl(ReadableMap configParams) {
    Log.i(ModuleName,
          "调用recognizeWithUrl方法, 调用参数: " + configParams.toString());
    try {
      String url = configParams.getString("url");
      _requestParams.setSourceType(
          QCloudSourceType.QCloudSourceTypeUrl); // 调用方式:URL
      _requestParams.setUrl(
          url); // 设置音频文件的URL下载地址(请替换为您自己的地址)
      initializeRecognizer();
      _recognizer.recognize(_requestParams);
    } catch (Exception e) {
      sendErrorEvent(
          OneSentenceRecognizerModuleErrorTypes.RECOGNIZE_WITH_URL_FAILED,
          e.getMessage());
    }
  }

  @ReactMethod
  public void recognizeWithParams(ReadableMap configParams) {
    Log.i(ModuleName,
          "调用recognizeWithParams方法, 调用参数: " + configParams.toString());
    String audioFilePath = configParams.getString("audioFilePath");

    if (audioFilePath == null) {
      sendErrorEvent(OneSentenceRecognizerModuleErrorTypes.PARAMETER_MISSING,
                     "audioFilePath参数缺失");
      return;
    }

    File audioFile = new File(audioFilePath);
    if (!audioFile.exists()) {
      sendErrorEvent(OneSentenceRecognizerModuleErrorTypes.FILE_DOES_NOT_EXIST,
                     "音频文件不存在");
      return;
    }

    try {
      FileInputStream fs = new FileInputStream(audioFile);
      byte[] audioData = new byte[fs.available()];
      int _ignored = fs.read(audioData);
      _requestParams.setSourceType(QCloudSourceType.QCloudSourceTypeData);
      _requestParams.setData(audioData);
      try {
        initializeRecognizer();
        _recognizer.recognize(_requestParams);
      } catch (Exception e) {
        sendErrorEvent(
            OneSentenceRecognizerModuleErrorTypes.RECOGNIZE_WITH_PARAMS_FAILED,
            e.getMessage());
      }
    } catch (Exception e) {
      sendErrorEvent(OneSentenceRecognizerModuleErrorTypes.FILE_READ_FAILED,
                     "读取音频文件失败");
    }
  }

  // 调用该方法前, 确认已经授权录音权限,
  // 在Virtual Devices下, 确认Microphone设置开启
  @ReactMethod
  public void startRecognizeWithRecorder() {
    Log.i(ModuleName, "调用startRecognizeWithRecorder方法");
    if (_isRecording) {
      return;
    }
    try {
      initializeRecognizer();
      _recognizer.setDefaultParams(
          _requestParams.getFilterDirty(), _requestParams.getFilterModal(),
          _requestParams.getFilterPunc(), _requestParams.getConvertNumMode(),
          _requestParams.getHotwordId(), _requestParams.getEngSerViceType());
      _recognizer.recognizeWithRecorder();
    } catch (Exception e) {
      sendErrorEvent(
          OneSentenceRecognizerModuleErrorTypes.RECOGNIZE_WITH_RECORDER_FAILED,
          e.getMessage());
    } finally {
      _isRecording = true;
    }
  }

  @ReactMethod
  public void stopRecognizeWithRecorder() {
    Log.i(ModuleName, "调用stopRecognizeWithRecorder方法");
    _isRecording = false;
    _recognizer.stopRecognizeWithRecorder();
  }

  // 识别结果回调
  public void recognizeResult(QCloudOneSentenceRecognizer recognizer,
                              String result, Exception exception) {
    Log.i(ModuleName, "识别结果回调");
    if (exception == null) {
      try {
        JSONObject resultJson = new JSONObject(result);
        JSONObject response = resultJson.getJSONObject("Response");
        if (response.has("Error")) {
          JSONObject errorObject = response.getJSONObject("Error");
          sendErrorEvent(errorObject.getString("Code"),
                         errorObject.getString("Message"));
        } else {
          sendEvent(_reactContext, "onRecognize",
                    ReactNativeJsonUtils.convertJsonToMap(response));
        }
      } catch (Exception e) {
        sendErrorEvent(OneSentenceRecognizerModuleErrorTypes.RECOGNIZE_FAILED,
                       e.getMessage());
      }

    } else {
      sendErrorEvent(OneSentenceRecognizerModuleErrorTypes.RECOGNIZE_FAILED,
                     exception.getMessage());
    }
  }

  // 开始录音回调
  public void didStartRecord() {
    Log.i(ModuleName, "开始录音回调");
    sendEvent(_reactContext, "onStartRecord", Arguments.createMap());
  }

  // 结束录音回调
  public void didStopRecord() {
    // TODO: Android这里有没有文件？
    Log.i(ModuleName, "结束录音回调");
    sendEvent(_reactContext, "onStopRecord", Arguments.createMap());
  }

  // 音量更新回调
  public void didUpdateVolume(int volumn) {
    Log.i(ModuleName, "音量更新回调");
    WritableMap resultBody = Arguments.createMap();
    resultBody.putInt("volumn", volumn);
    sendEvent(_reactContext, "onUpdateVolume", resultBody);
  }
}
