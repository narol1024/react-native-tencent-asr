// @see SDK doc: https://cloud.tencent.com/document/product/1093/35723

#import "RealTimeRecognizerModule.h"

@implementation RealTimeRecognizerModule {
  QCloudConfig *_requestParams;
  QCloudRealTimeRecognizer *_recognizer;
  bool _isRecording;
  bool _hasListeners;
}

static NSString *_moduleName = @"RealTimeRecognizerModule";

- (void)startObserving {
  _hasListeners = YES;
}

- (void)stopObserving {
  _hasListeners = NO;
}

- (NSArray<NSString *> *)supportedEvents {
  return @[
    @"RealTimeRecognizerModule.onSliceSuccessRecognize",
    @"RealTimeRecognizerModule.onSegmentSuccessRecognize",
    @"RealTimeRecognizerModule.onSuccessRecognize",
    @"RealTimeRecognizerModule.onErrorRecognize",
    @"RealTimeRecognizerModule.onStartRecord",
    @"RealTimeRecognizerModule.onStopRecord",
    @"RealTimeRecognizerModule.onUpdateVolume",
    @"RealTimeRecognizerModule.onSaveAudioDataAsFile",
    @"RealTimeRecognizerModule.onError",
  ];
}

// 初始化Recognizer
- (void)initializeRecognizer {
  _recognizer =
      [[QCloudRealTimeRecognizer alloc] initWithConfig:_requestParams];
  _recognizer.delegate = self;
}

RCT_EXPORT_MODULE()

// 初始化实时语音识别
RCT_EXPORT_METHOD(configure : (NSDictionary *)configParams) {
  NSLog(@"%@, 调用configure方法, 调用参数: %@", _moduleName, configParams);
  // 设置鉴权的参数
  NSString *appId = configParams[@"appId"];
  NSString *secretId = configParams[@"secretId"];
  NSString *secretKey = configParams[@"secretKey"];
  NSString *token = configParams[@"token"];
  QCloudConfig *requestParams;
  // 直接Token鉴权
  if ([token isEqual:@""]) {
    requestParams = [[QCloudConfig alloc] initWithAppId:appId
                                               secretId:secretId
                                              secretKey:secretKey
                                              projectId:0];

  } else {
    requestParams = [[QCloudConfig alloc] initWithAppId:appId
                                               secretId:secretId
                                              secretKey:secretKey
                                                  token:token
                                              projectId:0];
  }
  // 以下为可选配置参数
  requestParams.requestTimeout =
      [configParams[@"requestTimeout"] integerValue] ?: 10;
  requestParams.sliceTime = [configParams[@"sliceTime"] integerValue] ?: 40;
  requestParams.enableDetectVolume =
      [configParams[@"enableDetectVolume"] boolValue] ?: YES;
  requestParams.endRecognizeWhenDetectSilence =
      [configParams[@"endRecognizeWhenDetectSilence"] boolValue] ?: YES;
  requestParams.shouldSaveAsFile =
      [configParams[@"shouldSaveAsFile"] boolValue] ?: YES;
  requestParams.saveFilePath = [NSTemporaryDirectory()
      stringByAppendingPathComponent:@"recordaudio.wav"];

  requestParams.engineType = configParams[@"engineModelType"] ?: @"16k_zh";
  requestParams.filterDirty = [configParams[@"filterDirty"] integerValue];
  requestParams.filterModal = [configParams[@"filterModal"] integerValue];
  requestParams.filterPunc = [configParams[@"filterPunc"] integerValue];
  requestParams.convertNumMode =
      [configParams[@"convertNumMode"] integerValue] ?: 1;
  requestParams.hotwordId = configParams[@"hotwordId"] ?: @"";
  requestParams.customizationId =
      configParams[@"customizationId"] ?: @""; // 自学习模型id,详情见API文档
  requestParams.vadSilenceTime =
      [configParams[@"vadSilenceTime"] integerValue] ?: -1;
  requestParams.needvad = [configParams[@"needvad"] integerValue] ?: 1;
  requestParams.wordInfo = [configParams[@"wordInfo"] integerValue];
  requestParams.reinforceHotword =
      [configParams[@"reinforceHotword"] integerValue];
  requestParams.noiseThreshold = [configParams[@"noiseThreshold"] integerValue];
  requestParams.maxSpeakTime =
      [configParams[@"maxSpeakTime"] integerValue] ?: 1000 * 5;
  [requestParams setApiParam:@"noise_threshold" value:@(0.5)];

  _requestParams = requestParams;
}

// 开始实时语音识别
RCT_EXPORT_METHOD(startRealTimeRecognizer) {
  NSLog(@"%@, 调用startRealTimeRecognizer方法", _moduleName);
  if (_isRecording) {
    return;
  }
  _isRecording = YES;
  // 使用内置录音器前需要先设置AVAudioSession状态为可录音的模式
  [[AVAudioSession sharedInstance] setCategory:AVAudioSessionCategoryRecord
                                         error:nil];
  [[AVAudioSession sharedInstance] setActive:YES error:nil];
  // 每个Recognizer有效期, 每次调用都需要初始化1次, 以保持活跃状态
  [self initializeRecognizer];
  [_recognizer start];
}

