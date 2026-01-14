package com.example.caloriyatracker.models;

public class MealCreate {
    public int user_id;
    public int product_id;
    public double weight_grams;

    public MealCreate(int userId, int productId, double weightGrams) {
        this.user_id = userId;
        this.product_id = productId;
        this.weight_grams = weightGrams;
    }
}
