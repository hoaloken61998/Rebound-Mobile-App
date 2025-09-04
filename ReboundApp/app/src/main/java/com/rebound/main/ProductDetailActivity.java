package com.rebound.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.rebound.R;
import com.rebound.utils.CartManager;
import com.rebound.checkout.ShoppingCartActivity;
import com.rebound.models.Cart.ProductItem;
import com.rebound.utils.WishlistManager;
import com.rebound.models.Customer.Customer;
import com.rebound.utils.SharedPrefManager;

public class ProductDetailActivity extends AppCompatActivity {

    private ImageView productImage, heartIcon;
    private FrameLayout goldColor, silverColor;
    private ImageView checkGold, checkSilver;
    private TextView productTitle, soldText, ratingText, productDetailsContent;
    private TextView productPriceText, quantityValue, totalPriceValue;
    private ImageView btnDecrease, btnPlus;

    private int quantity = 1;
    private boolean isHearted = false;
    private int pricePerItem = 0;
    private ProductItem currentItem;
    private boolean isGoldSelected = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        // Ánh xạ view
        productImage = findViewById(R.id.imgProductdetailImage);
        goldColor = findViewById(R.id.drawableProductdetailColorGold);
        silverColor = findViewById(R.id.drawableProductdetailColorSilver);
        checkGold = findViewById(R.id.drawableProductdetailcolorGold);
        checkSilver = findViewById(R.id.drawableProductdetailCheckSilver);
        heartIcon = findViewById(R.id.imgProductdetailButtonHeart);

        productTitle = findViewById(R.id.txtProductdetailProductTitle);
        productPriceText = findViewById(R.id.txtProductdetailProductPrice);
        soldText = findViewById(R.id.drawableProductImageSold);
        ratingText = findViewById(R.id.txtProductdetailRatingText);
        productDetailsContent = findViewById(R.id.txtProductDetailContent);
        quantityValue = findViewById(R.id.txtProductDetailQuantityValue);
        totalPriceValue = findViewById(R.id.txtProductdetailTotalpriceValue);
        btnDecrease = findViewById(R.id.imgProductdetailButtonDecrease);
        btnPlus = findViewById(R.id.imgProductdetailButtonPlus);
        MaterialButton btnAddToCart = findViewById(R.id.btnProductdetailAddtocart);

        Intent intent = getIntent();
        currentItem = (ProductItem) intent.getSerializableExtra("product");

        if (currentItem != null) {
            productTitle.setText(currentItem.getProductName() != null ? currentItem.getProductName().toString() : "");
            soldText.setText(""); // You can load sold count from Firebase if needed
            productDetailsContent.setText(currentItem.getProductDescription() != null ? currentItem.getProductDescription().toString() : "");
            // Load image from URL using Glide
            String imageLink = currentItem.getImageLink() != null ? currentItem.getImageLink().toString() : null;
            if (imageLink != null && !imageLink.isEmpty()) {
                Glide.with(this)
                    .load(imageLink)
                    .placeholder(R.drawable.ic_placeholder)
                    .into(productImage);
            } else {
                productImage.setImageResource(R.drawable.ic_placeholder);
            }
            isGoldSelected = false;

            // Show rating instantly from ProductItem
            if (currentItem.getRating() != null) {
                ratingText.setText("Rating: " + currentItem.getRating());
            } else {
                ratingText.setText("Rating: N/A");
            }

            // Show number of sold for this product instantly from ProductItem
            if (currentItem.getSoldQuantity() != null) {
                soldText.setText("Sold: " + currentItem.getSoldQuantity());
            } else {
                soldText.setText("Sold: 0");
            }

            // Show price formatted
            if (currentItem.getProductPrice() != null) {
                productPriceText.setText(formatPrice(currentItem.getProductPrice()));
            } else {
                productPriceText.setText("");
            }

            try {
                String cleaned = "0";
                if (currentItem.getProductPrice() != null) {
                    cleaned = formatPrice(currentItem.getProductPrice()).replace(".", "").replace(" VNĐ", "");
                }
                pricePerItem = Integer.parseInt(cleaned);
            } catch (Exception e) {
                pricePerItem = 0;
            }
        }

