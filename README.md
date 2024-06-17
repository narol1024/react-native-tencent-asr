<div align="center">
	<img src="https://narol-blog.oss-cn-beijing.aliyuncs.com/blog-img/202406162113801.svg" width="200" height="200">
	<h1>React-Native-Tencent-ASR</h1>
	<p>
		<b>A React Native wrapper around the Tencent Cloud ASR SDK for Android and iOS.</b>
	</p>
</div>

[![npm version](https://img.shields.io/npm/v/react-native-tencent-asr.svg?style=flat)](https://www.npmjs.com/package/react-native-tencent-asr)
[![npm](https://img.shields.io/npm/dm/react-native-tencent-asr.svg)](https://www.npmjs.com/package/react-native-tencent-asr)
[![platform](https://img.shields.io/badge/platform-iOS%2FAndroid-green.svg?style=flat)](https://github.com/narol1024/react-native-tencent-asr)
[![GitHub license](https://img.shields.io/github/license/mashape/apistatus.svg?style=flat)](https://github.com/narol1024/react-native-tencent-asr/blob/main/LICENSE)

## Installation

### NPM

```sh
npm install react-native-tencent-asr
```

### iOS

执行`pod install`

### Android

- 在 AndroidManifest.xml 添加如下权限：

```xml
<uses-permission android:name="android.permission.RECORD_AUDIO"/>
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

- 拥有tecentcloud asr的sdk包没有发布到maven，因此需要编辑 android/build.gradle, 添加flatDir配置来应用这些包。

```
allprojects {
  repositories {
    flatDir {
      dirs project(':react-native-tencent-asr').file('libs')
    }
  }
}
```

## Features

- 录音文件识别极速版
- 实时语音识别
- 一句话识别
  - 一句话识别(网络URL)
  - 一句话识别(完整参数)
  - 一句话识别(内置录音器)

## Usage

### 语音识别通用参数

| 参数名称           |  类型  | 是否必填 | 参数描述                                                                                                            |
| ------------------ | :----: | :------: | ------------------------------------------------------------------------------------------------------------------- |
| appId              | String |   true   | 腾讯云 appId                                                                                                        |
| secretId           | String |   true   | 腾讯云 secretId                                                                                                     |
| secretKey          | String |   true   | 腾讯云 secretKey                                                                                                    |
| token              | String |  false   | 临时token                                                                                                           |
| projectId          | Number |  false   | 不设置默认使用0, 用于按项目管理云资源，可以对云资源进行分项目管理，详情见 https://console.cloud.tencent.com/project |
| engineModelType    | String |  false   | 引擎模型类型, 默认16k_zh                                                                                            |
| voiceFormat        | String |  false   | 音频格式, 默认为aac                                                                                                 |
| filterDirty        | Number |  false   | 是否过滤脏词, 默认不过滤, 0为不过滤脏话, 1为过滤脏话                                                                |
| filterModal        | Number |  false   | 是否过语气词（目前支持中文普通话引擎）, 默认不过滤, 0为不过滤语气词, 1为过滤部分语气词, 2为严格过滤                 |
| filterPunc         | Number |  false   | 是否过滤标点符号（目前支持中文普通话引擎）, 0为不过滤句末的句号, 1为滤句末的句号                                    |
| convertNumMode     | Number |  false   | 是否进行阿拉伯数字智能转换。默认值为1, 0为不转换, 直接输出中文数字, 1为根据场景智能转换为阿拉伯数字。               |
| speakerDiarization | Number |  false   | 是否开启说话人分离（目前支持中文普通话引擎）, 默认为不开启, 0为不开启, 1为开启。                                    |
| firstChannelOnly   | Number |  false   | 是否只识别首个声道, 默认为1, 0：识别所有声道, 1为识别首个声道。                                                     |
| wordInfo           | Number |  false   | 是否显示词级别时间戳, 默认为0, 0为不显示；1为显示, 不包含标点时间戳, 2为显示, 包含标点时间戳。                      |
| customizationID    | String |  false   | 自学习模型 id。如设置了该参数, 将生效对应的自学习模型。                                                             |
| hotwordId          | String |  false   | 热词表 id。如不设置该参数, 自动生效默认热词表, 如设置了该参数, 那么将生效对应的热词表                               |

### 录音文件识别极速版

#### 配置参数

| 参数名称        |  类型  | 是否必填 | 参数描述     |
| --------------- | :----: | :------: | ------------ |
| customizationId | String |  false   | 自学习模型id |

```javascript
import { FlashFileRecognizerModule } from 'react-native-tencent-asr';

FlashFileRecognizerModule.configure({
  appId: APP_ID,
  secretId: SECRET_ID,
  secretKey: SECRET_KEY,
});
```

#### 开始识别

| 参数名称 |  类型  | 是否必填 | 参数描述     |
| -------- | :----: | :------: | ------------ |
| filePath | String |   true   | 音频文件路径 |

```javascript
try {
  const result = await FlashFileRecognizerModule.flashFileRecognizer({
    filePath: 'The audio file path',
  });
} catch (err) {
  // handle error
}
```

#### 注册回调事件

| 事件名称 |   类型   | 参数描述       |
| -------- | :------: | -------------- |
| onError  | Function | 通用的错误事件 |

```javascript
import { FlashFileRecognizerModule } from 'react-native-tencent-asr';

FlashFileRecognizerModule.addListener('onError', (error) => {
  console.error('发生错误: ', error);
});
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

#### 一句话识别(网络URL)

| 参数名称 |  类型  | 是否必填 | 参数描述 |
| -------- | :----: | :------: | -------- |
| url      | String |   true   | 音频URL  |

```javascript
OneSentenceRecognizerModule.recognizeWithUrl({
  url: 'The Remote url of the audio file, such as https://xx.com/x.mp3',
});
```

#### 一句话识别(本地音频)

| 参数名称 |  类型  | 是否必填 | 参数描述     |
| -------- | :----: | :------: | ------------ |
| filePath | String |   true   | 音频文件路径 |

```javascript
OneSentenceRecognizerModule.recognizeWithParams({
  filePath: 'The audio file path',
});
```

#### 一句话识别(内置录音器)

注意，在调用该API前，请确保录音器权限已完成授权，授权方式请参考React-Native官方文档: https://reactnative.dev/docs/permissionsandroid

1. 开始录音

```javascript
OneSentenceRecognizerModule.startRecognizeWithRecorder();
```

2. 结束录音

```javascript
OneSentenceRecognizerModule.stopRecognizeWithRecorder();
```

#### 注册回调事件

| 事件名称       |   类型   | 参数描述                     |
| -------------- | :------: | ---------------------------- |
| onRecognize    | Function | 识别结果回调                 |
| onStartRecord  | Function | 开始录音回调                 |
| onStopRecord   | Function | 结束录音回调                 |
| onUpdateVolume | Function | 录音音量(单位为分贝)实时回调 |
| onError        | Function | 通用的错误事件               |

```javascript
import { OneSentenceRecognizerModule } from 'react-native-tencent-asr';

OneSentenceRecognizerModule.addListener('onRecognize', (result) => {
  console.log('语音识别结果', result);
});
```

### 实时识别

#### 配置参数

| 参数名称                      |  类型   | 是否必填 | 参数描述                                                                                                                                                                                                                                                             |
| ----------------------------- | :-----: | :------: | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| requestTimeout                | Number  |  false   | 请求超时时间（秒）                                                                                                                                                                                                                                                   |
| sliceTime                     | Number  |  false   | 语音分片时长默认40ms（无特殊需求不建议更改）                                                                                                                                                                                                                         |
| enableDetectVolume            | Boolean |  false   | 是否检测音量                                                                                                                                                                                                                                                         |
| endRecognizeWhenDetectSilence | Boolean |  false   | 是否检测到静音停止识别                                                                                                                                                                                                                                               |
| shouldSaveAsFile              | Boolean |  false   | 仅限使用SDK内置录音器有效，是否保存录音文件到本地 默认关闭                                                                                                                                                                                                           |
| saveFilePath                  | String  |  false   | 开启shouldSaveAsFile后音频保存的路径，仅限使用SDK内置录音器有效                                                                                                                                                                                                      |
| customizationId               | String  |  false   | 自学习模型id,详情见API文档                                                                                                                                                                                                                                           |
| vadSilenceTime                | Number  |  false   | 语音断句检测阈值,详情见API文档                                                                                                                                                                                                                                       |
| needvad                       | Number  |  false   | 默认1 0：关闭 vad，1：开启 vad。如果语音分片长度超过60秒，用户需开启 vad。                                                                                                                                                                                           |
| reinforceHotword              | Number  |  false   | 热词增强功能 0: 关闭, 1: 开启 默认0                                                                                                                                                                                                                                  |
| noiseThreshold                | Number  |  false   | 噪音参数阈值，默认为0，取值范围：[-1,1]                                                                                                                                                                                                                              |
| maxSpeakTime                  | Number  |  false   | 强制断句功能，取值范围 5000-90000(单位:毫秒），默认值0(不开启)。在连续说话不间断情况下，该参数将实现强制断句（此时结果变成稳态，slice_type=2）。如：游戏解说场景，解说员持续不间断解说，无法断句的情况下，将此参数设置为10000，则将在每10秒收到 slice_type=2的回调。 |

```javascript
import { RealTimeRecognizerModule } from 'react-native-tencent-asr';

RealTimeRecognizerModule.configure({
  appId: APP_ID,
  secretId: SECRET_ID,
  secretKey: SECRET_KEY,
});
```

#### 识别任务

注意，在调用该API前，请确保录音权限已完成授权，授权方式请参考React-Native官方文档: https://reactnative.dev/docs/permissionsandroid

1. 开始实时识别

```javascript
RealTimeRecognizerModule.startRealTimeRecognizer();
```

2. 结束实时识别

```javascript
RealTimeRecognizerModule.stopRealTimeRecognizer();
```

#### 注册回调事件

| 事件名称                  |   类型   | 参数描述                                                                  |
| ------------------------- | :------: | ------------------------------------------------------------------------- |
| onSliceSuccessRecognize   | Function | 每个语音包分片识别结果                                                    |
| onSegmentSuccessRecognize | Function | 语音流的识别结果,一次识别中可以包括多句话，这里持续返回的每句话的识别结果 |
| onSuccessRecognize        | Function | 一次识别任务最终的结果                                                    |
| onErrorRecognize          | Function | 一次识别任务失败回调                                                      |
| onStartRecord             | Function | 开始录音回调                                                              |
| onStopRecord              | Function | 结束录音回调                                                              |
| onUpdateVolume            | Function | 录音音量(单位为分贝)实时回调                                              |
| onSilentDetectTimeOut     | Function | 静音检测超时回调, 仅支持Android                                           |
| onSaveAudioDataAsFile     | Function | 录音停止后回调一次，再次开始录音会清空上一次保存的文件。                  |
| onError                   | Function | 通用的错误事件                                                            |

```javascript
import { RealTimeRecognizerModule } from 'react-native-tencent-asr';

// 注册事件
RealTimeRecognizerModule.addListener('onSuccessRecognize', (result) => {
  console.log('语音识别结果', result);
});
```

## Screenshot

![](https://narol-blog.oss-cn-beijing.aliyuncs.com/blog-img/202405021925507.png)

## License

MIT

---

Made with [create-react-native-library](https://github.com/callstack/react-native-builder-bob)
