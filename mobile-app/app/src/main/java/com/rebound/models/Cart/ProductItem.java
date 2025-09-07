package com.rebound.models.Cart;

import com.rebound.callback.FirebaseListCallback;
import com.rebound.callback.FirebaseSingleCallback;

import java.io.Serializable;

public class ProductItem implements Serializable {
    // --- Firebase fields ---
    private Object ProductName;
    private Object ProductDescription;
    private Object ProductPrice;
    private Object ImageLink;

    // Assuming these are numbers (Longs) in Firebase
    private Long ProductID;
    private Long CategoryID;
    private Long ProductStockQuantity;
    private Long StatusID;
    private Long SoldQuantity;
    private Double Rating;

    public ProductItem() {
        // Default constructor required for Firebase
    }


    public Object getProductName() {
        return ProductName;
    }

    public void setProductName(Object productName) {
        ProductName = productName;
    }

    public Object getProductDescription() {
        return ProductDescription;
    }

    public void setProductDescription(Object productDescription) {
        ProductDescription = productDescription;
    }

    public Object getProductPrice() {
        return ProductPrice;
    }

    public void setProductPrice(Object productPrice) {
        ProductPrice = productPrice;
    }

    public Object getImageLink() {
        return ImageLink;
    }

    public void setImageLink(Object imageLink) {
        ImageLink = imageLink;
    }

    public Long getProductID() {
        return ProductID;
    }

    public void setProductID(Long productID) {
        ProductID = productID;
    }

    public Long getCategoryID() {
        return CategoryID;
    }

    public void setCategoryID(Long categoryID) {
        CategoryID = categoryID;
    }

    public Long getProductStockQuantity() {
        return ProductStockQuantity;
    }

    public void setProductStockQuantity(Long productStockQuantity) {
        ProductStockQuantity = productStockQuantity;
    }

    public Long getStatusID() {
        return StatusID;
    }

    public void setStatusID(Long statusID) {
        StatusID = statusID;
    }

    public Long getSoldQuantity() {
        return SoldQuantity;
    }

    public void setSoldQuantity(Long soldQuantity) {
        SoldQuantity = soldQuantity;
    }

    public Double getRating() {
        return Rating;
    }

    public void setRating(Double rating) {
        Rating = rating;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ProductItem other = (ProductItem) obj;
        if (ProductID == null || other.ProductID == null) return false;
        return ProductID.equals(other.ProductID);
    }

    @Override
    public int hashCode() {
        return ProductID != null ? ProductID.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "ProductItem{" +
                "ProductID=" + ProductID +
                ", ProductName='" + ProductName + '\'' +
                ", ProductDescription='" + ProductDescription + '\'' +
                ", ProductPrice=" + ProductPrice +
                ", ImageLink='" + ImageLink + '\'' +
                ", CategoryID=" + CategoryID +
                ", ProductStockQuantity=" + ProductStockQuantity +
                ", StatusID=" + StatusID +
                ", SoldQuantity=" + SoldQuantity +
                ", Rating=" + Rating +
                '}';
    }
}
