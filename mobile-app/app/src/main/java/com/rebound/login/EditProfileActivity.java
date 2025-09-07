package com.rebound.login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.button.MaterialButton;
import com.rebound.R;
import com.rebound.connectors.CustomerConnector;
import com.rebound.models.Customer.Customer;
import com.rebound.utils.SharedPrefManager;

public class EditProfileActivity extends AppCompatActivity {
    ImageView imgBackEditProfile;
    MaterialButton btnUpdate;

    EditText edtUsername, edtFullName, edtEmail, edtPhone, edtPassword;
    TextView txtAvatarNote;
    Spinner spGender;

    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView imgAvatar, imgEdit;
    private Uri selectedImageUri;

    private Customer currentCustomer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_profile);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        addViews();
        loadUserData();
        addEvents();
    }

    private void addViews() {
        btnUpdate = findViewById(R.id.btnUpdate);
        imgBackEditProfile = findViewById(R.id.imgBackEditProfile);
        edtUsername = findViewById(R.id.edtEditProfileUsername);
        edtUsername.setFocusable(false);
        edtUsername.setClickable(false);
        edtFullName = findViewById(R.id.edtEditProfileFullName);
        edtEmail = findViewById(R.id.edtEditProfileEmail);
        edtPhone = findViewById(R.id.edtEditProfilePhone);
        edtPassword = findViewById(R.id.edtEditProfilePassword);
        spGender = findViewById(R.id.spGenderEditProfile);
        txtAvatarNote = findViewById(R.id.txtAvatarNote);
        imgAvatar = findViewById(R.id.imgAvatar);
        imgEdit = findViewById(R.id.imgEdit);
    }

    private void addEvents() {
        imgBackEditProfile.setOnClickListener(v -> finish());

        imgEdit.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            // THIS IS THE CRITICAL FIX: You must add the flag here.
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        btnUpdate.setOnClickListener(v -> {
            if (currentCustomer != null) {
                currentCustomer.setFullName(edtFullName.getText().toString().trim());
                currentCustomer.setEmail(edtEmail.getText().toString().trim());

                String phoneInput = edtPhone.getText().toString().trim();
                if (!phoneInput.isEmpty()) {
                    if (phoneInput.startsWith("0")) {
                        phoneInput = phoneInput.substring(1);
                    }
                    if (!phoneInput.matches("\\d+")) {
                        Toast.makeText(this, "Số điện thoại không hợp lệ", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    currentCustomer.setPhoneNumber(phoneInput);
                }

                // ✅ Giữ lại mật khẩu nếu không nhập mới
                String passwordInput = edtPassword.getText().toString().trim();
                if (!passwordInput.isEmpty()) {
                    currentCustomer.setPassword(passwordInput);
                }

                currentCustomer.setSex(spGender.getSelectedItem().toString());

                Customer savedCustomer = SharedPrefManager.getCurrentCustomer(this);
                if (selectedImageUri != null) {
                    currentCustomer.setAvatarURL(selectedImageUri.toString());
                } else {
                    if (savedCustomer != null && savedCustomer.getAvatarURL() != null) {
                        currentCustomer.setAvatarURL(savedCustomer.getAvatarURL());
                    } else {
                        currentCustomer.setAvatarURL(null);
                    }
                }

                SharedPrefManager.updateCustomer(this, currentCustomer);
                SharedPrefManager.setCurrentCustomer(this, currentCustomer);

                CustomerConnector connector = new CustomerConnector();
                android.util.Log.d("EditProfile", "Updating userID: " + currentCustomer.getUserID() + ", email: " + currentCustomer.getEmail());
                String debugUserId = currentCustomer.getUserID() != null ? currentCustomer.getUserID() : "null";
                android.util.Log.d("DebugUserID", "⚠️ currentCustomer.getUserID() = " + debugUserId);

                connector.updateCustomerInFirebase(currentCustomer, new com.rebound.callback.FirebaseSingleCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        Toast.makeText(EditProfileActivity.this, getString(R.string.profile_update_success), Toast.LENGTH_SHORT).show();
                        new android.os.Handler().postDelayed(EditProfileActivity.this::finish, 1200);
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        Toast.makeText(EditProfileActivity.this, "Failed to update profile in database: " + errorMessage + "\nuserID: " + currentCustomer.getUserID(), Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                Toast.makeText(this, getString(R.string.profile_update_user_not_found), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void loadUserData() {
        currentCustomer = SharedPrefManager.getCurrentCustomer(this);

        if (currentCustomer != null) {
            edtUsername.setText(currentCustomer.getUsername());
            edtFullName.setText(currentCustomer.getFullName());
            edtEmail.setText(currentCustomer.getEmail());
            // Safely format phone number as String, always show leading zero if not present
            String phone = "";
            try {
                phone = String.valueOf(currentCustomer.getPhoneNumber());
            } catch (Exception e) {
                phone = "";
            }
            if (!phone.isEmpty() && !phone.startsWith("0")) {
                phone = "0" + phone;
            }
            edtPhone.setText(phone);
            edtPassword.setText(String.valueOf(currentCustomer.getPassword()));
            txtAvatarNote.setText(currentCustomer.getFullName());

            String[] genderOptions = getResources().getStringArray(R.array.gender_options);
            int selectedIndex = 0;
            for (int i = 0; i < genderOptions.length; i++) {
                if (genderOptions[i].equalsIgnoreCase(currentCustomer.getSex())) {
                    selectedIndex = i;
                    break;
                }
            }
            spGender.setSelection(selectedIndex);

            if (currentCustomer.getAvatarURL() != null && !currentCustomer.getAvatarURL().isEmpty()) {
                Glide.with(this)
                        .load(Uri.parse(currentCustomer.getAvatarURL()))
                        .circleCrop()
                        .placeholder(R.mipmap.ic_avatar_sample)
                        .into(imgAvatar);
            }
        } else {
            Toast.makeText(this, getString(R.string.profile_update_user_not_found), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();

            // With the fix in Part 1, this block will now work correctly.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                try {
                    // Directly request the read permission you asked for when launching the intent.
                    getContentResolver().takePersistableUriPermission(selectedImageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } catch (SecurityException e) {
                    e.printStackTrace();
                }
            }

            Glide.with(this)
                    .load(selectedImageUri)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .circleCrop()
                    .placeholder(R.mipmap.ic_avatar_sample)
                    .into(imgAvatar);
        }
    }

}
