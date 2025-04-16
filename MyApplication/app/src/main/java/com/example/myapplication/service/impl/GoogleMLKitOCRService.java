package com.example.myapplication.service.impl;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import com.example.myapplication.service.OCRService;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.IOException;

/**
 * Google ML Kit OCR服务实现
 */
public class GoogleMLKitOCRService implements OCRService {

    private static final String TAG = "GoogleMLKitOCR";
    private Context context;
    private TextRecognizer textRecognizer;

    public GoogleMLKitOCRService(Context context) {
        this.context = context;
        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
    }

    @Override
    public void recognizeText(Bitmap bitmap, OCRCallback callback) {
        try {
            InputImage image = InputImage.fromBitmap(bitmap, 0);
            processImage(image, callback);
        } catch (Exception e) {
            Log.e(TAG, "Error creating input image from bitmap: " + e.getMessage());
            callback.onError("图像处理失败: " + e.getMessage());
        }
    }

    @Override
    public void recognizeText(Uri imageUri, OCRCallback callback) {
        try {
            InputImage image = InputImage.fromFilePath(context, imageUri);
            processImage(image, callback);
        } catch (IOException e) {
            Log.e(TAG, "Error creating input image from URI: " + e.getMessage());
            callback.onError("无法读取图像文件: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Error processing image: " + e.getMessage());
            callback.onError("图像处理失败: " + e.getMessage());
        }
    }

    /**
     * 处理图像并执行OCR识别
     * @param image 待处理的图像
     * @param callback 回调接口
     */
    private void processImage(InputImage image, OCRCallback callback) {
        textRecognizer.process(image)
                .addOnSuccessListener(text -> {
                    // 处理识别结果
                    String recognizedText = processTextBlocks(text);
                    if (recognizedText.isEmpty()) {
                        callback.onError("未能识别出文字");
                    } else {
                        callback.onSuccess(recognizedText);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "OCR recognition failed: " + e.getMessage());
                    callback.onError("文字识别失败: " + e.getMessage());
                });
    }

    /**
     * 处理识别出的文本块
     * @param text 识别结果
     * @return 提取的文本
     */
    private String processTextBlocks(Text text) {
        StringBuilder sb = new StringBuilder();
        for (Text.TextBlock block : text.getTextBlocks()) {
            // 获取每个文本块的内容
            sb.append(block.getText()).append("\n");
        }
        return sb.toString().trim();
    }

    /**
     * 释放资源
     */
    public void close() {
        if (textRecognizer != null) {
            textRecognizer.close();
            textRecognizer = null;
        }
    }
} 