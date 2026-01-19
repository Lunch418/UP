package com.example.caloriyatracker.ui.results;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.caloriyatracker.R;
import com.example.caloriyatracker.data.local.SessionManager;
import com.example.caloriyatracker.data.remote.ApiClient;
import com.example.caloriyatracker.models.Meal;
import com.example.caloriyatracker.models.Product;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResultsFragment extends Fragment {

    private TextView tvTodayKcal, tvAvg, tvBest, tvInGoal;
    private ProgressBar pbToday;
    private BarChart barChart;

    private SessionManager sm;

    private final Map<Integer, Product> productMap = new HashMap<>();
    private boolean chartInited = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inf, @Nullable ViewGroup c, @Nullable Bundle s) {
        return inf.inflate(R.layout.fragment_results, c, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        sm = new SessionManager(requireContext());

        tvTodayKcal = v.findViewById(R.id.tvTodayKcal);
        tvAvg = v.findViewById(R.id.tvAvg);
        tvBest = v.findViewById(R.id.tvBest);
        tvInGoal = v.findViewById(R.id.tvInGoal);
        pbToday = v.findViewById(R.id.pbToday);
        barChart = v.findViewById(R.id.barChart);

        if (!chartInited) {
            setupChart();
            chartInited = true;
        }

        refresh();
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    private void refresh() {
        if (!productMap.isEmpty()) {
            loadMeals();
        } else {
            loadProductsThenMeals();
        }
    }

    private void loadProductsThenMeals() {
        ApiClient.getApi().products().enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> res) {
                if (res.isSuccessful() && res.body() != null) {
                    productMap.clear();
                    for (Product p : res.body()) productMap.put(p.id, p);
                }
                loadMeals();
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                loadMeals();
            }
        });
    }

    private void loadMeals() {
        ApiClient.getApi().mealsByUser(sm.getUserId()).enqueue(new Callback<List<Meal>>() {
            @Override
            public void onResponse(Call<List<Meal>> call, Response<List<Meal>> res) {
                if (!res.isSuccessful() || res.body() == null) {
                    Toast.makeText(requireContext(), "Ошибка статистики", Toast.LENGTH_SHORT).show();
                    return;
                }
                render(res.body());
            }

            @Override
            public void onFailure(Call<List<Meal>> call, Throwable t) {
                Toast.makeText(requireContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void render(List<Meal> meals) {
        int goal = sm.getGoal();
        if (goal <= 0) goal = 2000;

        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(6);

        double[] dayTotals = new double[7];

        for (Meal m : meals) {
            String d = (m.consumed_at != null && m.consumed_at.length() >= 10)
                    ? m.consumed_at.substring(0, 10)
                    : null;
            if (d == null) continue;

            LocalDate date;
            try {
                date = LocalDate.parse(d);
            } catch (Exception e) {
                continue;
            }

            if (date.isBefore(start) || date.isAfter(today)) continue;

            Product p = productMap.get(m.product_id);
            if (p == null) continue;

            int idx = (int) (date.toEpochDay() - start.toEpochDay()); // 0..6
            if (idx < 0 || idx > 6) continue;

            double kcal = p.calories * m.weight_grams / 100.0;
            dayTotals[idx] += kcal;
        }

        double todayKcal = dayTotals[6];
        int todayKcalInt = (int) Math.round(todayKcal);

        tvTodayKcal.setText(todayKcalInt + " / " + goal + " ккал");

        pbToday.setMax(goal);
        pbToday.setProgress(Math.min(todayKcalInt, goal));

        double sum = 0;
        double best = 0;
        int inGoal = 0;

        for (double v : dayTotals) {
            sum += v;
            if (v > best) best = v;
            if (v <= goal && v > 0) inGoal++;
        }

        int avgInt = (int) Math.round(sum / 7.0);
        int bestInt = (int) Math.round(best);

        tvAvg.setText(avgInt + " ккал");
        tvBest.setText(bestInt + " ккал");
        tvInGoal.setText(inGoal + "/7");

        setChart(dayTotals, start);
    }

    private void setupChart() {
        Description d = new Description();
        d.setText("");
        barChart.setDescription(d);

        barChart.getAxisRight().setEnabled(false);
        barChart.getLegend().setEnabled(false);

        XAxis x = barChart.getXAxis();
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setGranularity(1f);
        x.setDrawGridLines(false);

        barChart.getAxisLeft().setDrawGridLines(true);
        barChart.setExtraOffsets(10, 10, 10, 10);
    }

    private void setChart(double[] totals, LocalDate start) {
        List<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            entries.add(new BarEntry(i, (float) totals[i]));
        }

        BarDataSet ds = new BarDataSet(entries, "");
        BarData data = new BarData(ds);
        data.setBarWidth(0.6f);
        barChart.setData(data);

        barChart.getXAxis().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int i = (int) value;
                if (i < 0 || i > 6) return "";
                LocalDate d = start.plusDays(i);
                return d.getDayOfMonth() + "." + String.format("%02d", d.getMonthValue());
            }
        });

        barChart.invalidate();
        barChart.animateY(500);
    }
}
