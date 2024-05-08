// @see SDK doc: https://cloud.tencent.com/document/product/1093/35723

package com.tencentasr.module;
import android.util.Log;
import androidx.annotation.NonNull;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.tencent.cloud.qcloudasrsdk.filerecognize.QCloudFlashRecognizer;
import com.tencent.cloud.qcloudasrsdk.filerecognize.QCloudFlashRecognizerListener;
import com.tencent.cloud.qcloudasrsdk.filerecognize.param.QCloudFlashRecognitionParams;
import com.tencentasr.util.ConfigParameterUtils;
import com.tencentasr.util.ErrorTypes;
import com.tencentasr.util.ReactNativeJsonUtils;
import java.io.File;
import java.io.FileInputStream;
import org.json.JSONObject;

// 本地模块错误
class FlashFileRecognizerModuleErrorTypes extends ErrorTypes {}

public class FlashFileRecognizerModule extends ReactContextBaseJavaModule
    implements QCloudFlashRecognizerListener {
  public static final String ModuleName = "FlashFileRecognizerModule";

  private String _appId;
  private String _secretId;
  private String _secretKey;
  private String _token;
  private Promise _promise;
  private QCloudFlashRecognitionParams _requestParams;
  private ReactContext _reactContext;
  private QCloudFlashRecognizer _recognizer;

  public FlashFileRecognizerModule(ReactApplicationContext reactContext) {
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
    _recognizer =
        _token != null
            ? new QCloudFlashRecognizer(_appId, _secretId, _secretKey, _token)
            : new QCloudFlashRecognizer(_appId, _secretId, _secretKey);
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

    _requestParams = (QCloudFlashRecognitionParams)
                         QCloudFlashRecognitionParams.defaultRequestParams();
    // 支持传音频文件数据或者音频文件路径，如果同时调用setData和setPath，sdk内将忽略setPath的值
    _requestParams.setVoiceFormat(ConfigParameterUtils.getStringOrDefault(
        configParams, "voiceFormat", "aac"));
    _requestParams.setEngineModelType(ConfigParameterUtils.getStringOrDefault(
        configParams, "engineModelType", "16k_zh"));
    _requestParams.setFilterDirty(
        ConfigParameterUtils.getIntOrDefault(configParams, "filterDirty", 0));
    _requestParams.setFilterModal(
        ConfigParameterUtils.getIntOrDefault(configParams, "filterModal", 0));
    _requestParams.setFilterPunc(
        ConfigParameterUtils.getIntOrDefault(configParams, "filterPunc", 0));
    _requestParams.setConvertNumMode(ConfigParameterUtils.getIntOrDefault(
        configParams, "convertNumMode", 1));
    _requestParams.setSpeakerDiarization(ConfigParameterUtils.getIntOrDefault(
        configParams, "speakerDiarization", 0));
    _requestParams.setFirstChannelOnly(ConfigParameterUtils.getIntOrDefault(
        configParams, "firstChannelOnly", 1));
    _requestParams.setWordInfo(
        ConfigParameterUtils.getIntOrDefault(configParams, "wordInfo", 0));
    _requestParams.setCustomizationID(ConfigParameterUtils.getStringOrDefault(
        configParams, "customizationID", ""));
    _requestParams.setHotwordID(
        ConfigParameterUtils.getStringOrDefault(configParams, "hotwordID", ""));
  }

  @ReactMethod
  public void recognize(ReadableMap configParams, Promise promise) {
    Log.i(ModuleName,
          "调用recognize方法, 调用参数: " + configParams.toString());

    String audioFilePath = configParams.getString("audioFilePath");

    if (audioFilePath == null) {
      sendErrorEvent(FlashFileRecognizerModuleErrorTypes.PARAMETER_MISSING,
                     "audioFilePath参数缺失");
      promise.reject("audioFilePath参数缺失");
      return;
    }

    File audioFile = new File(audioFilePath);
    if (!audioFile.exists()) {
      sendErrorEvent(FlashFileRecognizerModuleErrorTypes.FILE_DOES_NOT_EXIST,
                     "音频文件不存在");
      promise.reject("音频文件不存在");
      return;
    }
    try {
      FileInputStream fs = new FileInputStream(audioFile);
      byte[] audioData = new byte[fs.available()];
      int _ignored = fs.read(audioData);
      try {
        _promise = promise;
        initializeRecognizer();
        _requestParams.setData(audioData);
        _recognizer.recognize(_requestParams);
      } catch (Exception e) {
        sendErrorEvent(FlashFileRecognizerModuleErrorTypes.RECOGNIZE_FAILED,
                       e.getMessage());
        promise.reject(e.getMessage());
      }
    } catch (Exception e) {
      sendErrorEvent(FlashFileRecognizerModuleErrorTypes.FILE_READ_FAILED,
                     "读取音频文件失败");
      promise.reject("读取音频文件失败", e);
    }
  }

  // 录音文件识别结果回调
  public void recognizeResult(QCloudFlashRecognizer recognizer, String result,
                              Exception exception) {
    if (exception == null) {
      try {
        JSONObject resultJson = new JSONObject(result);
        if (resultJson.getInt("code") == 0) {
          _promise.resolve(ReactNativeJsonUtils.convertJsonToMap(resultJson));
        } else {
          _promise.reject(resultJson.getString("message"));
        }
      } catch (Exception e) {
        sendErrorEvent(FlashFileRecognizerModuleErrorTypes.RECOGNIZE_FAILED,
                       e.getMessage());
      }

    } else {
      sendErrorEvent(FlashFileRecognizerModuleErrorTypes.RECOGNIZE_FAILED,
                     exception.getMessage());
      _promise.reject(exception.getMessage());
    }
  }
}
