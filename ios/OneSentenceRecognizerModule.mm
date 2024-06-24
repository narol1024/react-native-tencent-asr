// @see SDK doc: https://cloud.tencent.com/document/product/1093/36502

#import "OneSentenceRecognizerModule.h"

@implementation OneSentenceRecognizerModule {
  NSString *_appId;
  NSString *_secretId;
  NSString *_secretKey;
  NSString *_token;
  NSDictionary *_requestParams;

  QCloudSentenceRecognizer *_recognizer;
  bool _isRecording;
  bool _hasListeners;
}

static NSString *_moduleName = @"OneSentenceRecognizerModule";

- (void)startObserving {
  _hasListeners = YES;
}

- (void)stopObserving {
  _hasListeners = NO;
}

- (NSArray<NSString *> *)supportedEvents {
  return @[
    @"OneSentenceRecognizerModule.onRecognize",
    @"OneSentenceRecognizerModule.onStartRecord",
    @"OneSentenceRecognizerModule.onStopRecord",
    @"OneSentenceRecognizerModule.onUpdateVolume",
    @"OneSentenceRecognizerModule.onError",
  ];
}

// 初始化Recognizer
- (void)initializeRecognizer {
  // 直接Token鉴权
  if ([_token isEqual:@""]) {
    _recognizer = [[QCloudSentenceRecognizer alloc] initWithAppId:_appId
                                                         secretId:_secretId
                                                        secretKey:_secretKey];

  } else {
    _recognizer = [[QCloudSentenceRecognizer alloc] initWithAppId:_appId
                                                         secretId:_secretId
                                                        secretKey:_secretKey
                                                            token:_token];
  }

  _recognizer.delegate = self;
}

RCT_EXPORT_MODULE()

// 配置AppID、SecretID、SecretKey, Token
RCT_EXPORT_METHOD(configure : (NSDictionary *)configParams) {
  NSLog(@"%@, 调用configure方法, 调用参数: %@", _moduleName, configParams);

  // 设置鉴权的参数
  _appId = configParams[@"appId"];
  _secretId = configParams[@"secretId"];
  _secretKey = configParams[@"secretKey"];
  _token = configParams[@"token"];

  // 设置自定义参数
  NSMutableDictionary *requestParams = [NSMutableDictionary dictionary];
  requestParams[@"engSerViceType"] =
      configParams[@"engineModelType"] ?: @"16k_zh";
  requestParams[@"voiceFormat"] = configParams[@"voiceFormat"] ?: @"aac";
  requestParams[@"filterDirty"] =
      @([configParams[@"filterDirty"] integerValue] ?: 0);
  requestParams[@"filterModal"] =
      @([configParams[@"filterModal"] integerValue] ?: 0);
  requestParams[@"filterPunc"] =
      @([configParams[@"filterPunc"] integerValue] ?: 0);
  requestParams[@"convertNumMode"] =
      @([configParams[@"convertNumMode"] integerValue] ?: 1);
  requestParams[@"wordInfo"] = @([configParams[@"wordInfo"] integerValue] ?: 1);
  requestParams[@"hotwordId"] = configParams[@"hotwordId"] ?: @"";
  _requestParams = requestParams;
}

// 快捷接口，可以通过URL快速识别
RCT_EXPORT_METHOD(recognizeWithUrl : (NSDictionary *)configParams) {
  NSLog(@"%@, 调用recognizeWithUrl方法, 调用参数: %@", _moduleName,
        configParams);

  NSString *url = configParams[@"url"];
  NSString *voiceFormat = configParams[@"voiceFormat"] ?: @"aac";
  NSString *engineModelType = configParams[@"engineModelType"] ?: @"16k_zh";

  // 每个Recognizer都有有效期, 每次调用都需要初始化1次, 以保持活跃状态
  [self initializeRecognizer];
  // 指定语音数据url 语音数据格式 识别引擎
  // 支持的格式及引擎名称以API文档为准，见https://cloud.tencent.com/document/product/1093/35646
  [_recognizer recoginizeWithUrl:url
                     voiceFormat:voiceFormat
                  EngSerViceType:engineModelType];
}

