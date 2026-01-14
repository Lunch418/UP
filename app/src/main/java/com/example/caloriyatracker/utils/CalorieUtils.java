package com.example.caloriyatracker.utils;

import com.example.caloriyatracker.models.Meal;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class CalorieUtils {

    // consumed_at может приходить как "2026-01-13T12:34:56" или "2026-01-13 12:34:56"
    private static Date parseDate(String s) {
        if (s == null) return null;
        String[] patterns = new String[]{
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd'T'HH:mm:ss.SSS",
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
        };

        for (String p : patterns) {
            try {
                SimpleDateFormat df = new SimpleDateFormat(p, Locale.US);
                df.setTimeZone(TimeZone.getTimeZone("UTC"));
                return df.parse(s);
            } catch (ParseException ignored) {}
        }
        return null;
    }

    public static boolean isToday(String consumedAtUtc) {
        Date d = parseDate(consumedAtUtc);
        if (d == null) return false;

        Calendar cal = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(d);

        return cal.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
                && cal.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    public static double caloriesForMeal(Meal m) {
        if (m == null || m.Product == null) return 0;
        // calories на 100г
        return (m.Product.calories / 100.0) * m.weight_grams;
    }

    public static double totalCaloriesToday(List<Meal> meals) {
        double sum = 0;
        if (meals == null) return 0;
        for (Meal m : meals) {
            if (isToday(m.consumed_at)) sum += caloriesForMeal(m);
        }
        return sum;
    }

    // последние 7 дней: ключ = 0..6 (0 = сегодня, 6 = 6 дней назад)
    public static double[] last7DaysCalories(List<Meal> meals) {
        double[] res = new double[7];
        if (meals == null) return res;

        Calendar now = Calendar.getInstance();

        for (Meal m : meals) {
            Date d = parseDate(m.consumed_at);
            if (d == null) continue;

            Calendar c = Calendar.getInstance();
            c.setTime(d);

            long diffMs = now.getTimeInMillis() - c.getTimeInMillis();
            long diffDays = diffMs / (1000L * 60 * 60 * 24);

            if (diffDays >= 0 && diffDays < 7) {
                int idx = (int) diffDays;
                res[idx] += caloriesForMeal(m);
            }
        }
        return res;
    }
}
