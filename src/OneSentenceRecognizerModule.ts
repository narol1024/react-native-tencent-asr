import { NativeModules, NativeEventEmitter } from 'react-native';
import type { EmitterSubscription } from 'react-native';
import type {
  CommonParams,
  RecognizerError,
  OneSentenceRecognizerParams,
  RecognizeWithUrlParams,
  RecognizeWithParams,
} from './types';
import { keysToCamelCase } from './util';

const NativeModulesEmitter = new NativeEventEmitter(
  NativeModules.OneSentenceRecognizerModule
);

function addListener(
  // 识别回调
  eventName: 'didRecognize',
  eventCallback: (result: {
    audioDuration: number;
    requestId: string;
    result: string;
    wordList:
      | null
      | {
          startTime: number;
          endTime: number;
          word: string;
        }[];
    wordSize: number;
  }) => void
): EmitterSubscription;
// 开始录音回调
function addListener(
  eventName: 'didStartRecord',
  eventCallback: (result: { error: RecognizerError } | null) => void
): EmitterSubscription;
// 结束录音回调
function addListener(
  eventName: 'didEndRecord',
  eventCallback: (result: { audioFilePath: string }) => void
): EmitterSubscription;
// 录音音量实时回调
function addListener(
  eventName: 'didUpdateVolume',
  eventCallback: (result: { volume: number }) => void
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
  return NativeModulesEmitter.addListener(eventName, (result) => {
    return eventCallback(keysToCamelCase(result));
  });
}

// 一句话识别模块
export const OneSentenceRecognizerModule = {
  // 配置AppID、SecretID、SecretKey
  configure(params: CommonParams & OneSentenceRecognizerParams): void {
    NativeModules.OneSentenceRecognizerModule.configure(params);
  },
  // 一句话识别(快捷识别, 网络URL)
  recognizeWithUrl(params: RecognizeWithUrlParams) {
    NativeModules.OneSentenceRecognizerModule.recognizeWithUrl(params);
  },
  // 一句话识别(完整参数)
  recognizeWithParams(params: RecognizeWithParams) {
    NativeModules.OneSentenceRecognizerModule.recognizeWithParams(params);
  },
  // 一句话识别(内置录音器), 开始录音, 安卓确保先请求录音权限
  async recognizeWithRecorder(params: { engineModelType?: string } = {}) {
    NativeModules.OneSentenceRecognizerModule.recognizeWithRecorder(params);
  },
  //  一句话识别(内置录音器), 停止录音
  stopRecognizeWithRecorder() {
    NativeModules.OneSentenceRecognizerModule.stopRecognizeWithRecorder();
  },
  // 注册各种回调事件
  addListener,
  // 移除事件
  removeAllListeners(
    eventName:
      | 'didRecognize'
      | 'didStartRecord'
      | 'didEndRecord'
      | 'didUpdateVolume'
      | 'onError'
  ) {
    return NativeModulesEmitter.removeAllListeners(eventName);
  },
};
