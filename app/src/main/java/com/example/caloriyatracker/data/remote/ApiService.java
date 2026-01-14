package com.example.caloriyatracker.data.remote;

import com.example.caloriyatracker.models.*;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {

    @POST("api/UserController/Login")
    Call<LoginResponse> login(@Body LoginRequest body);

    @POST("api/UserController/Add")
    Call<ApiMessage> register(@Body RegisterUser body);

    @PUT("api/UserController/UpdateCalorieGoal")
    Call<ApiMessage> updateCalorieGoal(@Query("id") int id, @Query("dailyCalorieGoal") int goal);

    @GET("api/ProductController/List")
    Call<List<Product>> products();

    @GET("api/MealController/ListByUser")
    Call<List<Meal>> mealsByUser(@Query("userId") int userId);

    @POST("api/MealController/Add")
    Call<ApiMessage> addMeal(@Body MealCreate body);

    @DELETE("api/MealController/Delete")
    Call<ApiMessage> deleteMeal(@Query("id") int id);

    @GET("api/GoalController/ListByUser")
    Call<List<Goal>> goalsByUser(@Query("userId") int userId);

    @POST("api/GoalController/Add")
    Call<ApiMessage> addGoal(@Body Goal body);

    @GET("api/AchievementController/ListByUser")
    Call<List<Achievement>> achievementsByUser(@Query("userId") int userId);
}
