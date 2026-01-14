package com.example.caloriyatracker.data.local;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private final SharedPreferences sp;

    public SessionManager(Context ctx) {
        sp = ctx.getSharedPreferences("session", Context.MODE_PRIVATE);
    }

    public void saveUser(int id, String username, String email, int goal) {
        sp.edit()
                .putInt("user_id", id)
                .putString("username", username)
                .putString("email", email)
                .putInt("goal", goal)
                .apply();
    }

    public int getUserId() { return sp.getInt("user_id", -1); }
    public String getUsername() { return sp.getString("username", ""); }
    public String getEmail() { return sp.getString("email", ""); }
    public int getGoal() { return sp.getInt("goal", 2000); }

    public void setGoal(int goal) { sp.edit().putInt("goal", goal).apply(); }

    public void clear() { sp.edit().clear().apply(); }
}
