package com.example.myapplication.util;

import androidx.room.TypeConverter;

import java.util.Date;

/**
 * Room数据库日期类型转换器
 */
public class DateConverter {
    
    /**
     * 将时间戳转换为Date对象
     * @param timestamp 时间戳 (毫秒)
     * @return Date对象
     */
    @TypeConverter
    public static Date fromTimestamp(Long timestamp) {
        return timestamp == null ? null : new Date(timestamp);
    }
    
    /**
     * 将Date对象转换为时间戳
     * @param date Date对象
     * @return 时间戳 (毫秒)
     */
    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }
} 