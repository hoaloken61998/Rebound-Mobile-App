package com.rebound.data;

import com.rebound.models.Onboarding.OnboardingItem;

import java.util.ArrayList;
import java.util.List;

public class OnboardingData {

    public static List<OnboardingItem> getItems() {
        List<OnboardingItem> items = new ArrayList<>();
        items.add(new OnboardingItem(
                "Seamless Shopping Experience",
                "Explore our stunning collection of ear piercings with a smooth and easy shopping experience."
        ));
        items.add(new OnboardingItem(
                "Ear Piercing Blog: Trends, Tips & Inspiration",
                "Stay up-to-date with the latest ear piercing trends, care tips, and style inspirations through our blog"
        ));
        items.add(new OnboardingItem(
                "Order Tracking: Stay Updated",
                "Track your order status from placement to delivery with our real-time order tracking feature."
        ));
        return items;
    }
}
