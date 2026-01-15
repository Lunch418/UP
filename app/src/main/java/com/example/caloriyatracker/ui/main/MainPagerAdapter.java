package com.example.caloriyatracker.ui.main;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.caloriyatracker.ui.home.HomeFragment;
import com.example.caloriyatracker.ui.profile.ProfileFragment;
import com.example.caloriyatracker.ui.results.ResultsFragment;

public class MainPagerAdapter extends FragmentStateAdapter {

    public MainPagerAdapter(@NonNull FragmentActivity fa) {
        super(fa);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) return new HomeFragment();
        if (position == 1) return new ProfileFragment();
        return new ResultsFragment();
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
