package com.rebound.models.Main;

import com.google.firebase.database.PropertyName;

public class NewsItem {

    @PropertyName("NewsID")
    private Object newsID;

    @PropertyName("News_Title")
    private String title;

    @PropertyName("News_shortDescription")
    private String subtitle;

    @PropertyName("News_Date")
    private String date;

    @PropertyName("News_Image")
    private String imageUrl;

    @PropertyName("News_fullContent")
    private String fullContent;

    public NewsItem() {
        // Firebase cần constructor rỗng
    }

    @PropertyName("NewsID")
    public Long getNewsID() {
        if (newsID instanceof Number) {
            return ((Number) newsID).longValue();
        }
        try {
            return Long.parseLong(newsID.toString());
        } catch (Exception e) {
            return null;
        }
    }

    @PropertyName("NewsID")
    public void setNewsID(Object newsID) {
        this.newsID = newsID;
    }

    @PropertyName("News_Title")
    public String getTitle() {
        return title;
    }

    @PropertyName("News_Title")
    public void setTitle(String title) {
        this.title = title;
    }

    @PropertyName("News_shortDescription")
    public String getSubtitle() {
        return subtitle;
    }

    @PropertyName("News_shortDescription")
    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    @PropertyName("News_Date")
    public String getDate() {
        return date;
    }

    @PropertyName("News_Date")
    public void setDate(String date) {
        this.date = date;
    }

    @PropertyName("News_Image")
    public String getImageUrl() {
        return imageUrl;
    }

    @PropertyName("News_Image")
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @PropertyName("News_fullContent")
    public String getFullContent() {
        return fullContent;
    }

    @PropertyName("News_fullContent")
    public void setFullContent(String fullContent) {
        this.fullContent = fullContent;
    }
}
