package com.rebound.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.rebound.utils.SharedPrefManager;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.rebound.models.Customer.ListCustomer;
import com.rebound.R;


public class OTPVerificationActivity extends AppCompatActivity {

    ImageView imgBackOTPVerification;

    TextView txtResend;

    String receivedOTP, email, phone;
    EditText edtOTP1, edtOTP2, edtOTP3, edtOTP4;
    ListCustomer listCustomer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_otpverification);

        email = getIntent().getStringExtra("email");
        receivedOTP = getIntent().getStringExtra("otp");
        phone = getIntent().getStringExtra("phone");

        // ✅ Load listCustomer từ SharedPreferences
        listCustomer = SharedPrefManager.getCustomerList(this);

        addViews();
        addEvents();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }


    private void addEvents() {
        imgBackOTPVerification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OTPVerificationActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            }
        });

        txtResend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(OTPVerificationActivity.this, "Resend OTP", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addViews() {
        imgBackOTPVerification = findViewById(R.id.imgBackOTPVerification);
        txtResend = findViewById(R.id.txtResend);
        edtOTP1 = findViewById(R.id.edtOTP1);
        edtOTP2 = findViewById(R.id.edtOTP2);
        edtOTP3 = findViewById(R.id.edtOTP3);
        edtOTP4 = findViewById(R.id.edtOTP4);

        // Auto move to next field after input
        setupOTPInputs();
    }

    private void setupOTPInputs() {
        edtOTP1.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 1) edtOTP2.requestFocus();
            }
            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
        edtOTP2.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 1) edtOTP3.requestFocus();
                else if (s.length() == 0) edtOTP1.requestFocus();
            }
            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
        edtOTP3.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 1) edtOTP4.requestFocus();
                else if (s.length() == 0) edtOTP2.requestFocus();
            }
            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
        edtOTP4.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 0) edtOTP3.requestFocus();
            }
            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
    }

    public void do_verify_otp(View view) {
        String userInputOTP = edtOTP1.getText().toString().trim() +
                edtOTP2.getText().toString().trim() +
                edtOTP3.getText().toString().trim() +
                edtOTP4.getText().toString().trim();

        if (userInputOTP.equals(receivedOTP)) {
            Toast.makeText(this, "OTP Verified", Toast.LENGTH_SHORT).show();

            // Find email by phone in listCustomer
            String foundEmail = null;
            if (listCustomer != null) {
                for (com.rebound.models.Customer.Customer c : listCustomer.getCustomers()) {
                    String cPhone = c.getPhoneNumber() != null ? c.getPhoneNumber().toString() : "";
                    String inputPhone = phone.startsWith("0") ? phone.substring(1) : phone;
                    if (cPhone.equals(inputPhone) || cPhone.equals(phone)) {
                        foundEmail = c.getEmail();
                        break;
                    }
                }
            }
            Intent intent = new Intent(OTPVerificationActivity.this, CreateNewPasswordActivity.class);
            intent.putExtra("email", foundEmail);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Incorrect OTP!", Toast.LENGTH_SHORT).show();
        }
    }


}