package com.rebound.checkout;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rebound.R;
import com.rebound.adapters.CartAdapter;
import com.rebound.connectors.FirebaseOrderConnector;
import com.rebound.main.NavBarActivity;
import com.rebound.models.Cart.ProductItem;
import com.rebound.models.Cart.ShippingAddress;
import com.rebound.models.Customer.Customer;
import com.rebound.models.Main.NotificationItem;
import com.rebound.models.Orders.Order;
import com.rebound.models.Orders.OrderItem;
import com.rebound.utils.CartManager;
import com.rebound.utils.NotificationStorage;
import com.rebound.utils.OrderManager;
import com.rebound.utils.SharedPrefManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CheckOutActivity extends AppCompatActivity {

    TextView txtTotal, txtName, txtAddress, txtPhone, txtCardInfo, txtSimplePaymentMethod;
    LinearLayout layoutCardInfo, layoutSimplePaymentMethod;
    ImageView imgBack;
    int totalAmountFromIntent = 0;

    boolean fromBankTransfer = false;
    String transactionId = "";
    String time = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_out);

        RecyclerView recyclerView = findViewById(R.id.recyclerViewCheckout);
        txtTotal = findViewById(R.id.txtCheckoutTotal);
        txtName = findViewById(R.id.txtCheckoutUsername);
        txtAddress = findViewById(R.id.txtCheckoutAddress);
        txtPhone = findViewById(R.id.txtCheckoutPhone);
        txtCardInfo = findViewById(R.id.txtCheckoutCard);
        txtSimplePaymentMethod = findViewById(R.id.txtSimplePaymentMethod);
        layoutCardInfo = findViewById(R.id.layoutCardInfo);
        layoutSimplePaymentMethod = findViewById(R.id.layoutSimplePaymentMethod);
        imgBack = findViewById(R.id.imgBack);
        imgBack.setOnClickListener(v -> finish());

        MaterialButton btnCheckout = findViewById(R.id.btnCheckout);

        totalAmountFromIntent = getIntent().getIntExtra("totalAmount", 0);
        txtTotal.setText(String.format("%,d VND", totalAmountFromIntent).replace(',', '.'));

        fromBankTransfer = getIntent().getBooleanExtra("fromBankTransfer", false);
        if (fromBankTransfer) {
            transactionId = getIntent().getStringExtra("transactionId");
            time = getIntent().getStringExtra("time");
        }

        String paymentMethod = getIntent().getStringExtra("paymentMethod");
        String cardType = getIntent().getStringExtra("cardType");

        layoutCardInfo.setVisibility(View.GONE);
        layoutSimplePaymentMethod.setVisibility(View.GONE);

        Customer currentCustomer = SharedPrefManager.getCurrentCustomer(this);
        if (currentCustomer != null) {
            String email = currentCustomer.getEmail();

            if ("Credit Card".equalsIgnoreCase(cardType) || "Debit Card".equalsIgnoreCase(cardType)) {
                layoutCardInfo.setVisibility(View.VISIBLE);
                layoutSimplePaymentMethod.setVisibility(View.GONE);

                String name = "";
                String number = "";

                if ("Credit Card".equalsIgnoreCase(cardType)) {
                    name = SharedPrefManager.getCreditCardName(this, email);
                    number = SharedPrefManager.getCreditCardNumber(this, email);
                } else {
                    name = SharedPrefManager.getDebitCardName(this, email);
                    number = SharedPrefManager.getDebitCardNumber(this, email);
                }

                if (number != null && number.length() >= 2) {
                    String lastDigits = number.substring(number.length() - 2);
                    txtCardInfo.setText(getString(R.string.checkout_card_format, cardType, name, lastDigits));
                } else {
                    txtCardInfo.setText(getString(R.string.checkout_no_card_info));
                }
            } else {
                layoutCardInfo.setVisibility(View.GONE);
                layoutSimplePaymentMethod.setVisibility(View.VISIBLE);
                txtSimplePaymentMethod.setText(paymentMethod == null || paymentMethod.isEmpty()
                        ? getString(R.string.checkout_method_not_available)
                        : paymentMethod);
            }

            ShippingAddress address = SharedPrefManager.getShippingAddress(this, email);
            if (address != null) {
                txtName.setText(address.getName() != null ? address.getName() : currentCustomer.getFullName());
                txtAddress.setText(address.getAddress() != null ? address.getAddress() : getString(R.string.checkout_shipping_not_available));
                txtPhone.setText(address.getPhone() != null ? address.getPhone() : getString(R.string.checkout_no_phone));
            } else {
                txtName.setText(currentCustomer.getFullName());
                txtAddress.setText(getString(R.string.checkout_shipping_not_available));
                txtPhone.setText(getString(R.string.checkout_no_phone));
            }
        } else {
            Toast.makeText(this, getString(R.string.checkout_user_not_found), Toast.LENGTH_SHORT).show();
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new CartAdapter(
                CartManager.getInstance().getCartItems(),
                null,
                true
        ));

        btnCheckout.setOnClickListener(v -> {
            Log.d("CheckOutActivity", "btnCheckout clicked"); // Debug log at start of click handler
            Customer current = SharedPrefManager.getCurrentCustomer(this);
            if (current != null) {
                List<ProductItem> cartItems = CartManager.getInstance().getCartItems();
                List<ProductItem> orderProducts = new ArrayList<>();
                for (ProductItem p : cartItems) {
                    // Directly add ProductItem to orderProducts
                    orderProducts.add(p);
                }

                // Get payment method string from intent
                final String paymentMethodStr = getIntent().getStringExtra("paymentMethod");
                final Long[] paymentMethodId = {null};
                if (paymentMethodStr != null) {
                    for (com.rebound.models.Cart.PaymentMethod pm : com.rebound.models.Cart.PaymentMethod.getDefaultCategories()) {
                        if (pm.getPaymentMethodName().equalsIgnoreCase(paymentMethodStr)) {
                            paymentMethodId[0] = pm.getPaymentMethodID();
                            break;
                        }
                    }
                }
                // Fetch latest OrderID from Firebase
                DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference("Order");
                ordersRef.orderByChild("OrderID").limitToLast(1).addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Log.d("CheckOutActivity", "onDataChange for OrderID called"); // Debug log at start of onDataChange
                        long latestOrderId = 0L;
                        // Find the max node key (which is a number as string)
                        long maxNodeKey = 0L;
                        // Find the max OrderID value (not node key)
                        long maxOrderId = 0L;
                        for (DataSnapshot orderSnap : snapshot.getChildren()) {
                            String key = orderSnap.getKey();
                            if (key != null) {
                                try {
                                    long keyNum = Long.parseLong(key);
                                    if (keyNum > maxNodeKey) maxNodeKey = keyNum;
                                } catch (Exception ignore) {}
                            }
                            Object idObj = orderSnap.child("OrderID").getValue();
                            if (idObj instanceof Long) {
                                if ((Long) idObj > maxOrderId) maxOrderId = (Long) idObj;
                            } else if (idObj instanceof Number) {
                                if (((Number) idObj).longValue() > maxOrderId) maxOrderId = ((Number) idObj).longValue();
                            } else if (idObj != null) {
                                try {
                                    long parsed = Long.parseLong(idObj.toString());
                                    if (parsed > maxOrderId) maxOrderId = parsed;
                                } catch (Exception ignore) {}
                            }
                        }
                        // Calculate next node key and next OrderID
                        long nextNodeKey = maxNodeKey + 1;
                        long nextOrderId = maxOrderId + 1;
                        // Use nextNodeKey as the new node's key, and nextOrderId as the new OrderID
                        // Example usage (replace with your actual order creation logic):
                        // DatabaseReference newOrderRef = ordersRef.child(String.valueOf(nextNodeKey));
                        // newOrderRef.child("OrderID").setValue(nextOrderId);

                        // --- Calculate Subtotal, DeliveryFee, Discount, Promotion ---
                        long deliveryFee = getIntent().hasExtra("deliveryFee") ? getIntent().getLongExtra("deliveryFee", 0L) : 0L;
                        double discountPercent = getIntent().hasExtra("discountPercent") ? getIntent().getDoubleExtra("discountPercent", 0.0) : 0.0;
                        double userPromotionPercent = getIntent().hasExtra("userPromotionPercent") ? getIntent().getDoubleExtra("userPromotionPercent", 0.0) : 0.0;
                        long subtotal = getIntent().hasExtra("subtotal") ? getIntent().getLongExtra("subtotal", totalAmountFromIntent) : (long) totalAmountFromIntent;
                        // Calculate discount and promotion only if not zero
                        double discountAmount = (discountPercent > 0) ? (subtotal * discountPercent / 100.0) : 0.0;
                        double promotionAmount = (userPromotionPercent > 0) ? (subtotal * userPromotionPercent / 100.0) : 0.0;
                        long totalAmount = subtotal + deliveryFee - (long)discountAmount - (long)promotionAmount;
                        if (totalAmount < 0) totalAmount = 0;

                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
                        String today = sdf.format(new Date());

                        Order newOrder = new Order();
                        newOrder.setOrderID(nextOrderId);
                        newOrder.setUserID(null);
                        // Retrieve userIdLong from SharedPreferences and make it final
                        Log.d("CheckOutActivity", "Reached before user_id log");
                        final android.content.SharedPreferences userPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                        final Long userIdLong = userPrefs.getLong("user_id", -1);
                        Log.d("CheckOutActivity", "user_id from SharedPreferences: " + userIdLong); // Debug log
                        newOrder.setUserID(userIdLong);
                        newOrder.setPaymentMethodID(paymentMethodId[0]);
                        newOrder.setDeliveryFee(deliveryFee);
                        newOrder.setDiscountValue(discountPercent);
                        newOrder.setSubtotal(subtotal);
                        newOrder.setTotalAmount(totalAmount);
                        newOrder.setUserPromotion((long)userPromotionPercent);
                        newOrder.setOrderDate(today);
                        newOrder.setStatus("Pending");
                        // Add order to Firebase with numeric key (as String)
                        FirebaseOrderConnector.addOrder(newOrder, String.valueOf(nextNodeKey), () -> {
                            Toast.makeText(CheckOutActivity.this, "Order placed successfully!", Toast.LENGTH_SHORT).show();
                            // After order is placed, add OrderItems for each product
                            DatabaseReference orderItemRef = FirebaseDatabase.getInstance().getReference("OrderItem");
                            orderItemRef.orderByChild("OrderItemID").limitToLast(1).addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    long maxOrderItemId = 0L;
                                    for (DataSnapshot itemSnap : snapshot.getChildren()) {
                                        Object idObj = itemSnap.child("OrderItemID").getValue();
                                        if (idObj instanceof Long) {
                                            if ((Long) idObj > maxOrderItemId) maxOrderItemId = (Long) idObj;
                                        } else if (idObj instanceof Number) {
                                            if (((Number) idObj).longValue() > maxOrderItemId) maxOrderItemId = ((Number) idObj).longValue();
                                        } else if (idObj != null) {
                                            try {
                                                long parsed = Long.parseLong(idObj.toString());
                                                if (parsed > maxOrderItemId) maxOrderItemId = parsed;
                                            } catch (Exception ignore) {}
                                        }
                                    }
                                    long nextOrderItemId = maxOrderItemId + 1;
                                    Log.d("CheckOutActivity", "cartItems size: " + cartItems.size());
                                    for (ProductItem p : cartItems) {
                                        Log.d("CheckOutActivity", "cartItem detail: ID=" + p.getProductID() + ", Name=" + p.getProductName() + ", StockQuantity=" + p.getProductStockQuantity() + ", Price=" + p.getProductPrice());
                                    }
                                    Log.d("CheckOutActivity", "orderProducts size: " + cartItems.size()); // Use cartItems directly
                                    if (cartItems.isEmpty()) {
                                        Log.d("CheckOutActivity", "cartItems is empty at order item creation");
                                    }
                                    for (ProductItem p : cartItems) {
                                        Log.d("CheckOutActivity", "Looping cartItems, ProductID: " + p.getProductID()); // Debug log
                                        Long stockQuantityLong = p.getProductStockQuantity();
                                        int quantity = stockQuantityLong != null ? stockQuantityLong.intValue() : 0; // Safely convert Long to int
                                        Log.d("CheckOutActivity", "ProductID: " + p.getProductID() + ", Quantity from ProductStockQuantity: " + quantity);
                                        if (quantity <= 0) continue; // Skip items with zero quantity
                                        long productId = p.getProductID() != null ? p.getProductID() : 0L;
                                        long price = 0L;
                                        try {
                                            if (p.getProductPrice() != null) {
                                                String priceStr = p.getProductPrice().toString().replace(".", "").replace(" VND", "").trim();
                                                price = Long.parseLong(priceStr);
                                            }
                                        } catch (Exception e) {
                                            price = 0L;
                                        }
                                        Log.d("CheckOutActivity", "Adding OrderItem: productId=" + productId + ", quantity=" + quantity + ", price=" + price);
                                        OrderItem orderItem = new OrderItem(
                                            Long.valueOf(nextOrderItemId++), // OrderItemID as Long
                                            newOrder.getOrderID(), // OrderID as Long
                                            productId, // ProductID as Long
                                            Long.valueOf(quantity), // Quantity as Long (from ProductStockQuantity)
                                            price, // Price as Object (Long)
                                            userIdLong // UserID as Long
                                        );
                                        FirebaseOrderConnector.addOrderItem(orderItem,
                                            () -> Log.d("CheckOutActivity", "OrderItem added successfully: " + orderItem),
                                            () -> Log.e("CheckOutActivity", "Failed to add OrderItem: " + orderItem + ", details: " + getOrderItemDebugString(orderItem))
                                        );
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {
                                    // Optionally handle error
                                }
                            });
                        }, () -> {
                            Toast.makeText(CheckOutActivity.this, "Failed to place order!", Toast.LENGTH_SHORT).show();
                        });

                        // Continue with notification and UI logic
                        String formattedTotal = String.format(java.util.Locale.US, "%,d VND", totalAmountFromIntent).replace(',', '.');
                        String title = getString(R.string.checkout_order_success_title);
                        String message = fromBankTransfer
                                ? getString(R.string.checkout_order_success_bank, transactionId, formattedTotal)
                                : getString(R.string.checkout_order_success_normal);
                        NotificationItem item = new NotificationItem(
                                NotificationItem.TYPE_NOTIFICATION,
                                title,
                                message,
                                "Just now",
                                System.currentTimeMillis()
                        );
                        NotificationStorage.saveNotification(CheckOutActivity.this, current.getEmail(), item);
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                                ContextCompat.checkSelfPermission(CheckOutActivity.this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                            NotificationCompat.Builder builder = new NotificationCompat.Builder(CheckOutActivity.this, "order_channel")
                                    .setSmallIcon(R.mipmap.ic_order)
                                    .setContentTitle(title)
                                    .setContentText(message)
                                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                    .setAutoCancel(true);
                            NotificationManagerCompat.from(CheckOutActivity.this).notify(1001, builder.build());
                        }
                        CartManager.getInstance().clearCart();
                        if (fromBankTransfer) {
                            Intent intent = new Intent(CheckOutActivity.this, PaymentSuccessActivity.class);
                            intent.putExtra("transactionId", transactionId);
                            intent.putExtra("time", time);
                            intent.putExtra("amount", totalAmountFromIntent);
                            startActivity(intent);
                            finish();
                        } else {
                            showPaymentSuccessPopup();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {
                        Toast.makeText(CheckOutActivity.this, "Failed to get latest OrderID!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "order_channel",
                    getString(R.string.checkout_notification_channel_title),
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription(getString(R.string.checkout_notification_channel_description));
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private void showPaymentSuccessPopup() {
        android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.setContentView(R.layout.payment_success_popup);
        dialog.setCancelable(true);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        TextView txtOrderId = dialog.findViewById(R.id.txtPaymentSuccessfulOrderId);
        txtOrderId.setText(getString(R.string.checkout_order_id, generateOrderId()));
        dialog.findViewById(R.id.imgClose).setOnClickListener(v -> {
            dialog.dismiss();
            goToMain();
        });
        MaterialButton btnBackShop = dialog.findViewById(R.id.btnPaymentSuccessfulBackShop);
        btnBackShop.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
        btnBackShop.setTextColor(Color.BLACK);
        btnBackShop.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#BEB488")));
        btnBackShop.setStrokeWidth(1);
        btnBackShop.setOnClickListener(v2 -> {
            btnBackShop.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#BEB488")));
            btnBackShop.setTextColor(Color.WHITE);
            btnBackShop.setStrokeWidth(0);
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                dialog.dismiss();
                goToMain();
            }, 300);
        });
        sendOrderNotification();
        dialog.show();
    }

    private String generateOrderId() {
        long timestamp = System.currentTimeMillis();
        int random = (int) (Math.random() * 1000);
        return "ORD-" + String.valueOf(timestamp).substring(6) + random;
    }

    private void goToMain() {
        Intent intent = new Intent(this, NavBarActivity.class);
        intent.putExtra("targetFragment", "main");
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private void sendOrderNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
            return;
        }
        Customer currentCustomer = SharedPrefManager.getCurrentCustomer(this);
        if (currentCustomer == null) return;
        String formattedTotal = String.format(java.util.Locale.US, "%,d VND", totalAmountFromIntent).replace(',', '.');
        String method = fromBankTransfer ? "Bank Transfer" : txtSimplePaymentMethod.getText().toString();
        String timeStr = fromBankTransfer ? time : new java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()).format(new java.util.Date());
        String title = getString(R.string.checkout_order_confirmed_title);
        String body = getString(R.string.checkout_order_confirmed_text, formattedTotal, method, timeStr);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "order_channel")
                .setSmallIcon(R.mipmap.ic_order)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);
        NotificationManagerCompat.from(this).notify(1001, builder.build());
        NotificationItem item = new NotificationItem(
                NotificationItem.TYPE_NOTIFICATION,
                title,
                body,
                "Just now",
                System.currentTimeMillis()
        );
        NotificationStorage.saveNotification(this, currentCustomer.getEmail(), item);
    }

    private String getOrderItemDebugString(OrderItem item) {
        return "OrderItemID=" + item.getOrderItemID() +
                ", OrderID=" + item.getOrderID() +
                ", ProductID=" + item.getProductID() +
                ", Quantity=" + item.getQuantity() +
                ", Price=" + item.getPrice() +
                ", UserID=" + item.getUserID() +
                ", Rating=" + item.getRating();
    }
}
