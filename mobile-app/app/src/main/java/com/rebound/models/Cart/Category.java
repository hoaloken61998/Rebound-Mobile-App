package com.rebound.models.Cart;

import java.util.ArrayList;
import java.util.List;

public class Category {
    // --- Firebase fields ---
    public String CategoryName;
    public Long CategoryID;

    public Category() {
        // Default constructor required for Firebase
    }

    public String getCategoryName() {
        return CategoryName;
    }

    public void setCategoryName(String categoryName) {
        CategoryName = categoryName;
    }
    public Long getCategoryID() {
        return CategoryID;
    }

    public void setCategoryID(Long categoryID) {
        CategoryID = categoryID;
    }

    public static List<Category> getDefaultCategories() {
        List<Category> categories = new ArrayList<>();
        Category c1 = new Category();
        c1.setCategoryID(1L);
        c1.setCategoryName("Earrings");
        categories.add(c1);
        Category c2 = new Category();
        c2.setCategoryID(2L);
        c2.setCategoryName("Ring");
        categories.add(c2);
        Category c3 = new Category();
        c3.setCategoryID(3L);
        c3.setCategoryName("Necklace");
        categories.add(c3);
        Category c4 = new Category();
        c4.setCategoryID(4L);
        c4.setCategoryName("Body Piercings");
        categories.add(c4);
        return categories;
    }
}
