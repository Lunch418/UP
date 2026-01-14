package com.example.caloriyatracker.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.caloriyatracker.R;
import com.example.caloriyatracker.data.local.SessionManager;
import com.example.caloriyatracker.data.remote.ApiClient;
import com.example.caloriyatracker.models.ApiMessage;
import com.example.caloriyatracker.models.Meal;
import com.example.caloriyatracker.models.MealCreate;
import com.example.caloriyatracker.models.Product;
import com.example.caloriyatracker.ui.adapters.MealAdapter;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private TextView tvHello;
    private TextView tvGoal;
    private TextView tvProgress;
    private ProgressBar progressBar;
    private MaterialButton btnAddMeal;
    private RecyclerView rvMeals;

    private SessionManager sm;
    private MealAdapter adapter;

    private List<Meal> cachedMeals = new ArrayList<>();
    private List<Product> cachedProducts = new ArrayList<>();

    private final Map<Integer, Product> productMap = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inf, @Nullable ViewGroup c, @Nullable Bundle s) {
        return inf.inflate(R.layout.fragment_home, c, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        sm = new SessionManager(requireContext());

        tvHello = v.findViewById(R.id.tvHello);
        tvGoal = v.findViewById(R.id.tvGoal);
        tvProgress = v.findViewById(R.id.tvProgress);
        progressBar = v.findViewById(R.id.progressBar);
        btnAddMeal = v.findViewById(R.id.btnAddMeal);
        rvMeals = v.findViewById(R.id.rvMeals);

        tvHello.setText("Привет, " + sm.getUsername() + "!");

        adapter = new MealAdapter(meal -> deleteMeal(meal.id));
        rvMeals.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvMeals.setAdapter(adapter);
        rvMeals.setItemAnimator(null);
        rvMeals.setNestedScrollingEnabled(false);

        btnAddMeal.setOnClickListener(x -> openAddMealSheet());

        loadProductsThenMeals();
    }

    private void loadProductsThenMeals() {
        ApiClient.getApi().products().enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> res) {
                if (!res.isSuccessful() || res.body() == null) {
                    Toast.makeText(requireContext(), "Ошибка загрузки продуктов", Toast.LENGTH_SHORT).show();
                    cachedProducts = new ArrayList<>();
                    buildProductMap(cachedProducts);
                    adapter.setProducts(cachedProducts);
                    loadMeals();
                    return;
                }

                cachedProducts = res.body();
                buildProductMap(cachedProducts);
                adapter.setProducts(cachedProducts);
                loadMeals();
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                cachedProducts = new ArrayList<>();
                buildProductMap(cachedProducts);
                adapter.setProducts(cachedProducts);
                loadMeals();
            }
        });
    }

    private void buildProductMap(List<Product> products) {
        productMap.clear();
        if (products == null) return;
        for (Product p : products) productMap.put(p.id, p);
    }

    private double calcKcal(double caloriesPer100g, double grams) {
        return caloriesPer100g * grams / 100.0;
    }

    private void applyMealsCalories(List<Meal> meals) {
        double totalToday = 0;

        for (Meal m : meals) {
            Product p = productMap.get(m.product_id);
            if (p != null) {
                m.kcal = calcKcal(p.calories, m.weight_grams);
            } else {
                m.kcal = 0;
            }
            if (isToday(m.consumed_at)) totalToday += m.kcal;
        }

        int goal = sm.getGoal();
        tvGoal.setText("Цель: " + goal + " ккал");
        tvProgress.setText(String.format(Locale.US, "%.0f / %d ккал", totalToday, goal));

        progressBar.setMax(goal);
        progressBar.setProgress((int) Math.min(totalToday, goal));

        adapter.setItems(meals);
    }

    private boolean isToday(String isoDate) {
        if (isoDate == null) return false;
        // очень простой вариант: сравнение по дате YYYY-MM-DD
        // у тебя consumed_at обычно приходит как "2026-01-13T..." или "2026-01-13 ..."
        String d = isoDate.length() >= 10 ? isoDate.substring(0, 10) : isoDate;
        java.time.LocalDate now = java.time.LocalDate.now();
        return d.equals(now.toString());
    }

    private void loadMeals() {
        ApiClient.getApi().mealsByUser(sm.getUserId()).enqueue(new Callback<List<Meal>>() {
            @Override
            public void onResponse(Call<List<Meal>> call, Response<List<Meal>> res) {
                if (!res.isSuccessful() || res.body() == null) {
                    Toast.makeText(requireContext(), "Ошибка загрузки приёмов пищи", Toast.LENGTH_SHORT).show();
                    return;
                }
                cachedMeals = res.body();
                applyMealsCalories(cachedMeals);
            }

            @Override
            public void onFailure(Call<List<Meal>> call, Throwable t) {
                Toast.makeText(requireContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openAddMealSheet() {
        if (cachedProducts == null || cachedProducts.isEmpty()) {
            Toast.makeText(requireContext(), "Нет продуктов. Добавь продукты в админке.", Toast.LENGTH_SHORT).show();
            return;
        }

        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View v = LayoutInflater.from(requireContext()).inflate(R.layout.sheet_add_meal, null, false);
        dialog.setContentView(v);

        AutoCompleteTextView acProduct = v.findViewById(R.id.acProduct);
        TextInputEditText etGrams = v.findViewById(R.id.etGrams);
        MaterialButton btnAdd = v.findViewById(R.id.btnAddMeal);

        List<String> names = new ArrayList<>();
        for (Product p : cachedProducts) {
            names.add(p.name + " (" + ((int) p.calories) + " ккал/100г)");
        }

        ArrayAdapter<String> a = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, names);
        acProduct.setAdapter(a);
        acProduct.setThreshold(0);
        acProduct.setOnClickListener(x -> acProduct.showDropDown());

        btnAdd.setOnClickListener(x -> {
            String selected = acProduct.getText() != null ? acProduct.getText().toString().trim() : "";
            String gramsStr = etGrams.getText() != null ? etGrams.getText().toString().trim() : "";

            if (selected.isEmpty()) {
                Toast.makeText(requireContext(), "Выбери продукт", Toast.LENGTH_SHORT).show();
                return;
            }
            if (gramsStr.isEmpty()) {
                Toast.makeText(requireContext(), "Введи граммы", Toast.LENGTH_SHORT).show();
                return;
            }

            int idx = names.indexOf(selected);
            if (idx < 0) {
                Toast.makeText(requireContext(), "Выбери продукт из списка", Toast.LENGTH_SHORT).show();
                return;
            }

            double grams;
            try {
                grams = Double.parseDouble(gramsStr);
            } catch (Exception e) {
                Toast.makeText(requireContext(), "Некорректные граммы", Toast.LENGTH_SHORT).show();
                return;
            }

            if (grams <= 0 || grams > 3000) {
                Toast.makeText(requireContext(), "Вес должен быть 1..3000 г", Toast.LENGTH_SHORT).show();
                return;
            }

            Product p = cachedProducts.get(idx);
            addMeal(p.id, grams, dialog);
        });

        dialog.show();
    }

    private void addMeal(int productId, double grams, BottomSheetDialog dialog) {
        MealCreate body = new MealCreate(sm.getUserId(), productId, grams);

        ApiClient.getApi().addMeal(body).enqueue(new Callback<ApiMessage>() {
            @Override
            public void onResponse(Call<ApiMessage> call, Response<ApiMessage> res) {
                if (!res.isSuccessful()) {
                    Toast.makeText(requireContext(), "Ошибка добавления", Toast.LENGTH_SHORT).show();
                    return;
                }
                dialog.dismiss();
                loadMeals();
            }

            @Override
            public void onFailure(Call<ApiMessage> call, Throwable t) {
                Toast.makeText(requireContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteMeal(int mealId) {
        ApiClient.getApi().deleteMeal(mealId).enqueue(new Callback<ApiMessage>() {
            @Override
            public void onResponse(Call<ApiMessage> call, Response<ApiMessage> res) {
                loadMeals();
            }

            @Override
            public void onFailure(Call<ApiMessage> call, Throwable t) {
                Toast.makeText(requireContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
