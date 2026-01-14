package com.example.caloriyatracker.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.caloriyatracker.R;
import com.example.caloriyatracker.data.local.SessionManager;
import com.example.caloriyatracker.data.remote.ApiClient;
import com.example.caloriyatracker.models.LoginRequest;
import com.example.caloriyatracker.models.LoginResponse;
import com.example.caloriyatracker.ui.main.MainActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPass;
    private Button btnLogin, btnToRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        SessionManager sm = new SessionManager(this);
        if (sm.getUserId() != -1) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        etEmail = findViewById(R.id.etEmail);
        etPass = findViewById(R.id.etPass);
        btnLogin = findViewById(R.id.btnLogin);
        btnToRegister = findViewById(R.id.btnToRegister);

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
            String pass = etPass.getText() != null ? etPass.getText().toString().trim() : "";

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Заполни email и пароль", Toast.LENGTH_SHORT).show();
                return;
            }

            ApiClient.getApi().login(new LoginRequest(email, pass))
                    .enqueue(new Callback<LoginResponse>() {
                        @Override
                        public void onResponse(Call<LoginResponse> call, Response<LoginResponse> res) {
                            if (!res.isSuccessful() || res.body() == null || res.body().user == null) {
                                Toast.makeText(LoginActivity.this, "Неверный email или пароль", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            sm.saveUser(
                                    res.body().user.id,
                                    res.body().user.username,
                                    res.body().user.email,
                                    res.body().user.dailyCalorieGoal
                            );

                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        }

                        @Override
                        public void onFailure(Call<LoginResponse> call, Throwable t) {
                            Toast.makeText(LoginActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        btnToRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class))
        );
    }
}
