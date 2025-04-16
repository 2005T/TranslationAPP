package com.example.myapplication.service;

import android.graphics.Bitmap;
import android.net.Uri;

/**
 * OCR图像文字识别服务接口
 */
public interface OCRService {
    
    /**
     * 从位图中识别文字
     * @param bitmap 待识别的图像
     * @param callback 识别回调
     */
    void recognizeText(Bitmap bitmap, OCRCallback callback);
    
    /**
     * 从图像URI中识别文字
     * @param imageUri 图像URI
     * @param callback 识别回调
     */
    void recognizeText(Uri imageUri, OCRCallback callback);
    
    /**
     * OCR识别回调接口
     */
    interface OCRCallback {
        /**
         * 识别成功回调
         * @param text 识别出的文本
         */
        void onSuccess(String text);
        
        /**
         * 识别失败回调
         * @param errorMessage 错误信息
         */
        void onError(String errorMessage);
    }
} 