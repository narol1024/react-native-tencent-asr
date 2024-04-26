#import "TencentAsr.h"
#import <QCloudFileRecognizer/QCloudFlashFileRecognizeParams.h>
#import <QCloudFileRecognizer/QCloudFlashFileRecognizer.h>
#import <Foundation/Foundation.h>


@interface TencentAsr () <QCloudFlashFileRecognizerDelegate>
@property (nonatomic, strong) QCloudFlashFileRecognizer *recognizer;
@property (nonatomic, copy) RCTPromiseResolveBlock resolver;
@property (nonatomic, copy) RCTPromiseRejectBlock rejecter;
@property (nonatomic, copy) NSString *appId;
@property (nonatomic, copy) NSString *secretId;
@property (nonatomic, copy) NSString *secretKey;
@end


@implementation TencentAsr
RCT_EXPORT_MODULE()

// 初始化SDK
RCT_EXPORT_METHOD(configureParams:(NSString *)appId
                  secretId:(NSString *)secretId
                  secretKey:(NSString *)secretKey)
{
    self.appId = appId;
    self.secretId = secretId;
    self.secretKey = secretKey;
}
// 识别音频文件
RCT_EXPORT_METHOD(recognizeFile:(NSDictionary *)customParams
                       resolver:(RCTPromiseResolveBlock)resolve
                       rejecter:(RCTPromiseRejectBlock)reject)
{

    self.resolver = resolve;
    self.rejecter = reject;
    
    NSLog(@"appId: %@", self.appId);
    NSLog(@"secretId: %@", self.secretId);
    NSLog(@"secretKey: %@", self.secretKey);

    _recognizer = [[QCloudFlashFileRecognizer alloc] initWithAppId:self.appId secretId:self.secretId secretKey:self.secretKey];
    [_recognizer EnableDebugLog:YES];
    _recognizer.delegate = self;

    QCloudFlashFileRecognizeParams *params = [QCloudFlashFileRecognizeParams defaultRequestParams];

    NSLog(@"filePath: %@", customParams[@"filePath"]);
    NSLog(@"voiceFormat: %@", customParams[@"voiceFormat"]);
        
    NSData *audioData = [[NSData alloc] initWithContentsOfFile:customParams[@"filePath"]];


    params.engineModelType = @"16k_zh";
    params.audioData = audioData;
    params.voiceFormat = customParams[@"voiceFormat"];

    [_recognizer recognize:params];
}


// 上传文件成功回调
- (void)FlashFileRecognizer:(QCloudFlashFileRecognizer *_Nullable)recognizer status:(nullable NSInteger *) status text:(nullable NSString *)text resultData:(nullable NSDictionary *) resultData
{
    if(status == 0){
        NSLog(@"识别成功");
        self.resolver(text);
    }else{
        NSLog(@"上传文件成功，但服务器端识别失败");
        self.rejecter(@"status_error", @"Recognition failed", nil);
    }
}


-(void)FlashFileRecgnizerLogOutPutWithLog:(NSString *)log{
    NSLog(@"log===%@",log);
}
@end
