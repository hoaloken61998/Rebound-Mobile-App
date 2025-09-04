package com.rebound.adapters;

import android.content.Context;
import android.content.Intent;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.rebound.R;
import com.rebound.main.ProductDetailActivity;
import com.rebound.models.Cart.ProductItem;

import java.util.ArrayList;
import java.util.List;

public class LastCollectionAdapter extends RecyclerView.Adapter<LastCollectionAdapter.ViewHolder> {

    private List<ProductItem> originalList; // danh sách gốc
    private List<ProductItem> filteredList; // danh sách sau filter
    private String currentKeyword = "";

    public LastCollectionAdapter(List<ProductItem> list) {
        this.originalList = new ArrayList<>(list);
        this.filteredList = new ArrayList<>(list);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_lastest_collection, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ProductItem item = filteredList.get(position);

        // Highlight keyword nếu có
        // Defensive conversion for Object field
        String titleText = item.getProductName() != null ? item.getProductName().toString() : "";
        String lowerTitleText = titleText.toLowerCase();
        if (!currentKeyword.isEmpty()) {
            int start = lowerTitleText.indexOf(currentKeyword.toLowerCase());
            if (start >= 0) {
                SpannableString spannable = new SpannableString(titleText);
                int end = start + currentKeyword.length();
                int color = ContextCompat.getColor(holder.itemView.getContext(), R.color.purple_500);
                spannable.setSpan(new ForegroundColorSpan(color), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                holder.title.setText(spannable);
            } else {
                holder.title.setText(titleText);
            }
        } else {
            holder.title.setText(titleText);
        }

        // Format price to ensure >= 1.000.000 VNĐ
        String formattedPrice = "";
        Object priceObj = item.getProductPrice();
        if (priceObj != null) {
            long priceValue = 0;
            if (priceObj instanceof Number) {
                priceValue = ((Number) priceObj).longValue();
                if (priceValue <= 10000) {
                    priceValue = priceValue * 1000;
                }
            } else if (priceObj instanceof String) {
                try {
                    String s = ((String) priceObj).replace(",", "").replace(".", "").trim();
                    long parsed = Long.parseLong(s);
                    if (parsed <= 10000) {
                        priceValue = parsed * 1000;
                    } else {
                        priceValue = parsed;
                    }
                } catch (Exception e) {
                    priceValue = 0;
                }
            }
            formattedPrice = String.format("%,d VNĐ", priceValue).replace(",", ".");
        }
        holder.price.setText(formattedPrice);
        // Load image from URL using Glide
        String imageLink = item.getImageLink() != null ? item.getImageLink().toString() : "";
        if (!imageLink.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                .load(imageLink)
                .placeholder(R.drawable.ic_placeholder) // Use your placeholder image
                .into(holder.image);
        } else {
            holder.image.setImageResource(R.drawable.ic_placeholder); // fallback image
        }

        // Click để mở chi tiết sản phẩm
        holder.itemView.setOnClickListener(v -> {
            Context context = holder.itemView.getContext();
            Intent intent = new Intent(context, ProductDetailActivity.class);
            intent.putExtra("product", item);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    // Hàm lọc theo từ khóa
    public void filter(String keyword) {
        currentKeyword = keyword != null ? keyword.trim() : "";
        filteredList.clear();

        if (currentKeyword.isEmpty()) {
            filteredList.addAll(originalList);
        } else {
            String lowerKeyword = currentKeyword.toLowerCase();
            for (ProductItem item : originalList) {
                if (item.getProductName() != null && item.getProductName().toString().toLowerCase().contains(lowerKeyword)) {
                    filteredList.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    // ViewHolder class
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title, price;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.imgProduct);
            title = itemView.findViewById(R.id.txtProduct);
            price = itemView.findViewById(R.id.txtProductPrice);

        }
    }
    public void updateData(List<ProductItem> newList) {
        originalList.clear();
        originalList.addAll(newList);

        filteredList.clear();
        filteredList.addAll(newList);

        notifyDataSetChanged();
    }
    public void updateList(List<ProductItem> newList) {
        this.originalList = new ArrayList<>(newList);
        this.filteredList = new ArrayList<>(newList);
        notifyDataSetChanged();
    }
}