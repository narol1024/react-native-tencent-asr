import { NativeModules, NativeEventEmitter } from 'react-native';
import type { EmitterSubscription } from 'react-native';
import type {
  CommonParams,
  FlashFileRecognizerParams,
  FlashFileRecognizerResult,
} from './types';
import { keysToCamelCase } from './util';

const nativeModulesEmitter = new NativeEventEmitter(
  NativeModules.FlashFileRecognizerModule
);

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
  return nativeModulesEmitter.addListener(
    'FlashFileRecognizerModule.' + eventName,
    (result) => {
      return eventCallback(keysToCamelCase(result));
    }
  );
}

// 录音文件识别极速版模块
export const FlashFileRecognizerModule = {
  // 配置识别的各种参数
  configure(params: CommonParams & FlashFileRecognizerParams): void {
    NativeModules.FlashFileRecognizerModule.configure(params);
  },
  // 快速识别
  async recognize(params: {
    // 音频文件路径
    audioFilePath: string;
  }): Promise<FlashFileRecognizerResult> {
    try {
      const result =
        await NativeModules.FlashFileRecognizerModule.recognize(params);
      return keysToCamelCase(result);
    } catch (error) {
      return Promise.reject(error);
    }
  },
  // 注册各种回调事件
  addListener,
  // 移除事件
  removeAllListeners(eventName: 'onError') {
    return nativeModulesEmitter.removeAllListeners(
      'FlashFileRecognizerModule.' + eventName
    );
  },
};
