package com.example.caloriyatracker.ui.main;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.caloriyatracker.R;
import com.example.caloriyatracker.data.local.SessionManager;
import com.example.caloriyatracker.ui.auth.LoginActivity;
import com.example.caloriyatracker.ui.home.HomeFragment;
import com.example.caloriyatracker.ui.profile.ProfileFragment;
import com.example.caloriyatracker.ui.results.ResultsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SessionManager sm = new SessionManager(this);
        if (sm.getUserId() == -1) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        BottomNavigationView nav = findViewById(R.id.bottomNav);
        nav.setOnItemSelectedListener(item -> {
            Fragment f;
            if (item.getItemId() == R.id.nav_home) f = new HomeFragment();
            else if (item.getItemId() == R.id.nav_profile) f = new ProfileFragment();
            else f = new ResultsFragment();

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, f)
                    .commit();
            return true;
        });

        nav.setSelectedItemId(R.id.nav_home);
    }
}
