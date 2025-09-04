package com.rebound.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.rebound.R;
import com.rebound.models.Orders.Order;
import com.rebound.models.Cart.ProductItem;

import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    public interface OnOrderClickListener {
        void onDelete(Order order);
        void onBuyAgain(Order order);
        void onTrackOrderClicked(Order order);
        void onOrderReceived(Order order);
    }

    private final Context context;
    private final List<Order> orderList;
    private final String screenType;
    private final OnOrderClickListener listener;

    public OrderAdapter(Context context, List<Order> orderList, String screenType, OnOrderClickListener listener) {
        this.context = context;
        this.orderList = orderList;
        this.screenType = screenType;
        this.listener = listener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);
        String status = order.getStatus() != null ? order.getStatus() : "";

        holder.txtStatus.setText(status);
        String formattedAmount = String.format("%,d", order.getTotalAmount()).replace(',', '.');
        holder.txtTotal.setText(context.getString(R.string.total_amount_label) + ": " + formattedAmount + " đ");

        holder.groupToReceive.setVisibility(View.GONE);
        holder.groupShipped.setVisibility(View.GONE);
        holder.layoutOrderItems.removeAllViews();

        if ("ongoing".equals(screenType)) {
            holder.groupToReceive.setVisibility(View.VISIBLE);
            holder.btnDelete.setVisibility(View.GONE);

            boolean isToReceive = status.equalsIgnoreCase("To Receive") || status.equalsIgnoreCase("Pending") || status.equalsIgnoreCase("Ongoing");


            holder.btnOrderReceived.setEnabled(isToReceive);


            holder.btnOrderReceived.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.white));


            holder.btnOrderReceived.setTextColor(ContextCompat.getColor(context, R.color.gray_text));


            if (holder.btnOrderReceived instanceof com.google.android.material.button.MaterialButton) {
                com.google.android.material.button.MaterialButton btn = (com.google.android.material.button.MaterialButton) holder.btnOrderReceived;
                btn.setStrokeColorResource(R.color.gray_border); // phải có màu này trong colors.xml
                btn.setStrokeWidth(2); // viền mỏng đẹp
                btn.setCornerRadius(100); // bo góc giống nút kia
            }
            holder.btnOrderReceived.setOnClickListener(v -> {
                if (isToReceive && listener != null) listener.onOrderReceived(order);
            });


            holder.btnTrackOrder.setEnabled(true);
            holder.btnTrackOrder.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.accent_dark));
            holder.btnTrackOrder.setTextColor(ContextCompat.getColor(context, android.R.color.white));
            holder.btnTrackOrder.setOnClickListener(v -> {
                if (listener != null) listener.onTrackOrderClicked(order);
            });

        } else if ("completed".equals(screenType)) {
            holder.groupShipped.setVisibility(View.VISIBLE);
            holder.btnDelete.setVisibility(View.GONE);

            holder.btnBuyAgain.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.accent_dark));
            holder.btnBuyAgain.setTextColor(ContextCompat.getColor(context, android.R.color.white));
            holder.btnBuyAgain.setOnClickListener(v -> {
                if (listener != null) listener.onBuyAgain(order);
            });

            holder.btnTrackOrder.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.accent_dark));
            holder.btnTrackOrder.setTextColor(ContextCompat.getColor(context, android.R.color.white));
            holder.btnTrackOrder.setOnClickListener(v -> {
                if (listener != null) listener.onTrackOrderClicked(order);
            });
        }
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView txtStatus, txtTotal;
        LinearLayout layoutOrderItems, groupShipped, groupToReceive;
        Button btnDelete, btnBuyAgain, btnOrderReceived, btnTrackOrder;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            txtStatus = itemView.findViewById(R.id.txtOrderStatus);
            txtTotal = itemView.findViewById(R.id.txtOrderItemTotal);
            layoutOrderItems = itemView.findViewById(R.id.layoutOrderItems);
            groupShipped = itemView.findViewById(R.id.groupShipped);
            groupToReceive = itemView.findViewById(R.id.groupToReceive);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnBuyAgain = itemView.findViewById(R.id.btnBuyAgain);
            btnOrderReceived = itemView.findViewById(R.id.btnOrderReceived);
            btnTrackOrder = itemView.findViewById(R.id.btnTrackOrder);
        }
    }
}
