package com.example.myapplication.model;

import java.util.Date;

/**
 * 翻译结果实体类，包含原文和译文以及相关元数据
 */
public class TranslationResult {
    private String id;
    private String sourceText;
    private String translatedText;
    private String sourceLanguage;
    private String targetLanguage;
    private Date timestamp;
    private TranslationType type;
    
    // 翻译类型枚举
    public enum TranslationType {
        TEXT,    // 文本翻译
        SPEECH,  // 语音翻译
        IMAGE    // 图片OCR翻译
    }
    
    // 构造函数
    public TranslationResult() {
        this.timestamp = new Date();
    }
    
    public TranslationResult(String sourceText, String translatedText, 
                            String sourceLanguage, String targetLanguage,
                            TranslationType type) {
        this.sourceText = sourceText;
        this.translatedText = translatedText;
        this.sourceLanguage = sourceLanguage;
        this.targetLanguage = targetLanguage;
        this.timestamp = new Date();
        this.type = type;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getSourceText() {
        return sourceText;
    }
    
    public void setSourceText(String sourceText) {
        this.sourceText = sourceText;
    }
    
    public String getTranslatedText() {
        return translatedText;
    }
    
    public void setTranslatedText(String translatedText) {
        this.translatedText = translatedText;
    }
    
    public String getSourceLanguage() {
        return sourceLanguage;
    }
    
    public void setSourceLanguage(String sourceLanguage) {
        this.sourceLanguage = sourceLanguage;
    }
    
    public String getTargetLanguage() {
        return targetLanguage;
    }
    
    public void setTargetLanguage(String targetLanguage) {
        this.targetLanguage = targetLanguage;
    }
    
    public Date getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
    
    public TranslationType getType() {
        return type;
    }
    
    public void setType(TranslationType type) {
        this.type = type;
    }
} 