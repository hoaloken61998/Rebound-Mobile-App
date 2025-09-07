package com.rebound.login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.rebound.R;
import com.rebound.connectors.CustomerConnector;
import com.rebound.main.OnBoardingActivity;
import com.rebound.models.Customer.Customer;
import com.rebound.utils.SharedPrefManager;

public class CompleteProfileActivity extends AppCompatActivity {

    MaterialButton btnCompleteProfile;
    ImageView imgBack;
    EditText edtName, edtPhone;
    Spinner spGender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete_profile);

        // Ánh xạ view
        btnCompleteProfile = findViewById(R.id.btnCompleteProfile);
        imgBack = findViewById(R.id.imgBack);
        edtName = findViewById(R.id.edtCompleteProfileName);
        edtPhone = findViewById(R.id.edtCompleteProfilePhone);
        spGender = findViewById(R.id.spGenderCompleteProfile);

        // Nút hoàn tất
        btnCompleteProfile.setOnClickListener(view -> {
            String name = edtName.getText().toString().trim();
            String phone = edtPhone.getText().toString().trim();
            String gender = spGender.getSelectedItem().toString();

            if (name.isEmpty() || phone.isEmpty() || gender.equals(getString(R.string.select_gender))) {
                Toast.makeText(this, getString(R.string.complete_profile_fill_all), Toast.LENGTH_SHORT).show();
                return;
            }

            // Get registration info from intent
            Intent intent = getIntent();
            String username = intent.getStringExtra("username");
            String email = intent.getStringExtra("email");
            String password = intent.getStringExtra("password");

            // Check phone uniqueness (optional, can be improved with Firebase query)
            // Kiểm tra số điện thoại đã tồn tại
            if (SharedPrefManager.isPhoneTaken(this, phone, email)) {
                Toast.makeText(this, getString(R.string.complete_profile_phone_exists), Toast.LENGTH_SHORT).show();
                return;
            }

            // Fetch all users to get the highest UserID
            com.rebound.utils.SharedPrefManager.getAllCustomersFromFirebase(new com.rebound.callback.FirebaseListCallback<com.rebound.models.Customer.Customer>() {
                @Override
                public void onSuccess(java.util.ArrayList<com.rebound.models.Customer.Customer> customers) {
                    long highestUserId = 0;
                    if (customers != null) {
                        for (com.rebound.models.Customer.Customer c : customers) {
                            try {
                                long userId = 0;
                                String userIdStr = c.getUserID();
                                if (userIdStr != null && !userIdStr.isEmpty()) {
                                    userId = Long.parseLong(userIdStr);
                                }
                                if (userId > highestUserId) {
                                    highestUserId = userId;
                                }
                            } catch (Exception ex) {
                                // Ignore parse errors, skip this customer
                            }
                        }
                    }
                    // Fix: User index (key) should match UserID
                    long newUserId = highestUserId + 1;
                    long userKey = highestUserId; // Use the same value for key and UserID
                    // NOTE: Only use PascalCase setters to ensure Firebase fields are PascalCase
                    com.rebound.models.Customer.Customer newCustomer = new com.rebound.models.Customer.Customer();
                    newCustomer.setAvatarURL("");
                    newCustomer.setDateOfBirth("");
                    newCustomer.setEmail(email);
                    newCustomer.setFullName(name);
                    try {
                        newCustomer.setPassword(Long.parseLong(password));
                    } catch (NumberFormatException e) {
                        newCustomer.setPassword(0L);
                    }
                    try {
                        newCustomer.setPhoneNumber(Long.parseLong(phone));
                    } catch (NumberFormatException e) {
                        newCustomer.setPhoneNumber(0L);
                    }
                    newCustomer.setRegistrationDate("");
                    newCustomer.setSex(gender);
                    newCustomer.setUserID(newUserId);
                    newCustomer.setUserRanking("");
                    newCustomer.setUsername(username);

                    // Add to Firebase with numeric key matching UserID8
                    CustomerConnector connector = new CustomerConnector();
                    connector.addCustomerToFirebaseWithId(userKey, newCustomer, new com.rebound.callback.FirebaseSingleCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            Toast.makeText(CompleteProfileActivity.this, getString(R.string.account_created_successfully), Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(CompleteProfileActivity.this, com.rebound.main.OnBoardingActivity.class);
                            startActivity(intent);
                            finish();
                        }
                        @Override
                        public void onFailure(String errorMessage) {
                            Toast.makeText(CompleteProfileActivity.this, getString(R.string.error_creating_account) + ": " + errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                @Override
                public void onFailure(String errorMessage) {
                    Toast.makeText(CompleteProfileActivity.this, getString(R.string.error_creating_account) + ": " + errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        });

        // Nút back
        imgBack.setOnClickListener(view -> finish());
    }
}
