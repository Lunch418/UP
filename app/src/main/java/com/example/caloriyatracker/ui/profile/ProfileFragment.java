package com.example.caloriyatracker.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.caloriyatracker.R;
import com.example.caloriyatracker.data.local.SessionManager;
import com.example.caloriyatracker.data.remote.ApiClient;
import com.example.caloriyatracker.models.ApiMessage;
import com.example.caloriyatracker.ui.auth.LoginActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    private TextView tvUserInfo;
    private EditText etGoal;
    private Button btnSave, btnLogout;

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

        tvUserInfo.setText("ID: " + sm.getUserId() + "\n" + sm.getUsername() + "\n" + sm.getEmail());
        etGoal.setText(String.valueOf(sm.getGoal()));

        btnSave.setOnClickListener(x -> saveGoal());
        btnLogout.setOnClickListener(x -> {
            sm.clear();
            startActivity(new Intent(requireContext(), LoginActivity.class));
            requireActivity().finish();
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
