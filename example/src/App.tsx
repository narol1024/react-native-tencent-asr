import React from 'react';

import { StyleSheet, View, Text, SafeAreaView } from 'react-native';

import { RealTimeRecognizerApp } from './RealTimeRecognizerApp';
import { FlashFileRecognizerApp } from './FlashFileRecognizerApp';
import { OneSentenceRecognizerApp } from './OneSentenceRecognizer';

export default function App() {
  const [result, setResult] = React.useState<string>('');
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
  },
  resultView: {
    width: '100%',
    minHeight: 240,
    marginVertical: 20,
    borderColor: '#888',
    borderWidth: 1,
  },
});
