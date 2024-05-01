// @see SDK doc: https://cloud.tencent.com/document/product/1093/52580

#import "RealTimeRecognizerModule.h"

@implementation RealTimeRecognizerModule {
  QCloudRealTimeRecognizer *_recognizer;
  bool _isRecording;
  bool _hasListeners;
}

- (void)startObserving {
  _hasListeners = YES;
}

- (void)stopObserving {
  _hasListeners = NO;
}

- (NSArray<NSString *> *)supportedEvents {
  return @[
    @"RealTimeRecognizerOnSliceRecognize",
    @"RealTimeRecognizerOnSegmentSuccessRecognize",
    @"RealTimeRecognizerDidFinish", @"RealTimeRecognizerDidError",
    @"RealTimeRecognizerDidStartRecord", @"RealTimeRecognizerDidStopRecord",
    @"RealTimeRecognizerDidUpdateVolumeDB",
    @"RealTimeRecognizerDidSaveAudioDataAsFile"
  ];
}

RCT_EXPORT_MODULE()

// 初始化实时语音识别
RCT_EXPORT_METHOD(configure : (NSDictionary *)configParams) {

  NSLog(@"实时语音识别, 用户自定义参数: %@", configParams);

  NSString *appId = configParams[@"appId"];
  NSString *secretId = configParams[@"secretId"];
  NSString *secretKey = configParams[@"secretKey"];
  NSString *token = configParams[@"token"];

  // 1.创建 QCloudConfig 实例
  QCloudConfig *config = [[QCloudConfig alloc] initWithAppId:appId
                                                    secretId:secretId
                                                   secretKey:secretKey
                                                       token:token
                                                   projectId:0];

  // 以下为可选配置参数
  config.requestTimeout = [configParams[@"requestTimeout"] integerValue]
                              ?: 10; // 请求超时时间（秒）
  config.sliceTime = [configParams[@"sliceTime"] integerValue]
                         ?: 40; // 语音分片时长默认40ms（无特殊需求不建议更改）
  config.enableDetectVolume =
      [configParams[@"enableDetectVolume"] boolValue] ?: YES; // 是否检测音量
  config.endRecognizeWhenDetectSilence =
      [configParams[@"endRecognizeWhenDetectSilence"] boolValue]
          ?: YES; // 是否检测到静音停止识别
  config.shouldSaveAsFile =
      [configParams[@"shouldSaveAsFile"] boolValue]
          ?: YES; // 仅限使用SDK内置录音器有效，是否保存录音文件到本地 默认关闭
  config.saveFilePath = [NSTemporaryDirectory()
      stringByAppendingPathComponent:
          @"recordaudio.wav"]; // 开启shouldSaveAsFile后音频保存的路径，仅限使用SDK内置录音器有效,默认路径为[NSTemporaryDirectory()
                               // stringByAppendingPathComponent:@"recordaudio.wav"]

  // 以下为API参数配置，参数描述见API文档：https://cloud.tencent.com/document/product/1093/48982
  config.engineType = configParams[@"engineModelType"] ?: @"16k_zh";
  ; // 设置引擎，不设置默认16k_zh
  config.filterDirty = [configParams[@"filterDirty"]
      integerValue]; // 是否过滤脏词，具体的取值见API文档的filter_dirty参数
  config.filterModal = [configParams[@"filterModal"]
      integerValue]; // 过滤语气词具体的取值见API文档的filter_modal参数
  config.filterPunc = [configParams[@"filterPunc"]
      integerValue]; // 过滤句末的句号具体的取值见API文档的filter_punc参数
  config.convertNumMode =
      [configParams[@"convertNumMode"] integerValue]
          ?: 1; // 是否进行阿拉伯数字智能转换。具体的取值见API文档的convert_num_mode参数
  // config.hotwordId = @""; //热词id。具体的取值见API文档的hotword_id参数
  // config.customizationId = @"";  //自学习模型id,详情见API文档
  // config.vadSilenceTime = -1;    //语音断句检测阈值,详情见API文档
  config.needvad = [configParams[@"needvad"] integerValue]
                       ?: 1; // 默认1 0：关闭 vad，1：开启 vad。
                             // 如果语音分片长度超过60秒，用户需开启 vad。
  config.wordInfo = [configParams[@"wordInfo"]
      integerValue]; // 是否显示词级别时间戳。,详情见API文档
  config.reinforceHotword = [configParams[@"reinforceHotword"]
      integerValue]; // 热词增强功能 0: 关闭, 1: 开启 默认0
  config.noiseThreshold = [configParams[@"noiseThreshold"]
      integerValue]; // 噪音参数阈值，默认为0，取值范围：[-1,1]
  config.maxSpeakTime =
      [configParams[@"maxSpeakTime"] integerValue]
          ?: 1000 *
                 5; // 强制断句功能，取值范围
                    // 5000-90000(单位:毫秒），默认值0(不开启)。
                    // 在连续说话不间断情况下，该参数将实现强制断句（此时结果变成稳态，slice_type=2）。如：游戏解说场景，解说员持续不间断解说，无法断句的情况下，将此参数设置为10000，则将在每10秒收到
                    // slice_type=2的回调。

  [config
      setApiParam:@"noise_threshold"
            value:
                @(0.5)]; // 设置自定义请求参数,用于在请求中添加SDK尚未支持的参数

  _recognizer = [[QCloudRealTimeRecognizer alloc] initWithConfig:config];
  _recognizer.delegate = self;
}

