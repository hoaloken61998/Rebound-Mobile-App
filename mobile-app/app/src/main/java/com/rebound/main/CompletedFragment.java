package com.rebound.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.rebound.R;
import com.rebound.adapters.OrderAdapter;
import com.rebound.connectors.FirebaseOrderConnector;
import com.rebound.models.Orders.Order;
import com.rebound.callback.OrderFetchCallback;

import java.util.ArrayList;
import java.util.List;

public class CompletedFragment extends Fragment {

    private RecyclerView recyclerView;
    private OrderAdapter adapter;
    private final List<Order> orderList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_completed, container, false);
        recyclerView = view.findViewById(R.id.recyclerCompletedOrders);

        adapter = new OrderAdapter(getContext(), orderList, "completed", new OrderAdapter.OnOrderClickListener() {
            @Override
            public void onDelete(Order order) {
                new MaterialAlertDialogBuilder(getContext(), R.style.CustomDialogStyle)
                        .setTitle("Delete Order")
                        .setMessage("Are you sure you want to delete this order?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            // Delete from Firebase
                            FirebaseOrderConnector.deleteOrderById(String.valueOf(order.getOrderID()),
                                () -> {
                                    orderList.remove(order);
                                    adapter.notifyDataSetChanged();
                                    Toast.makeText(getContext(), "Order deleted", Toast.LENGTH_SHORT).show();
                                },
                                () -> Toast.makeText(getContext(), "Failed to delete order from database", Toast.LENGTH_SHORT).show()
                            );
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }

            @Override
            public void onBuyAgain(Order order) {
//                for (ProductItem product : order.getProductList()) {
//                    ProductItem item = new ProductItem();
//                    item.ProductName = product.ProductName;
//                    item.ProductPrice = product.ProductPrice;
//                    item.ImageLink = product.ImageLink;
//                    // Set other fields as needed
//                    CartManager.getInstance().addToCart(item);
//                }

                Toast.makeText(getContext(), "Added to cart", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onTrackOrderClicked(Order order) {
                // Không áp dụng
            }

            @Override
            public void onOrderReceived(Order order) {
                // Không áp dụng
            }


        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        loadCompletedOrders();
        return view;
    }

    private void loadCompletedOrders() {
        FirebaseOrderConnector.getOrdersForLoggedInUser(getContext(), new OrderFetchCallback() {
            @Override
            public void onOrdersFetched(List<Order> orders) {
                orderList.clear();
                for (Order o : orders) {
                    if ("Complete".equalsIgnoreCase(o.getStatus())) {
                        orderList.add(o);
                    }
                }
                if (adapter != null) adapter.notifyDataSetChanged();
            }
        });
    }

    // Deprecated: now handled by OrdersActivity via setOrders()
    public void setOrders(List<Order> orders) {
        orderList.clear();
        if (orders != null) {
            orderList.addAll(orders);
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }
}
