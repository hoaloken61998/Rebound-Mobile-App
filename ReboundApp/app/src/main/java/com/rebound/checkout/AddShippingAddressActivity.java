package com.rebound.checkout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.rebound.R;
import com.rebound.callback.AddressCallback;
import com.rebound.connectors.AddressConnector;
import com.rebound.models.Customer.Customer;
import com.rebound.models.Cart.Address;
import com.rebound.models.Cart.ShippingAddress;
import com.rebound.utils.SharedPrefManager;

public class AddShippingAddressActivity extends AppCompatActivity {

    ImageView imgBack;
    MaterialButton btnAddShippingAddress, btnSelectLoadedAddress;
    EditText edtName, edtAddress, edtCity, edtDistrict, edtWard, edtPhone;
    Address loadedAddress;
    private Long currentUserId; // Store user ID for later use

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_shipping_address);

        // Ánh xạ view
        imgBack = findViewById(R.id.imgBackAddShippingAddress);
        btnAddShippingAddress = findViewById(R.id.btnAddShippingAddress);
        btnSelectLoadedAddress = findViewById(R.id.btnSelectLoadedAddress);

        edtName = findViewById(R.id.edtAddShippingAddressName);
        edtAddress = findViewById(R.id.edtAddShippingAddress);
        edtCity = findViewById(R.id.edtAddShippingCity);
        edtDistrict = findViewById(R.id.edtAddShippingDistrict);
        edtWard = findViewById(R.id.edtAddShippingWard);
        edtPhone = findViewById(R.id.edtAddShippingPhone);

        // Load address from AddressConnector for logged-in user
        Customer currentCustomer = SharedPrefManager.getCurrentCustomer(this);
        if (currentCustomer != null) {
            Object userIdObj = currentCustomer.getUserID();
            if (userIdObj instanceof Long) {
                currentUserId = (Long) userIdObj;
            } else if (userIdObj instanceof String) {
                String userIdStr = (String) userIdObj;
                if (userIdStr == null || userIdStr.trim().isEmpty()) {
                    Toast.makeText(this, "UserID is empty or null", Toast.LENGTH_LONG).show();
                    return;
                }
                try {
                    currentUserId = Long.parseLong(userIdStr);
                } catch (NumberFormatException e1) {
                    try {
                        Double d = Double.parseDouble(userIdStr);
                        currentUserId = d.longValue();
                    } catch (Exception e2) {
                        Log.e("AddShippingAddress", "Failed to parse userId: '" + userIdStr + "'", e2);
                        Toast.makeText(this, "UserID is not a valid integer: '" + userIdStr + "'", Toast.LENGTH_LONG).show();
                        return;
                    }
                }
            }
            if (currentUserId != null) {
                AddressConnector addressConnector = new AddressConnector();
                addressConnector.getDefaultAddressForUser(this, currentUserId, new AddressCallback() {
                    @Override
                    public void onAddressLoaded(Address address) {
                        if (address != null) {
                            loadedAddress = address;
                            edtName.setText(address.getReceiverName() != null ? address.getReceiverName() : "");
                            edtAddress.setText(address.getStreet() != null ? address.getStreet() : "");
                            edtCity.setText(address.getProvince() != null ? address.getProvince() : "");
                            edtDistrict.setText(address.getDistrict() != null ? address.getDistrict().toString() : "");
                            edtWard.setText(address.getWard() != null ? address.getWard().toString() : "");
                            edtPhone.setText(address.getReceiverPhone() != null ? formatPhone(address.getReceiverPhone().toString()) : "");
                        } else {
                            Toast.makeText(AddShippingAddressActivity.this, "No default address found. You can add a new one.", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onError(String error) {
//                        Toast.makeText(AddShippingAddressActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
                    }
                });
            }
        }

        imgBack.setOnClickListener(v -> finish());

        btnAddShippingAddress.setOnClickListener(v -> {
            String name = edtName.getText().toString().trim();
            String address = edtAddress.getText().toString().trim();
            String city = edtCity.getText().toString().trim();
            String districtStr = edtDistrict.getText().toString().trim();
            String wardStr = edtWard.getText().toString().trim();
            String phone = edtPhone.getText().toString().trim();

            if (name.isEmpty() || address.isEmpty() || city.isEmpty() || districtStr.isEmpty() || wardStr.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, getString(R.string.checkout_add_address_fill_all_fields), Toast.LENGTH_SHORT).show();
                return;
            }

            if (currentUserId == null) {
                Toast.makeText(this, getString(R.string.checkout_add_address_user_not_logged_in), Toast.LENGTH_SHORT).show();
                return;
            }

            // If no address was loaded, create a new one. Otherwise, update the existing one.
            Address addressToSave;
            if (loadedAddress != null) {
                addressToSave = loadedAddress;
            } else {
                addressToSave = new Address();
            }

            // Populate the address object with data from EditTexts
            addressToSave.setReceiverName(name);
            addressToSave.setStreet(address);
            addressToSave.setProvince(city);
            addressToSave.setDistrict(districtStr); // If you have a District class, parse here
            addressToSave.setWard(wardStr); // If you have a Ward class, parse here
            addressToSave.setReceiverPhone(Long.parseLong(phone));

            AddressConnector addressConnector = new AddressConnector();
            addressConnector.updateAddress(addressToSave, new AddressCallback() {
                @Override
                public void onAddressLoaded(Address address) {
                    // Return the added address to the calling activity
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("added_address", address);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(AddShippingAddressActivity.this, "Failed to save address: " + error, Toast.LENGTH_LONG).show();
                }
            });
        });

        btnSelectLoadedAddress.setOnClickListener(v -> {
            if (loadedAddress == null) {
                Toast.makeText(this, "No address loaded to select.", Toast.LENGTH_SHORT).show();
                return;
            }
            loadedAddress.setIsDefault("yes");
            Customer currentCustomer1 = SharedPrefManager.getCurrentCustomer(this);
            if (currentCustomer1 != null) {
                SharedPrefManager.saveShippingAddress(this, currentCustomer1.getEmail(),
                        new ShippingAddress(
                                loadedAddress.getReceiverName(),
                                loadedAddress.getStreet(),
                                String.valueOf(loadedAddress.getReceiverPhone())
                        )
                );
            }
            Toast.makeText(this, "Address selected!", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private String formatPhone(String phone) {
        if (phone == null) return "";
        phone = phone.replaceAll("[^0-9]", "");
        if (phone.startsWith("0")) return phone;
        return "0" + phone;
    }
}