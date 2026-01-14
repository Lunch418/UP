package com.example.caloriyatracker.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.caloriyatracker.R;
import com.example.caloriyatracker.models.Meal;
import com.example.caloriyatracker.models.Product;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MealAdapter extends RecyclerView.Adapter<MealAdapter.VH> {

    public interface OnDeleteClick {
        void onDelete(Meal meal);
    }

    private final List<Meal> items = new ArrayList<>();
    private final OnDeleteClick onDeleteClick;

    private final Map<Integer, Product> productMap = new HashMap<>();

    public MealAdapter(OnDeleteClick onDeleteClick) {
        this.onDeleteClick = onDeleteClick;
    }

    public void setItems(List<Meal> data) {
        items.clear();
        if (data != null) items.addAll(data);
        notifyDataSetChanged();
    }

    public void setProducts(List<Product> products) {
        productMap.clear();
        if (products != null) {
            for (Product p : products) productMap.put(p.id, p);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_meal, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Meal m = items.get(position);

        Product p = productMap.get(m.product_id);
        String name = (p != null && p.name != null) ? p.name : ("product_id=" + m.product_id);

        h.tvName.setText(name);
        h.tvInfo.setText(String.format(Locale.US, "%.0f г • %.0f ккал", m.weight_grams, m.kcal));

        boolean today = isToday(m.consumed_at);
        h.tvBadge.setVisibility(today ? View.VISIBLE : View.GONE);

        h.btnDelete.setOnClickListener(v -> {
            if (onDeleteClick != null) onDeleteClick.onDelete(m);
        });
    }

    private boolean isToday(String isoDate) {
        if (isoDate == null) return false;
        String d = isoDate.length() >= 10 ? isoDate.substring(0, 10) : isoDate;
        java.time.LocalDate now = java.time.LocalDate.now();
        return d.equals(now.toString());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvInfo, tvBadge;
        MaterialButton btnDelete;

        VH(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvMealName);
            tvInfo = itemView.findViewById(R.id.tvMealInfo);
            tvBadge = itemView.findViewById(R.id.tvMealBadge);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
