package com.example.myapplication.ui.adapter;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.databinding.ItemHistoryBinding;
import com.example.myapplication.model.Language;
import com.example.myapplication.model.TranslationResult;
import com.example.myapplication.model.UserSettings;
import com.example.myapplication.service.TextToSpeechService;
import com.example.myapplication.service.impl.AndroidTextToSpeechService;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class HistoryAdapter extends ListAdapter<TranslationResult, HistoryAdapter.HistoryViewHolder> {

    private OnItemClickListener listener;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    public HistoryAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<TranslationResult> DIFF_CALLBACK = new DiffUtil.ItemCallback<TranslationResult>() {
        @Override
        public boolean areItemsTheSame(@NonNull TranslationResult oldItem, @NonNull TranslationResult newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull TranslationResult oldItem, @NonNull TranslationResult newItem) {
            return oldItem.equals(newItem);
        }
    };

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemHistoryBinding binding = ItemHistoryBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new HistoryViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        TranslationResult item = getItem(position);
        holder.bind(item);
    }

    public class HistoryViewHolder extends RecyclerView.ViewHolder {
        private final ItemHistoryBinding binding;
        private final TextToSpeechService ttsService;

        public HistoryViewHolder(@NonNull ItemHistoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            // 初始化TTS服务，实际项目中应考虑避免每个ViewHolder创建一个实例
            ttsService = new AndroidTextToSpeechService(binding.getRoot().getContext());
            ttsService.initialize(new TextToSpeechService.InitCallback() {
                @Override
                public void onSuccess() {
                    // TTS初始化成功
                }

                @Override
                public void onError(String errorMessage) {
                    Toast.makeText(binding.getRoot().getContext(), "语音引擎初始化失败", Toast.LENGTH_SHORT).show();
                }
            });
        }

        public void bind(TranslationResult item) {
            // 设置时间戳
            binding.dateTimeText.setText(dateFormat.format(item.getTimestamp()));

            // 设置翻译类型
            String typeText;
            switch (item.getType()) {
                case TEXT:
                    typeText = "文本翻译";
                    break;
                case SPEECH:
                    typeText = "语音翻译";
                    break;
                case IMAGE:
                    typeText = "图像翻译";
                    break;
                default:
                    typeText = "翻译";
                    break;
            }
            binding.translationTypeText.setText(typeText);

            // 设置源语言和目标语言
            Language sourceLanguage = Language.findLanguageByCode(item.getSourceLanguage());
            Language targetLanguage = Language.findLanguageByCode(item.getTargetLanguage());
            
            binding.sourceLanguageText.setText(sourceLanguage != null ? sourceLanguage.getLocalName() : item.getSourceLanguage());
            binding.targetLanguageText.setText(targetLanguage != null ? targetLanguage.getLocalName() : item.getTargetLanguage());

            // 设置源文本和翻译文本
            binding.sourceTextView.setText(item.getSourceText());
            binding.translatedTextView.setText(item.getTranslatedText());

            // 设置点击事件
            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(item);
                }
            });

            // 设置删除按钮点击事件
            binding.deleteButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(item);
                }
            });

            // 设置复制按钮点击事件
            binding.copyButton.setOnClickListener(v -> {
                String translatedText = item.getTranslatedText();
                if (translatedText != null && !translatedText.isEmpty()) {
                    ClipboardManager clipboard = (ClipboardManager) v.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("Translated Text", translatedText);
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(v.getContext(), "已复制到剪贴板", Toast.LENGTH_SHORT).show();
                    
                    if (listener != null) {
                        listener.onCopyClick(translatedText);
                    }
                }
            });

            // 设置朗读按钮点击事件
            binding.speakButton.setOnClickListener(v -> {
                String translatedText = item.getTranslatedText();
                if (translatedText != null && !translatedText.isEmpty()) {
                    ttsService.speak(translatedText, item.getTargetLanguage(), UserSettings.getInstance().getSpeechRate());
                    
                    if (listener != null) {
                        listener.onSpeakClick(translatedText, item.getTargetLanguage());
                    }
                }
            });

            // 设置分享按钮点击事件
            binding.shareButton.setOnClickListener(v -> {
                String translatedText = item.getTranslatedText();
                if (translatedText != null && !translatedText.isEmpty()) {
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_TEXT, translatedText);
                    v.getContext().startActivity(Intent.createChooser(shareIntent, "分享翻译结果"));
                    
                    if (listener != null) {
                        listener.onShareClick(translatedText);
                    }
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(TranslationResult result);
        void onDeleteClick(TranslationResult result);
        void onCopyClick(String text);
        void onSpeakClick(String text, String languageCode);
        void onShareClick(String text);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
} 