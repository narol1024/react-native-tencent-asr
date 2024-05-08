import React, { useEffect } from 'react';

import {
  StyleSheet,
  View,
  Text,
  SafeAreaView,
  Platform,
  PermissionsAndroid,
} from 'react-native';

import { RealTimeRecognizerApp } from './RealTimeRecognizerApp';
import { FlashFileRecognizerApp } from './FlashFileRecognizerApp';
import { OneSentenceRecognizerApp } from './OneSentenceRecognizer';

export default function App() {
  const [result, setResult] = React.useState<string>('');
  useEffect(() => {
    if (Platform.OS === 'android') {
      PermissionsAndroid.request('android.permission.RECORD_AUDIO').then(
        (granted) => {
          if (granted === PermissionsAndroid.RESULTS.GRANTED) {
            console.log('已授权录音');
          }
        }
      );
    }
  }, []);
  return (
    <SafeAreaView style={styles.app}>
      <View style={styles.container}>
        <View style={styles.resultView}>
          {result && <Text>识别结果: {result}</Text>}
        </View>
        <RealTimeRecognizerApp
          onRecognize={(result: string) => {
            setResult(result);
          }}
        />
        <OneSentenceRecognizerApp
          onRecognize={(result: string) => {
            setResult(result);
          }}
        />
        <FlashFileRecognizerApp
          onRecognize={(result: string) => {
            setResult(result);
          }}
        />
      </View>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  app: {
    width: '100%',
    height: '100%',
  },
  container: {
    flex: 1,
    padding: 10,
    alignItems: 'center',
    justifyContent: 'flex-start',
    gap: 20,
  },
  resultView: {
    width: '100%',
    minHeight: 240,
    marginVertical: 20,
    borderColor: '#888',
    borderWidth: 1,
  },
});