// 开始实时语音识别
RCT_EXPORT_METHOD(startRealTimeRecognizer) {
  if (_isRecording) {
    return;
  }
  // 使用内置录音器前需要先设置AVAudioSession状态为可录音的模式
  _isRecording = YES;
  [[AVAudioSession sharedInstance] setCategory:AVAudioSessionCategoryRecord
                                         error:nil];
  [[AVAudioSession sharedInstance] setActive:YES error:nil];
  NSLog(@"开始实时语音识别");
  [_recognizer start];
}

// 结束实时语音识别
RCT_EXPORT_METHOD(stopRealTimeRecognizer) {
  NSLog(@"结束实时语音识别");
  _isRecording = NO;
  [_recognizer stop];
}

// 语音分片的识别（非稳态结果，会持续修正）
- (void)realTimeRecognizerOnSliceRecognize:
            (QCloudRealTimeRecognizer *)recognizer
                                    result:(QCloudRealTimeResult *)result {
  NSDictionary *resultBody = @{
    @"code" : @([result code]),
    @"message" : [result message],
    @"voiceId" : [result voiceId],
    @"text" : [result text],
    @"recognizedText" : [result recognizedText],
  };
  NSLog(@"语音包分片识别结果: %@", resultBody);
  [self sendEventWithName:@"RealTimeRecognizerOnSliceRecognize"
                     body:resultBody];
}

// 持续返回的每句话的识别结果
- (void)realTimeRecognizerOnSegmentSuccessRecognize:
            (QCloudRealTimeRecognizer *)recognizer
                                             result:(QCloudRealTimeResult *)
                                                        result {
  NSDictionary *resultBody = @{
    @"code" : @([result code]),
    @"message" : [result message],
    @"voiceId" : [result voiceId],
    @"text" : [result text],
    @"recognizedText" : [result recognizedText],
  };
  NSLog(@"语音流的识别结果: %@", resultBody);
  [self sendEventWithName:@"RealTimeRecognizerOnSegmentSuccessRecognize"
                     body:resultBody];
}

// 识别任务总文本
- (void)realTimeRecognizerDidFinish:(QCloudRealTimeRecognizer *)recognizer
                             result:(NSString *)result {
  NSLog(@"识别任务总文本: %@", result);
  [self sendEventWithName:@"RealTimeRecognizerDidFinish"
                     body:@{@"result" : result}];
}

// 识别任务失败回调
- (void)realTimeRecognizerDidError:(QCloudRealTimeRecognizer *)recognizer
                            result:(QCloudRealTimeResult *)result {

  NSLog(@"识别任务失败回调: %@", [result jsonText]);

  [self sendEventWithName:@"RealTimeRecognizerDidError" body:[result jsonText]];
}

// 开始录音回调
- (void)realTimeRecognizerDidStartRecord:(QCloudRealTimeRecognizer *)recognizer
                                   error:(NSError *_Nullable)error {
  NSDictionary *body = error ? @{
    @"error" : @{
      @"code" : error.userInfo[@"Code"],
      @"message" : error.userInfo[@"Message"]
    }
  }
                             : nil;

  NSLog(@"开始录音回调: %@", body);
  [self sendEventWithName:@"RealTimeRecognizerDidStartRecord" body:body];
}

// 结束录音回调
- (void)realTimeRecognizerDidStopRecord:(QCloudRealTimeRecognizer *)recognizer {
  [self sendEventWithName:@"RealTimeRecognizerDidStopRecord" body:nil];
}

// 录音音量(单位为分贝)实时回调,此回调计算音量的分贝值。
- (void)realTimeRecognizerDidUpdateVolumeDB:
            (QCloudRealTimeRecognizer *)recognizer
                                     volume:(float)volume {
  NSLog(@"录音音量(单位为分贝)实时回调: %@", @(volume));
  [self sendEventWithName:@"RealTimeRecognizerDidUpdateVolumeDB"
                     body:@{@"volume" : @(volume)}];
}

// 录音停止后回调一次，再次开始录音会清空上一次保存的文件。
- (void)realTimeRecognizerDidSaveAudioDataAsFile:
            (QCloudRealTimeRecognizer *)recognizer
                                   audioFilePath:(NSString *)audioFilePath {
  NSLog(@"录音文件: %@", audioFilePath);

  [self sendEventWithName:@"RealTimeRecognizerDidSaveAudioDataAsFile"
                     body:@{@"audioFilePath" : audioFilePath}];
}

- (void)realTimeRecgnizerLogOutPutWithLog:(NSString *)log {
  NSLog(@"实时语音识别日志输出%@", log);
}

@end
