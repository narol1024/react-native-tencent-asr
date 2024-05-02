import { NativeModules } from 'react-native';

interface CommonParams {
  appId: string;
  secretId: string;
  secretKey: string;
  token?: string;
}

interface RealTimeRecognizerParams {
  // 请求超时时间（秒）
  requestTimeout?: number;
  // 语音分片时长默认40ms（无特殊需求不建议更改）
  sliceTime?: number;
  // 是否检测音量
  enableDetectVolume?: boolean;
  // 是否检测到静音停止识别
  endRecognizeWhenDetectSilence?: boolean;
  // 仅限使用SDK内置录音器有效，是否保存录音文件到本地 默认关闭
  shouldSaveAsFile?: boolean;
  // 开启shouldSaveAsFile后音频保存的路径，仅限使用SDK内置录音器有效
  saveFilePath?: string;
  // 设置引擎，不设置默认16k_zh
  engineModelType?: string;
  // 是否过滤脏词，具体的取值见API文档的filter_dirty参数
  filterDirty?: number;
  // 过滤语气词具体的取值见API文档的filter_modal参数
  filterModal?: number;
  // 过滤句末的句号具体的取值见API文档的filter_punc参数
  filterPunc?: number;
  // 是否进行阿拉伯数字智能转换。具体的取值见API文档的convert_num_mode参数
  convertNumMode?: number;
  // 热词id。具体的取值见API文档的hotword_id参数
  hotwordId?: string;
  // 自学习模型id,详情见API文档
  customizationId?: string;
  // 语音断句检测阈值,详情见API文档
  vadSilenceTime?: number;
  // 默认1 0：关闭 vad，1：开启 vad。
  // 如果语音分片长度超过60秒，用户需开启 vad。
  needvad?: number;
  // 是否显示词级别时间戳。,详情见API文档
  wordInfo?: number;
  // 热词增强功能 0: 关闭, 1: 开启 默认0
  reinforceHotword?: number;
  // 噪音参数阈值，默认为0，取值范围：[-1,1]
  noiseThreshold?: number;
  // 强制断句功能，取值范围 5000-90000(单位:毫秒），默认值0(不开启)。
  // 在连续说话不间断情况下，该参数将实现强制断句（此时结果变成稳态，slice_type=2）。
  // 如：游戏解说场景，解说员持续不间断解说，无法断句的情况下，将此参数设置为10000，则将在每10秒收到
  // slice_type=2的回调。
  maxSpeakTime?: number;
}

interface RecognizeWithUrlParams {
  url: string;
  voiceFormat?: string;
  engineModelType?: string;
}

interface RecognizeWithParams extends Omit<RecognizeWithUrlParams, 'url'> {
  url?: string;
  filePath?: string;
  /**
   * 音频格式，默认为aac。
   * 支持 wav、pcm、ogg-opus、speex、silk、mp3、m4a、aac。
   */
  voiceFormat?: string;
  /**
   * 设置识别引擎,支持的识别引擎以API文档为准。
   * 见https://cloud.tencent.com/document/product/1093/35646
   */
  engineModelType?: string;
  /**
   * 是否过滤脏词（目前支持中文普通话引擎）。
   * 0：不过滤脏词；1：过滤脏词；2：将脏词替换为 * 。
   * 默认值为 0。
   */
  filterDirty?: number;
  /**
   * 是否过语气词（目前支持中文普通话引擎）。
   * 0：不过滤语气词；1：部分过滤；2：严格过滤。
   * 默认值为 0。
   */
  filterModal?: number;
  /**
   * 是否过滤标点符号（目前支持中文普通话引擎）。
   * 0：不过滤，1：过滤句末标点，2：过滤所有标点。默认值为 0。
   */
  filterPunc?: number;
  /**
   * 是否进行阿拉伯数字智能转换。
   * 0：不转换，直接输出中文数字，1：根据场景智能转换为阿拉伯数字。
   * 默认值为 1。
   */
  convertNumMode?: number;
  /**
   * 是否显示词级别时间戳。
   * 0：不显示；1：显示，不包含标点时间戳，2：显示，包含标点时间戳。
   * 默认值为 0。
   */
  wordInfo?: number;
  /**
   * 热词表 id。如果设置了该参数，那么将生效对应的热词表。
   * 如果不设置该参数，默认生效的是系统的热词表。
   */
  hotwordId?: string;
}

