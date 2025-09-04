package com.rebound.models.Reservation;

import com.google.firebase.database.PropertyName;

public class BookingSchedule {

    @PropertyName("BookingID")
    private Object bookingID;

    @PropertyName("BookingTime")
    private String bookingTime;

    @PropertyName("LocationID")
    private Object locationID;

    @PropertyName("ServiceID")
    private Object serviceID;

    @PropertyName("Status")
    private String status;

    @PropertyName("UserID")
    private Object userID;

    public BookingSchedule() {}

    public BookingSchedule(Object bookingID, String bookingTime, Object locationID, Object serviceID, String status, Object userID) {
        this.bookingID = bookingID;
        this.bookingTime = bookingTime;
        this.locationID = locationID;
        this.serviceID = serviceID;
        this.status = status;
        this.userID = userID;
    }

    // --- BookingID ---
    @PropertyName("BookingID")
    public Long getBookingID() {
        return convertObjectToLong(bookingID);
    }

    @PropertyName("BookingID")
    public void setBookingID(Object bookingID) {
        this.bookingID = bookingID;
    }

    // --- BookingTime ---
    @PropertyName("BookingTime")
    public String getBookingTime() {
        return bookingTime;
    }

    @PropertyName("BookingTime")
    public void setBookingTime(String bookingTime) {
        this.bookingTime = bookingTime;
    }

    // --- LocationID ---
    @PropertyName("LocationID")
    public Long getLocationID() {
        return convertObjectToLong(locationID);
    }

    @PropertyName("LocationID")
    public void setLocationID(Object locationID) {
        this.locationID = locationID;
    }

    // --- ServiceID ---
    @PropertyName("ServiceID")
    public Long getServiceID() {
        return convertObjectToLong(serviceID);
    }

    @PropertyName("ServiceID")
    public void setServiceID(Object serviceID) {
        this.serviceID = serviceID;
    }

    // --- Status ---
    @PropertyName("Status")
    public String getStatus() {
        return status;
    }

    @PropertyName("Status")
    public void setStatus(String status) {
        this.status = status;
    }

    // --- UserID ---
    @PropertyName("UserID")
    public Long getUserID() {
        if (userID == null) return null;
        if (userID instanceof Number) {
            return ((Number) userID).longValue();
        }
        try {
            double d = Double.parseDouble(userID.toString());
            return (long) d;
        } catch (Exception e) {
            return null;
        }
    }

    @PropertyName("UserID")
    public void setUserID(Object userID) {
        this.userID = userID;
    }

    // --- Helper ---
    private Long convertObjectToLong(Object value) {
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).longValue();
        try {
            return Long.parseLong(value.toString());
        } catch (Exception e) {
            return null;
        }
    }
}