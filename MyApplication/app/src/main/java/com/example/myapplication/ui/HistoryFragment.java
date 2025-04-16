package com.example.myapplication.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.myapplication.databinding.FragmentHistoryBinding;
import com.example.myapplication.model.TranslationResult;
import com.example.myapplication.service.TranslationHistoryManager;
import com.example.myapplication.ui.adapter.HistoryAdapter;

import java.util.List;

public class HistoryFragment extends Fragment {

    private FragmentHistoryBinding binding;
    private TranslationHistoryManager historyManager;
    private HistoryAdapter historyAdapter;
    private boolean isFilterVisible = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHistoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 初始化历史记录管理器
        historyManager = new TranslationHistoryManager(requireContext());

        // 设置RecyclerView
        setupRecyclerView();

        // 设置搜索功能
        setupSearch();

        // 设置筛选功能
        setupFilter();

        // 设置清空历史按钮
        binding.clearHistoryButton.setOnClickListener(v -> {
            historyManager.clearAllHistory();
            updateHistoryList();
            Toast.makeText(requireContext(), "历史记录已清空", Toast.LENGTH_SHORT).show();
        });

        // 初始加载历史记录
        updateHistoryList();
    }

    private void setupRecyclerView() {
        historyAdapter = new HistoryAdapter();
        binding.historyRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.historyRecyclerView.setAdapter(historyAdapter);

        // 设置项目点击和操作监听
        historyAdapter.setOnItemClickListener(new HistoryAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(TranslationResult result) {
                // 点击历史记录项（可扩展功能）
            }

            @Override
            public void onDeleteClick(TranslationResult result) {
                historyManager.deleteHistory(result);
                updateHistoryList();
                Toast.makeText(requireContext(), "已删除该记录", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCopyClick(String text) {
                // 复制功能在适配器中实现
            }

            @Override
            public void onSpeakClick(String text, String languageCode) {
                // 朗读功能在适配器中实现
            }

            @Override
            public void onShareClick(String text) {
                // 分享功能在适配器中实现
            }
        });
    }

    private void setupSearch() {
        // 搜索按钮点击事件
        binding.searchButton.setOnClickListener(v -> {
            String query = binding.searchEditText.getText().toString().trim();
            if (!query.isEmpty()) {
                List<TranslationResult> results = historyManager.searchHistory(query);
                updateHistoryListWithResults(results);
            } else {
                updateHistoryList();
            }
        });

        // 搜索框文本变化监听
        binding.searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().isEmpty()) {
                    updateHistoryList();
                }
            }
        });
    }

    private void setupFilter() {
        // 筛选按钮点击事件
        binding.filterButton.setOnClickListener(v -> {
            isFilterVisible = !isFilterVisible;
            binding.filterChipGroup.setVisibility(isFilterVisible ? View.VISIBLE : View.GONE);
        });

        // 筛选芯片点击事件
        binding.allChip.setOnClickListener(v -> updateHistoryList());
        binding.textChip.setOnClickListener(v -> filterByType(TranslationResult.TranslationType.TEXT));
        binding.voiceChip.setOnClickListener(v -> filterByType(TranslationResult.TranslationType.SPEECH));
        binding.imageChip.setOnClickListener(v -> filterByType(TranslationResult.TranslationType.IMAGE));
    }

    private void filterByType(TranslationResult.TranslationType type) {
        List<TranslationResult> results = historyManager.getHistoryByType(type);
        updateHistoryListWithResults(results);
    }

    private void updateHistoryList() {
        List<TranslationResult> historyList = historyManager.getAllHistory();
        updateHistoryListWithResults(historyList);
    }

    private void updateHistoryListWithResults(List<TranslationResult> results) {
        historyAdapter.submitList(results);
        
        // 显示空状态
        if (results.isEmpty()) {
            binding.emptyHistoryText.setVisibility(View.VISIBLE);
        } else {
            binding.emptyHistoryText.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // 每次页面可见时刷新列表
        updateHistoryList();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 