package com.rebound.login;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rebound.utils.GmailSender;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.rebound.models.Customer.Customer;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.rebound.models.Customer.ListCustomer;
import com.rebound.R;
import com.rebound.utils.SharedPrefManager;

public class ForgotPasswordActivity extends AppCompatActivity {

    ImageView imgBackForgotPassword;
    TextView txtBottomForgotPasswordLogin;
    Button btnSendCode;
    EditText edtForgotPasswordPhone;
    ListCustomer listCustomer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password);




        listCustomer = SharedPrefManager.getCustomerList(this);
        if (listCustomer == null) {
            listCustomer = new ListCustomer(); // tạo danh sách mẫu
            SharedPrefManager.saveCustomerList(this, listCustomer); // lưu lại
        }

        addViews();
        addEvents();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void addEvents() {
        imgBackForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ForgotPasswordActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        txtBottomForgotPasswordLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openLoginActivity();
            }
        });

    }

    private void addViews() {
        imgBackForgotPassword = findViewById(R.id.imgBackForgotPassword);
        txtBottomForgotPasswordLogin = findViewById(R.id.txtBottomForgotPasswordLogin);
        btnSendCode = findViewById(R.id.btnSendCode);
        edtForgotPasswordPhone = findViewById(R.id.edtForgotPasswordEmail); // reuse id for phone
    }

    private void openLoginActivity() {
        Intent intent = new Intent(ForgotPasswordActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private String generateOTP() {
        int otp = (int)(Math.random() * 9000) + 1000; // random 6 số
        return String.valueOf(otp);
    }

    public void do_send_code(View view) {
        String phone = edtForgotPasswordPhone.getText().toString().trim();
        String phoneForCompare = phone.startsWith("0") ? phone.substring(1) : phone;
        Long phoneForCompareLong = null;
        try {
            phoneForCompareLong = Long.parseLong(phoneForCompare);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid phone number format", Toast.LENGTH_SHORT).show();
            return;
        }

        // Query Firebase for PhoneNumber (without leading zero) as String
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("User");
        userRef.orderByChild("PhoneNumber").equalTo(phoneForCompare)
            .limitToFirst(1)
            .get()
            .addOnSuccessListener(snapshot -> {
                if (snapshot.exists()) {
                    String otp = generateOTP();
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(phone, null, "Your OTP code is: " + otp, null, null);
                    Toast.makeText(this, "OTP sent to phone: " + phone, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ForgotPasswordActivity.this, OTPVerificationActivity.class);
                    intent.putExtra("phone", phone);
                    intent.putExtra("otp", otp);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Phone number does not exist", Toast.LENGTH_SHORT).show();
                }
            })
            .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}