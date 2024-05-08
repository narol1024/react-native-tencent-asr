// @see 更多类型参考文档 https://cloud.tencent.com/document/product/1093/48982
export interface CommonParams {
  // 腾讯云 appId
  appId: string;
  // 腾讯云 secretId
  secretId: string;
  // 腾讯云 secretKey
  secretKey: string;
  // 临时token鉴权
  token?: string;
  // 不设置默认使用0, 用于按项目管理云资源，可以对云资源进行分项目管理，详情见 https://console.cloud.tencent.com/project
  projectId?: number;
}

export interface RecognizerRequestParams {
  // 引擎模型类型, 默认16k_zh
  engineModelType?: string;
  // 音频格式, 默认为aac
  voiceFormat?: string;
  // 是否过滤脏词, 默认不过滤, 0为不过滤脏话, 1为过滤脏话
  filterDirty?: number;
  // 是否过语气词（目前支持中文普通话引擎）, 默认不过滤, 0为不过滤语气词, 1为过滤部分语气词, 2为严格过滤
  filterModal?: number;
  // 是否过滤标点符号（目前支持中文普通话引擎）, 0为不过滤句末的句号, 1为滤句末的句号
  filterPunc?: number;
  // 是否进行阿拉伯数字智能转换。默认值为1, 0为不转换, 直接输出中文数字, 1为根据场景智能转换为阿拉伯数字。
  convertNumMode?: number;
  // 是否开启说话人分离（目前支持中文普通话引擎）, 默认为不开启, 0为不开启, 1为开启。
  speakerDiarization?: number;
  // 是否只识别首个声道, 默认为1, 0：识别所有声道, 1为识别首个声道。
  firstChannelOnly?: number;
  // 是否显示词级别时间戳, 默认为0, 0为不显示；1为显示, 不包含标点时间戳, 2为显示, 包含标点时间戳。
  wordInfo?: number;
  // 自学习模型 id。如设置了该参数, 将生效对应的自学习模型。
  customizationID?: string;
  // 热词表 id。如不设置该参数, 自动生效默认热词表, 如设置了该参数, 那么将生效对应的热词表。
  hotwordId?: string;
}

export type RecognizerError = {
  code: number;
  message: string;
};

export interface FlashFileRecognizerResult {
  requestId: string;
  code: number;
  message: string;
  audioDuration: number;
  flashResult: {
    text: string;
    channelId: number;
    sentenceList: {
      text: string;
      startTime: number;
      endTime: number;
      speakerId: number;
    }[];
  }[];
}

export interface FlashFileRecognizerParams extends RecognizerRequestParams {
  // 自学习模型id,详情见API文档
  customizationId?: string;
}

export interface OneSentenceRecognizerParams extends RecognizerRequestParams {}

export interface RecognizeWithUrlParams extends OneSentenceRecognizerParams {
  url: string;
}

export interface RecognizeWithParams extends OneSentenceRecognizerParams {
  url?: string;
  audioFilePath?: string;
}

export interface RealTimeRecognizeParams extends RecognizerRequestParams {
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
  // 自学习模型id,详情见API文档
  customizationId?: string;
  // 语音断句检测阈值,详情见API文档
  vadSilenceTime?: number;
  // 默认1 0：关闭 vad，1：开启 vad。
  // 如果语音分片长度超过60秒，用户需开启 vad。
  needvad?: number;
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
