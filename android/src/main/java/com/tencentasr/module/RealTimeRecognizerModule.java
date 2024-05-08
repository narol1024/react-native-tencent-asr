// @see SDK doc: https://cloud.tencent.com/document/product/1093/35723
package com.tencentasr.module;
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
import com.tencent.aai.AAIClient;
import com.tencent.aai.audio.data.AudioRecordDataSource;
import com.tencent.aai.audio.data.PcmAudioDataSource;
import com.tencent.aai.audio.utils.WavCache;
import com.tencent.aai.auth.LocalCredentialProvider;
import com.tencent.aai.config.ClientConfiguration;
import com.tencent.aai.exception.ClientException;
import com.tencent.aai.exception.ServerException;
import com.tencent.aai.listener.AudioRecognizeResultListener;
import com.tencent.aai.listener.AudioRecognizeStateListener;
import com.tencent.aai.log.AAILogger;
import com.tencent.aai.log.LoggerListener;
import com.tencent.aai.model.AudioRecognizeConfiguration;
import com.tencent.aai.model.AudioRecognizeRequest;
import com.tencent.aai.model.AudioRecognizeResult;
import com.tencentasr.util.ConfigParameterUtils;
import com.tencentasr.util.ErrorTypes;
import com.tencentasr.util.ReactNativeJsonUtils;
import org.json.JSONObject;

// 本地模块错误
class RealTimeRecognizerModuleErrorTypes extends ErrorTypes {
  // 语音流的语音识别结果回调接口失败
  public static final String HANDLE_RESULT_JSON_ERROR =
      "HANDLE_RESULT_JSON_ERROR";
}

public class RealTimeRecognizerModule extends ReactContextBaseJavaModule {
  public static final String ModuleName = "RealTimeRecognizerModule";
  private int _appId;
  private String _secretId;
  private String _secretKey;
  // 不设置默认使用0，说明：项目功能用于按项目管理云资源，可以对云资源进行分项目管理，详情见
  // https://console.cloud.tencent.com/project
  private int _projectId = 0;
  private String _token;
  private AAIClient _aaiClient;
  private Boolean _isRecording = false;
  private final ReactContext _reactContext;
  private AudioRecognizeRequest _audioRecognizeRequest;
  private AudioRecognizeResultListener _audioRecognizeResultlistener;
  private AudioRecognizeStateListener _audioRecognizeStateListener;
  private AudioRecognizeConfiguration _audioRecognizeConfiguration;

