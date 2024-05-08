import { NativeModules, NativeEventEmitter } from 'react-native';
import type {
  CommonParams,
  RecognizerError,
  RealTimeRecognizeParams,
} from './types';
import type { EmitterSubscription } from 'react-native';
import { keysToCamelCase } from './util';

const nativeEventEmitter = new NativeEventEmitter(
  NativeModules.RealTimeRecognizerModule
);

function addListener(
  // 每个语音包分片识别结果
  eventName: 'onSliceSuccessRecognize',
  eventCallback: (result: {
    code: number;
    message: string;
    recognizedText: string;
    text: string;
    voiceId: string;
  }) => void
): EmitterSubscription;
// 语音流的识别结果,一次识别中可以包括多句话，这里持续返回的每句话的识别结果
function addListener(
  eventName: 'onSegmentSuccessRecognize',
  eventCallback: (result: {
    code: number;
    message: string;
    recognizedText: string;
    text: string;
    voiceId: string;
  }) => void
): EmitterSubscription;
// 一次识别任务最终的结果
function addListener(
  eventName: 'onSuccessRecognize',
  eventCallback: (result: { recognizedText: string }) => void
): EmitterSubscription;
// 一次识别任务失败回调
function addListener(
  eventName: 'onErrorRecognize',
  eventCallback: (result: RecognizerError) => void
): EmitterSubscription;
// 开始录音回调
function addListener(
  eventName: 'onStartRecord',
  eventCallback: (result: { error: RecognizerError }) => void
): EmitterSubscription;
// 结束录音回调
function addListener(
  eventName: 'onStopRecord',
  eventCallback: () => void
): EmitterSubscription;
// 录音音量(单位为分贝)实时回调,此回调计算音量的分贝值
function addListener(
  eventName: 'onUpdateVolume',
  eventCallback: (result: { volume: number }) => void
): EmitterSubscription;
// 录音停止后回调一次，再次开始录音会清空上一次保存的文件。
function addListener(
  eventName: 'onSaveAudioDataAsFile',
  eventCallback: (result: { audioFilePath: string }) => void
): EmitterSubscription;
// 各种处理错误事件
function addListener(
  eventName: 'onError',
  eventCallback: (err: { code: string; message: string }) => void
): EmitterSubscription;
// 其它事件
function addListener(
  eventName: string,
  eventCallback: (result: any) => void
): EmitterSubscription {
  return nativeEventEmitter.addListener(
    'RealTimeRecognizerModule.' + eventName,
    (result: any) => {
      return eventCallback(keysToCamelCase(result));
    }
  );
}

// 实时语音模块
export const RealTimeRecognizerModule = {
  // 配置识别的各种参数
  configure(params: CommonParams & RealTimeRecognizeParams): void {
    NativeModules.RealTimeRecognizerModule.configure(params);
  },
  // 开始录音
  startRealTimeRecognizer() {
    NativeModules.RealTimeRecognizerModule.startRealTimeRecognizer();
  },
  // 结束录音并开始识别
  stopRealTimeRecognizer() {
    NativeModules.RealTimeRecognizerModule.stopRealTimeRecognizer();
  },
  // 注册各种回调事件
  addListener,
  // 移除事件
  removeAllListeners(
    eventName:
      | 'onSliceRecognize'
      | 'onSegmentSuccessRecognize'
      | 'onErrorRecognize'
      | 'onStartRecord'
      | 'onStopRecord'
      | 'onUpdateVolume'
      | 'onSaveAudioDataAsFile'
      | 'onError'
  ) {
    return nativeEventEmitter.removeAllListeners(
      'RealTimeRecognizerModule.' + eventName
    );
  },
};
