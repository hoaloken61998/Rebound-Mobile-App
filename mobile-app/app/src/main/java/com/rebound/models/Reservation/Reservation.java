package com.rebound.models.Reservation;

public class Reservation {
    private String email;
    private String date;
    private String time;
    private String service;

    public Reservation(String email, String date, String time, String service) {
        this.email = email;
        this.date = date;
        this.time = time;
        this.service = service;
    }

    // Getter và Setter
    public String getEmail() {
        return email;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getService() {
        return service;
    }
}

