package com.example.myapplication.service.impl;

import android.content.Context;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import com.example.myapplication.service.TextToSpeechService;

import java.util.Locale;

/**
 * Android原生TextToSpeech服务实现
 */
public class AndroidTextToSpeechService implements TextToSpeechService {
    
    private static final String TAG = "AndroidTTS";
    private TextToSpeech tts;
    private Context context;
    private boolean isInitialized = false;
    
    public AndroidTextToSpeechService(Context context) {
        this.context = context;
    }
    
    @Override
    public void initialize(InitCallback callback) {
        if (tts != null) {
            shutdown();
        }
        
        tts = new TextToSpeech(context, status -> {
            if (status == TextToSpeech.SUCCESS) {
                isInitialized = true;
                callback.onSuccess();
            } else {
                Log.e(TAG, "Failed to initialize TTS engine: " + status);
                callback.onError("初始化语音引擎失败");
            }
        });
    }
    
    @Override
    public void speak(String text, String languageCode, float speechRate) {
        if (!isInitialized || tts == null) {
            Log.e(TAG, "TTS not initialized");
            return;
        }
        
        // 设置语言
        Locale locale = getLocaleFromLanguageCode(languageCode);
        int result = tts.setLanguage(locale);
        
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            Log.e(TAG, "Language not supported: " + languageCode);
            return;
        }
        
        // 设置语速
        tts.setSpeechRate(speechRate);
        
        // 播放文本
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "UniqueID_" + System.currentTimeMillis());
        } else {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
    }
    
    @Override
    public void stop() {
        if (tts != null && isInitialized) {
            tts.stop();
        }
    }
    
    @Override
    public void shutdown() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            isInitialized = false;
            tts = null;
        }
    }
    
    @Override
    public boolean isLanguageAvailable(String languageCode) {
        if (!isInitialized || tts == null) {
            return false;
        }
        
        Locale locale = getLocaleFromLanguageCode(languageCode);
        int result = tts.isLanguageAvailable(locale);
        
        return result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED;
    }
    
    /**
     * 从语言代码获取Locale对象
     * @param languageCode 语言代码，如"en", "zh"
     * @return 对应的Locale对象
     */
    private Locale getLocaleFromLanguageCode(String languageCode) {
        switch (languageCode) {
            case "zh":
                return Locale.CHINESE;
            case "en":
                return Locale.ENGLISH;
            case "fr":
                return Locale.FRENCH;
            case "de":
                return Locale.GERMAN;
            case "it":
                return Locale.ITALIAN;
            case "ja":
                return Locale.JAPANESE;
            case "ko":
                return Locale.KOREAN;
            default:
                // 尝试直接从语言代码创建Locale
                return new Locale(languageCode);
        }
    }
} 