#import <React/RCTBridgeModule.h>

#import <QCloudFileRecognizer/QCloudFlashFileRecognizeParams.h>
#import <QCloudFileRecognizer/QCloudFlashFileRecognizer.h>

@interface FlashFileRecognizerModule
    : NSObject <RCTBridgeModule, QCloudFlashFileRecognizerDelegate>

@end
