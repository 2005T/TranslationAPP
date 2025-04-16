package com.example.myapplication.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication.databinding.FragmentTextTranslationBinding;
import com.example.myapplication.model.Language;
import com.example.myapplication.model.TranslationResult;
import com.example.myapplication.model.UserSettings;
import com.example.myapplication.service.TextToSpeechService;
import com.example.myapplication.service.TranslationHistoryManager;
import com.example.myapplication.service.TranslationService;
import com.example.myapplication.service.impl.AndroidTextToSpeechService;
import com.example.myapplication.service.impl.GoogleMLKitTranslationService;

import java.util.ArrayList;
import java.util.List;

public class TextTranslationFragment extends Fragment {

    private FragmentTextTranslationBinding binding;
    private TranslationService translationService;
    private TextToSpeechService ttsService;
    private TranslationHistoryManager historyManager;
    private UserSettings userSettings;
    private List<Language> languages;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTextTranslationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 初始化服务
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
        // 翻译按钮
        binding.translateButton.setOnClickListener(v -> {
            translateText();
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
            String sourceText = binding.sourceText.getText().toString();
            String translatedText = binding.translatedText.getText().toString();
            
            if (!sourceText.isEmpty() && !translatedText.isEmpty()) {
                binding.sourceText.setText(translatedText);
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

    private void translateText() {
        String sourceText = binding.sourceText.getText().toString().trim();
        if (sourceText.isEmpty()) {
            Toast.makeText(requireContext(), "请输入要翻译的文本", Toast.LENGTH_SHORT).show();
            return;
        }

        // 显示加载指示器
        binding.progressBar.setVisibility(View.VISIBLE);

        // 获取选中的语言
        Language sourceLanguage = languages.get(binding.sourceLanguageSpinner.getSelectedItemPosition());
        Language targetLanguage = languages.get(binding.targetLanguageSpinner.getSelectedItemPosition());

        // 调用翻译服务
        translationService.translateText(sourceText, targetLanguage.getCode(), sourceLanguage.getCode(), new TranslationService.TranslationCallback() {
            @Override
            public void onSuccess(TranslationResult result) {
                requireActivity().runOnUiThread(() -> {
                    // 隐藏加载指示器
                    binding.progressBar.setVisibility(View.GONE);
                    
                    // 显示翻译结果
                    binding.translatedText.setText(result.getTranslatedText());
                    
                    // 保存到历史记录
                    if (userSettings.isAutoSaveHistory()) {
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
    public void onDestroyView() {
        super.onDestroyView();
        ttsService.shutdown();
        binding = null;
    }
} 