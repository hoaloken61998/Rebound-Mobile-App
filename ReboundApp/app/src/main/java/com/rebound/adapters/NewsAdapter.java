package com.rebound.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.rebound.R;
import com.rebound.main.NewsDetailActivity;
import com.rebound.models.Main.NewsItem;

import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {

    private final Context context;
    private final List<NewsItem> items;

    public NewsAdapter(Context context, List<NewsItem> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_news, parent, false);
        return new NewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        NewsItem item = items.get(position);

        holder.title.setText(item.getTitle());
        holder.subtitle.setText(item.getSubtitle());
        holder.date.setText(item.getDate());

        String imageUrl = item.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.mipmap.ic_launcher)
                    .error(R.mipmap.ic_launcher) // nếu lỗi cũng hiện icon mặc định
                    .into(holder.image);
        } else {
            // Ẩn ImageView nếu không có ảnh
            holder.image.setVisibility(View.GONE);
        }

        // Xử lý sự kiện click
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, NewsDetailActivity.class);
            intent.putExtra("title", item.getTitle());
            intent.putExtra("desc", item.getFullContent()); // dùng nội dung chi tiết
            intent.putExtra("date", item.getDate());
            intent.putExtra("imageUrl", item.getImageUrl()); // truyền URL thay vì resId
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class NewsViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title, subtitle, date;

        public NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.imgThumbnailNews);
            title = itemView.findViewById(R.id.titleNews);
            subtitle = itemView.findViewById(R.id.txtNewsSubtitle);
            date = itemView.findViewById(R.id.txtNewsDate);
        }
    }
}
