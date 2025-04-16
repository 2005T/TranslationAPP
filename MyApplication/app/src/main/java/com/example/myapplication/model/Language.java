package com.example.myapplication.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 语言模型类，定义支持的语言
 */
public class Language {
    private String code;       // 语言代码，如 "en"
    private String name;       // 语言名称，如 "English"
    private String localName;  // 本地化语言名称，如 "英语"
    
    public Language(String code, String name, String localName) {
        this.code = code;
        this.name = name;
        this.localName = localName;
    }
    
    // 获取支持的语言列表
    public static List<Language> getSupportedLanguages() {
        List<Language> languages = new ArrayList<>();
        
        // 添加支持的语言
        languages.add(new Language("auto", "Auto Detect", "自动检测"));
        languages.add(new Language("zh", "Chinese", "中文"));
        languages.add(new Language("en", "English", "英语"));
        languages.add(new Language("ja", "Japanese", "日语"));
        languages.add(new Language("ko", "Korean", "韩语"));
        languages.add(new Language("fr", "French", "法语"));
        languages.add(new Language("de", "German", "德语"));
        languages.add(new Language("es", "Spanish", "西班牙语"));
        languages.add(new Language("it", "Italian", "意大利语"));
        languages.add(new Language("ru", "Russian", "俄语"));
        languages.add(new Language("pt", "Portuguese", "葡萄牙语"));
        languages.add(new Language("ar", "Arabic", "阿拉伯语"));
        languages.add(new Language("hi", "Hindi", "印地语"));
        
        return languages;
    }
    
    // 根据语言代码查找语言
    public static Language findLanguageByCode(String code) {
        for (Language language : getSupportedLanguages()) {
            if (language.getCode().equals(code)) {
                return language;
            }
        }
        return null;  // 未找到匹配的语言
    }
    
    // Getters
    public String getCode() {
        return code;
    }
    
    public String getName() {
        return name;
    }
    
    public String getLocalName() {
        return localName;
    }
    
    @Override
    public String toString() {
        return localName + " (" + name + ")";
    }
} 