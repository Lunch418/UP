package com.example.caloriyatracker.models;

public class LoginRequest {
    public String Email;
    public String Password;

    public LoginRequest(String email, String password) {
        this.Email = email;
        this.Password = password;
    }
}
