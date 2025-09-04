package com.rebound.models.Orders;

import com.google.firebase.database.PropertyName;
import java.io.Serializable;

public class Order implements Serializable {
    @PropertyName("OrderID")
    private Long orderID;

    @PropertyName("UserID")
    private Long userID;

    @PropertyName("PaymentMethodID")
    private Long paymentMethodID;

    @PropertyName("DeliveryFee")
    private Long deliveryFee;

    @PropertyName("DiscountValue")
    private Double discountValue;

    @PropertyName("Subtotal")
    private Long subtotal;

    @PropertyName("TotalAmount")
    private Long totalAmount;

    @PropertyName("UserPromotion")
    private Long userPromotion;

    @PropertyName("OrderDate")
    private String orderDate;

    @PropertyName("Status")
    private String status;

    public Order() {}

    public Order(Long orderID, Long userID, Long paymentMethodID, Long deliveryFee,
                 Double discountValue, Long subtotal, Long totalAmount,
                 Long userPromotion, String orderDate, String status) {
        this.orderID = orderID;
        this.userID = userID;
        this.paymentMethodID = paymentMethodID;
        this.deliveryFee = deliveryFee;
        this.discountValue = discountValue;
        this.subtotal = subtotal;
        this.totalAmount = totalAmount;
        this.userPromotion = userPromotion;
        this.orderDate = orderDate;
        this.status = status;
    }

    @PropertyName("OrderID")
    public Long getOrderID() { return orderID; }
    @PropertyName("OrderID")
    public void setOrderID(Long orderID) { this.orderID = orderID; }

    @PropertyName("UserID")
    public Long getUserID() { return userID; }
    @PropertyName("UserID")
    public void setUserID(Long userID) { this.userID = userID; }

    @PropertyName("PaymentMethodID")
    public Long getPaymentMethodID() { return paymentMethodID; }
    @PropertyName("PaymentMethodID")
    public void setPaymentMethodID(Long paymentMethodID) { this.paymentMethodID = paymentMethodID; }

    @PropertyName("DeliveryFee")
    public Long getDeliveryFee() { return deliveryFee; }
    @PropertyName("DeliveryFee")
    public void setDeliveryFee(Long deliveryFee) { this.deliveryFee = deliveryFee; }

    @PropertyName("DiscountValue")
    public Double getDiscountValue() { return discountValue; }
    @PropertyName("DiscountValue")
    public void setDiscountValue(Double discountValue) { this.discountValue = discountValue; }

    @PropertyName("Subtotal")
    public Long getSubtotal() { return subtotal; }
    @PropertyName("Subtotal")
    public void setSubtotal(Long subtotal) { this.subtotal = subtotal; }

    @PropertyName("TotalAmount")
    public Long getTotalAmount() { return totalAmount; }
    @PropertyName("TotalAmount")
    public void setTotalAmount(Long totalAmount) { this.totalAmount = totalAmount; }

    @PropertyName("UserPromotion")
    public Long getUserPromotion() { return userPromotion; }
    @PropertyName("UserPromotion")
    public void setUserPromotion(Long userPromotion) { this.userPromotion = userPromotion; }

    @PropertyName("OrderDate")
    public String getOrderDate() { return orderDate; }
    @PropertyName("OrderDate")
    public void setOrderDate(String orderDate) { this.orderDate = orderDate; }

    // ✅ Getter/Setter cho "Status"
    @PropertyName("Status")
    public String getStatus() { return status; }

    @PropertyName("Status")
    public void setStatus(String status) { this.status = status; }

    // ✅ Fallback nếu Firebase trả về field "status" (chữ thường)
}
