package com.rebound.models.Cart;

import java.util.ArrayList;
import java.util.List;

public class PaymentMethod {
    private Long PaymentMethodID ;
    private String PaymentMethodName;

    public PaymentMethod() {}
    public PaymentMethod(Long paymentMethodID, String paymentMethodName) {
        PaymentMethodID = paymentMethodID;
        PaymentMethodName = paymentMethodName;
    }
    public Long getPaymentMethodID() {
        return PaymentMethodID;
    }

    public void setPaymentMethodID(Long paymentMethodID) {
        PaymentMethodID = paymentMethodID;
    }

    public String getPaymentMethodName() {
        return PaymentMethodName;
    }

    public void setPaymentMethodName(String paymentMethodName) {
        PaymentMethodName = paymentMethodName;
    }

    public static List<PaymentMethod> getDefaultCategories() {
        List<PaymentMethod> paymentMethods = new ArrayList<>();
        PaymentMethod p1 = new PaymentMethod();
        p1.setPaymentMethodID(1L);
        p1.setPaymentMethodName("Credit Card");
        paymentMethods.add(p1);
        PaymentMethod p2 = new PaymentMethod();
        p2.setPaymentMethodID(2L);
        p2.setPaymentMethodName("Debit Card");
        paymentMethods.add(p2);
        PaymentMethod p3 = new PaymentMethod();
        p3.setPaymentMethodID(3L);
        p3.setPaymentMethodName("Bank Transfer");
        paymentMethods.add(p3);
        PaymentMethod p4 = new PaymentMethod();
        p4.setPaymentMethodID(4L);
        p4.setPaymentMethodName("Cash on Delivery");
        paymentMethods.add(p4);

        return paymentMethods;
    }
}
