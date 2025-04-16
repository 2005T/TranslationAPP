package com.example.myapplication.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.myapplication.util.DateConverter;

/**
 * Room数据库类，管理翻译历史记录
 */
@Database(entities = {TranslationHistoryEntity.class}, version = 1, exportSchema = false)
@TypeConverters({DateConverter.class})
public abstract class TranslationDatabase extends RoomDatabase {
    
    public abstract TranslationHistoryDao translationHistoryDao();
    
    private static volatile TranslationDatabase INSTANCE;
    
    // 单例模式获取数据库实例
    public static TranslationDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (TranslationDatabase.class) {
                if (INSTANCE == null) {
                    // 创建数据库实例
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            TranslationDatabase.class,
                            "translation_database")
                            // 允许在主线程查询数据库 (实际开发中应避免)
                            .allowMainThreadQueries()
                            // 数据库升级策略
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
} 