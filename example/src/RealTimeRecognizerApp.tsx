import React, { useState, useEffect } from 'react';

import { Button, NativeEventEmitter, NativeModules } from 'react-native';
import { RealTimeRecognizerModule } from 'react-native-tencent-asr';
import { APP_ID, SECRET_ID, SECRET_KEY } from './constants';

const RealTimeRecognizerModuleEmitter = new NativeEventEmitter(
  NativeModules.RealTimeRecognizerModule
);

export function RealTimeRecognizerApp(props: any) {
  const [isRecording, setIsRecording] = useState(false);

  useEffect(() => {
    RealTimeRecognizerModuleEmitter.addListener(
      'RealTimeRecognizerOnSegmentSuccessRecognize',
      (result) => {
        console.log('语音的识别结果', result);
        props.onRecognize(result.recognizedText);
      }
    );
    return () => {
      RealTimeRecognizerModuleEmitter.removeAllListeners(
        'RealTimeRecognizerOnSegmentSuccessRecognize'
      );
    };
  }, [props]);

  useEffect(() => {
    RealTimeRecognizerModule.configure({
      appId: APP_ID,
      secretId: SECRET_ID,
      secretKey: SECRET_KEY,
    });
  }, []);

  return (
    <Button
      title={isRecording ? '停止实时识别' : '实时识别'}
      onPress={async () => {
        if (isRecording) {
          RealTimeRecognizerModule.stopRealTimeRecognizer();
          setIsRecording(false);
        } else {
          RealTimeRecognizerModule.startRealTimeRecognizer();
          setIsRecording(true);
        }
      }}
    />
  );
}
