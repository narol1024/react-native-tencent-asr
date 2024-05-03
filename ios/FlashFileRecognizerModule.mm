// @see SDK doc: https://cloud.tencent.com/document/product/1093/35723

#import "FlashFileRecognizerModule.h"

@implementation FlashFileRecognizerModule {
  NSString *_appId;
  NSString *_secretId;
  NSString *_secretKey;
  NSString *_token;
  QCloudFlashFileRecognizer *_recognizer;
  RCTPromiseResolveBlock _recognizerResolve;
  RCTPromiseRejectBlock _recognizerReject;
}
RCT_EXPORT_MODULE()

// 配置AppID、SecretID、SecretKey, Token
RCT_EXPORT_METHOD(configure : (NSDictionary *)params) {
  NSLog(@"录音文件识别极速版, 配置AppID、SecretID、SecretKey, Token参数: %@",
        params);
  _appId = params[@"appId"];
  _secretId = params[@"secretId"];
  _secretKey = params[@"secretKey"];
  if ([params[@"token"] isEqual:@""]) {
    _token = params[@"token"];
  }
}

// 录音文件识别极速版
RCT_EXPORT_METHOD(flashFileRecognizer
                  : (NSDictionary *)configParams resolver
                  : (RCTPromiseResolveBlock)resolve rejecter
                  : (RCTPromiseRejectBlock)reject) {
  NSLog(@"录音文件识别极速版, 用户自定义参数: %@", configParams);

  NSString *appId = _appId;
  NSString *secretId = _secretId;
  NSString *secretKey = _secretKey;
  NSString *token = _token;
  NSString *filePath = configParams[@"filePath"];

  // 直接Token鉴权
  if ([token isEqual:@""]) {
    _recognizer = [[QCloudFlashFileRecognizer alloc] initWithAppId:appId
                                                          secretId:secretId
                                                         secretKey:secretKey];
  } else {
    _recognizer = [[QCloudFlashFileRecognizer alloc] initWithAppId:appId
                                                          secretId:secretId
                                                         secretKey:secretKey
                                                             token:token];
  }

  QCloudFlashFileRecognizeParams *params =
      [QCloudFlashFileRecognizeParams defaultRequestParams];

  // 引擎模型类型,默认16k_zh。8k_zh：8k 中文普通话通用；16k_zh：16k
  // 中文普通话通用；16k_zh_video：16k 音视频领域。
  params.engineModelType = configParams[@"engineModelType"] ?: @"16k_zh";
  // 音频格式，默认为aac。支持 wav、pcm、ogg-opus、speex、silk、mp3、m4a、aac。
  params.voiceFormat = configParams[@"voiceFormat"] ?: @"aac";
  // // 0 ：默认状态 不过滤脏话 1：过滤脏话
  params.filterDirty = [configParams[@"filterDirty"] integerValue];
  // 0 ：默认状态 不过滤语气词  1：过滤部分语气词 2:严格过滤
  params.filterModal = [configParams[@"filterModal"] integerValue];
  // 0 ：默认状态 不过滤句末的句号 1：滤句末的句号
  params.filterPunc = [configParams[@"filterPunc"] integerValue];
  // 1：默认状态 根据场景智能转换为阿拉伯数字；0：全部转为中文数字。
  params.convertNumMode = [configParams[@"convertNumMode"] integerValue] ?: 1;
  // 是否开启说话人分离（目前支持中文普通话引擎），默认为0，0：不开启，1：开启。
  params.speakerDiarization =
      [configParams[@"speakerDiarization"] integerValue];
  // 是否只识别首个声道，默认为1。0：识别所有声道；1：识别首个声道。
  params.firstChannelOnly =
      [configParams[@"firstChannelOnly"] integerValue] ?: 1;
  // 是否显示词级别时间戳，默认为0。0：不显示；1：显示，不包含标点时间戳，2：显示，包含标点时间戳。
  params.wordInfo = [configParams[@"wordInfo"] integerValue];
  // 自学习模型 id。如设置了该参数，将生效对应的自学习模型。
  params.customizationID = configParams[@"wordInfo"] ?: @"";
  // 热词表
  // id。如不设置该参数，自动生效默认热词表；如设置了该参数，那么将生效对应的热词表。
  params.hotwordID = configParams[@"hotwordID"] ?: @"";
  // 音频数据
  NSData *audioData = [[NSData alloc] initWithContentsOfFile:filePath];
  params.audioData = audioData;

  _recognizer.delegate = self;
  _recognizerResolve = resolve;
  _recognizerReject = reject;
  [_recognizer EnableDebugLog:YES];
  [_recognizer recognize:params];
}

// 上传文件成功回调
- (void)FlashFileRecognizer:(QCloudFlashFileRecognizer *_Nullable)recognizer
                     status:(nullable NSInteger *)status
                       text:(nullable NSString *)text
                 resultData:(nullable NSDictionary *)resultData {
  if (status == 0) {
    NSLog(@"识别成功");
    NSDictionary *resultBody = @{
      @"text" : text,
      @"resultData" : resultData,
    };
    _recognizerResolve(resultBody);
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
