package com.rebound.connectors;


public class BranchConnector {
    private String name;
    private String address;
    private String hours;
    private int imageResId;


    public BranchConnector(String name, String address, String hours, int imageResId) {
        this.name = name;
        this.address = address;
        this.hours = hours;
        this.imageResId = imageResId;
    }


    public String getName() {
        return name;
    }


    public String getAddress() {
        return address;
    }


    public String getHours() {
        return hours;
    }


    public int getImageResId() {
        return imageResId;
    }
}

