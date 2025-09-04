package com.rebound.adapters;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.rebound.R;
import com.rebound.models.Cart.ProductItem;

import java.util.List;

public class ProductARAdapter extends RecyclerView.Adapter<ProductARAdapter.ViewHolder> {

    private final List<ProductItem> productList;
    private final Context context;
    private final OnItemClickListener listener; // ADD THIS LINE

    // --- Define an interface for click events ---
    public interface OnItemClickListener {
        void onItemClick(ProductItem product);
    }

    // --- Modify the constructor to accept the listener ---
    public ProductARAdapter(List<ProductItem> productList, Context context, OnItemClickListener listener) {
        this.productList = productList;
        this.context = context;
        this.listener = listener; // ADD THIS LINE
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product_ar, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ProductItem product = productList.get(position);

        // --- ADD THIS BLOCK to handle the click ---
        holder.bind(product, listener);

        holder.txtProductAR.setText(product.getProductName() != null ? product.getProductName().toString() : ""); //
        String imageLink = product.getImageLink() != null ? product.getImageLink().toString() : null; //
        if (imageLink != null && !imageLink.isEmpty()) {
            Glide.with(context)
                    .load(imageLink)
                    .placeholder(R.drawable.ic_placeholder)
                    .into(holder.imgProductAR);
        } else {
            holder.imgProductAR.setImageResource(R.drawable.ic_placeholder);
        }

        int totalItem = getItemCount();
        ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();

        if (totalItem > 0 && totalItem <= 4) {
            DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
            int screenWidth = displayMetrics.widthPixels;
            layoutParams.width = screenWidth / totalItem;
        } else {
            layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        }
        holder.itemView.setLayoutParams(layoutParams);
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProductAR;
        TextView txtProductAR;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProductAR = itemView.findViewById(R.id.imgProductAR);
            txtProductAR = itemView.findViewById(R.id.txtProductAR);
        }

        // --- ADD THIS METHOD to connect the item view to the listener ---
        public void bind(final ProductItem item, final OnItemClickListener listener) {
            itemView.setOnClickListener(v -> listener.onItemClick(item));
        }
    }
}