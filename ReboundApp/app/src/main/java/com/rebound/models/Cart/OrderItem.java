package com.rebound.models.Cart;

public class OrderItem {
    // All fields should be private
    private long orderItemID;
    private long orderID;
    private long productID;
    private int quantity;
    private long price;
    private long userID;

    public OrderItem() {}

    public OrderItem(long orderItemID, long orderID, long productID, int quantity, long price, long userID) {
        this.orderItemID = orderItemID;
        this.orderID = orderID;
        this.productID = productID;
        this.quantity = quantity;
        this.price = price;
        this.userID = userID;
    }

    public long getOrderItemID() { return orderItemID; }
    public void setOrderItemID(long orderItemID) { this.orderItemID = orderItemID; }

    public long getOrderID() { return orderID; }
    public void setOrderID(long orderID) { this.orderID = orderID; }

    public long getProductID() { return productID; }
    public void setProductID(long productID) { this.productID = productID; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public long getPrice() { return price; }
    public void setPrice(long price) { this.price = price; }

    public long getUserID() { return userID; }
    public void setUserID(long userID) { this.userID = userID; }
}
