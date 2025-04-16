package com.example.myapplication.service;

import com.example.myapplication.model.TranslationResult;

/**
 * 翻译服务接口
 */
public interface TranslationService {

    /**
     * 文本翻译
     * @param sourceText 源文本
     * @param targetLanguage 目标语言代码
     * @param sourceLanguage 源语言代码 (可选，auto表示自动检测)
     * @param callback 回调接口
     */
    void translateText(String sourceText, String targetLanguage, String sourceLanguage, TranslationCallback callback);

    /**
     * 翻译回调接口
     */
    interface TranslationCallback {
        /**
         * 翻译成功时回调
         * @param result 翻译结果
         */
        void onSuccess(TranslationResult result);

        /**
         * 翻译失败时回调
         * @param errorMessage 错误信息
         */
        void onError(String errorMessage);
    }
} 