// @see SDK doc: https://cloud.tencent.com/document/product/1093/35723

#import "FlashFileRecognizerModule.h"

@implementation FlashFileRecognizerModule {
  NSString *_appId;
  NSString *_secretId;
  NSString *_secretKey;
  NSString *_token;
  NSDictionary *_requestParams;

  QCloudFlashFileRecognizer *_recognizer;
  RCTPromiseResolveBlock _recognizerResolve;
  RCTPromiseRejectBlock _recognizerReject;
}

// 初始化Recognizer
- (void)initializeRecognizer {
  // 直接Token鉴权
  if ([_token isEqual:@""]) {
    _recognizer = [[QCloudFlashFileRecognizer alloc] initWithAppId:_appId
                                                          secretId:_secretId
                                                         secretKey:_secretKey];

  } else {
    _recognizer = [[QCloudFlashFileRecognizer alloc] initWithAppId:_appId
                                                          secretId:_secretId
                                                         secretKey:_secretKey
                                                             token:_token];
  }

  _recognizer.delegate = self;
}

RCT_EXPORT_MODULE()

// 配置AppID、SecretID、SecretKey, Token
RCT_EXPORT_METHOD(configure : (NSDictionary *)configParams) {
  NSLog(@"录音文件识别极速版模块",
        @"配置AppID、SecretID、SecretKey, Token参数: %@", configParams);

  // 设置鉴权的参数
  _appId = configParams[@"appId"];
  _secretId = configParams[@"secretId"];
  _secretKey = configParams[@"secretKey"];
  _token = configParams[@"token"];

  // 设置自定义参数
  NSMutableDictionary *requestParams = [NSMutableDictionary dictionary];
  requestParams[@"engineModelType"] =
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
  requestParams[@"speakerDiarization"] =
      @([configParams[@"speakerDiarization"] integerValue] ?: 0);
  requestParams[@"firstChannelOnly"] =
      @([configParams[@"firstChannelOnly"] integerValue] ?: 1);
  requestParams[@"wordInfo"] = @([configParams[@"wordInfo"] integerValue] ?: 1);
  requestParams[@"customizationID"] = configParams[@"customizationID"] ?: @"";
  requestParams[@"hotwordID"] = configParams[@"hotwordId"] ?: @"";
  _requestParams = requestParams;
}

// 录音文件识别极速版
RCT_EXPORT_METHOD(recognize
                  : (NSDictionary *)configParams resolver
                  : (RCTPromiseResolveBlock)resolve rejecter
                  : (RCTPromiseRejectBlock)reject) {
  NSLog(@"录音文件识别极速版, 用户自定义参数: %@", configParams);

  NSString *filePath = configParams[@"filePath"];
  // 音频数据
  NSData *audioData = [[NSData alloc] initWithContentsOfFile:filePath];
  // 每个Recognizer有效期, 每次调用都需要初始化1次, 以保持活跃状态
  [self initializeRecognizer];
  QCloudFlashFileRecognizeParams *requestParams =
      [QCloudFlashFileRecognizeParams defaultRequestParams];
  requestParams.audioData = audioData;
  // 用KVC合并参数
  for (NSString *key in _requestParams) {
    id value = _requestParams[key];
    [requestParams setValue:value forKey:key];
  }
  _recognizerResolve = resolve;
  _recognizerReject = reject;
  [_recognizer recognize:requestParams];
}

// 上传文件成功回调
- (void)FlashFileRecognizer:(QCloudFlashFileRecognizer *_Nullable)recognizer
                     status:(nullable NSInteger *)status
                       text:(nullable NSString *)text
                 resultData:(nullable NSDictionary *)resultData {
  if (status == 0) {
    NSLog(@"识别成功");
    _recognizerResolve(resultData);
  } else {
    NSLog(@"识别失败");
    _recognizerReject(@"status_error", @"Recognition failed", nil);
  }
}

// 日志输出
- (void)FlashFileRecgnizerLogOutPutWithLog:(NSString *)log {
  NSLog(@"录音文件识别极速版日志输出%@", log);
}
@end
