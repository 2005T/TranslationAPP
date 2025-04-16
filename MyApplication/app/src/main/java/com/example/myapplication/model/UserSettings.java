package com.example.myapplication.model;

import com.example.myapplication.service.TranslationServiceFactory.TranslationServiceType;

/**
 * 用户设置类，用于存储用户偏好
 */
public class UserSettings {
    private String defaultSourceLanguage;
    private String defaultTargetLanguage;
    private float speechRate;  // 语音播放速度 (0.5f-2.0f)
    private boolean autoSaveHistory;  // 是否自动保存历史记录
    private boolean darkModeEnabled;  // 是否启用深色模式
    private TranslationServiceType translationServiceType; // 翻译服务类型
    
    // 单例模式
    private static UserSettings instance;
    
    // 私有构造函数
    private UserSettings() {
        // 默认设置
        this.defaultSourceLanguage = "auto";  // 自动检测
        this.defaultTargetLanguage = "zh";    // 中文
        this.speechRate = 1.0f;               // 正常语速
        this.autoSaveHistory = true;          // 默认自动保存历史
        this.darkModeEnabled = false;         // 默认浅色模式
        this.translationServiceType = TranslationServiceType.GOOGLE_ML_KIT; // 默认使用GoogleMLKit
    }
    
    // 获取单例实例
    public static UserSettings getInstance() {
        if (instance == null) {
            instance = new UserSettings();
        }
        return instance;
    }
    
    // Getters and Setters
    public String getDefaultSourceLanguage() {
        return defaultSourceLanguage;
    }
    
    public void setDefaultSourceLanguage(String defaultSourceLanguage) {
        this.defaultSourceLanguage = defaultSourceLanguage;
    }
    
    public String getDefaultTargetLanguage() {
        return defaultTargetLanguage;
    }
    
    public void setDefaultTargetLanguage(String defaultTargetLanguage) {
        this.defaultTargetLanguage = defaultTargetLanguage;
    }
    
    public float getSpeechRate() {
        return speechRate;
    }
    
    public void setSpeechRate(float speechRate) {
        // 确保语速在有效范围内
        if (speechRate < 0.5f) {
            this.speechRate = 0.5f;
        } else if (speechRate > 2.0f) {
            this.speechRate = 2.0f;
        } else {
            this.speechRate = speechRate;
        }
    }
    
    public boolean isAutoSaveHistory() {
        return autoSaveHistory;
    }
    
    public void setAutoSaveHistory(boolean autoSaveHistory) {
        this.autoSaveHistory = autoSaveHistory;
    }
    
    public boolean isDarkModeEnabled() {
        return darkModeEnabled;
    }
    
    public void setDarkModeEnabled(boolean darkModeEnabled) {
        this.darkModeEnabled = darkModeEnabled;
    }
    
    public TranslationServiceType getTranslationServiceType() {
        return translationServiceType;
    }
    
    public void setTranslationServiceType(TranslationServiceType translationServiceType) {
        this.translationServiceType = translationServiceType;
    }
} 