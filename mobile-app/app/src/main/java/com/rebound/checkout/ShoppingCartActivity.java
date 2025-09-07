package com.rebound.checkout;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rebound.R;
import com.rebound.adapters.CartAdapter;
import com.rebound.models.Cart.ProductItem;
import com.rebound.utils.CartManager;

import java.util.List;

public class ShoppingCartActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    TextView txtSubtotal, txtDelivery, txtDiscount, txtTotal;
    EditText editPromo;
    Button btnApply, btnCheckout;
    ImageView imgBackShoppingCart;

    private int discountAmount = 0;

    private CartAdapter adapter;
    private List<ProductItem> cartItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_cart);

        addViews();
        addEvents();
        updateSummary();
    }

    private void addViews() {
        recyclerView = findViewById(R.id.recyclerView);
        txtSubtotal = findViewById(R.id.txtShoppingCartSummarySubtotal);
        txtDiscount = findViewById(R.id.txtShoppingCartSummaryDiscount);
        txtTotal = findViewById(R.id.txtShoppingCartSummaryTotal);
        editPromo = findViewById(R.id.edtShoppingCartPromo);
        btnApply = findViewById(R.id.btnShoppingCartApply);
        btnCheckout = findViewById(R.id.btnCheckout);
        txtDelivery = findViewById(R.id.txtShoppingCartSummaryDelivery);
        imgBackShoppingCart = findViewById(R.id.imgBackShoppingCart);

        cartItems = CartManager.getInstance().getCartItems();

        adapter = new CartAdapter(cartItems, this::updateSummary, false, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void addEvents() {
        btnApply.setOnClickListener(v -> {
            String code = editPromo.getText().toString().trim();
            if (code.equalsIgnoreCase("SALE10")) {
                discountAmount = 100000;
                Toast.makeText(this, getString(R.string.discount_applied), Toast.LENGTH_SHORT).show();
            } else {
                discountAmount = 0;
                Toast.makeText(this, getString(R.string.invalid_promo), Toast.LENGTH_SHORT).show();
            }
            updateSummary();
        });

        btnCheckout.setOnClickListener(v -> {
            if (cartItems == null || cartItems.isEmpty()) {
                Toast.makeText(this, getString(R.string.empty_cart_warning), Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(this, CheckOutShippingActivity.class);
            intent.putExtra("totalAmount", getTotal());
            startActivity(intent);
        });

        imgBackShoppingCart.setOnClickListener(v -> finish());
    }

    private void updateSummary() {
        int subtotal = getSubTotal();
        txtSubtotal.setText(format(subtotal));
        txtDelivery.setText(getString(R.string.free_shipping));
        txtDiscount.setText(format(discountAmount));
        txtTotal.setText(format(getTotal()));
    }

    private int extractPrice(Object priceObj) {
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
        return (int) priceValue;
    }

    private int getSubTotal() {
        if (cartItems == null) {
            return 0;
        }
        return cartItems.stream()
                .mapToInt(item -> {
                    if (item == null) {
                        return 0;
                    }
                    try {
                        int unitPrice = extractPrice(item.getProductPrice());
                        int qty = 1;
                        try {
                            Long stockQuantityLong = item.getProductStockQuantity();
                            if (stockQuantityLong != null) {
                                if (stockQuantityLong > Integer.MAX_VALUE) {
                                    qty = Integer.MAX_VALUE;
                                } else if (stockQuantityLong < 1) {
                                    qty = 1;
                                } else {
                                    qty = stockQuantityLong.intValue();
                                }
                            }
                        } catch (Exception e) {
                            qty = 1;
                        }
                        return unitPrice * qty;
                    } catch (Exception e) {
                        return 0;
                    }
                })
                .sum();
    }


    private int getTotal() {
        return getSubTotal() - discountAmount;
    }

    private String format(int amount) {
        return String.format("%,d", amount).replace(',', '.') + " VND";
    }
}
