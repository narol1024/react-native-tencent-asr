import React, { useEffect } from 'react';

import { Button, StyleSheet, View } from 'react-native';
import { FlashFileRecognizerModule } from 'react-native-tencent-asr';
import { APP_ID, SECRET_ID, SECRET_KEY } from './constants';
import RNFetchBlob from 'rn-fetch-blob';

const dirs = RNFetchBlob.fs.dirs;

export function FlashFileRecognizerApp(props: any) {
  useEffect(() => {
    FlashFileRecognizerModule.configure({
      appId: APP_ID,
      secretId: SECRET_ID,
      secretKey: SECRET_KEY,
    });
  }, []);
  return (
    <View style={styles.container}>
      <Button
        title="录音文件识别极速版"
        onPress={async () => {
          try {
            await RNFetchBlob.config({
              path: dirs.DocumentDir + '/202405012158499.mp3',
            }).fetch(
              'GET',
              'https://narol-blog.oss-cn-beijing.aliyuncs.com/blog-img/202405012158499.mp3'
            );
            const result = await FlashFileRecognizerModule.flashFileRecognizer({
              filePath: dirs.DocumentDir + '/202405012158499.mp3',
              voiceFormat: 'mp3',
            });
            props.onRecognize(result);
          } catch (error) {
            props.onRecognize('识别错误');
          }
        }}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    width: '100%',
    borderTopColor: '#dedede',
    borderTopWidth: 1,
  },
});
