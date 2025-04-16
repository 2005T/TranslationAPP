package com.example.myapplication.database;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.myapplication.model.TranslationResult;
import com.example.myapplication.util.DateConverter;

import java.util.Date;
import java.util.UUID;

/**
 * 翻译历史数据库实体类
 */
@Entity(tableName = "translation_history")
@TypeConverters(DateConverter.class)
public class TranslationHistoryEntity {
    
    @PrimaryKey
    @NonNull
    private String id;
    
    private String sourceText;
    private String translatedText;
    private String sourceLanguage;
    private String targetLanguage;
    
    @TypeConverters(DateConverter.class)
    private Date timestamp;
    
    private String type; // 存储为字符串: "TEXT", "SPEECH", "IMAGE"
    
    public TranslationHistoryEntity() {
        this.id = UUID.randomUUID().toString();
        this.timestamp = new Date();
    }
    
    // 从TranslationResult创建实体
    public static TranslationHistoryEntity fromTranslationResult(TranslationResult result) {
        TranslationHistoryEntity entity = new TranslationHistoryEntity();
        if (result.getId() != null) {
            entity.setId(result.getId());
        }
        entity.setSourceText(result.getSourceText());
        entity.setTranslatedText(result.getTranslatedText());
        entity.setSourceLanguage(result.getSourceLanguage());
        entity.setTargetLanguage(result.getTargetLanguage());
        entity.setTimestamp(result.getTimestamp());
        entity.setType(result.getType().name());
        return entity;
    }
    
    // 转换为TranslationResult
    public TranslationResult toTranslationResult() {
        TranslationResult result = new TranslationResult();
        result.setId(this.id);
        result.setSourceText(this.sourceText);
        result.setTranslatedText(this.translatedText);
        result.setSourceLanguage(this.sourceLanguage);
        result.setTargetLanguage(this.targetLanguage);
        result.setTimestamp(this.timestamp);
        result.setType(TranslationResult.TranslationType.valueOf(this.type));
        return result;
    }
    
    // Getters and Setters
    @NonNull
    public String getId() {
        return id;
    }
    
    public void setId(@NonNull String id) {
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
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
} 