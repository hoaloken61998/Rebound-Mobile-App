package com.rebound.main;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.rebound.R;
import com.rebound.adapters.OrderAdapter;
import com.rebound.connectors.FirebaseOrderConnector;
import com.rebound.models.Orders.Order;
import com.rebound.callback.OrderFetchCallback;
import com.rebound.utils.OrderManager;

import java.util.ArrayList;
import java.util.List;

public class OngoingFragment extends Fragment {

    private RecyclerView recyclerView;
    private List<Order> orderList = new ArrayList<>();
    private OrderAdapter adapter;
    private List<Order> currentOrders = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ongoing, container, false);
        recyclerView = view.findViewById(R.id.recyclerOngoingOrders);


        adapter = new OrderAdapter(getContext(), orderList, "ongoing", new OrderAdapter.OnOrderClickListener() {
            @Override
            public void onDelete(Order order) {
                // Không dùng trong ongoing
            }

            @Override
            public void onBuyAgain(Order order) {
                // Không dùng trong ongoing
            }

            @Override
            public void onTrackOrderClicked(Order order) {
                FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragmentContainer, new TrackOrderFragment());
                transaction.addToBackStack(null);
                transaction.commit();
            }

            @Override
            public void onOrderReceived(Order order) {
                if (order == null || order.getOrderID() == null) {
                    Log.e("OngoingFragment", "Order or OrderID is null when 'Received' is clicked");
                    Toast.makeText(getContext(), "Error: Unable to update order", Toast.LENGTH_SHORT).show();
                    return;
                }

                Log.d("OngoingFragment", "'Received' clicked for OrderID=" + order.getOrderID());

                FirebaseOrderConnector.updateOrderStatus(String.valueOf(order.getOrderID()), "Completed",
                        () -> {
                            Log.d("OngoingFragment", "Firebase successfully updated status for OrderID=" + order.getOrderID());
                            orderList.remove(order);
                            adapter.notifyDataSetChanged();

                            if (getActivity() instanceof OrdersActivity) {
                                ((OrdersActivity) getActivity()).refreshCompletedTab();
                            }

                            Toast.makeText(getContext(), "Order marked as completed", Toast.LENGTH_SHORT).show();
                        },
                        () -> {
                            Log.e("OngoingFragment", "Failed to update status for OrderID=" + order.getOrderID());
                            Toast.makeText(getContext(), "Failed to update order status", Toast.LENGTH_SHORT).show();
                        });
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        loadOngoingOrders();
        return view;
    }

    private void loadOngoingOrders() {
        Log.d("OngoingFragment", "loadOngoingOrders: called");
        FirebaseOrderConnector.getOrdersForLoggedInUser(getContext(), new OrderFetchCallback() {
            @Override
            public void onOrdersFetched(List<Order> orders) {
               Log.d("OngoingFragment", "onOrdersFetched: orders.size=" + (orders != null ? orders.size() : 0));
                orderList.clear();
                for (Order o : orders) {
                    if (o != null && "Pending".equalsIgnoreCase(o.getStatus())) {
                        orderList.add(o);
                    }
                }
                adapter.notifyDataSetChanged();
            }
        });
    }
    @Override
    public void onResume() {
        super.onResume();
        // Only reload if currentOrders is empty (first load), otherwise keep currentOrders
        if (currentOrders.isEmpty()) {
            loadOngoingOrders();
        } else {
            orderList.clear();
            orderList.addAll(currentOrders);
            if (adapter != null) adapter.notifyDataSetChanged();
        }
    }

    public void setOrders(List<Order> orders) {
        if (orderList == null) orderList = new ArrayList<>();
        orderList.clear();
        currentOrders.clear();
        int count = 0;
        if (orders != null) {
            for (Order o : orders) {
                if (o != null && o.getStatus() != null) {
                    android.util.Log.d("OngoingFragment", "OrderID: " + o.getOrderID() + ", Status: " + o.getStatus());
                }
                // Accept both "Pending" and "Ongoing" (or "To Receive")
                if (o != null && o.getStatus() != null && (o.getStatus().equalsIgnoreCase("Pending") || o.getStatus().equalsIgnoreCase("Ongoing") || o.getStatus().equalsIgnoreCase("To Receive"))) {
                    orderList.add(o);
                    currentOrders.add(o);
                    count++;
                }
            }
        }
        android.util.Log.d("OngoingFragment", "setOrders called, count: " + count);
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }
}
