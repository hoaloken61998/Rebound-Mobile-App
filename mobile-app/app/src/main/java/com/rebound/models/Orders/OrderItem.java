package com.rebound.models.Orders;

import java.io.Serializable;

public class OrderItem implements Serializable {

    private Long OrderItemID;
    private Long OrderID;
    private Long ProductID;
    private Long Quantity;
    private Object Price;

    private Long UserID; // Added UserID for consistency with Order class



    private Long Rating;

    // Add a constructor to set all fields, with Rating default to 0
    public OrderItem(Long orderItemID, Long orderID, Long productID, Long quantity, Object price, Long customerID) {
        this.OrderItemID = orderItemID;
        this.OrderID = orderID;
        this.ProductID = productID;
        this.Quantity = quantity;
        this.Price = price;
        this.UserID = customerID;
        this.Rating = 0L; // Default rating
    }

    public Long getOrderItemID() {
        return OrderItemID;
    }

    public void setOrderItemID(Long orderItemID) {
        OrderItemID = orderItemID;
    }

    public Long getOrderID() {
        return OrderID;
    }

    public void setOrderID(Long orderID) {
        OrderID = orderID;
    }

    public Long getProductID() {
        return ProductID;
    }

    public void setProductID(Long productID) {
        ProductID = productID;
    }

    public Long getQuantity() {
        return Quantity;
    }

    public void setQuantity(Long quantity) {
        Quantity = quantity;
    }

    public Object getPrice() {
        return Price;
    }

    public void setPrice(Object price) {
        Price = price;
    }

    public Long getRating() {
        return Rating;
    }

    public void setRating(Long rating) {
        Rating = rating;
    }

    public Long getUserID() {
        return UserID;
    }

    public void setUserID(Long userID) {
        UserID = userID;
    }
}
