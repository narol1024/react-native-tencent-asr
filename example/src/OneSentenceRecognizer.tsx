import React, { useEffect, useState } from 'react';
import { PermissionsAndroid } from 'react-native';

import { Button, StyleSheet, View } from 'react-native';
import { OneSentenceRecognizerModule } from 'react-native-tencent-asr';
import { APP_ID, SECRET_ID, SECRET_KEY } from './constants';
import RNFetchBlob from 'rn-fetch-blob';

const dirs = RNFetchBlob.fs.dirs;

export function OneSentenceRecognizerApp(props: any) {
  const [isRecording, setIsRecording] = useState(false);
  useEffect(() => {
    OneSentenceRecognizerModule.addListener('DidRecognize', (result) => {
      if (result.error) {
        console.log('语音识别失败', result.error);
        props.onRecognize('识别失败');
        return;
      }
      props.onRecognize(result.data.result);
    });
    return () => {
      OneSentenceRecognizerModule.removeAllListeners('DidRecognize');
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
          const granted = await PermissionsAndroid.request(
            'android.permission.RECORD_AUDIO'
          );
          if (granted === PermissionsAndroid.RESULTS.GRANTED) {
            if (isRecording) {
              OneSentenceRecognizerModule.stopRecognizeWithRecorder();
              setIsRecording(false);
            } else {
              OneSentenceRecognizerModule.recognizeWithRecorder();
              setIsRecording(true);
            }
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
    marginBottom: 10,
  },
});