interface FlashFileRecognizerParams {
  filePath: string;
  /**
   * 引擎模型类型, 默认16k_zh。
   * 8k_zh：8k 中文普通话通用；
   * 16k_zh：16k 中文普通话通用；
   * 16k_zh_video：16k 音视频领域。
   */
  engineModelType?: string;
  /**
   * 音频格式，默认为aac。
   * 支持 wav、pcm、ogg-opus、speex、silk、mp3、m4a、aac。
   */
  voiceFormat?: string;
  // 0 ：默认状态 不过滤脏话 1：过滤脏话
  filterDirty?: number;
  // 0 ：默认状态 不过滤语气词  1：过滤部分语气词 2:严格过滤
  filterModal?: number;
  // 0 ：默认状态 不过滤句末的句号 1：滤句末的句号
  filterPunc?: number;
  /**
   * 1：默认状态 根据场景智能转换为阿拉伯数字；
   * 0：全部转为中文数字。
   */
  convertNumMode?: number;
  // 是否开启说话人分离（目前支持中文普通话引擎），默认为0，0：不开启，1：开启。
  speakerDiarization?: number;
  // 是否只识别首个声道，默认为1。0：识别所有声道；1：识别首个声道。
  firstChannelOnly?: number;
  // 是否显示词级别时间戳，默认为0。0：不显示；1：显示，不包含标点时间戳，2：显示，包含标点时间戳。
  wordInfo?: number;
  // 自学习模型 id。如设置了该参数，将生效对应的自学习模型。
  customizationID?: string;
  /**
   * 热词表 id。如不设置该参数，自动生效默认热词表；
   * 如设置了该参数，那么将生效对应的热词表。
   */
  hotwordID?: string;
}

// 实时语音模块
export const RealTimeRecognizerModule = {
  configure(params: CommonParams & RealTimeRecognizerParams): void {
    NativeModules.RealTimeRecognizerModule.configure(params);
  },
  startRealTimeRecognizer(): Promise<string> {
    return NativeModules.RealTimeRecognizerModule.startRealTimeRecognizer();
  },
  stopRealTimeRecognizer(): Promise<string> {
    return NativeModules.RealTimeRecognizerModule.stopRealTimeRecognizer();
  },
};
// 一句话识别模块
export const OneSentenceRecognizerModule = {
  // 配置AppID、SecretID、SecretKey
  configure(params: CommonParams): void {
    NativeModules.OneSentenceRecognizerModule.configure(params);
  },
  // 一句话识别(快捷识别, 网络URL)
  recognizeWithUrl(params: RecognizeWithUrlParams) {
    NativeModules.OneSentenceRecognizerModule.recognizeWithUrl(params);
  },
  // 一句话识别(完整参数)
  recognizeWithParams(params: RecognizeWithParams) {
    NativeModules.OneSentenceRecognizerModule.recognizeWithParams(params);
  },
  // 一句话识别(内置录音器), 开始录音
  startRecognizeWithRecorder(params: { engineModelType?: string } = {}) {
    NativeModules.OneSentenceRecognizerModule.startRecognizeWithRecorder(
      params
    );
  },
  //  一句话识别(内置录音器), 停止录音
  stopRecognizeWithRecorder() {
    NativeModules.OneSentenceRecognizerModule.stopRecognizeWithRecorder();
  },
};

// 录音文件识别极速版模块
export const FlashFileRecognizerModule = {
  // 配置AppID、SecretID、SecretKey, Token
  configure(params: CommonParams): void {
    NativeModules.FlashFileRecognizerModule.configure(params);
  },
  // 快速识别
  flashFileRecognizer(params: FlashFileRecognizerParams): Promise<string> {
    return NativeModules.FlashFileRecognizerModule.flashFileRecognizer(params);
  },
};
