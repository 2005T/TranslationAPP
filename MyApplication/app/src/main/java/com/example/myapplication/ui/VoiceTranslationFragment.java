package com.example.myapplication.ui;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.myapplication.databinding.FragmentVoiceTranslationBinding;
import com.example.myapplication.model.Language;
import com.example.myapplication.model.TranslationResult;
import com.example.myapplication.model.UserSettings;
import com.example.myapplication.service.SpeechToTextService;
import com.example.myapplication.service.TextToSpeechService;
import com.example.myapplication.service.TranslationHistoryManager;
import com.example.myapplication.service.TranslationService;
import com.example.myapplication.service.impl.AndroidSpeechToTextService;
import com.example.myapplication.service.impl.AndroidTextToSpeechService;
import com.example.myapplication.service.impl.GoogleMLKitTranslationService;

import java.util.ArrayList;
import java.util.List;

public class VoiceTranslationFragment extends Fragment {

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;

    private FragmentVoiceTranslationBinding binding;
    private SpeechToTextService speechToTextService;
    private TranslationService translationService;
    private TextToSpeechService ttsService;
    private TranslationHistoryManager historyManager;
    private UserSettings userSettings;
    private List<Language> languages;
    private boolean isRecording = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentVoiceTranslationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 初始化服务
        speechToTextService = new AndroidSpeechToTextService(requireContext());
        translationService = new GoogleMLKitTranslationService();
        ttsService = new AndroidTextToSpeechService(requireContext());
        historyManager = new TranslationHistoryManager(requireContext());
        userSettings = UserSettings.getInstance();

