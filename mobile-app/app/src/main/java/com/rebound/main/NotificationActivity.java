package com.rebound.main;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rebound.R;
import com.rebound.adapters.NotificationAdapter;
import com.rebound.models.Main.NotificationItem;
import com.rebound.models.Customer.Customer;
import com.rebound.utils.FirebaseNotificationFetcher;
import com.rebound.utils.SharedPrefManager;
import com.rebound.callback.FirebaseListCallback;

import java.util.ArrayList;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {

    RecyclerView recyclerViewNotification;
    ImageView imgBackNotification;
    NotificationAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        recyclerViewNotification = findViewById(R.id.recyclerViewNotification);
        recyclerViewNotification.setLayoutManager(new LinearLayoutManager(this));

        // Lấy user hiện tại
        Customer currentCustomer = SharedPrefManager.getCurrentCustomer(this);
        Double userID = null;
        if (currentCustomer != null && currentCustomer.getUserID() != null) {
            try {
                userID = Double.parseDouble(currentCustomer.getUserID());
            } catch (NumberFormatException e) {
                Log.e("NotificationDebug", "UserID không hợp lệ: " + currentCustomer.getUserID());
            }
        }

        Log.d("NotificationDebug", "UserID hiện tại: " + userID + " (" + (userID != null ? userID.getClass().getSimpleName() : "null") + ")");

        if (userID == null) {
            startActivity(new Intent(this, NoNotificationActivity.class));
            finish();
            return;
        }

        // Gọi Firebase để lấy thông báo
        FirebaseNotificationFetcher.getNotificationsByUserID(userID, new FirebaseListCallback<NotificationItem>() {
            @Override
            public void onSuccess(ArrayList<NotificationItem> allNoti) {
                if (allNoti == null || allNoti.isEmpty()) {
                    Log.d("NotificationDebug", "Không có thông báo.");
                    startActivity(new Intent(NotificationActivity.this, NoNotificationActivity.class));
                    finish();
                    return;
                }

                for (NotificationItem item : allNoti) {
                    if (item.getType() == NotificationItem.TYPE_NOTIFICATION) {
                        item.setTimeAgo(formatTimeAgo(item.getTimestamp()));
                    }
                }

                List<NotificationItem> organizedList = categorizeNotifications(allNoti);

                int notificationCount = 0;
                for (NotificationItem item : organizedList) {
                    if (item.getType() == NotificationItem.TYPE_NOTIFICATION) notificationCount++;
                }

                if (notificationCount == 0) {
                    Log.d("NotificationDebug", "Không có item nào thuộc loại TYPE_NOTIFICATION.");
                    startActivity(new Intent(NotificationActivity.this, NoNotificationActivity.class));
                    finish();
                    return;
                }

                Log.d("NotificationDebug", "Số thông báo thực tế: " + notificationCount);
                adapter = new NotificationAdapter(organizedList);
                recyclerViewNotification.setAdapter(adapter);
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(NotificationActivity.this, "Không thể tải thông báo: " + errorMessage, Toast.LENGTH_SHORT).show();
                startActivity(new Intent(NotificationActivity.this, NoNotificationActivity.class));
                finish();
            }
        });

        imgBackNotification = findViewById(R.id.imgBackNotification);
        imgBackNotification.setOnClickListener(v -> finish());
    }

    private String formatTimeAgo(long timestamp) {
        long now = System.currentTimeMillis();
        long diff = now - timestamp;

        long minutes = diff / (1000 * 60);
        if (minutes < 1) return "Just now";
        if (minutes < 60) return minutes + " minutes ago";

        long hours = minutes / 60;
        if (hours < 24) return hours + " hours ago";

        long days = hours / 24;
        return days + " days ago";
    }

    private List<NotificationItem> categorizeNotifications(List<NotificationItem> all) {
        long oneHourAgo = System.currentTimeMillis() - 3600 * 1000;

        List<NotificationItem> latest = new ArrayList<>();
        List<NotificationItem> older = new ArrayList<>();

        for (NotificationItem item : all) {
            if (item.getType() == NotificationItem.TYPE_NOTIFICATION) {
                if (item.getTimestamp() >= oneHourAgo) {
                    latest.add(item);
                } else {
                    older.add(item);
                }
            }
        }

        List<NotificationItem> result = new ArrayList<>();
        if (!latest.isEmpty()) {
            result.add(new NotificationItem(NotificationItem.TYPE_HEADER, "Latest", "", ""));
            result.addAll(latest);
        }
        if (!older.isEmpty()) {
            result.add(new NotificationItem(NotificationItem.TYPE_HEADER, "Older", "", ""));
            result.addAll(older);
        }
        return result;
    }
}
