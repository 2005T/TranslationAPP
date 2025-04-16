package com.example.myapplication.service.impl;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

import com.example.myapplication.service.SpeechToTextService;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Android原生语音识别服务实现
 */
public class AndroidSpeechToTextService implements SpeechToTextService {
    
    private static final String TAG = "AndroidSTT";
    private SpeechRecognizer speechRecognizer;
    private Context context;
    private SpeechRecognitionCallback callback;
    private boolean isListening = false;
    
    public AndroidSpeechToTextService(Context context) {
        this.context = context;
        // 创建语音识别器
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
    }
    
    @Override
    public void startListening(String languageCode, SpeechRecognitionCallback callback) {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            callback.onError("当前设备不支持语音识别");
            return;
        }
        
        this.callback = callback;
        
        // 设置识别监听器
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                isListening = true;
                callback.onReadyForSpeech();
            }
            
            @Override
            public void onBeginningOfSpeech() {
                callback.onBeginningOfSpeech();
            }
            
            @Override
            public void onRmsChanged(float rmsdB) {
                // 音量变化回调，可用于显示音量动画
            }
            
            @Override
            public void onBufferReceived(byte[] buffer) {
                // 接收语音缓冲数据，一般不需要处理
            }
            
            @Override
            public void onEndOfSpeech() {
                isListening = false;
                callback.onEndOfSpeech();
            }
            
            @Override
            public void onError(int error) {
                isListening = false;
                String errorMessage;
                
                switch (error) {
                    case SpeechRecognizer.ERROR_AUDIO:
                        errorMessage = "音频录制错误";
                        break;
                    case SpeechRecognizer.ERROR_CLIENT:
                        errorMessage = "客户端错误";
                        break;
                    case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                        errorMessage = "权限不足";
                        break;
                    case SpeechRecognizer.ERROR_NETWORK:
                        errorMessage = "网络错误";
                        break;
                    case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                        errorMessage = "网络超时";
                        break;
                    case SpeechRecognizer.ERROR_NO_MATCH:
                        errorMessage = "未能匹配语音";
                        break;
                    case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                        errorMessage = "识别服务忙";
                        break;
                    case SpeechRecognizer.ERROR_SERVER:
                        errorMessage = "服务器错误";
                        break;
                    case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                        errorMessage = "语音超时";
                        break;
                    default:
                        errorMessage = "未知错误";
                        break;
                }
                
                callback.onError(errorMessage);
            }
            
            @Override
            public void onResults(Bundle results) {
                isListening = false;
                // 处理识别结果
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String text = matches.get(0); // 获取最佳匹配结果
                    callback.onResult(text);
                } else {
                    callback.onError("未识别到语音");
                }
            }
            
            @Override
            public void onPartialResults(Bundle partialResults) {
                // 部分识别结果，可用于实时显示
                ArrayList<String> matches = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    callback.onResult(matches.get(0));
                }
            }
            
            @Override
            public void onEvent(int eventType, Bundle params) {
                // 额外事件，一般不需要处理
            }
        });
        
        // 创建识别意图
        Intent recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        
        try {
            speechRecognizer.startListening(recognizerIntent);
        } catch (Exception e) {
            Log.e(TAG, "Error starting speech recognition: " + e.getMessage());
            callback.onError("启动语音识别失败: " + e.getMessage());
        }
    }
    
    @Override
    public void stopListening() {
        if (speechRecognizer != null && isListening) {
            speechRecognizer.stopListening();
            isListening = false;
        }
    }
    
    /**
     * 释放资源
     * 在Activity或Fragment的onDestroy中调用
     */
    public void destroy() {
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
            speechRecognizer = null;
        }
    }
} 