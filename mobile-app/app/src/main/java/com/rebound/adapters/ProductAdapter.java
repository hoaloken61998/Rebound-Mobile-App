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
import com.rebound.main.ProductDetailActivity;
import com.rebound.models.Cart.ProductItem;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {
    private List<ProductItem> list;

    public ProductAdapter(List<ProductItem> list) {
        this.list = list;
    }

    public void updateList(List<ProductItem> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ProductItem item = list.get(position);
        holder.title.setText(item.getProductName() != null ? item.getProductName().toString() : "");
        // Format price correctly for all values
        String formattedPrice = "";
        Object priceObj = item.getProductPrice();
        if (priceObj != null) {
            long priceValue = 0;
            if (priceObj instanceof Number) {
                priceValue = ((Number) priceObj).longValue();
                // If priceValue <= 10000, treat as thousands (e.g., 950 -> 950000)
                if (priceValue <= 10000) {
                    priceValue = priceValue * 1000;
                }
            } else if (priceObj instanceof String) {
                try {
                    String s = ((String) priceObj).replace(",", "").replace(".", "").trim();
                    long parsed = Long.parseLong(s);
                    // If parsed <= 10000, treat as thousands (e.g., 950 -> 950000)
                    if (parsed <= 10000) {
                        priceValue = parsed * 1000;
                    } else {
                        priceValue = parsed;
                    }
                } catch (Exception e) {
                    priceValue = 0;
                }
            }
            // Format with dot as thousands separator
            formattedPrice = String.format("%,d VNĐ", priceValue).replace(",", ".");
        }
        holder.price.setText(formattedPrice);
        // Show rating instantly from ProductItem if available
        if (item.getRating() != null) {
            holder.rating.setText(item.getRating().toString());
        } else {
            holder.rating.setText("");
        }
        String imageLink = item.getImageLink() != null ? item.getImageLink().toString() : null;
        if (imageLink != null && !imageLink.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                .load(imageLink)
                .placeholder(R.drawable.ic_placeholder)
                .into(holder.image);
        } else {
            holder.image.setImageResource(R.drawable.ic_placeholder);
        }
        holder.itemView.setOnClickListener(v -> {
            Context context = holder.itemView.getContext();
            Intent intent = new Intent(context, ProductDetailActivity.class);
            intent.putExtra("product", item); // Gửi object đã Serializable
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title, price, rating;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.imgProduct);
            title = itemView.findViewById(R.id.txtProduct);
            price = itemView.findViewById(R.id.txtProductPrice);
            rating = itemView.findViewById(R.id.txtProductRating);
        }
    }
}
