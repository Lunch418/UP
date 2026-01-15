package com.example.caloriyatracker.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.caloriyatracker.R;
import com.example.caloriyatracker.models.Achievement;

import java.util.ArrayList;
import java.util.List;

public class AchievementAdapter extends RecyclerView.Adapter<AchievementAdapter.VH> {

    private final List<Achievement> items = new ArrayList<>();

    public void setItems(List<Achievement> data) {
        items.clear();
        if (data != null) items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_achievement, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Achievement a = items.get(position);

        h.tvName.setText(a.name != null ? a.name : "Достижение");
        h.tvDesc.setText(a.description != null ? a.description : "");

        String date = a.earned_at != null ? a.earned_at : "";
        date = date.replace('T', ' ');
        if (date.length() > 16) date = date.substring(0, 16);
        h.tvDate.setText(date);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvDesc, tvDate;

        VH(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvAchName);
            tvDesc = itemView.findViewById(R.id.tvAchDesc);
            tvDate = itemView.findViewById(R.id.tvAchDate);
        }
    }
}
