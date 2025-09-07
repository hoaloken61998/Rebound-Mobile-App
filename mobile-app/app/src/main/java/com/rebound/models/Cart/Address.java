package com.rebound.models.Cart;

import com.google.firebase.database.PropertyName;
import java.io.Serializable;

public class Address implements Serializable {

    // All private fields are now camelCase to follow Java conventions
    private Long addressID;
    private Long userID;
    private String receiverName;
    private Long receiverPhone;
    private String province;
    private Object district;
    private Object ward;
    private String street;
    private String details;
    private String isDefault;

    public Address() {
        // Default constructor is required by Firebase
    }


    // Getters have @PropertyName to control the database key names (PascalCase)
    @PropertyName("AddressID")
    public Long getAddressID() {
        return addressID;
    }
    @PropertyName("AddressID")
    public void setAddressID(Long addressID) {
        this.addressID = addressID;
    }
    @PropertyName("UserID")
    public Long getUserID() {
        return userID;
    }
    @PropertyName("UserID")
    public void setUserID(Long userID) {
        this.userID = userID;
    }
    @PropertyName("ReceiverName")
    public String getReceiverName() {
        return receiverName;
    }
    @PropertyName("ReceiverName")
    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }
    @PropertyName("ReceiverPhone")
    public Long getReceiverPhone() {
        return receiverPhone;
    }
    @PropertyName("ReceiverPhone")
    public void setReceiverPhone(Long receiverPhone) {
        this.receiverPhone = receiverPhone;
    }
    @PropertyName("Province")
    public String getProvince() {
        return province;
    }
    @PropertyName("Province")
    public void setProvince(String province) {
        this.province = province;
    }
    @PropertyName("District")
    public Object getDistrict() {
        return district;
    }
    @PropertyName("District")
    public void setDistrict(Object district) {
        this.district = district;
    }
    @PropertyName("Ward")
    public Object getWard() {
        return ward;
    }
    @PropertyName("Ward")
    public void setWard(Object ward) {
        this.ward = ward;
    }
    @PropertyName("Street")
    public String getStreet() {
        return street;
    }
    @PropertyName("Street")
    public void setStreet(String street) {
        this.street = street;
    }
    @PropertyName("Details")
    public String getDetails() {
        return details;
    }
    @PropertyName("Details")
    public void setDetails(String details) {
        this.details = details;
    }
    @PropertyName("IsDefault")
    public String getIsDefault() {
        return isDefault;
    }
    @PropertyName("IsDefault")
    public void setIsDefault(String isDefault) {
        this.isDefault = isDefault;
    }
}