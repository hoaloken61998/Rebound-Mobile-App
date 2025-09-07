package com.rebound.data;

import com.rebound.R;
import com.rebound.models.Main.LastCollectionItem;

import java.util.ArrayList;
import java.util.List;

public class LastCollectionData {

    public static List<LastCollectionItem> getItems() {
        List<LastCollectionItem> list = new ArrayList<>();

        list.add(new LastCollectionItem(
                "Top 14kt Eternal Cz Buddha",
                "1.950.000 VND",
                R.mipmap.lastcollection1,
                "4.9",
                "87 SOLD",
                "An elegant pendant crafted in 14kt gold with a CZ Buddha design symbolizing peace and eternity.",
                R.mipmap.ic_earring_gold, // hoặc ảnh vàng riêng
                R.mipmap.ic_earring_silver // hoặc ảnh bạc riêng
        ));

        list.add(new LastCollectionItem(
                "Top 14kt Crux Xanh Ember",
                "12.400.000 VND",
                R.mipmap.lastcollection2,
                "4.8",
                "42 SOLD",
                "Crux Xanh Ember necklace blends 14kt gold with deep green stones for a celestial glow.",
                R.mipmap.ic_earring_gold,
                R.mipmap.ic_earring_silver
        ));

        list.add(new LastCollectionItem(
                "Top 14kt Celestial Diamond",
                "6.480.000 VND",
                R.mipmap.lastcollection3,
                "5.0",
                "103 SOLD",
                "A breathtaking celestial diamond piece designed for timeless elegance and grace.",
                R.mipmap.ic_earring_gold,
                R.mipmap.ic_earring_silver
        ));

        return list;
    }
}
