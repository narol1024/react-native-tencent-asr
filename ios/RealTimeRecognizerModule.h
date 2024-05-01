#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>

#import "AVFAudio/AVAudioSession.h"

#import <QCloudRealTime/QCloudAudioDataSource.h>
#import <QCloudRealTime/QCloudConfig.h>
#import <QCloudRealTime/QCloudRealTimeRecognizer.h>
#import <QCloudRealTime/QCloudRealTimeResult.h>

@interface RealTimeRecognizerModule
    : RCTEventEmitter <RCTBridgeModule, QCloudRealTimeRecognizerDelegate>

@end
