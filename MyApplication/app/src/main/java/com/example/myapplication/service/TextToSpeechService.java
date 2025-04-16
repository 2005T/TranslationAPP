package com.example.myapplication.service;

/**
 * 文本转语音服务接口
 */
public interface TextToSpeechService {
    
    /**
     * 初始化语音合成引擎
     * @param callback 初始化回调
     */
    void initialize(InitCallback callback);
    
    /**
     * 播放文本
     * @param text 要播放的文本
     * @param languageCode 语言代码
     * @param speechRate 语速 (0.5-2.0)
     */
    void speak(String text, String languageCode, float speechRate);
    
    /**
     * 停止播放
     */
    void stop();
    
    /**
     * 释放资源
     */
    void shutdown();
    
    /**
     * 检查语言是否可用
     * @param languageCode 语言代码
     * @return 是否支持该语言
     */
    boolean isLanguageAvailable(String languageCode);
    
    /**
     * 初始化回调接口
     */
    interface InitCallback {
        /**
         * 初始化成功
         */
        void onSuccess();
        
        /**
         * 初始化失败
         * @param errorMessage 错误信息
         */
        void onError(String errorMessage);
    }
} 