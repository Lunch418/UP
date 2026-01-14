package com.example.caloriyatracker.models;

public class RegisterUser {
    public String username;
    public String email;
    public String password_hash;

    public RegisterUser(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password_hash = password;
    }
}
