package com.example.myapplication.service.impl;

import android.util.Log;

import com.example.myapplication.model.TranslationResult;
import com.example.myapplication.service.TranslationService;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

/**
 * Google ML Kit 翻译服务实现
 */
public class GoogleMLKitTranslationService implements TranslationService {
    
    private static final String TAG = "GoogleMLKitTranslation";
    
    /**
     * 翻译文本
     */
    @Override
    public void translateText(String sourceText, String targetLanguage, String sourceLanguage, 
                              TranslationCallback callback) {
        // 处理自动检测语言的情况
        final String finalSourceLanguage;
        if ("auto".equals(sourceLanguage)) {
            // 注意：ML Kit不直接支持语言自动检测，如果需要，可以使用Language Identification API
            // 这里简化处理，默认为英语
            finalSourceLanguage = TranslateLanguage.ENGLISH;
        } else {
            finalSourceLanguage = sourceLanguage;
        }
        
        try {
            // 创建翻译器选项
            TranslatorOptions options = new TranslatorOptions.Builder()
                    .setSourceLanguage(finalSourceLanguage)
                    .setTargetLanguage(targetLanguage)
                    .build();
            
            // 创建翻译器
            Translator translator = Translation.getClient(options);
            
            // 下载模型（如果需要）
            DownloadConditions conditions = new DownloadConditions.Builder()
                    .requireWifi()
                    .build();
            
            translator.downloadModelIfNeeded(conditions)
                    .addOnSuccessListener(unused -> {
                        // 模型下载成功，开始翻译
                        translator.translate(sourceText)
                                .addOnSuccessListener(translatedText -> {
                                    // 翻译成功
                                    TranslationResult result = new TranslationResult(
                                            sourceText,
                                            translatedText,
                                            finalSourceLanguage,
                                            targetLanguage,
                                            TranslationResult.TranslationType.TEXT
                                    );
                                    callback.onSuccess(result);
                                    
                                    // 释放翻译器资源
                                    translator.close();
                                })
                                .addOnFailureListener(e -> {
                                    // 翻译失败
                                    Log.e(TAG, "Error translating text: " + e.getMessage());
                                    callback.onError("翻译失败: " + e.getMessage());
                                    translator.close();
                                });
                    })
                    .addOnFailureListener(e -> {
                        // 模型下载失败
                        Log.e(TAG, "Error downloading translation model: " + e.getMessage());
                        callback.onError("翻译模型下载失败: " + e.getMessage());
                        translator.close();
                    });
        } catch (Exception e) {
            Log.e(TAG, "Exception in translation service: " + e.getMessage());
            callback.onError("翻译服务异常: " + e.getMessage());
        }
    }
} 