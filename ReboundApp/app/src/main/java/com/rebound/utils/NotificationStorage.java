package com.rebound.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.rebound.models.Main.NotificationItem;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class NotificationStorage {

    private static final String PREFS_NAME = "NotificationPrefs";
    private static final String KEY_PREFIX = "notifications_";

    public static void saveNotification(Context context, String userKey, NotificationItem item) {
        List<NotificationItem> list = getNotifications(context, userKey);
        list.add(1, item); // chèn sau phần "Latest" header
        saveList(context, userKey, list);
    }

    public static List<NotificationItem> getNotifications(Context context, String userKey) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_PREFIX + userKey, null);
        Type type = new TypeToken<List<NotificationItem>>() {}.getType();
        if (json != null) {
            return new Gson().fromJson(json, type);
        } else {
            // Tạo mới có header
            List<NotificationItem> init = new ArrayList<>();
            init.add(new NotificationItem(NotificationItem.TYPE_HEADER, "Latest", "", ""));
            init.add(new NotificationItem(NotificationItem.TYPE_HEADER, "Older", "", ""));
            return init;
        }
    }

    private static void saveList(Context context, String userKey, List<NotificationItem> list) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_PREFIX + userKey, new Gson().toJson(list));
        editor.apply();
    }
}
