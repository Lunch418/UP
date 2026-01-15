package com.example.caloriyatracker.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.caloriyatracker.R;
import com.example.caloriyatracker.data.local.SessionManager;
import com.example.caloriyatracker.data.remote.ApiClient;
import com.example.caloriyatracker.models.Achievement;
import com.example.caloriyatracker.models.ApiMessage;
import com.example.caloriyatracker.ui.adapters.AchievementAdapter;
import com.example.caloriyatracker.ui.auth.LoginActivity;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    private TextView tvUserInfo;
    private EditText etGoal;
    private Button btnSave, btnLogout;

    private RecyclerView rvAchievements;
    private AchievementAdapter achAdapter;

    private SessionManager sm;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inf, @Nullable ViewGroup c, @Nullable Bundle s) {
        return inf.inflate(R.layout.fragment_profile, c, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        sm = new SessionManager(requireContext());

        tvUserInfo = v.findViewById(R.id.tvUserInfo);
        etGoal = v.findViewById(R.id.etGoal);
        btnSave = v.findViewById(R.id.btnSaveGoal);
        btnLogout = v.findViewById(R.id.btnLogout);

        rvAchievements = v.findViewById(R.id.rvAchievements);

        achAdapter = new AchievementAdapter();
        rvAchievements.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvAchievements.setAdapter(achAdapter);
        rvAchievements.setItemAnimator(null);
        rvAchievements.setNestedScrollingEnabled(false);

        tvUserInfo.setText("ID: " + sm.getUserId() + "\n" + sm.getUsername() + "\n" + sm.getEmail());
        etGoal.setText(String.valueOf(sm.getGoal()));

        btnSave.setOnClickListener(x -> saveGoal());
        btnLogout.setOnClickListener(x -> {
            sm.clear();
            startActivity(new Intent(requireContext(), LoginActivity.class));
            requireActivity().finish();
        });

        loadAchievements();
    }

    private void loadAchievements() {
        ApiClient.getApi().achievementsByUser(sm.getUserId()).enqueue(new Callback<List<Achievement>>() {
            @Override
            public void onResponse(Call<List<Achievement>> call, Response<List<Achievement>> res) {
                if (!res.isSuccessful() || res.body() == null) {
                    Toast.makeText(requireContext(), "Не удалось загрузить достижения", Toast.LENGTH_SHORT).show();
                    return;
                }
                achAdapter.setItems(res.body());
            }

            @Override
            public void onFailure(Call<List<Achievement>> call, Throwable t) {
                Toast.makeText(requireContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveGoal() {
        String g = etGoal.getText().toString().trim();
        if (g.isEmpty()) {
            Toast.makeText(requireContext(), "Введи норму", Toast.LENGTH_SHORT).show();
            return;
        }

        int goal = Integer.parseInt(g);

        ApiClient.getApi().updateCalorieGoal(sm.getUserId(), goal).enqueue(new Callback<ApiMessage>() {
            @Override
            public void onResponse(Call<ApiMessage> call, Response<ApiMessage> res) {
                if (!res.isSuccessful()) {
                    Toast.makeText(requireContext(), "Ошибка сохранения", Toast.LENGTH_SHORT).show();
                    return;
                }
                sm.setGoal(goal);
                Toast.makeText(requireContext(), "Норма обновлена", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<ApiMessage> call, Throwable t) {
                Toast.makeText(requireContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
