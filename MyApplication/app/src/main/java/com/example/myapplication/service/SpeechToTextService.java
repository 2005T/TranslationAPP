package com.example.myapplication.service;

/**
 * 语音识别服务接口
 */
public interface SpeechToTextService {

    /**
     * 开始语音识别
     * @param languageCode 语言代码
     * @param callback 回调接口
     */
    void startListening(String languageCode, SpeechRecognitionCallback callback);

    /**
     * 停止语音识别
     */
    void stopListening();

    /**
     * 语音识别回调接口
     */
    interface SpeechRecognitionCallback {
        /**
         * 识别结果回调
         * @param text 识别出的文本
         */
        void onResult(String text);

        /**
         * 识别错误回调
         * @param errorMessage 错误信息
         */
        void onError(String errorMessage);

        /**
         * 识别准备就绪回调
         */
        void onReadyForSpeech();

        /**
         * 识别开始回调
         */
        void onBeginningOfSpeech();

        /**
         * 识别结束回调
         */
        void onEndOfSpeech();
    }
} 