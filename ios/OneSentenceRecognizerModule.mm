// @see SDK doc: https://cloud.tencent.com/document/product/1093/36502

#import "OneSentenceRecognizerModule.h"

@implementation OneSentenceRecognizerModule {
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

RCT_EXPORT_MODULE()

// 配置AppID、SecretID、SecretKey, Token
RCT_EXPORT_METHOD(configure : (NSDictionary *)configParams) {
  NSLog(@"一句话识别, 配置AppID、SecretID、SecretKey, Token参数: %@",
        configParams);

  NSString *appId = configParams[@"appId"];
  NSString *secretId = configParams[@"secretId"];
  NSString *secretKey = configParams[@"secretKey"];
  NSString *token = configParams[@"token"];

  // 直接Token鉴权
  if ([token isEqual:@""]) {
    _recognizer = [[QCloudSentenceRecognizer alloc] initWithAppId:appId
                                                         secretId:secretId
                                                        secretKey:secretKey];

  } else {
    _recognizer = [[QCloudSentenceRecognizer alloc] initWithAppId:appId
                                                         secretId:secretId
                                                        secretKey:secretKey
                                                            token:token];
  }

  _recognizer.delegate = self;
}

// 快捷接口，可以通过URL快速识别
RCT_EXPORT_METHOD(recognizeWithUrl : (NSDictionary *)configParams) {
  NSLog(@"快捷接口参数: %@", configParams);

  NSString *url = configParams[@"url"];
  NSString *voiceFormat = configParams[@"voiceFormat"] ?: @"aac";
  NSString *engineModelType = configParams[@"engineModelType"] ?: @"16k_zh";

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
  NSString *filePath = configParams[@"filePath"];
  // 获取一个已设置默认参数params
  QCloudSentenceRecognizeParams *params =
      [_recognizer defaultRecognitionParams];
  // 设置语音频数据格式，支持的格式以API文档为准，见https://cloud.tencent.com/document/product/1093/35646
  params.voiceFormat = configParams[@"voiceFormat"] ?: @"wav";
  // 设置语音数据来源，见QCloudAudioSourceType定义
  if ([configParams[@"sourceType"] isEqual:@"url"]) {
    params.url = url;
    params.sourceType = QCloudAudioSourceTypeUrl;
  } else {
    NSData *audioData = [[NSData alloc] initWithContentsOfFile:filePath];
    params.data = audioData;
    params.sourceType = QCloudAudioSourceTypeAudioData;
  }
  // 设置识别引擎,支持的识别引擎以API文档为准，见https://cloud.tencent.com/document/product/1093/35646
  params.engSerViceType = configParams[@"engineModelType"] ?: @"16k_zh";

  // 以下为可选项
  // 是否过滤脏词（目前支持中文普通话引擎）。0：不过滤脏词；1：过滤脏词；2：将脏词替换为
  // * 。默认值为 0。
  params.filterDirty = [configParams[@"filterDirty"] integerValue];
  // 是否过语气词（目前支持中文普通话引擎）。0：不过滤语气词；1：部分过滤；2：严格过滤
  // 。默认值为 0。
  params.filterModal = [configParams[@"filterModal"] integerValue];
  // 是否过滤标点符号（目前支持中文普通话引擎）。
  // 0：不过滤，1：过滤句末标点，2：过滤所有标点。默认值为 0。
  params.filterPunc = [configParams[@"filterPunc"] integerValue];
  // 是否进行阿拉伯数字智能转换。0：不转换，直接输出中文数字，1：根据场景智能转换为阿拉伯数字。默认值为1。
  params.convertNumMode = [configParams[@"convertNumMode"] integerValue] ?: 1;
  // 是否显示词级别时间戳。0：不显示；1：显示，不包含标点时间戳，2：显示，包含标点时间戳。默认值为
  // 0。
  params.wordInfo = [configParams[@"wordInfo"] integerValue] ?: 1;
  params.hotwordId = configParams[@"hotwordId"] ?: @"";

  [_recognizer recognizeWithParams:params];
}

// 通过 SDK 内置录音器调用，开始录音
RCT_EXPORT_METHOD(startRecognizeWithRecorder : (NSDictionary *)configParams) {
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
    resultBody[@"text"] = text;
    resultBody[@"resultData"] = resultData;
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
