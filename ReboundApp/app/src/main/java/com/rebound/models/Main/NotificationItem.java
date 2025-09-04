package com.rebound.models.Main;

import com.google.firebase.database.PropertyName;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NotificationItem {

    public static final int TYPE_HEADER = 0;
    public static final int TYPE_NOTIFICATION = 1;
    public static final int TYPE_ITEM = TYPE_NOTIFICATION;

    private int type = TYPE_NOTIFICATION;
    private String title;
    private String content;
    private String timeAgo;
    private long timestamp;

    @PropertyName("NotificationID")
    private Object notificationID;

    @PropertyName("Message")
    private String message;

    @PropertyName("SentAt")
    private String sentAt;

    @PropertyName("UserID")
    private Object userID;

    public NotificationItem() {}

    public NotificationItem(int type, String title, String content, String timeAgo) {
        this.type = type;
        this.title = title;
        this.content = content;
        this.timeAgo = timeAgo;
    }

    public NotificationItem(int type, String title, String content, String timeAgo, long timestamp) {
        this.type = type;
        this.title = title;
        this.content = content;
        this.timeAgo = timeAgo;
        this.timestamp = timestamp;
    }

    @PropertyName("NotificationID")
    public Long getNotificationID() {
        if (notificationID instanceof Number) return ((Number) notificationID).longValue();
        try {
            return Long.parseLong(notificationID.toString());
        } catch (Exception e) {
            return null;
        }
    }

    @PropertyName("NotificationID")
    public void setNotificationID(Object notificationID) {
        this.notificationID = notificationID;
    }

    @PropertyName("Message")
    public String getMessage() {
        return message;
    }

    @PropertyName("Message")
    public void setMessage(String message) {
        this.message = message;
    }

    @PropertyName("SentAt")
    public String getSentAt() {
        return sentAt;
    }

    @PropertyName("SentAt")
    public void setSentAt(String sentAt) {
        this.sentAt = sentAt;

        // Parse date with UTC timezone
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC")); // Đảm bảo không lệch
            Date date = sdf.parse(sentAt);
            if (date != null) {
                this.timestamp = date.getTime();
            }
        } catch (Exception e) {
            this.timestamp = 0;
        }
    }

    @PropertyName("UserID")
    public Long getUserID() {
        if (userID == null) return null;
        if (userID instanceof Number) {
            return ((Number) userID).longValue();
        }
        try {
            double d = Double.parseDouble(userID.toString());
            return (long) d;
        } catch (Exception e) {
            return null;
        }
    }

    @PropertyName("UserID")
    public void setUserID(Object userID) {
        this.userID = userID;
    }

    // --- UI logic ---
    public int getType() {
        return type;
    }

    public String getTitle() {
        return title != null ? title : message;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content != null ? content : "";
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTimeAgo() {
        return timeAgo != null ? timeAgo : sentAt;
    }

    public void setTimeAgo(String timeAgo) {
        this.timeAgo = timeAgo;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    // --- Optional: categorize ---
    public List<NotificationItem> categorizeNotifications(List<NotificationItem> all) {
        long oneHourAgo = System.currentTimeMillis() - 3600 * 1000;
        List<NotificationItem> latest = new ArrayList<>();
        List<NotificationItem> older = new ArrayList<>();
        List<NotificationItem> result = new ArrayList<>();

        for (NotificationItem item : all) {
            if (item.getType() == TYPE_NOTIFICATION) {
                if (item.getTimestamp() >= oneHourAgo) {
                    latest.add(item);
                } else {
                    older.add(item);
                }
            }
        }

        if (!latest.isEmpty()) {
            result.add(new NotificationItem(TYPE_HEADER, "Latest", "", ""));
            result.addAll(latest);
        }

        if (!older.isEmpty()) {
            result.add(new NotificationItem(TYPE_HEADER, "Older", "", ""));
            result.addAll(older);
        }

        return result;
    }

    private static String objectToString(Object obj) {
        if (obj == null) return null;
        if (obj instanceof String) {
            try {
                double d = Double.parseDouble((String) obj);
                if (d == (long) d) return String.valueOf((long) d);
                return String.valueOf(d);
            } catch (NumberFormatException e) {
                return (String) obj;
            }
        }
        if (obj instanceof Number) {
            long l = ((Number) obj).longValue();
            return String.valueOf(l);
        }
        return obj.toString();
    }
}
