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

  private Promise _promise;
  private QCloudFlashRecognizer _recognizer;

  public FlashFileRecognizerModule(ReactApplicationContext reactContext) {
    super(reactContext);
  }

  @Override
  @NonNull
  public String getName() {
    return NAME;
  }

  @ReactMethod
  public void configure(final ReadableMap configParams) {
    Log.d("录音文件识别极速版", "配置AppID、SecretID、SecretKey, Token参数: " +
                                    configParams.toString());

    String appId = configParams.getString("appId");
    String secretId = configParams.getString("secretId");
    String secretKey = configParams.getString("secretKey");
    String token = configParams.getString("token");

    _recognizer =
        token != null
            ? new QCloudFlashRecognizer(appId, secretId, secretKey, token)
            : new QCloudFlashRecognizer(appId, secretId, secretKey);

    _recognizer.setCallback(this);
  }

  @ReactMethod
  public void flashFileRecognizer(ReadableMap configParams, Promise promise) {
    String audioFilePath = configParams.getString("filePath");

    if (audioFilePath == null) {
      promise.reject(new RuntimeException("Missing filePath parameter."));
      return;
    }
    _promise = promise;

    File audioFile = new File(audioFilePath);
    if (!audioFile.exists()) {
      promise.reject(new RuntimeException("Audio file does not exist."));
      return;
    }
    try {
      FileInputStream fs = new FileInputStream(audioFile);
      byte[] audioData = new byte[fs.available()];
      fs.read(audioData);
      QCloudFlashRecognitionParams params =
          (QCloudFlashRecognitionParams)
              QCloudFlashRecognitionParams.defaultRequestParams();
      // 支持传音频文件数据或者音频文件路径，如果同时调用setData和setPath，sdk内将忽略setPath的值
      params.setData(audioData);
      params.setVoiceFormat(ConfigParameterUtils.getStringOrDefault(
          configParams, "voiceFormat", "aac"));
      params.setEngineModelType(ConfigParameterUtils.getStringOrDefault(
          configParams, "engineModelType", "16k_zh"));
      params.setFilterDirty(
          ConfigParameterUtils.getIntOrDefault(configParams, "filterDirty", 0));
      params.setFilterModal(
          ConfigParameterUtils.getIntOrDefault(configParams, "filterModal", 0));
      params.setFilterPunc(
          ConfigParameterUtils.getIntOrDefault(configParams, "filterPunc", 0));
      params.setConvertNumMode(ConfigParameterUtils.getIntOrDefault(
          configParams, "convertNumMode", 1));
      params.setSpeakerDiarization(ConfigParameterUtils.getIntOrDefault(
          configParams, "speakerDiarization", 0));
      params.setFirstChannelOnly(ConfigParameterUtils.getIntOrDefault(
          configParams, "firstChannelOnly", 1));
      params.setWordInfo(
          ConfigParameterUtils.getIntOrDefault(configParams, "wordInfo", 0));
      params.setCustomizationID(ConfigParameterUtils.getStringOrDefault(
          configParams, "customizationID", ""));
      params.setHotwordID(ConfigParameterUtils.getStringOrDefault(
          configParams, "hotwordID", ""));

      _recognizer.recognize(params);
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
