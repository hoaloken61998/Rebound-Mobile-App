package com.rebound.models.Main;

public class NewsDetailItem {
    private String title;
    private String description;
    private String date;
    private int image;

    public NewsDetailItem(String title, String description, String date, int image) {
        this.title = title;
        this.description = description;
        this.date = date;
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {  // nếu bạn đang dùng getContent(), thì đổi tên này lại
        return description;
    }

    public String getDate() {
        return date;
    }

    public int getImageResId() {  // để gọi từ NewsData
        return image;
    }

    // Nếu bạn đang gọi getContent(), thêm alias bên dưới
    public String getContent() {
        return description;
    }
}
