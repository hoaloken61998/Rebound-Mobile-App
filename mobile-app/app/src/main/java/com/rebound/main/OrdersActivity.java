package com.rebound.main;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.rebound.R;
import com.rebound.callback.OrderFetchCallback;
import com.rebound.connectors.FirebaseOrderConnector;
import com.rebound.models.Orders.Order;

import java.util.ArrayList;
import java.util.List;

public class OrdersActivity extends AppCompatActivity {

    private TextView tabOngoing, tabCompleted;

    private final Fragment ongoingFragment = new OngoingFragment();
    private final Fragment completedFragment = new CompletedFragment();

    private List<Order> allOrders = new ArrayList<>();
    private boolean ordersLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

        // Gán view
        tabOngoing = findViewById(R.id.tabOngoing);
        tabCompleted = findViewById(R.id.tabCompleted);

        ImageView imgBack = findViewById(R.id.imgBack);
        imgBack.setOnClickListener(v -> finish());

        // Load mặc định Fragment ongoing
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, ongoingFragment)
                .commit();

        fetchOrdersAndInitTabs();
    }

    private void fetchOrdersAndInitTabs() {
        if (ordersLoaded) {
            showOngoingOrders();
            setupTabListeners();
            return;
        }
        FirebaseOrderConnector.getOrdersForLoggedInUser(this, new OrderFetchCallback() {
            @Override
            public void onOrdersFetched(List<Order> orders) {
                Log.d("OrdersActivity", "onOrdersFetched called, orders.size(): " + (orders != null ? orders.size() : 0));
                if (orders != null) {
                    for (Order o : orders) {
                        Log.d("OrdersActivity", "Fetched OrderID: " + o.getOrderID() + ", UserID: " + o.getUserID() + ", Status: " + o.getStatus());
                    }
                }
                allOrders = orders;
                ordersLoaded = true;
                showOngoingOrders();
                setupTabListeners();
            }
        });
    }

    private void setupTabListeners() {
        tabOngoing.setOnClickListener(v -> {
            showOngoingOrders();
            tabOngoing.setBackgroundResource(R.drawable.tab_selected);
            tabCompleted.setBackgroundResource(android.R.color.transparent);
            tabOngoing.setTextColor(getColor(R.color.white));
            tabCompleted.setTextColor(getColor(R.color.accent_dark));
        });
        tabCompleted.setOnClickListener(v -> {
            showCompletedOrders();
            tabCompleted.setBackgroundResource(R.drawable.tab_selected);
            tabOngoing.setBackgroundResource(android.R.color.transparent);
            tabCompleted.setTextColor(getColor(R.color.white));
            tabOngoing.setTextColor(getColor(R.color.accent_dark));
        });
    }

    private void showOngoingOrders() {
        List<Order> ongoing = new ArrayList<>();
        for (Order o : allOrders) {
            if (o.getStatus() != null && (o.getStatus().equalsIgnoreCase("Pending") || o.getStatus().equalsIgnoreCase("Ongoing"))) {
                ongoing.add(o);
            }
        }
        if (ongoingFragment instanceof OngoingFragment) {
            ((OngoingFragment) ongoingFragment).setOrders(ongoing);
        }
        switchFragment(ongoingFragment);
    }

    private void showCompletedOrders() {
        List<Order> completed = new ArrayList<>();
        for (Order o : allOrders) {
            if (o.getStatus() != null && o.getStatus().equalsIgnoreCase("Completed")) {
                completed.add(o);
            }
        }
        if (completedFragment instanceof CompletedFragment) {
            ((CompletedFragment) completedFragment).setOrders(completed);
        }
        switchFragment(completedFragment);
    }

    private void switchFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }
    public void refreshCompletedTab() {
        List<Order> completed = new ArrayList<>();
        for (Order o : allOrders) {
            if ("Completed".equalsIgnoreCase(o.getStatus())) {
                completed.add(o);
            }
        }

        if (completedFragment instanceof CompletedFragment) {
            ((CompletedFragment) completedFragment).setOrders(completed);
        }
    }
}