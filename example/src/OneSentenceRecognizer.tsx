import React, { useEffect, useState } from 'react';

import { Button, StyleSheet, View } from 'react-native';
import { OneSentenceRecognizerModule } from 'react-native-tencent-asr';
import { APP_ID, SECRET_ID, SECRET_KEY } from './constants';
import RNFetchBlob from 'rn-fetch-blob';

const dirs = RNFetchBlob.fs.dirs;

export function OneSentenceRecognizerApp(props: any) {
  const [isRecording, setIsRecording] = useState(false);
  useEffect(() => {
    OneSentenceRecognizerModule.addListener('onRecognize', (result) => {
      props.onRecognize(result.result);
    });
    OneSentenceRecognizerModule.addListener('onError', (error) => {
      console.error(error);
    });
    OneSentenceRecognizerModule.addListener('onUpdateVolume', (result) => {
      console.log('>>', result);
    });
    return () => {
      OneSentenceRecognizerModule.removeAllListeners('onRecognize');
      OneSentenceRecognizerModule.removeAllListeners('onError');
    };
  }, [props]);

  useEffect(() => {
    OneSentenceRecognizerModule.configure({
      appId: APP_ID,
      secretId: SECRET_ID,
      secretKey: SECRET_KEY,
    });
  }, []);

  return (
    <View style={styles.container}>
      <Button
        title="一句话识别(网络URL)"
        onPress={async () => {
          OneSentenceRecognizerModule.recognizeWithUrl({
            url: 'https://narol-blog.oss-cn-beijing.aliyuncs.com/blog-img/202405012158499.mp3',
            voiceFormat: 'mp3',
          });
        }}
      />
      <Button
        title="一句话识别(本地音频)"
        onPress={async () => {
          try {
            await RNFetchBlob.config({
              path: dirs.DocumentDir + '/202405012158499.mp3',
            }).fetch(
              'GET',
              'https://narol-blog.oss-cn-beijing.aliyuncs.com/blog-img/202405012158499.mp3'
            );
            OneSentenceRecognizerModule.recognizeWithParams({
              audioFilePath: dirs.DocumentDir + '/202405012158499.mp3',
              voiceFormat: 'mp3',
            });
          } catch (error) {
            props.onRecognize('识别错误');
          }
        }}
      />
      <Button
        title={isRecording ? '停止录音' : '一句话识别(内置录音器)'}
        onPress={async () => {
          if (isRecording) {
            OneSentenceRecognizerModule.stopRecognizeWithRecorder();
            setIsRecording(false);
          } else {
            OneSentenceRecognizerModule.recognizeWithRecorder();
            setIsRecording(true);
          }
        }}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flexDirection: 'column',
    gap: 4,
    width: '100%',
    borderTopColor: '#dedede',
    borderTopWidth: 1,
  },
});