// 结束实时语音识别
RCT_EXPORT_METHOD(stopRealTimeRecognizer) {
  NSLog(@"%@, 调用stopRealTimeRecognizer方法", _moduleName);
  _isRecording = NO;
  [_recognizer stop];
}

// 语音分片的识别（非稳态结果，会持续修正）
- (void)realTimeRecognizerOnSliceRecognize:
            (QCloudRealTimeRecognizer *)recognizer
                                    result:(QCloudRealTimeResult *)result {
  NSLog(@"%@, 语音分片的识别回调", _moduleName);
  NSDictionary *resultBody = @{
    @"code" : @([result code]),
    @"message" : [result message],
    @"voiceId" : [result voiceId],
    @"text" : [result text],
  };
  NSLog(@"语音包分片识别结果: %@", resultBody);
  [self sendEventWithName:@"RealTimeRecognizerModule.onSliceSuccessRecognize"
                     body:resultBody];
}

// 持续返回的每句话的识别结果
- (void)realTimeRecognizerOnSegmentSuccessRecognize:
            (QCloudRealTimeRecognizer *)recognizer
                                             result:(QCloudRealTimeResult *)
                                                        result {
  NSLog(@"%@, 每句话的识别结果回调", _moduleName);
  NSDictionary *resultBody = @{
    @"code" : @([result code]),
    @"message" : [result message],
    @"voiceId" : [result voiceId],
    @"text" : [result text],
  };
  NSLog(@"语音流的识别结果: %@", resultBody);
  [self sendEventWithName:@"RealTimeRecognizerModule.onSegmentSuccessRecognize"
                     body:resultBody];
}

// 识别任务总文本
- (void)realTimeRecognizerDidFinish:(QCloudRealTimeRecognizer *)recognizer
                             result:(NSString *)result {
  NSLog(@"%@, 识别任务最终的回调%@: ", _moduleName, result);
  [self sendEventWithName:@"RealTimeRecognizerModule.onSuccessRecognize"
                     body:@{@"text" : result}];
}

// 识别任务失败回调
- (void)realTimeRecognizerDidError:(QCloudRealTimeRecognizer *)recognizer
                            result:(QCloudRealTimeResult *)result {
  NSLog(@"%@, 识别任务失败回调%@: ", _moduleName, [result jsonText]);
  NSData *jsonData = [[result jsonText] dataUsingEncoding:NSUTF8StringEncoding];
  NSError *error;
  id jsonObject = [NSJSONSerialization JSONObjectWithData:jsonData
                                                  options:kNilOptions
                                                    error:&error];

  if (jsonObject && !error) {
    [self sendEventWithName:@"RealTimeRecognizerModule.onErrorRecognize"
                       body:jsonObject];
  }
}

// 开始录音回调
- (void)realTimeRecognizerDidStartRecord:(QCloudRealTimeRecognizer *)recognizer
                                   error:(NSError *_Nullable)error {
  NSLog(@"%@, 开始录音回调", _moduleName);
  if (error != nil) {
    NSMutableDictionary *resultBody = [[NSMutableDictionary alloc] init];
    resultBody[@"code"] = error.userInfo[@"Code"];
    resultBody[@"message"] = error.userInfo[@"Message"];
    [self sendEventWithName:@"RealTimeRecognizerModule.onError"
                       body:resultBody];
  } else {
    [self sendEventWithName:@"RealTimeRecognizerModule.onStartRecord" body:nil];
  }
}

// 结束录音回调
- (void)realTimeRecognizerDidStopRecord:(QCloudRealTimeRecognizer *)recognizer {
  NSLog(@"%@, 结束录音回调", _moduleName);
  [self sendEventWithName:@"RealTimeRecognizerModule.onStopRecord" body:nil];
}

// 录音音量(单位为分贝)实时回调,此回调计算音量的分贝值。
- (void)realTimeRecognizerDidUpdateVolumeDB:
            (QCloudRealTimeRecognizer *)recognizer
                                     volume:(float)volume {
  NSLog(@"%@, 录音音量实时回调, 音量:%@", _moduleName, @(volume));
  [self sendEventWithName:@"RealTimeRecognizerModule.onUpdateVolume"
                     body:@{@"volume" : @(volume)}];
}

// 录音停止后回调一次，再次开始录音会清空上一次保存的文件。
- (void)realTimeRecognizerDidSaveAudioDataAsFile:
            (QCloudRealTimeRecognizer *)recognizer
                                   audioFilePath:(NSString *)audioFilePath {
  NSLog(@"%@, 结束录音回调, 音频文件路径:%@", _moduleName, audioFilePath);
  [self sendEventWithName:@"RealTimeRecognizerModule.onSaveAudioDataAsFile"
                     body:@{@"audioFilePath" : audioFilePath}];
}

- (void)realTimeRecgnizerLogOutPutWithLog:(NSString *)log {
  NSLog(@"%@, 统一日志输出: %@", _moduleName, log);
}

@end
