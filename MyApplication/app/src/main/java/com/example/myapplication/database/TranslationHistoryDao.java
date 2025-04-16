package com.example.myapplication.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.myapplication.model.TranslationResult;

import java.util.Date;
import java.util.List;

/**
 * 翻译历史数据访问对象(DAO)
 */
@Dao
public interface TranslationHistoryDao {
    
    /**
     * 插入一条翻译记录
     * @param translationResult 翻译结果
     */
    @Insert
    void insert(TranslationHistoryEntity translationResult);
    
    /**
     * 更新翻译记录
     * @param translationResult 翻译结果
     */
    @Update
    void update(TranslationHistoryEntity translationResult);
    
    /**
     * 删除翻译记录
     * @param translationResult 待删除的翻译记录
     */
    @Delete
    void delete(TranslationHistoryEntity translationResult);
    
    /**
     * 删除所有翻译历史
     */
    @Query("DELETE FROM translation_history")
    void deleteAll();
    
    /**
     * 获取所有翻译历史记录，按时间戳降序排列
     * @return 所有翻译历史记录
     */
    @Query("SELECT * FROM translation_history ORDER BY timestamp DESC")
    List<TranslationHistoryEntity> getAllHistory();
    
    /**
     * 根据源语言获取翻译历史
     * @param sourceLanguage 源语言代码
     * @return 匹配的翻译历史
     */
    @Query("SELECT * FROM translation_history WHERE sourceLanguage = :sourceLanguage ORDER BY timestamp DESC")
    List<TranslationHistoryEntity> getHistoryBySourceLanguage(String sourceLanguage);
    
    /**
     * 根据目标语言获取翻译历史
     * @param targetLanguage 目标语言代码
     * @return 匹配的翻译历史
     */
    @Query("SELECT * FROM translation_history WHERE targetLanguage = :targetLanguage ORDER BY timestamp DESC")
    List<TranslationHistoryEntity> getHistoryByTargetLanguage(String targetLanguage);
    
    /**
     * 根据翻译类型获取历史
     * @param type 翻译类型
     * @return 匹配的翻译历史
     */
    @Query("SELECT * FROM translation_history WHERE type = :type ORDER BY timestamp DESC")
    List<TranslationHistoryEntity> getHistoryByType(String type);
    
    /**
     * 获取指定日期范围内的翻译历史
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 在给定日期范围内的翻译历史
     */
    @Query("SELECT * FROM translation_history WHERE timestamp BETWEEN :startDate AND :endDate ORDER BY timestamp DESC")
    List<TranslationHistoryEntity> getHistoryByDateRange(Date startDate, Date endDate);
    
    /**
     * 搜索翻译历史
     * @param query 搜索关键字
     * @return 匹配的翻译历史
     */
    @Query("SELECT * FROM translation_history WHERE sourceText LIKE '%' || :query || '%' OR translatedText LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    List<TranslationHistoryEntity> searchHistory(String query);
} 