        // 初始化语音合成服务
        ttsService.initialize(new TextToSpeechService.InitCallback() {
            @Override
            public void onSuccess() {
                // TTS初始化成功
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(requireContext(), "语音引擎初始化失败: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

        // 设置语言选择器
        setupLanguageSpinners();

        // 设置按钮点击事件
        setupButtonListeners();
    }

    private void setupLanguageSpinners() {
        languages = Language.getSupportedLanguages();
        List<String> languageNames = new ArrayList<>();

        for (Language language : languages) {
            languageNames.add(language.getLocalName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                languageNames
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        binding.sourceLanguageSpinner.setAdapter(adapter);
        binding.targetLanguageSpinner.setAdapter(adapter);

        // 设置默认语言
        String defaultSourceLang = userSettings.getDefaultSourceLanguage();
        String defaultTargetLang = userSettings.getDefaultTargetLanguage();

        for (int i = 0; i < languages.size(); i++) {
            if (languages.get(i).getCode().equals(defaultSourceLang)) {
                binding.sourceLanguageSpinner.setSelection(i);
            }
            if (languages.get(i).getCode().equals(defaultTargetLang)) {
                binding.targetLanguageSpinner.setSelection(i);
            }
        }

        // 设置选择监听器
        binding.sourceLanguageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // 可以在这里保存用户选择
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        binding.targetLanguageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // 可以在这里保存用户选择
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setupButtonListeners() {
        // 录音按钮
        binding.recordButton.setOnClickListener(v -> {
            if (isRecording) {
                stopRecording();
            } else {
                startRecording();
            }
        });

        // 交换语言按钮
        binding.swapLanguageButton.setOnClickListener(v -> {
            int sourcePos = binding.sourceLanguageSpinner.getSelectedItemPosition();
            int targetPos = binding.targetLanguageSpinner.getSelectedItemPosition();
            
            // 如果源语言是自动检测，不允许交换
            if (sourcePos == 0) {
                Toast.makeText(requireContext(), "自动检测语言不能设为目标语言", Toast.LENGTH_SHORT).show();
                return;
            }
            
            binding.sourceLanguageSpinner.setSelection(targetPos);
            binding.targetLanguageSpinner.setSelection(sourcePos);
            
            // 如果已有翻译结果，交换文本
            String sourceText = binding.recognizedText.getText().toString();
            String translatedText = binding.translatedText.getText().toString();
            
            if (!sourceText.isEmpty() && !translatedText.isEmpty()) {
                binding.recognizedText.setText(translatedText);
                binding.translatedText.setText(sourceText);
            }
        });

        // 复制按钮
        binding.copyButton.setOnClickListener(v -> {
            String translatedText = binding.translatedText.getText().toString();
            if (!translatedText.isEmpty()) {
                ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Translated Text", translatedText);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(requireContext(), "已复制到剪贴板", Toast.LENGTH_SHORT).show();
            }
        });

        // 朗读按钮
        binding.speakButton.setOnClickListener(v -> {
            String translatedText = binding.translatedText.getText().toString();
            if (!translatedText.isEmpty()) {
                Language targetLanguage = languages.get(binding.targetLanguageSpinner.getSelectedItemPosition());
                ttsService.speak(translatedText, targetLanguage.getCode(), userSettings.getSpeechRate());
            }
        });

        // 分享按钮
        binding.shareButton.setOnClickListener(v -> {
            String translatedText = binding.translatedText.getText().toString();
            if (!translatedText.isEmpty()) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, translatedText);
                startActivity(Intent.createChooser(shareIntent, "分享翻译结果"));
            }
        });
    }

    private void startRecording() {
        // 检查录音权限
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);
            return;
        }

        // 更新UI
        isRecording = true;
        binding.recordButton.setBackgroundColor(Color.RED);
        binding.recordingStatusTextView.setText("正在录音...");
        binding.recordingVisualizerView.setVisibility(View.VISIBLE);

        // 清空之前的结果
        binding.recognizedText.setText("");
        binding.translatedText.setText("");

        // 获取选中的源语言
        Language sourceLanguage = languages.get(binding.sourceLanguageSpinner.getSelectedItemPosition());

        // 开始语音识别
        speechToTextService.startListening(sourceLanguage.getCode(), new SpeechToTextService.SpeechRecognitionCallback() {
            @Override
            public void onResult(String text) {
                requireActivity().runOnUiThread(() -> {
                    // 显示识别结果
                    binding.recognizedText.setText(text);
                    
                    // 自动翻译识别到的文本
                    if (!text.isEmpty()) {
                        translateRecognizedText(text);
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                requireActivity().runOnUiThread(() -> {
                    stopRecording();
                    Toast.makeText(requireContext(), "语音识别错误: " + errorMessage, Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onReadyForSpeech() {
                requireActivity().runOnUiThread(() -> {
                    binding.recordingStatusTextView.setText("请开始说话...");
                });
            }

            @Override
            public void onBeginningOfSpeech() {
                requireActivity().runOnUiThread(() -> {
                    binding.recordingStatusTextView.setText("正在聆听...");
                });
            }

            @Override
            public void onEndOfSpeech() {
                requireActivity().runOnUiThread(() -> {
                    stopRecording();
                    binding.recordingStatusTextView.setText("语音识别完成");
                });
            }
        });
    }

    private void stopRecording() {
        if (isRecording) {
            isRecording = false;
            speechToTextService.stopListening();
            binding.recordButton.setBackgroundColor(Color.parseColor("#6200EE")); // 恢复默认颜色
            binding.recordingVisualizerView.setVisibility(View.INVISIBLE);
        }
    }

    private void translateRecognizedText(String recognizedText) {
        // 显示加载指示器
        binding.progressBar.setVisibility(View.VISIBLE);

        // 获取选中的语言
        Language sourceLanguage = languages.get(binding.sourceLanguageSpinner.getSelectedItemPosition());
        Language targetLanguage = languages.get(binding.targetLanguageSpinner.getSelectedItemPosition());

        // 调用翻译服务
        translationService.translateText(recognizedText, targetLanguage.getCode(), sourceLanguage.getCode(), new TranslationService.TranslationCallback() {
            @Override
            public void onSuccess(TranslationResult result) {
                requireActivity().runOnUiThread(() -> {
                    // 隐藏加载指示器
                    binding.progressBar.setVisibility(View.GONE);
                    
                    // 显示翻译结果
                    binding.translatedText.setText(result.getTranslatedText());
                    
                    // 保存到历史记录
                    if (userSettings.isAutoSaveHistory()) {
                        result.setType(TranslationResult.TranslationType.SPEECH);
                        historyManager.saveTranslationHistory(result);
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                requireActivity().runOnUiThread(() -> {
                    // 隐藏加载指示器
                    binding.progressBar.setVisibility(View.GONE);
                    
                    // 显示错误信息
                    Toast.makeText(requireContext(), "翻译失败: " + errorMessage, Toast.LENGTH_SHORT).show();
                    binding.translatedText.setText("翻译出错，请重试。");
                });
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startRecording();
            } else {
                Toast.makeText(requireContext(), "需要录音权限才能使用语音翻译功能", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (speechToTextService instanceof AndroidSpeechToTextService) {
            ((AndroidSpeechToTextService) speechToTextService).destroy();
        }
        ttsService.shutdown();
        binding = null;
    }
} 