// 完整接口，可设置更多参数
RCT_EXPORT_METHOD(recognizeWithParams : (NSDictionary *)configParams) {
  NSLog(@"%@, 调用recognizeWithParams方法, 调用参数: %@", _moduleName,
        configParams);
  NSString *url = configParams[@"url"];
  NSString *audioFilePath = configParams[@"audioFilePath"];

  // 每个Recognizer都有有效期, 每次调用都需要初始化1次, 以保持活跃状态
  [self initializeRecognizer];

  // 获取一个已设置默认参数params
  QCloudSentenceRecognizeParams *requestParams =
      [_recognizer defaultRecognitionParams];

  // 设置语音数据来源，见QCloudAudioSourceType定义
  if ([configParams[@"sourceType"] isEqual:@"url"]) {
    requestParams.url = url;
    requestParams.sourceType = QCloudAudioSourceTypeUrl;
  } else {
    NSData *audioData = [[NSData alloc] initWithContentsOfFile:audioFilePath];
    requestParams.data = audioData;
    requestParams.sourceType = QCloudAudioSourceTypeAudioData;
  }

  // 用KVC合并参数
  for (NSString *key in _requestParams) {
    id value = _requestParams[key];
    if (value != nil) {
      [requestParams setValue:value forKey:key];
    }
  }
  [_recognizer recognizeWithParams:requestParams];
}

// 通过 SDK 内置录音器调用，开始录音
RCT_EXPORT_METHOD(startRecognizeWithRecorder) {
  NSLog(@"%@, 调用startRecognizeWithRecorder方法", _moduleName);
  if (_isRecording) {
    return;
  }
  // 使用内置录音器前需要先设置AVAudioSession状态为可录音的模式
  _isRecording = YES;
  [[AVAudioSession sharedInstance] setCategory:AVAudioSessionCategoryRecord
                                         error:nil];
  [[AVAudioSession sharedInstance] setActive:YES error:nil];

  // 每个Recognizer都有有效期, 每次调用都需要初始化1次, 以保持活跃状态
  [self initializeRecognizer];
  [_recognizer startRecognizeWithRecorder:_requestParams[@"engSerViceType"]];
}

// 停止录音并上传录音数据开始识别
RCT_EXPORT_METHOD(stopRecognizeWithRecorder) {
  NSLog(@"%@, 调用stopRecognizeWithRecorder方法", _moduleName);
  _isRecording = NO;
  [_recognizer stopRecognizeWithRecorder];
}

// 一句话识别回调
- (void)oneSentenceRecognizerDidRecognize:(QCloudSentenceRecognizer *)recognizer
                                     text:(nullable NSString *)text
                                    error:(nullable NSError *)error
                               resultData:(nullable NSDictionary *)resultData {
  NSLog(@"%@, 识别结果回调", _moduleName);
  if (error != nil) {
    NSMutableDictionary *resultBody = [[NSMutableDictionary alloc] init];
    resultBody[@"code"] = error.userInfo[@"Code"];
    resultBody[@"message"] = error.userInfo[@"Message"];
    [self sendEventWithName:@"OneSentenceRecognizerModule.onError"
                       body:resultBody];
  } else {
    NSMutableDictionary *resultBody = resultData[@"Response"];
    [self sendEventWithName:@"OneSentenceRecognizerModule.onRecognize"
                       body:resultBody];
  }
}

// 开始录音回调
- (void)oneSentenceRecognizerDidStartRecord:
            (QCloudSentenceRecognizer *)recognizer
                                      error:(nullable NSError *)error {
  NSLog(@"%@, 开始录音回调, %@", _moduleName, error);
  if (error != nil) {
    NSMutableDictionary *resultBody = [[NSMutableDictionary alloc] init];
    resultBody[@"code"] = @(error.code);
    resultBody[@"message"] = error.userInfo[@"Reason"];
    [self sendEventWithName:@"OneSentenceRecognizerModule.onError"
                       body:resultBody];
  } else {
    [self sendEventWithName:@"OneSentenceRecognizerModule.onStartRecord"
                       body:nil];
  }
}
// 结束录音回调
- (void)oneSentenceRecognizerDidEndRecord:(QCloudSentenceRecognizer *)recognizer
                            audioFilePath:(nonnull NSString *)audioFilePath {
  NSLog(@"%@, 结束录音回调, 音频文件路径:%@", _moduleName, audioFilePath);
  NSDictionary *resultBody = @{
    @"audioFilePath" : audioFilePath,
  };
  NSLog(@"结束录音回调: %@", resultBody);
  [self sendEventWithName:@"OneSentenceRecognizerModule.onStopRecord"
                     body:resultBody];
}

// 录音音量实时回调
- (void)oneSentenceRecognizerDidUpdateVolume:
            (QCloudSentenceRecognizer *)recognizer
                                      volume:(float)volume {
  NSLog(@"%@, 录音音量实时回调, 音量:%@", _moduleName, @(volume));
  NSDictionary *resultBody = @{
    @"volume" : @(volume),
  };
  [self sendEventWithName:@"OneSentenceRecognizerModule.onUpdateVolume"
                     body:resultBody];
}
/**
 * 日志输出
 * @param log 日志
 */
- (void)SentenceRecgnizerLogOutPutWithLog:(NSString *_Nullable)log {
  NSLog(@"%@, 统一日志输出: %@", _moduleName, log);
}

@end
