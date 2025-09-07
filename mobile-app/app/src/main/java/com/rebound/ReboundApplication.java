package com.rebound;

import android.app.Application;

public class ReboundApplication extends Application {
    private static ReboundApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        com.rebound.utils.CartManager.init(this);
        com.rebound.utils.OrderManager.init(this);
    }

    public static ReboundApplication getInstance() {
        return instance;
    }

    public static android.content.Context getAppContext() {
        return instance.getApplicationContext();
    }
}