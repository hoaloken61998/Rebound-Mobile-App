package com.rebound.utils;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;

public class ContentLoader {
    public static String loadAssetText(Context context, String filename) {
        try {
            InputStream is = context.getAssets().open(filename);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            return new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return "";
        }
    }
}

