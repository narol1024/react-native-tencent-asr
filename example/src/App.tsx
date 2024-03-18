import * as React from 'react';

import { StyleSheet, View, Text, Button } from 'react-native';
import { initAsr, recognizeFile } from 'react-native-tencent-asr';
import { AudioRecorder, AudioUtils } from 'react-native-audio';

initAsr({
  appId: '',
  secretId: '',
  secretKey: '',
});

export default function App() {
  const [result, setResult] = React.useState<string>('');

  React.useEffect(() => {
    AudioRecorder.requestAuthorization().then((isAuthor) => {
      console.log('是否授权: ' + isAuthor);
      if (!isAuthor) {
        console.log('APP需要使用录音，请打开录音权限允许APP使用');
        return;
      }
      const audioPath =
        AudioUtils.DocumentDirectoryPath +
        `/quick_audio_${new Date().getTime()}.aac`;
      const option = {
        SampleRate: 44100.0, //采样率
        Channels: 2, //通道
        AudioQuality: 'High', //音质
        AudioEncoding: 'aac', //音频编码 aac
        OutputFormat: 'mpeg_4', //输出格式
        MeteringEnabled: false, //是否计量
        MeasurementMode: false, //测量模式
        AudioEncodingBitRate: 32000, //音频编码比特率
        IncludeBase64: true, //是否是base64格式
        AudioSource: 0, //音频源
      } as any;
      AudioRecorder.prepareRecordingAtPath(audioPath, option);
      // 录音进展
      AudioRecorder.onProgress = (data) => {
        console.log('录音中数据', data);
      };
      // 完成录音
      AudioRecorder.onFinished = (data) => {
        // data 录音数据，可以在此存储需要传给接口的路径数据
        console.log('录音结束数据', data);
        recognizeFile({
          filePath: audioPath,
        }).then(setResult);
      };
    });
  }, []);

  return (
    <View style={styles.container}>
      <Text>Result: {result}</Text>
      <Button
        title="开始录音"
        onPress={async () => {
          try {
            await AudioRecorder.startRecording();
          } catch (err) {
            console.error(err);
          }
        }}
      />
      <Button
        title="结束录音"
        onPress={async () => {
          try {
            await AudioRecorder.stopRecording();
          } catch (err) {
            console.error(err);
          }
        }}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  box: {
    width: 60,
    height: 60,
    marginVertical: 20,
  },
});
