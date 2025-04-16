package com.example.myapplication;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.myapplication.databinding.ActivityMainBinding;
import com.example.myapplication.ui.HistoryFragment;
import com.example.myapplication.ui.ImageTranslationFragment;
import com.example.myapplication.ui.SettingsFragment;
import com.example.myapplication.ui.TextTranslationFragment;
import com.example.myapplication.ui.VoiceTranslationFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity implements NavigationBarView.OnItemSelectedListener {

    private ActivityMainBinding binding;
    private ViewPager2 viewPager;
    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 初始化视图
        viewPager = binding.viewPager;
        bottomNavigation = binding.bottomNavigation;

        // 设置ViewPager适配器
        viewPager.setAdapter(new ViewPagerAdapter(this));
        
        // 禁用ViewPager2的滑动
        viewPager.setUserInputEnabled(false);

        // 设置底部导航监听器
        bottomNavigation.setOnItemSelectedListener(this);
        
        // 默认选中第一个选项（文本翻译）
        bottomNavigation.setSelectedItemId(R.id.navigation_text);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        
        if (itemId == R.id.navigation_text) {
            viewPager.setCurrentItem(0);
            return true;
        } else if (itemId == R.id.navigation_voice) {
            viewPager.setCurrentItem(1);
            return true;
        } else if (itemId == R.id.navigation_image) {
            viewPager.setCurrentItem(2);
            return true;
        } else if (itemId == R.id.navigation_history) {
            viewPager.setCurrentItem(3);
            return true;
        } else if (itemId == R.id.navigation_settings) {
            viewPager.setCurrentItem(4);
            return true;
        }
        
        return false;
    }

    /**
     * ViewPager适配器，管理不同功能的Fragment
     */
    private static class ViewPagerAdapter extends FragmentStateAdapter {

        public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new TextTranslationFragment();
                case 1:
                    return new VoiceTranslationFragment();
                case 2:
                    return new ImageTranslationFragment();
                case 3:
                    return new HistoryFragment();
                case 4:
                    return new SettingsFragment();
                default:
                    return new TextTranslationFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 5; // 文本、语音、图像、历史、设置
        }
    }
}