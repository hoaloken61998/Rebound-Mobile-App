package com.rebound.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.rebound.callback.FirebaseListCallback;
import com.rebound.models.Customer.Customer;
import com.rebound.models.Customer.ListCustomer;
import com.rebound.models.Cart.ShippingAddress;
import com.rebound.models.Reservation.Reservation;
import com.rebound.models.Main.NotificationItem;
import com.rebound.utils.NotificationStorage;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.rebound.models.Main.NotificationItem;



public class SharedPrefManager {

    private static final String PREF_NAME = "customer_data";
    private static final String KEY_CUSTOMER_LIST = "list_customer";

    public static void saveCustomerList(Context context, ListCustomer listCustomer) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(listCustomer);
        editor.putString(KEY_CUSTOMER_LIST, json);
        editor.apply();
    }

    public static ListCustomer getCustomerList(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_CUSTOMER_LIST, null);
        if (json != null) {
            Gson gson = new Gson();
            return gson.fromJson(json, ListCustomer.class);
        }
        return null;
    }

    // 🔹 Lấy Customer theo username
    public static Customer getCustomerByUsername(Context context, String username) {
        ListCustomer listCustomer = getCustomerList(context);
        if (listCustomer != null) {
            for (Customer c : listCustomer.getCustomers()) {
                if (c.getUsername() != null && c.getUsername().equalsIgnoreCase(username)) {
                    return c;
                }
            }
        }
        return null;
    }

    // 🔹 Cập nhật Customer theo username
    public static void updateCustomer(Context context, Customer updatedCustomer) {
        ListCustomer listCustomer = getCustomerList(context);
        if (listCustomer != null) {
            for (int i = 0; i < listCustomer.getCustomers().size(); i++) {
                Customer c = listCustomer.getCustomers().get(i);
                if (c.getUsername() != null && c.getUsername().equals(updatedCustomer.getUsername())) {
                    listCustomer.getCustomers().set(i, updatedCustomer);
                    saveCustomerList(context, listCustomer);
                    return;
                }
            }
        }
    }

    public static boolean isUsernameTaken(Context context, String username) {
        ListCustomer listCustomer = getCustomerList(context);
        if (listCustomer != null) {
            for (Customer c : listCustomer.getCustomers()) {
                if (c.getUsername() != null && c.getUsername().equalsIgnoreCase(username)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void addCustomer(Context context, Customer customer) {
        ListCustomer listCustomer = getCustomerList(context);
        if (listCustomer == null) {
            listCustomer = new ListCustomer();
        }

        // Kiểm tra nếu đã có thì cập nhật
        for (int i = 0; i < listCustomer.getCustomers().size(); i++) {
            String existingUsername = listCustomer.getCustomers().get(i).getUsername();
            String newUsername = customer.getUsername();
            if (existingUsername != null && existingUsername.equals(newUsername)) {
                listCustomer.getCustomers().set(i, customer);
                saveCustomerList(context, listCustomer);
                return;
            }
        }

        // Nếu chưa có thì thêm mới
        listCustomer.getCustomers().add(customer);
        saveCustomerList(context, listCustomer);
    }

    public static Customer getCustomerByEmail(Context context, String email) {
        ListCustomer listCustomer = getCustomerList(context);
        if (listCustomer != null) {
            for (Customer c : listCustomer.getCustomers()) {
                if (c.getEmail() != null && c.getEmail().equalsIgnoreCase(email)) {
                    return c;
                }
            }
        }
        return null;
    }

    public static boolean isPhoneTaken(Context context, String phone, String exceptEmail) {
        ListCustomer listCustomer = getCustomerList(context);
        if (listCustomer != null) {
            for (Customer c : listCustomer.getCustomers()) {
                // PhoneNumber is now long, so convert to String for comparison
                if (String.valueOf(c.getPhoneNumber()).equals(phone)) {
                    // Nếu là user khác (không cùng email), thì coi như trùng
                    if (c.getEmail() != null && !c.getEmail().equalsIgnoreCase(exceptEmail)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean isEmailTaken(Context context, String email) {
        ListCustomer listCustomer = getCustomerList(context);
        if (listCustomer != null) {
            for (Customer c : listCustomer.getCustomers()) {
                if (c.getEmail() != null && c.getEmail().equalsIgnoreCase(email)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void setCurrentCustomer(Context context, Customer customer) {
        SharedPreferences prefs = context.getSharedPreferences("user_session", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("logged_in_email", customer.getEmail());
        editor.apply();
    }

    public static Customer getCurrentCustomer(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("user_session", Context.MODE_PRIVATE);
        String email = prefs.getString("logged_in_email", null);

        if (email != null) {
            return getCustomerByEmail(context, email);
        }
        return null;
    }

    public static void logoutCurrentCustomer(Context context) {
        // Clear the logged-in email from user_session
        SharedPreferences prefs = context.getSharedPreferences("user_session", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove("logged_in_email");
        editor.apply();
    }

    private static final String KEY_SHIPPING_ADDRESS = "shipping_address_";

    // 🔹 Lưu shipping address theo email user
    public static void saveShippingAddress(Context context, String userEmail, ShippingAddress address) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        editor.putString(KEY_SHIPPING_ADDRESS + userEmail, gson.toJson(address));
        editor.apply();
    }

    // Lấy shipping address theo email user
    public static ShippingAddress getShippingAddress(Context context, String userEmail) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_SHIPPING_ADDRESS + userEmail, null);
        if (json != null) {
            return new Gson().fromJson(json, ShippingAddress.class);
        }
        return null;
    }

//    private static final String NAME_ON_CARD_KEY_PREFIX = "name_on_card_";
//
//    public static void setNameOnCard(Context context, String email, String name) {
//        SharedPreferences.Editor editor = getPreferences(context).edit();
//        editor.putString(NAME_ON_CARD_KEY_PREFIX + email, name);
//        editor.apply();
//    }
//
//    public static String getNameOnCard(Context context, String email) {
//        return getPreferences(context).getString(NAME_ON_CARD_KEY_PREFIX + email, "");
//    }
//    private static SharedPreferences getPreferences(Context context) {
//        return context.getSharedPreferences("checkout_prefs", Context.MODE_PRIVATE);
//    }

    private static final String CARD_PREFS = "CARD_PREFS";

    //
//    // Lưu tên in trên thẻ
//    public static void setNameOnCard(Context context, String email, String nameOnCard) {
//        SharedPreferences prefs = context.getSharedPreferences(CARD_PREFS, Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = prefs.edit();
//        editor.putString(email + "_nameOnCard", nameOnCard);
//        editor.apply();
//    }
//
//    public static String getNameOnCard(Context context, String email) {
//        SharedPreferences prefs = context.getSharedPreferences(CARD_PREFS, Context.MODE_PRIVATE);
//        return prefs.getString(email + "_nameOnCard", "");
//    }
//
//    // Có thể mở rộng lưu thêm số thẻ nếu muốn
//    public static void setCardNumber(Context context, String email, String cardNumber) {
//        SharedPreferences prefs = context.getSharedPreferences(CARD_PREFS, Context.MODE_PRIVATE);
//        prefs.edit().putString(email + "_cardNumber", cardNumber).apply();
//    }
//
//    public static String getCardNumber(Context context, String email) {
//        SharedPreferences prefs = context.getSharedPreferences(CARD_PREFS, Context.MODE_PRIVATE);
//        return prefs.getString(email + "_cardNumber", "");
//    }
// CREDIT CARD
// Credit Card
    public static void setCreditCard(Context context, String email, String name, String number) {
        SharedPreferences prefs = context.getSharedPreferences(CARD_PREFS, Context.MODE_PRIVATE);
        prefs.edit()
                .putString(email + "_credit_name", name)
                .putString(email + "_credit_number", number)
                .apply();
    }

    public static String getCreditCardName(Context context, String email) {
        SharedPreferences prefs = context.getSharedPreferences(CARD_PREFS, Context.MODE_PRIVATE);
        return prefs.getString(email + "_credit_name", "");
    }

    public static String getCreditCardNumber(Context context, String email) {
        SharedPreferences prefs = context.getSharedPreferences(CARD_PREFS, Context.MODE_PRIVATE);
        return prefs.getString(email + "_credit_number", "");
    }

    // Debit Card
    public static void setDebitCard(Context context, String email, String name, String number) {
        SharedPreferences prefs = context.getSharedPreferences(CARD_PREFS, Context.MODE_PRIVATE);
        prefs.edit()
                .putString(email + "_debit_name", name)
                .putString(email + "_debit_number", number)
                .apply();
    }

    public static String getDebitCardName(Context context, String email) {
        SharedPreferences prefs = context.getSharedPreferences(CARD_PREFS, Context.MODE_PRIVATE);
        return prefs.getString(email + "_debit_name", "");
    }

    public static String getDebitCardNumber(Context context, String email) {
        SharedPreferences prefs = context.getSharedPreferences(CARD_PREFS, Context.MODE_PRIVATE);
        return prefs.getString(email + "_debit_number", "");
    }

    // Check existence
    public static boolean hasCreditCard(Context context, String email) {
        return !getCreditCardNumber(context, email).isEmpty();
    }

    public static boolean hasDebitCard(Context context, String email) {
        return !getDebitCardNumber(context, email).isEmpty();
    }

    // Reservation
    private static final String KEY_RESERVATION = "reservation_";

    public static void saveReservation(Context context, Reservation reservation) {
        SharedPreferences prefs = context.getSharedPreferences("ReservationPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        String json = new Gson().toJson(reservation);
        editor.putString(reservation.getEmail(), json);
        editor.apply();
    }

    // Lấy đặt lịch theo email
    public static Reservation getReservation(Context context, String email) {
        SharedPreferences prefs = context.getSharedPreferences("ReservationPrefs", Context.MODE_PRIVATE);
        String json = prefs.getString(email, null);

        if (json != null) {
            return new Gson().fromJson(json, Reservation.class);
        }
        return null;
    }

    // Callback interface for async checks
    public interface TakenCallback {
        void onResult(boolean isTaken);

        void onError(String error);
    }

    // Check username from Firebase (modular, logic in callback)
    public static void getAllCustomersFromFirebase(FirebaseListCallback<Customer> callback) {
        com.rebound.connectors.FirebaseConnector.getAllItems("User", Customer.class, callback);
    }
}