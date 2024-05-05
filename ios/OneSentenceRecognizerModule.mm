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

- (void)startObserving {
  _hasListeners = YES;
}

- (void)stopObserving {
  _hasListeners = NO;
}

- (NSArray<NSString *> *)supportedEvents {
  return @[
    @"DidRecognize",
    @"DidStartRecord",
    @"DidEndRecord",
    @"DidUpdateVolume",
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
  NSLog(@"一句话识别模块", @"配置AppID、SecretID、SecretKey, Token参数: %@",
        configParams);

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
  NSLog(@"快捷接口用户自定义参数: %@", configParams);

  NSString *url = configParams[@"url"];
  NSString *voiceFormat = configParams[@"voiceFormat"] ?: @"aac";
  NSString *engineModelType = configParams[@"engineModelType"] ?: @"16k_zh";

  // 每个Recognizer有效期, 每次调用都需要初始化1次, 以保持活跃状态
  [self initializeRecognizer];
  // 指定语音数据url 语音数据格式 识别引擎
  // 支持的格式及引擎名称以API文档为准，见https://cloud.tencent.com/document/product/1093/35646
  [_recognizer recoginizeWithUrl:url
                     voiceFormat:voiceFormat
                  EngSerViceType:engineModelType];
}

// 完整接口，可设置更多参数
RCT_EXPORT_METHOD(recognizeWithParams : (NSDictionary *)configParams) {
  NSLog(@"完整接口参数: %@", configParams);
  NSString *url = configParams[@"url"];
  NSString *audioFilePath = configParams[@"audioFilePath"];

  // 每个Recognizer有效期, 每次调用都需要初始化1次, 以保持活跃状态
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
RCT_EXPORT_METHOD(recognizeWithRecorder : (NSDictionary *)configParams) {
  if (_isRecording) {
    return;
  }
  NSLog(@"通过 SDK 内置录音器调用，用户参数: %@", configParams);
  NSString *engSerViceType = configParams[@"engineModelType"] ?: nil;

  // 使用内置录音器前需要先设置AVAudioSession状态为可录音的模式
  _isRecording = YES;
  [[AVAudioSession sharedInstance] setCategory:AVAudioSessionCategoryRecord
                                         error:nil];
  [[AVAudioSession sharedInstance] setActive:YES error:nil];

  // 每个Recognizer有效期, 每次调用都需要初始化1次, 以保持活跃状态
  [self initializeRecognizer];
  [_recognizer
      startRecognizeWithRecorder:
          engSerViceType]; // 16k_zh >
                           // 识别引擎,传nil将默认使用16k_zh,支持的识别引擎以API文档为准，见https://cloud.tencent.com/document/product/1093/35646
}

// 停止录音并上传录音数据开始识别
RCT_EXPORT_METHOD(stopRecognizeWithRecorder) {
  NSLog(@"通过 SDK 内置录音器调用，停止录音并上传录音数据开始识别");
  _isRecording = NO;
  [_recognizer stopRecognizeWithRecorder];
}

/**
 * 一句话识别回调delegate
 * @param result 识别结果文本，error=nil此字段才存在值
 * @param error 错误信息，详细错误信息见error.domain和error.userInfo字段
 * @param rawData 识别原始数据
 */
- (void)oneSentenceRecognizerDidRecognize:(QCloudSentenceRecognizer *)recognizer
                                     text:(nullable NSString *)text
                                    error:(nullable NSError *)error
                               resultData:(nullable NSDictionary *)resultData {
  NSMutableDictionary *resultBody = [[NSMutableDictionary alloc] init];

  if (error) {
    resultBody[@"error"] = @{
      @"code" : error.userInfo[@"Code"],
      @"message" : error.userInfo[@"Message"]
    };
  } else {
    resultBody[@"data"] = resultData[@"Response"];
  }
  NSLog(@"一句话识别回调结果: %@", resultBody);
  [self sendEventWithName:@"DidRecognize" body:resultBody];
}

/**
 * 开始录音回调
 */
- (void)oneSentenceRecognizerDidStartRecord:
            (QCloudSentenceRecognizer *)recognizer
                                      error:(nullable NSError *)error {
  NSDictionary *body = error ? @{
    @"code" : error.userInfo[@"Code"],
    @"message" : error.userInfo[@"Message"]
  }
                             : nil;

  NSLog(@"开始录音回调: %@", body);
  [self sendEventWithName:@"DidStartRecord" body:body];
}
/**
 * 结束录音回调, SDK通过此方法回调后内部开始上报语音数据进行识别
 */
- (void)oneSentenceRecognizerDidEndRecord:(QCloudSentenceRecognizer *)recognizer
                            audioFilePath:(nonnull NSString *)audioFilePath {
  NSDictionary *resultBody = @{
    @"audioFilePath" : audioFilePath,
  };
  NSLog(@"结束录音回调: %@", resultBody);
  [self sendEventWithName:@"DidEndRecord" body:resultBody];
}

/**
 * 录音音量实时回调用
 * @param recognizer 识别器实例
 * @param volume 声音音量，取值范围（-40-0）
 */
- (void)oneSentenceRecognizerDidUpdateVolume:
            (QCloudSentenceRecognizer *)recognizer
                                      volume:(float)volume {
  NSDictionary *resultBody = @{
    @"volume" : @(volume),
  };
  NSLog(@"录音音量实时回调: %@", resultBody);
  [self sendEventWithName:@"DidUpdateVolume" body:resultBody];
}
/**
 * 日志输出
 * @param log 日志
 */
- (void)SentenceRecgnizerLogOutPutWithLog:(NSString *_Nullable)log {
  NSLog(@"一句话识别日志输出%@", log);
}

@end
