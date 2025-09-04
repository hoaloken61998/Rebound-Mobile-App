package com.rebound.models.Orders;
public class TimelineStep {
    public String title;
    public String date;
    public boolean isLast;

    public TimelineStep(String title, String date, boolean isLast) {
        this.title = title;
        this.date = date;
        this.isLast = isLast;
    }
}
