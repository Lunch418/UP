package com.example.caloriyatracker.ui.results;

import android.os.Bundle;
import android.view.*;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.caloriyatracker.R;
import com.example.caloriyatracker.data.local.SessionManager;
import com.example.caloriyatracker.data.remote.ApiClient;
import com.example.caloriyatracker.models.Meal;
import com.example.caloriyatracker.utils.CalorieUtils;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.*;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResultsFragment extends Fragment {

    private TextView tvResInfo;
    private LineChart chart;

    private SessionManager sm;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inf, @Nullable ViewGroup c, @Nullable Bundle s) {
        return inf.inflate(R.layout.fragment_results, c, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        sm = new SessionManager(requireContext());

        tvResInfo = v.findViewById(R.id.tvResInfo);
        chart = v.findViewById(R.id.chart);

        loadMealsAndBuildChart();
    }

    private void loadMealsAndBuildChart() {
        ApiClient.getApi().mealsByUser(sm.getUserId()).enqueue(new Callback<List<Meal>>() {
            @Override
            public void onResponse(Call<List<Meal>> call, Response<List<Meal>> res) {
                if (!res.isSuccessful() || res.body() == null) {
                    Toast.makeText(requireContext(), "Ошибка статистики", Toast.LENGTH_SHORT).show();
                    return;
                }

                double[] days = CalorieUtils.last7DaysCalories(res.body()); // 0=сегодня
                ArrayList<Entry> entries = new ArrayList<>();

                // рисуем от старого к новому: 6..0
                for (int i = 6; i >= 0; i--) {
                    float x = (6 - i);         // 0..6
                    float y = (float) days[i]; // калории
                    entries.add(new Entry(x, y));
                }

                LineDataSet ds = new LineDataSet(entries, "Ккал (7 дней)");
                ds.setLineWidth(2f);
                ds.setCircleRadius(4f);

                chart.setData(new LineData(ds));
                chart.getDescription().setEnabled(false);
                chart.invalidate();

                tvResInfo.setText("Калории за последние 7 дней (0=6 дней назад → 6=сегодня)");
            }

            @Override
            public void onFailure(Call<List<Meal>> call, Throwable t) {
                Toast.makeText(requireContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
