package com.rebound.adapters;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.rebound.R;
import com.rebound.models.Orders.TimelineStep;

import java.util.List;

public class TimelineAdapter extends RecyclerView.Adapter<TimelineAdapter.TimelineViewHolder> {

    private final List<TimelineStep> steps;

    public TimelineAdapter(List<TimelineStep> steps) {
        this.steps = steps;
    }

    @NonNull
    @Override
    public TimelineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_timeline_step, parent, false);
        return new TimelineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TimelineViewHolder holder, int position) {
        TimelineStep step = steps.get(position);
        holder.txtTitle.setText(step.title);
        holder.txtDate.setText(step.date);
        // Không cần xử lý viewLine vì dùng TimelineDecoration
    }

    @Override
    public int getItemCount() {
        return steps.size();
    }

    public static class TimelineViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitle, txtDate;

        public TimelineViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.txtStepTitle);
            txtDate = itemView.findViewById(R.id.txtStepDate);
        }
    }
}
