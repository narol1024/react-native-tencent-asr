```
在调用内置录音器时，需要提前授权
```

# react-native-tencent-asr

[![npm version](https://img.shields.io/npm/v/react-native-tencent-asr.svg?style=flat)](https://www.npmjs.com/package/react-native-tencent-asr)
[![npm](https://img.shields.io/npm/dm/react-native-tencent-asr.svg)](https://www.npmjs.com/package/react-native-tencent-asr)
[![platform](https://img.shields.io/badge/platform-iOS%2FAndroid-lightgrey.svg?style=flat)](https://github.com/narol1024/react-native-tencent-asr)
[![GitHub license](https://img.shields.io/github/license/mashape/apistatus.svg?style=flat)](https://github.com/narol1024/react-native-tencent-asr/blob/main/LICENSE)

A React Native wrapper around the Tencent Cloud ASR SDK for Android and iOS.

## Installation

### NPM

```sh
npm install react-native-tencent-asr
```

### iOS

执行`pod install`

### Android

Coming soon...

## Features

### IOS

- [x] 录音文件识别极速版
- [x] 实时语音识别
- [x] 一句话识别
  - [x] 一句话识别(网络URL)
  - [x] 一句话识别(完整参数)
  - [x] 一句话识别(内置录音器)

### Android

- [ ] 录音文件识别极速版
- [ ] 实时语音识别
- [ ] 一句话识别

## Usage

### 实时识别

#### 配置参数

```javascript
import { RealTimeRecognizerModule } from 'react-native-tencent-asr';

// 配置AppID、SecretID、SecretKey
RealTimeRecognizerModule.configure({
  appId: APP_ID,
  secretId: SECRET_ID,
  secretKey: SECRET_KEY,
});
```

#### 识别任务

```javascript
// 开始实时识别
RealTimeRecognizerModule.startRealTimeRecognizer();
// 结束实时识别
RealTimeRecognizerModule.stopRealTimeRecognizer();
```

#### 注册回调事件

```javascript
import { NativeEventEmitter, NativeModules } from 'react-native';
import { RealTimeRecognizerModule } from 'react-native-tencent-asr';

const RealTimeRecognizerModuleEmitter = new NativeEventEmitter(
  NativeModules.RealTimeRecognizerModule
);

// 注册事件
RealTimeRecognizerModuleEmitter.addListener(
  'OnSegmentSuccessRecognize',
  (result) => {
    console.log('语音识别结果', result);
  }
);
```

详细事件列表，请查看SDK文档, https://cloud.tencent.com/document/product/1093/35723

- onSliceRecognize
- onSegmentSuccessRecognize
- onSliceSuccessRecognize
- onErrorRecognize
- onStartRecord
- onStopRecord
- onUpdateVolume
- onSilentDetectTimeOut(仅支持Android)
- onSaveAudioDataAsFile
- onError

### 一句话识别

#### 配置参数

```javascript
import { OneSentenceRecognizerModule } from 'react-native-tencent-asr';

// 配置参数
OneSentenceRecognizerModule.configure({
  appId: APP_ID,
  secretId: SECRET_ID,
  secretKey: SECRET_KEY,
});
```

#### 一句话识别(网络URL)

```javascript
OneSentenceRecognizerModule.recognizeWithUrl({
  url: 'The Remote url of the audio file, such as https://xx.com/x.mp3',
  voiceFormat: 'mp3',
});
```

#### 一句话识别(本地音频)

```javascript
OneSentenceRecognizerModule.recognizeWithParams({
  filePath: 'The audio file path',
  voiceFormat: 'mp3',
});
```

#### 一句话识别(内置录音器)

```javascript
OneSentenceRecognizerModule.startRecognizeWithRecorder();
OneSentenceRecognizerModule.stopRecognizeWithRecorder();
```

#### 注册回调事件

```javascript
import { OneSentenceRecognizerModule } from 'react-native-tencent-asr';

const OneSentenceRecognizerModuleEmitter = new NativeEventEmitter(
  NativeModules.OneSentenceRecognizerModule
);

OneSentenceRecognizerModuleEmitter.addListener(
  'DidRecognize',
  (result) => {
    if (result.error) {
      console.log('语音识别失败', result.error);
      return;
    }
    console.log('语音识别结果', result);
  }
);
```

详细事件列表，请查看SDK文档, https://cloud.tencent.com/document/product/1093/36502

- onRecognize
- onStartRecord
- onStopRecord
- onUpdateVolume
- onError

### 录音文件识别极速版

```javascript
import { FlashFileRecognizerModule } from 'react-native-tencent-asr';

FlashFileRecognizerModule.configure({
  appId: APP_ID,
  secretId: SECRET_ID,
  secretKey: SECRET_KEY,
});

try {
  const result = await FlashFileRecognizerModule.flashFileRecognizer({
    filePath: 'The audio file path',
    voiceFormat: 'mp3',
  });
} catch (err) {
  // handle error
}
```

## Screenshot

![](https://narol-blog.oss-cn-beijing.aliyuncs.com/blog-img/202405021925507.png)

## License

MIT

---

Made with [create-react-native-library](https://github.com/callstack/react-native-builder-bob)
