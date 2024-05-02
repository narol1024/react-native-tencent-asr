# react-native-tencent-asr

[![npm version](https://img.shields.io/npm/v/react-native-tencent-asr.svg?style=flat)](https://www.npmjs.com/package/react-native-tencent-asr)
[![npm](https://img.shields.io/npm/dm/react-native-tencent-asr.svg)](https://www.npmjs.com/package/react-native-tencent-asr)
[![platform](https://img.shields.io/badge/platform-iOS%2FAndroid-lightgrey.svg?style=flat)](https://github.com/narol1024/react-native-tencent-asr)
[![GitHub license](https://img.shields.io/github/license/mashape/apistatus.svg?style=flat)](https://github.com/narol1024/react-native-tencent-asr/blob/main/LICENSE)

A React Native wrapper around the Tencent Cloud ASR SDK for Android and iOS.

## Installation

### NPM installation

```sh
npm install react-native-tencent-asr
```

### IOS

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

```javascript
import { NativeEventEmitter, NativeModules } from 'react-native';
import { RealTimeRecognizerModule } from 'react-native-tencent-asr';

const RealTimeRecognizerModuleEmitter = new NativeEventEmitter(
  NativeModules.RealTimeRecognizerModule
);

// 配置参数
RealTimeRecognizerModule.configure({
  appId: APP_ID,
  secretId: SECRET_ID,
  secretKey: SECRET_KEY,
});
// 注册事件
RealTimeRecognizerModuleEmitter.addListener(
  'RealTimeRecognizerOnSegmentSuccessRecognize',
  (result) => {
    console.log('语音的识别结果', result);
  }
);
// 开始实时识别
RealTimeRecognizerModule.startRealTimeRecognizer();
// 结束实时识别
RealTimeRecognizerModule.stopRealTimeRecognizer();
```

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

#### 注册回调事件

```javascript
import { OneSentenceRecognizerModule } from 'react-native-tencent-asr';

const OneSentenceRecognizerModuleEmitter = new NativeEventEmitter(
  NativeModules.OneSentenceRecognizerModule
);

OneSentenceRecognizerModuleEmitter.addListener(
  'OneSentenceRecognizerDidRecognize',
  (result) => {
    if (result.error) {
      console.log('语音识别失败', result.error);
      return;
    }
    console.log('语音识别结果', result);
  }
);
```

#### 一句话识别(网络URL)

```javascript
import { OneSentenceRecognizerModule } from 'react-native-tencent-asr';

OneSentenceRecognizerModule.recognizeWithUrl({
  url: 'Your audio file remote url, such as https://xx.com/x.mp3',
  voiceFormat: 'mp3',
});
```

#### 一句话识别(本地音频)

```javascript
import { OneSentenceRecognizerModule } from 'react-native-tencent-asr';

OneSentenceRecognizerModule.recognizeWithParams({
  filePath: 'Your audio file path',
  voiceFormat: 'mp3',
});
```

#### 一句话识别(内置录音器)

```javascript
import { OneSentenceRecognizerModule } from 'react-native-tencent-asr';

OneSentenceRecognizerModule.startRecognizeWithRecorder();
OneSentenceRecognizerModule.stopRecognizeWithRecorder();
```

### 录音文件识别极速版

```javascript
import { FlashFileRecognizerModule } from 'react-native-tencent-asr';

FlashFileRecognizerModule.configure({
  appId: APP_ID,
  secretId: SECRET_ID,
  secretKey: SECRET_KEY,
});

const result = await FlashFileRecognizerModule.flashFileRecognizer({
  filePath: 'Your audio file path',
  voiceFormat: 'mp3',
});
```

## Screenshot

![](https://narol-blog.oss-cn-beijing.aliyuncs.com/blog-img/202405021925507.png)

## License

MIT

---

Made with [create-react-native-library](https://github.com/callstack/react-native-builder-bob)
