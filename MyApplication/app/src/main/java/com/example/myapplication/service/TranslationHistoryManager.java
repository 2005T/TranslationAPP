package com.example.myapplication.service;

import android.content.Context;

import com.example.myapplication.database.TranslationDatabase;
import com.example.myapplication.database.TranslationHistoryDao;
import com.example.myapplication.database.TranslationHistoryEntity;
import com.example.myapplication.model.TranslationResult;
import com.example.myapplication.model.UserSettings;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * 翻译历史记录管理类
 */
public class TranslationHistoryManager {
    
    private TranslationHistoryDao historyDao;
    private UserSettings userSettings;
    private Executor executor;
    
    public TranslationHistoryManager(Context context) {
        // 获取数据库实例
        TranslationDatabase db = TranslationDatabase.getDatabase(context);
        historyDao = db.translationHistoryDao();
        userSettings = UserSettings.getInstance();
        executor = Executors.newSingleThreadExecutor();
    }
    
    /**
     * 保存翻译结果到历史记录
     * @param result 翻译结果
     */
    public void saveTranslationHistory(TranslationResult result) {
        // 检查是否开启了自动保存
        if (!userSettings.isAutoSaveHistory()) {
            return;
        }
        
        executor.execute(() -> {
            // 将TranslationResult转换为数据库实体
            TranslationHistoryEntity entity = TranslationHistoryEntity.fromTranslationResult(result);
            historyDao.insert(entity);
        });
    }
    
    /**
     * 获取所有翻译历史
     * @return 翻译历史列表
     */
    public List<TranslationResult> getAllHistory() {
        List<TranslationHistoryEntity> entities = historyDao.getAllHistory();
        return convertToTranslationResults(entities);
    }
    
    /**
     * 根据源语言获取历史记录
     * @param sourceLanguage 源语言代码
     * @return 翻译历史列表
     */
    public List<TranslationResult> getHistoryBySourceLanguage(String sourceLanguage) {
        List<TranslationHistoryEntity> entities = historyDao.getHistoryBySourceLanguage(sourceLanguage);
        return convertToTranslationResults(entities);
    }
    
    /**
     * 根据目标语言获取历史记录
     * @param targetLanguage 目标语言代码
     * @return 翻译历史列表
     */
    public List<TranslationResult> getHistoryByTargetLanguage(String targetLanguage) {
        List<TranslationHistoryEntity> entities = historyDao.getHistoryByTargetLanguage(targetLanguage);
        return convertToTranslationResults(entities);
    }
    
    /**
     * 根据翻译类型获取历史记录
     * @param type 翻译类型
     * @return 翻译历史列表
     */
    public List<TranslationResult> getHistoryByType(TranslationResult.TranslationType type) {
        List<TranslationHistoryEntity> entities = historyDao.getHistoryByType(type.name());
        return convertToTranslationResults(entities);
    }
    
    /**
     * 根据日期范围获取历史记录
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 翻译历史列表
     */
    public List<TranslationResult> getHistoryByDateRange(Date startDate, Date endDate) {
        List<TranslationHistoryEntity> entities = historyDao.getHistoryByDateRange(startDate, endDate);
        return convertToTranslationResults(entities);
    }
    
    /**
     * 搜索历史记录
     * @param query 搜索关键字
     * @return 匹配的翻译历史列表
     */
    public List<TranslationResult> searchHistory(String query) {
        List<TranslationHistoryEntity> entities = historyDao.searchHistory(query);
        return convertToTranslationResults(entities);
    }
    
    /**
     * 删除历史记录
     * @param result 要删除的翻译结果
     */
    public void deleteHistory(TranslationResult result) {
        executor.execute(() -> {
            TranslationHistoryEntity entity = TranslationHistoryEntity.fromTranslationResult(result);
            historyDao.delete(entity);
        });
    }
    
    /**
     * 清空所有历史记录
     */
    public void clearAllHistory() {
        executor.execute(() -> {
            historyDao.deleteAll();
        });
    }
    
    /**
     * 将数据库实体列表转换为TranslationResult列表
     * @param entities 数据库实体列表
     * @return TranslationResult列表
     */
    private List<TranslationResult> convertToTranslationResults(List<TranslationHistoryEntity> entities) {
        List<TranslationResult> results = new ArrayList<>();
        for (TranslationHistoryEntity entity : entities) {
            results.add(entity.toTranslationResult());
        }
        return results;
    }
} 