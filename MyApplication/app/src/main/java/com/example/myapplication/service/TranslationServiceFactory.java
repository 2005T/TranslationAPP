package com.example.myapplication.service;

import com.example.myapplication.model.UserSettings;
import com.example.myapplication.service.impl.GoogleMLKitTranslationService;
import com.example.myapplication.service.impl.TencentTMTTranslationService;

/**
 * 翻译服务工厂类，用于获取当前使用的翻译服务
 */
public class TranslationServiceFactory {
    
    public enum TranslationServiceType {
        GOOGLE_ML_KIT,   // Google ML Kit 本地翻译
        TENCENT_TMT      // 腾讯机器翻译服务 (需联网)
    }
    
    private static TranslationService googleMLKitService;
    private static TranslationService tencentTMTService;
    
    /**
     * 获取默认的翻译服务实例
     * @return 翻译服务实例
     */
    public static TranslationService getDefaultService() {
        TranslationServiceType type = UserSettings.getInstance().getTranslationServiceType();
        return getService(type);
    }
    
    /**
     * 获取指定类型的翻译服务实例
     * @param type 翻译服务类型
     * @return 翻译服务实例
     */
    public static TranslationService getService(TranslationServiceType type) {
        switch (type) {
            case GOOGLE_ML_KIT:
                return getGoogleMLKitService();
            case TENCENT_TMT:
                return getTencentTMTService();
            default:
                return getGoogleMLKitService(); // 默认使用Google ML Kit
        }
    }
    
    /**
     * 获取Google ML Kit翻译服务实例（单例模式）
     * @return Google ML Kit翻译服务实例
     */
    private static TranslationService getGoogleMLKitService() {
        if (googleMLKitService == null) {
            googleMLKitService = new GoogleMLKitTranslationService();
        }
        return googleMLKitService;
    }
    
    /**
     * 获取腾讯TMT翻译服务实例（单例模式）
     * @return 腾讯TMT翻译服务实例
     */
    private static TranslationService getTencentTMTService() {
        if (tencentTMTService == null) {
            tencentTMTService = new TencentTMTTranslationService();
        }
        return tencentTMTService;
    }
} 