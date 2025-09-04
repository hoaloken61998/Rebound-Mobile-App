package com.rebound.login;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rebound.R;
import com.rebound.main.NavBarActivity;
import com.rebound.models.Customer.Customer;
import com.rebound.models.Customer.ListCustomer;
import com.rebound.utils.SharedPrefManager;

public class WelcomeActivity extends AppCompatActivity {

    Button btnWelcomeLogin;
    Button btnWelcomeRegister;
    TextView txtWelcomeGuest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_welcome);

        // Yêu cầu quyền gửi SMS
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, 1002);
        }

        addViews();
        addEvents();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ListCustomer listCustomer = SharedPrefManager.getCustomerList(this);
        if (listCustomer == null) {
            listCustomer = new ListCustomer();
            SharedPrefManager.saveCustomerList(this, listCustomer);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1002) {
            if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                finish(); // Nếu từ chối, thoát app
            }
        }
    }

    private void addEvents() {
        btnWelcomeLogin.setOnClickListener(view -> {
            startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
        });

        btnWelcomeRegister.setOnClickListener(view -> {
            Intent intent = new Intent(WelcomeActivity.this, RegisterActivity.class);
            intent.putExtra("previous_activity", "welcome");
            startActivity(intent);
        });

        txtWelcomeGuest.setOnClickListener(view -> {
            Customer guest = new Customer();
            guest.setUsername("guest");
            guest.setEmail("guest@guest.com");
            SharedPrefManager.setCurrentCustomer(this, guest);
            startActivity(new Intent(WelcomeActivity.this, NavBarActivity.class));
        });
    }

    private void addViews() {
        txtWelcomeGuest = findViewById(R.id.txtWelcomeGuest);
        btnWelcomeLogin = findViewById(R.id.btnWelcomeLogin);
        btnWelcomeRegister = findViewById(R.id.btnWelcomeRegister);
    }
}