        quantityValue.setText(String.valueOf(quantity));
        updateTotalPrice();

        ImageView backButton = findViewById(R.id.imgProductdetailButtonBack);
        backButton.setOnClickListener(v -> finish());

        goldColor.setOnClickListener(v -> {
            // No longer show unsupported message, just let the user click and do nothing
        });

        silverColor.setOnClickListener(v -> {
            // No longer show unsupported message, just let the user click and do nothing
        });

        heartIcon.setOnClickListener(v -> {
            Customer currentCustomer = SharedPrefManager.getCurrentCustomer(ProductDetailActivity.this);
            if (currentCustomer == null) {
                Toast.makeText(ProductDetailActivity.this, "Please log in to add to your wishlist.", Toast.LENGTH_SHORT).show();
                return;
            }
            isHearted = !isHearted;
            if (isHearted) {
                heartIcon.setImageResource(R.drawable.ic_heart_filled);
                WishlistManager.getInstance(this).addToWishlist(currentItem);
                Toast.makeText(this, "Added to wishlist", Toast.LENGTH_SHORT).show();
            } else {
                heartIcon.setImageResource(R.mipmap.ic_heart);
                // Convert ProductName to String for removeFromWishlist
                WishlistManager.getInstance(this).removeFromWishlist(currentItem.getProductName() != null ? currentItem.getProductName().toString() : "");
                Toast.makeText(this, "Removed from wishlist", Toast.LENGTH_SHORT).show();
            }
        });

        btnDecrease.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                quantityValue.setText(String.valueOf(quantity));
                updateTotalPrice();
            }
        });

        btnPlus.setOnClickListener(v -> {
            quantity++;
            quantityValue.setText(String.valueOf(quantity));
            updateTotalPrice();
        });

        btnAddToCart.setOnClickListener(v -> {
            Customer currentCustomer = SharedPrefManager.getCurrentCustomer(this);
            if (currentCustomer == null) {
                Toast.makeText(this, "Please log in to add items to your cart", Toast.LENGTH_SHORT).show();
                return;
            }
            CartManager.getInstance().setUserEmail(currentCustomer.getUsername());
            if (currentItem != null) {
                ProductItem itemCopy = new ProductItem();
                itemCopy.setProductName(currentItem.getProductName());
                itemCopy.setProductPrice(currentItem.getProductPrice());
                itemCopy.setImageLink(currentItem.getImageLink());
                itemCopy.setProductDescription(currentItem.getProductDescription());
                itemCopy.setProductID(currentItem.getProductID());
                itemCopy.setCategoryID(currentItem.getCategoryID());
                itemCopy.setStatusID(currentItem.getStatusID());
                // Set the quantity directly in the cart
                CartManager.getInstance().setProductQuantity(itemCopy, quantity);
                Toast.makeText(this, "Cart updated", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, ShoppingCartActivity.class));
            }
        });
    }

    private void updateTotalPrice() {
        int totalPrice = pricePerItem * quantity;
        String priceText = formatPrice(totalPrice);
        totalPriceValue.setText(priceText);
    }

    private static String formatPrice(Object priceObj) {
        long priceValue = 0;
        if (priceObj != null) {
            if (priceObj instanceof Number) {
                priceValue = ((Number) priceObj).longValue();
                if (priceValue <= 10000) {
                    priceValue = priceValue * 1000;
                }
            } else if (priceObj instanceof String) {
                try {
                    String s = ((String) priceObj).replace(",", "").replace(".", "").replace("VND", "").replace("VNĐ", "").replace("₫", "").trim();
                    long parsed = Long.parseLong(s);
                    if (parsed <= 10000) {
                        priceValue = parsed * 1000;
                    } else {
                        priceValue = parsed;
                    }
                } catch (Exception e) {
                    priceValue = 0;
                }
            }
        }
        return String.format("%,d VNĐ", priceValue).replace(",", ".");
    }
}
