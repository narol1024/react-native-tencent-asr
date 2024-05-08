import React, { useState, useEffect } from 'react';

import { Button, StyleSheet, View } from 'react-native';
import { RealTimeRecognizerModule } from 'react-native-tencent-asr';
import { APP_ID, SECRET_ID, SECRET_KEY } from './constants';

export function RealTimeRecognizerApp(props: any) {
  const [isRecording, setIsRecording] = useState(false);
  useEffect(() => {
    RealTimeRecognizerModule.addListener(
      'onSegmentSuccessRecognize',
      (result) => {
        console.log(result);
        props.onRecognize(result.text);
      }
    );
    RealTimeRecognizerModule.addListener('onError', (error) => {
      console.error('发生错误: ', error);
    });
    return () => {
      RealTimeRecognizerModule.removeAllListeners('onSegmentSuccessRecognize');
      RealTimeRecognizerModule.removeAllListeners('onError');
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
    <View style={styles.container}>
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
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    width: '100%',
  },
});
