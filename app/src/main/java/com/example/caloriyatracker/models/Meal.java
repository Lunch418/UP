package com.example.caloriyatracker.models;

public class Meal {
    public int id;
    public int user_id;
    public int product_id;
    public double weight_grams;
    public String consumed_at;
    public Product Product;
    public transient double kcal;
}
