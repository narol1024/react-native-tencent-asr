#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>

#import <QCloudFileRecognizer/QCloudFlashFileRecognizeParams.h>
#import <QCloudFileRecognizer/QCloudFlashFileRecognizer.h>

@interface FlashFileRecognizerModule
    : RCTEventEmitter <RCTBridgeModule, QCloudFlashFileRecognizerDelegate>

@end
