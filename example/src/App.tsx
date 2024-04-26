import * as React from 'react';

import { StyleSheet, View, Text, Button } from 'react-native';
import { configureAsrParams, recognizeFile } from 'react-native-tencent-asr';
import AudioRecorderPlayer, {
  AVEncodingOption,
} from 'react-native-audio-recorder-player';

const APP_ID = process.env.APP_ID || '';
const SECRET_ID = process.env.SECRET_ID || '';
const SECRET_KEY = process.env.SECRET_KEY || '';

configureAsrParams({
  appId: APP_ID,
  secretId: SECRET_ID,
  secretKey: SECRET_KEY,
});

const audioRecorderPlayer = new AudioRecorderPlayer();

export default function App() {
  const [result, setResult] = React.useState<string>('');
  const [isRecording, setIsRecording] = React.useState(false);
  return (
    <View style={styles.container}>
      {result && <Text>录音识别结果: {result}</Text>}
      <View></View>
      <Button
        title={isRecording ? '停止录音' : '开始录音'}
        onPress={async () => {
          if (isRecording) {
            try {
              const audioPath = await audioRecorderPlayer.stopRecorder();
              const result = await recognizeFile({
                filePath: audioPath.replace('file://', ''),
              });
              setResult(result);
            } catch (err) {
              console.error(err);
            } finally {
              setIsRecording(false);
            }
          } else {
            try {
              const audioSet = {
                AVFormatIDKeyIOS: AVEncodingOption.aac,
              };

              await audioRecorderPlayer.startRecorder(undefined, audioSet);
              audioRecorderPlayer.addRecordBackListener(() => {});
            } catch (err) {
              console.error(err);
            } finally {
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
