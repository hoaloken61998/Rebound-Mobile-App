package com.rebound.adapters;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.rebound.R;
import com.rebound.models.Cart.ProductItem;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {
    private final List<ProductItem> cartList;
    private final Runnable updateTotal;
    private final boolean isReadOnly;
    private final Context context; // ✅ Thêm context để hiển thị AlertDialog

    public CartAdapter(List<ProductItem> cartList, Runnable updateTotal, boolean isReadOnly) {
        this.cartList = cartList;
        this.updateTotal = updateTotal;
        this.isReadOnly = isReadOnly;
        this.context = null; // fallback nếu không dùng context
    }

    public CartAdapter(List<ProductItem> cartList, Runnable updateTotal, boolean isReadOnly, Context context) {
        this.cartList = cartList;
        this.updateTotal = updateTotal;
        this.isReadOnly = isReadOnly;
        this.context = context;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, variant, price, quantity;
        ImageView btnPlus, btnMinus, image;

        public ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.txtProductName);
            variant = itemView.findViewById(R.id.txtProductVariant);
            price = itemView.findViewById(R.id.txtProductPrice);
            quantity = itemView.findViewById(R.id.txtProductQuantity);
            btnPlus = itemView.findViewById(R.id.btnPlus);
            btnMinus = itemView.findViewById(R.id.btnMinus);
            image = itemView.findViewById(R.id.imgProduct);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
        return new ViewHolder(v);
    }

    private int extractPrice(Object priceObj) {
        long priceValue = 0;
        if (priceObj != null) {
            if (priceObj instanceof Number) {
                priceValue = ((Number) priceObj).longValue();
                if (priceValue <= 10000) {
                    priceValue = priceValue * 1000;
                }
            } else if (priceObj instanceof String) {
                try {
                    String s = ((String) priceObj).replace(",", "").replace(".", "").replace("VND", "").replace("VNĐ", "").replace("₫", "").trim();
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
        }
        return (int) priceValue;
    }

    private String formatPrice(int amount) {
        return String.format("%,d VND", amount).replace(',', '.');
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ProductItem item = cartList.get(position);

        // Defensive conversion for Object fields
        holder.name.setText(item.getProductName() != null ? item.getProductName().toString() : "");
        // Variant: not available in new model, so leave blank or set a placeholder
        holder.variant.setText("");
        // Defensive conversion for ImageLink
        String imageLink = item.getImageLink() != null ? item.getImageLink().toString() : "";
        if (!imageLink.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                .load(imageLink)
                .placeholder(R.drawable.ic_placeholder)
                .into(holder.image);
        } else {
            holder.image.setImageResource(R.drawable.ic_placeholder);
        }
        // Price calculation: parse ProductPrice (remove non-digits), multiply by quantity
        int unitPrice = extractPrice(item.getProductPrice());
        int quantity = 1;
        try {
            Long stockQuantityLong = item.getProductStockQuantity();
            if (stockQuantityLong != null) {
                if (stockQuantityLong > Integer.MAX_VALUE) {
                    quantity = Integer.MAX_VALUE;
                } else if (stockQuantityLong < Integer.MIN_VALUE) {
                    quantity = Integer.MIN_VALUE;
                } else {
                    quantity = stockQuantityLong.intValue();
                }
                if (quantity < 1) {
                    quantity = 1;
                }
            }
        } catch (Exception e) {
            quantity = 1;
        }
        int totalPrice = unitPrice * quantity;
        holder.price.setText(formatPrice(totalPrice));

        if (isReadOnly) {
            holder.btnPlus.setVisibility(View.GONE);
            holder.btnMinus.setVisibility(View.GONE);
            holder.quantity.setText("Qty: " + quantity);
        } else {
            holder.btnPlus.setVisibility(View.VISIBLE);
            holder.btnMinus.setVisibility(View.VISIBLE);
            holder.quantity.setText(String.valueOf(quantity));

            final int quantityFinal = quantity;
            final ProductItem finalItem = item;
            final int pos = position;
            holder.btnPlus.setOnClickListener(v -> {
                int newQty = quantityFinal + 1;
                finalItem.setProductStockQuantity(Long.valueOf(newQty));
                notifyItemChanged(pos);
                if (updateTotal != null) updateTotal.run();
            });

            holder.btnMinus.setOnClickListener(v -> {
                if (quantityFinal > 1) {
                    int newQty = quantityFinal - 1;
                    finalItem.setProductStockQuantity(Long.valueOf(newQty));
                    notifyItemChanged(pos);
                    if (updateTotal != null) updateTotal.run();
                } else {
                    // Logic to show "Remove item" dialog
                    new androidx.appcompat.app.AlertDialog.Builder(holder.itemView.getContext())
                            .setTitle("Remove item")
                            .setMessage("Do you want to remove this item from your cart?")
                            .setPositiveButton("Yes", (dialog, which) -> {
                                com.rebound.utils.CartManager.getInstance().removeFromCart(finalItem);
                                cartList.remove(pos);
                                notifyItemRemoved(pos);
                                notifyItemRangeChanged(pos, cartList.size());
                                if (updateTotal != null) updateTotal.run();
                            })
                            .setNegativeButton("No", null)
                            .show();
                }
            });


            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) holder.quantity.getLayoutParams();
            if (isReadOnly) {
                params.setMarginStart(0);
                params.setMarginEnd(0);
            } else {
                params.setMarginStart(40);
                params.setMarginEnd(40);
            }
            holder.quantity.setLayoutParams(params);
        }
    }

    @Override
    public int getItemCount() {
        return cartList.size();
    }
}
