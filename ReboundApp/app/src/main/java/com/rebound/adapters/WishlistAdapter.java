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
import com.rebound.models.Cart.ProductItem;
import com.rebound.main.ProductDetailActivity;

import java.util.List;

public class WishlistAdapter extends RecyclerView.Adapter<WishlistAdapter.ViewHolder> {
    private final List<ProductItem> wishlist;
    private final Context context;

    public WishlistAdapter(List<ProductItem> wishlist, Context context) {
        this.wishlist = wishlist;
        this.context = context;
    }

    @NonNull
    @Override
    public WishlistAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WishlistAdapter.ViewHolder holder, int position) {
        ProductItem item = wishlist.get(position);

        // Gán dữ liệu vào View từ Firebase fields
        String imageLink = item.getImageLink() != null ? item.getImageLink().toString() : null;
        if (imageLink != null && !imageLink.isEmpty()) {
            Glide.with(context)
                .load(imageLink)
                .placeholder(R.drawable.ic_placeholder)
                .into(holder.imgProduct);
        } else {
            holder.imgProduct.setImageResource(R.drawable.ic_placeholder);
        }
        holder.txtProduct.setText(item.getProductName() != null ? item.getProductName().toString() : "");
        holder.txtProductPrice.setText(item.getProductPrice() != null ? item.getProductPrice().toString() : "");
        holder.txtProductRating.setText(""); // No rating in Firebase

        // Khi bấm vào sản phẩm
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProductDetailActivity.class);
            intent.putExtra("product", item); // ProductItem implements Serializable
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return wishlist.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView txtProduct, txtProductRating, txtProductPrice;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            txtProduct = itemView.findViewById(R.id.txtProduct);
            txtProductRating = itemView.findViewById(R.id.txtProductRating);
            txtProductPrice = itemView.findViewById(R.id.txtProductPrice);
        }
    }
}
