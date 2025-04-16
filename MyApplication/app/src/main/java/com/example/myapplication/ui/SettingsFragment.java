package com.example.myapplication.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.example.myapplication.BuildConfig;
import com.example.myapplication.databinding.FragmentSettingsBinding;
import com.example.myapplication.model.Language;
import com.example.myapplication.model.UserSettings;
import com.example.myapplication.service.TextToSpeechService;
import com.example.myapplication.service.impl.AndroidTextToSpeechService;

import java.util.ArrayList;
import java.util.List;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private UserSettings userSettings;
    private TextToSpeechService ttsService;
    private List<Language> languages;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 初始化用户设置和TTS服务
        userSettings = UserSettings.getInstance();
        ttsService = new AndroidTextToSpeechService(requireContext());
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

        // 设置语音速度控制
        setupSpeechRateControl();

        // 设置开关控件
        setupSwitches();

        // 设置其他UI元素
        setupOtherUI();
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

        binding.defaultSourceLanguageSpinner.setAdapter(adapter);
        binding.defaultTargetLanguageSpinner.setAdapter(adapter);

        // 设置当前选择的默认语言
        String defaultSourceLang = userSettings.getDefaultSourceLanguage();
        String defaultTargetLang = userSettings.getDefaultTargetLanguage();

        for (int i = 0; i < languages.size(); i++) {
            if (languages.get(i).getCode().equals(defaultSourceLang)) {
                binding.defaultSourceLanguageSpinner.setSelection(i);
            }
            if (languages.get(i).getCode().equals(defaultTargetLang)) {
                binding.defaultTargetLanguageSpinner.setSelection(i);
            }
        }

        // 设置选择监听器
        binding.defaultSourceLanguageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Language selectedLanguage = languages.get(position);
                userSettings.setDefaultSourceLanguage(selectedLanguage.getCode());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        binding.defaultTargetLanguageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Language selectedLanguage = languages.get(position);
                userSettings.setDefaultTargetLanguage(selectedLanguage.getCode());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setupSpeechRateControl() {
        // 当前语速值
        float currentRate = userSettings.getSpeechRate();
        
        // 设置SeekBar的进度（将语速值转换为进度值）
        int progress = (int) ((currentRate - 0.5f) * 30); // 0.5-2.0 -> 0-45
        binding.speechRateSeekBar.setProgress(progress);
        
        // 更新语速值显示
        updateSpeechRateText(currentRate);
        
        // 设置SeekBar变化监听
        binding.speechRateSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // 将进度值转换回语速值 (0-30 -> 0.5-2.0)
                float rate = 0.5f + (progress / 30.0f) * 1.5f;
                
                // 限制在有效范围内
                if (rate < 0.5f) rate = 0.5f;
                if (rate > 2.0f) rate = 2.0f;
                
                // 更新显示和保存设置
                updateSpeechRateText(rate);
                userSettings.setSpeechRate(rate);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        
        // 测试播放按钮
        binding.testSpeechButton.setOnClickListener(v -> {
            String testText = "这是一段测试语音，用于验证语音播放速度设置。";
            String languageCode = userSettings.getDefaultTargetLanguage();
            float speechRate = userSettings.getSpeechRate();
            ttsService.speak(testText, languageCode, speechRate);
        });
    }
    
    private void updateSpeechRateText(float rate) {
        binding.speechRateValueText.setText(String.format("%.1fx", rate));
    }

    private void setupSwitches() {
        // 自动保存历史记录开关
        binding.autoSaveHistorySwitch.setChecked(userSettings.isAutoSaveHistory());
        binding.autoSaveHistorySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            userSettings.setAutoSaveHistory(isChecked);
        });
        
        // 深色模式开关
        binding.darkModeSwitch.setChecked(userSettings.isDarkModeEnabled());
        binding.darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            userSettings.setDarkModeEnabled(isChecked);
            updateDarkMode(isChecked);
        });
    }
    
    private void updateDarkMode(boolean darkModeEnabled) {
        if (darkModeEnabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
    
    private void setupOtherUI() {
        // 设置应用版本号
        binding.versionTextView.setText("版本: " + BuildConfig.VERSION_NAME);
        
        // 隐私政策按钮点击事件
        binding.privacyPolicyButton.setOnClickListener(v -> {
            // 实际应用中应该跳转到真实的隐私政策页面
            String privacyPolicyUrl = "https://example.com/privacy-policy";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(privacyPolicyUrl));
            startActivity(intent);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ttsService.shutdown();
        binding = null;
    }
} 