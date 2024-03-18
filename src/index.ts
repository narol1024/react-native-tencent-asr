import { NativeModules, Platform } from 'react-native';

const LINKING_ERROR =
  `The package 'react-native-tencent-asr' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

const TencentAsr = NativeModules.TencentAsr
  ? NativeModules.TencentAsr
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

interface AsrConfig {
  appId: string;
  secretId: string;
  secretKey: string;
}

interface RecognizeFileOptions {
  filePath: string;
}

export function initAsr(config: AsrConfig) {
  const { appId, secretId, secretKey } = config || {};
  if (!appId || !secretId || !secretKey) {
    throw new Error('Parameter is missing.');
  }
  TencentAsr.init(appId, secretId, secretKey);
}

export function recognizeFile(options: RecognizeFileOptions): Promise<string> {
  const { filePath } = options;
  return TencentAsr.recognizeFile(filePath);
}
