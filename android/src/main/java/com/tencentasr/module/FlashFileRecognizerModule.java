// @see SDK doc: https://cloud.tencent.com/document/product/1093/35723

package com.tencentasr.module;
import android.util.Log;
import androidx.annotation.NonNull;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.tencent.cloud.qcloudasrsdk.filerecognize.QCloudFlashRecognizer;
import com.tencent.cloud.qcloudasrsdk.filerecognize.QCloudFlashRecognizerListener;
import com.tencent.cloud.qcloudasrsdk.filerecognize.param.QCloudFlashRecognitionParams;
import com.tencentasr.util.ConfigParameterUtils;
import java.io.File;
import java.io.FileInputStream;

public class FlashFileRecognizerModule extends ReactContextBaseJavaModule
    implements QCloudFlashRecognizerListener {
  public static final String NAME = "FlashFileRecognizerModule";

  private String _appId;
  private String _secretId;
  private String _secretKey;
  private String _token;
  private Promise _promise;
  private QCloudFlashRecognitionParams _requestParams;
  private QCloudFlashRecognizer _recognizer;

  public FlashFileRecognizerModule(ReactApplicationContext reactContext) {
    super(reactContext);
  }

  @Override
  @NonNull
  public String getName() {
    return NAME;
  }

  // 初始化Recognizer
  private void initializeRecognizer() {
    _recognizer =
        _token != null
            ? new QCloudFlashRecognizer(_appId, _secretId, _secretKey, _token)
            : new QCloudFlashRecognizer(_appId, _secretId, _secretKey);
    _recognizer.setCallback(this);
  }

  @ReactMethod
  public void configure(final ReadableMap configParams) {
    Log.d("录音文件识别极速版", "配置AppID、SecretID、SecretKey, Token参数: " +
                                    configParams.toString());

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
    String audioFilePath = configParams.getString("audioFilePath");

    if (audioFilePath == null) {
      promise.reject(new RuntimeException("audioFilePath参数缺失"));
      return;
    }

    File audioFile = new File(audioFilePath);
    if (!audioFile.exists()) {
      promise.reject(new RuntimeException("音频文件不存在"));
      return;
    }
    try {
      FileInputStream fs = new FileInputStream(audioFile);
      byte[] audioData = new byte[fs.available()];
      fs.read(audioData);

      _promise = promise;
      initializeRecognizer();
      _requestParams.setData(audioData);
      _recognizer.recognize(_requestParams);
    } catch (Exception e) {
      promise.reject("IOException",
                     "An error occurred while reading the audio file.", e);
    }
  }

  // 录音文件识别结果回调 ，详见api文档
  // @see https://cloud.tencent.com/document/product/1093/52097
  public void recognizeResult(QCloudFlashRecognizer recognizer, String result,
                              Exception exception) {
    if (exception == null) {
      // TODO: 应该使用WritableMap
      _promise.resolve(result);
    } else {
      _promise.reject(exception.getLocalizedMessage(), "Recognition failed");
    }
  }
}
