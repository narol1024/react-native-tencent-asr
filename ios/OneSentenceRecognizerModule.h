#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>

#import "AVFAudio/AVAudioSession.h"

#import <QCloudOneSentence/QCloudSentenceRecognizeParams.h>
#import <QCloudOneSentence/QCloudSentenceRecognizer.h>

@interface OneSentenceRecognizerModule
    : RCTEventEmitter <RCTBridgeModule, QCloudSentenceRecognizerDelegate>

@end
