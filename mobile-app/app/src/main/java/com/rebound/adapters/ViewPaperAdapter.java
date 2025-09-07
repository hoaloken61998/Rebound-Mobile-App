package com.rebound.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rebound.R;

public class ViewPaperAdapter extends RecyclerView.Adapter<ViewPaperAdapter.ImageViewHolder> {

    private Context context;
    private int[] imageResIds;

    public ViewPaperAdapter(Context context, int[] imageResIds) {
        this.context = context;
        this.imageResIds = imageResIds;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_card_swipe, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        holder.imgCard.setImageResource(imageResIds[position]);
    }

    @Override
    public int getItemCount() {
        return imageResIds.length;
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imgCard;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCard = itemView.findViewById(R.id.imgCard);
        }
    }
}
