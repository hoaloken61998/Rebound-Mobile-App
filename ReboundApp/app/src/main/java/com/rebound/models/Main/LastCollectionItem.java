package com.rebound.models.Main;

public class LastCollectionItem {
    public String title;
    public String price;
    public int imageRes;
    public String rating;
    public String sold;
    public String description;
    public int imageGoldRes;
    public int imageSilverRes;

    public LastCollectionItem(String title, String price, int imageRes, String rating, String sold, String description, int imageGoldRes, int imageSilverRes) {
        this.title = title;
        this.price = price;
        this.imageRes = imageRes;
        this.rating = rating;
        this.sold = sold;
        this.description = description;
        this.imageGoldRes = imageGoldRes;
        this.imageSilverRes = imageSilverRes;
    }
}

