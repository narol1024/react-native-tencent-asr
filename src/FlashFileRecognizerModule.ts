import { NativeModules } from 'react-native';
import type {
  CommonParams,
  FlashFileRecognizerParams,
  FlashFileRecognizerResult,
} from './types';
import { keysToCamelCase, transformToJson } from './util';

// 录音文件识别极速版模块
export const FlashFileRecognizerModule = {
  // 配置识别的各种参数
  configure(params: CommonParams & FlashFileRecognizerParams): void {
    NativeModules.FlashFileRecognizerModule.configure(params);
  },
  // 快速识别
  async recognize(params: {
    // 音频文件路径
    filePath: string;
  }): Promise<FlashFileRecognizerResult> {
    try {
      const result =
        await NativeModules.FlashFileRecognizerModule.recognize(params);
      return keysToCamelCase(
        transformToJson(result)
      ) as unknown as FlashFileRecognizerResult;
    } catch (error) {
      return Promise.reject(error);
    }
  },
};
