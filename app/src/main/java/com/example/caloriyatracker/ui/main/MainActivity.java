package com.example.caloriyatracker.ui.main;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.caloriyatracker.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewPager = findViewById(R.id.viewPager);
        bottomNav = findViewById(R.id.bottomNav);

        viewPager.setAdapter(new MainPagerAdapter(this));
        viewPager.setOffscreenPageLimit(3);

        // Свайп -> обновляем выбранную вкладку
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (position == 0) bottomNav.setSelectedItemId(R.id.nav_home);
                else if (position == 1) bottomNav.setSelectedItemId(R.id.nav_profile);
                else bottomNav.setSelectedItemId(R.id.nav_results);
            }
        });

        // Нажатие на bottomNav -> перелистываем
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) viewPager.setCurrentItem(0, true);
            else if (id == R.id.nav_profile) viewPager.setCurrentItem(1, true);
            else if (id == R.id.nav_results) viewPager.setCurrentItem(2, true);
            return true;
        });

        // старт
        bottomNav.setSelectedItemId(R.id.nav_home);
    }
}
