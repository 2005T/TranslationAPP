package com.example.myapplication.ui;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.myapplication.databinding.FragmentImageTranslationBinding;
import com.example.myapplication.model.Language;
import com.example.myapplication.model.TranslationResult;
import com.example.myapplication.model.UserSettings;
import com.example.myapplication.service.OCRService;
import com.example.myapplication.service.TextToSpeechService;
import com.example.myapplication.service.TranslationHistoryManager;
import com.example.myapplication.service.TranslationService;
import com.example.myapplication.service.impl.AndroidTextToSpeechService;
import com.example.myapplication.service.impl.GoogleMLKitOCRService;
import com.example.myapplication.service.impl.GoogleMLKitTranslationService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ImageTranslationFragment extends Fragment {

    private static final int REQUEST_CAMERA_PERMISSION = 100;

    private FragmentImageTranslationBinding binding;
    private OCRService ocrService;
    private TranslationService translationService;
    private TextToSpeechService ttsService;
    private TranslationHistoryManager historyManager;
    private UserSettings userSettings;
    private List<Language> languages;
    private Bitmap selectedImage;
    private String recognizedText;
    private String translatedText;
    private boolean isOverlayMode = false;

    private final ActivityResultLauncher<Intent> takePictureLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                    Bundle extras = result.getData().getExtras();
                    if (extras != null) {
                        Bitmap imageBitmap = (Bitmap) extras.get("data");
                        handleCapturedImage(imageBitmap);
                    }
                }
            });

    private final ActivityResultLauncher<Intent> selectImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                    Uri selectedImageUri = result.getData().getData();
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), selectedImageUri);
                        handleCapturedImage(bitmap);
                    } catch (IOException e) {
                        Toast.makeText(requireContext(), "无法加载图片: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentImageTranslationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 初始化服务
        ocrService = new GoogleMLKitOCRService(requireContext());
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
        setupLanguageSpinner();

        // 设置按钮点击事件
        setupButtonListeners();
    }

    private void setupLanguageSpinner() {
        languages = Language.getSupportedLanguages();
        List<String> languageNames = new ArrayList<>();
        
        // 移除自动检测选项，因为OCR不支持
        for (int i = 1; i < languages.size(); i++) {
            languageNames.add(languages.get(i).getLocalName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                languageNames
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        binding.targetLanguageSpinner.setAdapter(adapter);

        // 设置默认目标语言
        String defaultTargetLang = userSettings.getDefaultTargetLanguage();
        for (int i = 1; i < languages.size(); i++) {
            if (languages.get(i).getCode().equals(defaultTargetLang)) {
                // 调整索引，因为我们移除了自动检测选项
                binding.targetLanguageSpinner.setSelection(i - 1);
                break;
            }
        }

        // 设置选择监听器
        binding.targetLanguageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // 如果已有识别的文本，自动翻译
                if (recognizedText != null && !recognizedText.isEmpty()) {
                    translateRecognizedText();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setupButtonListeners() {
        // 拍照按钮
        binding.takePhotoButton.setOnClickListener(v -> {
            takePicture();
        });

        // 选择图片按钮
        binding.selectImageButton.setOnClickListener(v -> {
            selectImage();
        });

        // 翻译按钮
        binding.translateButton.setOnClickListener(v -> {
            if (selectedImage != null) {
                performOCR();
            } else {
                Toast.makeText(requireContext(), "请先拍照或选择包含文字的图片", Toast.LENGTH_SHORT).show();
            }
        });

        // 切换视图按钮
        binding.switchViewButton.setOnClickListener(v -> {
            isOverlayMode = !isOverlayMode;
            updateViewMode();
        });

        // 复制按钮
        binding.copyButton.setOnClickListener(v -> {
            if (translatedText != null && !translatedText.isEmpty()) {
                ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Translated Text", translatedText);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(requireContext(), "已复制到剪贴板", Toast.LENGTH_SHORT).show();
            }
        });

        // 朗读按钮
        binding.speakButton.setOnClickListener(v -> {
            if (translatedText != null && !translatedText.isEmpty()) {
                // 调整索引，考虑到自动检测选项被移除
                Language targetLanguage = languages.get(binding.targetLanguageSpinner.getSelectedItemPosition() + 1);
                ttsService.speak(translatedText, targetLanguage.getCode(), userSettings.getSpeechRate());
            }
        });

        // 分享按钮
        binding.shareButton.setOnClickListener(v -> {
            if (translatedText != null && !translatedText.isEmpty()) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, translatedText);
                startActivity(Intent.createChooser(shareIntent, "分享翻译结果"));
            }
        });
    }

    private void takePicture() {
        // 检查相机权限
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            return;
        }

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            takePictureLauncher.launch(takePictureIntent);
        } else {
            Toast.makeText(requireContext(), "无法访问相机", Toast.LENGTH_SHORT).show();
        }
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        selectImageLauncher.launch(intent);
    }

    private void handleCapturedImage(Bitmap bitmap) {
        selectedImage = bitmap;
        binding.capturedImageView.setImageBitmap(bitmap);
        binding.placeholderText.setVisibility(View.GONE);
        
        // 自动执行OCR识别
        performOCR();
    }

    private void performOCR() {
        // 显示加载指示器
        binding.progressBar.setVisibility(View.VISIBLE);

        // 执行OCR识别
        ocrService.recognizeText(selectedImage, new OCRService.OCRCallback() {
            @Override
            public void onSuccess(String text) {
                recognizedText = text;
                
                // 显示识别结果
                requireActivity().runOnUiThread(() -> {
                    binding.recognizedTextView.setText(recognizedText);
                    
                    // 自动翻译识别出的文本
                    translateRecognizedText();
                });
            }

            @Override
            public void onError(String errorMessage) {
                requireActivity().runOnUiThread(() -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(requireContext(), "OCR识别失败: " + errorMessage, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void translateRecognizedText() {
        if (recognizedText == null || recognizedText.isEmpty()) {
            binding.progressBar.setVisibility(View.GONE);
            return;
        }

        // 获取选中的目标语言（注意索引调整）
        Language targetLanguage = languages.get(binding.targetLanguageSpinner.getSelectedItemPosition() + 1);
        
        // 假设源语言为英语（在实际应用中可以使用语言检测API）
        String sourceLanguage = "en";
        
        // 翻译文本
        translationService.translateText(recognizedText, targetLanguage.getCode(), sourceLanguage, new TranslationService.TranslationCallback() {
            @Override
            public void onSuccess(TranslationResult result) {
                translatedText = result.getTranslatedText();
                
                requireActivity().runOnUiThread(() -> {
                    // 隐藏加载指示器
                    binding.progressBar.setVisibility(View.GONE);
                    
                    // 显示翻译结果
                    binding.translatedTextView.setText(translatedText);
                    
                    // 保存到历史记录
                    if (userSettings.isAutoSaveHistory()) {
                        result.setType(TranslationResult.TranslationType.IMAGE);
                        historyManager.saveTranslationHistory(result);
                    }

                    // 更新视图模式
                    updateViewMode();
                });
            }

            @Override
            public void onError(String errorMessage) {
                requireActivity().runOnUiThread(() -> {
                    // 隐藏加载指示器
                    binding.progressBar.setVisibility(View.GONE);
                    
                    // 显示错误信息
                    Toast.makeText(requireContext(), "翻译失败: " + errorMessage, Toast.LENGTH_SHORT).show();
                    binding.translatedTextView.setText("翻译出错，请重试。");
                });
            }
        });
    }

    private void updateViewMode() {
        // 后续实现叠加翻译视图模式
        // 此处为简化版，只是通知用户功能切换
        Toast.makeText(requireContext(), isOverlayMode ? "已切换到叠加模式" : "已切换到文本模式", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                takePicture();
            } else {
                Toast.makeText(requireContext(), "需要相机权限才能拍摄图片", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (ocrService instanceof GoogleMLKitOCRService) {
            ((GoogleMLKitOCRService) ocrService).close();
        }
        ttsService.shutdown();
        binding = null;
    }
} 