  public RealTimeRecognizerModule(ReactApplicationContext reactContext) {
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

  // 初始化recognizer
  private void initializeRecognizer() {
    Activity currentActivity = getCurrentActivity();
    // 初始化一个aaiclient并鉴权
    _aaiClient =
        _token != null
            ? new AAIClient(currentActivity, _appId, _projectId, _secretId,
                            _secretKey, new LocalCredentialProvider(_token))
            : new AAIClient(currentActivity, _appId, _projectId, _secretId,
                            new LocalCredentialProvider(_secretKey));
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

    _appId = Integer.parseInt(
        ConfigParameterUtils.getStringOrDefault(configParams, "appId", ""));
    _secretId = configParams.getString("secretId");
    _secretKey = configParams.getString("secretKey");
    _projectId =
        ConfigParameterUtils.getIntOrDefault(configParams, "projectId", 0);
    _token = configParams.getString("token");

    // 初始化语音识别请求
    AudioRecognizeRequest.Builder builder = new AudioRecognizeRequest.Builder();
    _audioRecognizeRequest =
        builder.pcmAudioDataSource(new AudioRecordDataSource(false))
            .setEngineModelType(ConfigParameterUtils.getStringOrDefault(
                configParams, "engineModelType", "16k_zh"))
            .setFilterDirty(ConfigParameterUtils.getIntOrDefault(
                configParams, "filterDirty", 0))
            .setFilterModal(ConfigParameterUtils.getIntOrDefault(
                configParams, "filterModal", 0))
            .setFilterPunc(ConfigParameterUtils.getIntOrDefault(
                configParams, "filterPunc", 0))
            .setConvert_num_mode(ConfigParameterUtils.getIntOrDefault(
                configParams, "convertNumMode", 1))
            .setNeedvad(ConfigParameterUtils.getIntOrDefault(configParams,
                                                             "needvad", 1))
            .build();
    // 3、初始化语音识别结果监听器。
    _audioRecognizeResultlistener = new AudioRecognizeResultListener() {
      // 语音分片的识别（非稳态结果，会持续修正）
      public void onSliceSuccess(AudioRecognizeRequest request,
                                 AudioRecognizeResult result, int seq) {
        try {
          JSONObject resultJson = new JSONObject(result.getResultJson());
          JSONObject innerResultJson = resultJson.getJSONObject("result");
          WritableMap resultBody = Arguments.createMap();

          resultBody.putInt("code", resultJson.getInt("code"));
          resultBody.putString("message", resultJson.getString("message"));
          resultBody.putString("text",
                               innerResultJson.getString("voice_text_str"));
          resultBody.putString("voiceId", resultJson.getString("voice_id"));

          sendEvent(_reactContext, "onSliceSuccessRecognize", resultBody);
        } catch (Exception e) {
          sendErrorEvent(
              RealTimeRecognizerModuleErrorTypes.HANDLE_RESULT_JSON_ERROR,
              e.getMessage());
        }
      }
      // 持续返回的每句话的识别结果
      public void onSegmentSuccess(AudioRecognizeRequest request,
                                   AudioRecognizeResult result, int seq) {
        try {
          JSONObject resultJson = new JSONObject(result.getResultJson());
          JSONObject innerResultJson = resultJson.getJSONObject("result");
          WritableMap resultBody = Arguments.createMap();

          resultBody.putInt("code", resultJson.getInt("code"));
          resultBody.putString("message", resultJson.getString("message"));
          resultBody.putString("text",
                               innerResultJson.getString("voice_text_str"));
          resultBody.putString("voiceId", resultJson.getString("voice_id"));

          sendEvent(_reactContext, "onSegmentSuccessRecognize", resultBody);
        } catch (Exception e) {
          sendErrorEvent(
              RealTimeRecognizerModuleErrorTypes.HANDLE_RESULT_JSON_ERROR,
              e.getMessage());
        }
      }
      // 识别结束回调，返回所有的识别结果
      public void onSuccess(AudioRecognizeRequest request, String result) {
        try {
          WritableMap resultBody = Arguments.createMap();
          resultBody.putString("text", result);
          sendEvent(_reactContext, "onSuccessRecognize", resultBody);
        } catch (Exception e) {
          sendErrorEvent(
              RealTimeRecognizerModuleErrorTypes.HANDLE_RESULT_JSON_ERROR,
              e.getMessage());
        }
      }
      // 识别任务失败回调
      public void onFailure(
          AudioRecognizeRequest request, final ClientException clientException,
          final ServerException serverException, String response) {
        if (clientException != null) {
          WritableMap errorMap = Arguments.createMap();
          // TODO: 这里的clientException怎么取到code和message
          errorMap.putString("code", "");
          errorMap.putString("message", "");
          sendEvent(_reactContext, "onErrorRecognize", errorMap);
        } else if (serverException != null) {
          try {
            JSONObject responseJson = new JSONObject(response);
            sendEvent(_reactContext, "onErrorRecognize",
                      ReactNativeJsonUtils.convertJsonToMap(responseJson));
          } catch (Exception e) {
            sendErrorEvent(
                RealTimeRecognizerModuleErrorTypes.HANDLE_RESULT_JSON_ERROR,
                e.getMessage());
          }
        }
      }
    };
    // 初始化语音识别的状态监听器。
    _audioRecognizeStateListener = new AudioRecognizeStateListener() {
      // 开始录音
      public void onStartRecord(AudioRecognizeRequest audioRecognizeRequest) {
        sendEvent(_reactContext, "onStartRecord", null);
      }
      // 结束录音
      public void onStopRecord(AudioRecognizeRequest audioRecognizeRequest) {
        sendEvent(_reactContext, "onStopRecord", null);
      }
      // 音量回调（废弃, 请使用onVoiceDb）
      public void onVoiceVolume(AudioRecognizeRequest audioRecognizeRequest,
                                int volume) {}
      // 音量回调
      public void onVoiceDb(float volume) {
        WritableMap resultBody = Arguments.createMap();
        resultBody.putDouble("volume", volume);
        sendEvent(_reactContext, "onUpdateVolume", resultBody);
      }
      // 返回音频流
      public void onNextAudioData(final short[] audioDatas,
                                  final int readBufferLength) {
        // TODO: 确认安卓是否需要这个事件
      }
      // 静音检测超时回调
      public void onSilentDetectTimeOut() {
        sendEvent(_reactContext, "onSilentDetectTimeOut", null);
      }
    };

    // 自定义识别配置
    _audioRecognizeConfiguration = new AudioRecognizeConfiguration.Builder()
                                       .setSilentDetectTimeOut(false)
                                       .audioFlowSilenceTimeOut(5000)
                                       .minVolumeCallbackTime(80)
                                       .build();
    initializeRecognizer();
  }

  // 启动语音识别
  @ReactMethod
  public void startRealTimeRecognizer() {
    Log.i(ModuleName, "调用startRealTimeRecognizer方法");
    if (_isRecording) {
      return;
    }
    new Thread(new Runnable() {
      @Override
      public void run() {
        if (_aaiClient != null) {
          _isRecording = true;
          _aaiClient.startAudioRecognize(
              _audioRecognizeRequest, _audioRecognizeResultlistener,
              _audioRecognizeStateListener, _audioRecognizeConfiguration);
        }
      }
    }).start();
  }

  // 停止语音识别，等待最终识别结果
  @ReactMethod
  public void stopRealTimeRecognizer() {
    Log.i(ModuleName, "调用stopRealTimeRecognizer方法");
    new Thread(new Runnable() {
      public void run() {
        if (_aaiClient != null) {
          _isRecording = false;
          _aaiClient.stopAudioRecognize();
        }
      }
    }).start();
  }
}
