package com.rebound.login;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.rebound.R;
import com.rebound.utils.FirebaseCustomerChecker;
import com.rebound.utils.SharedPrefManager;

public class RegisterActivity extends AppCompatActivity {

    MaterialButton btnRegister;
    ImageView imgBackRegister;
    TextView txtRegisterBottom;
    MaterialCheckBox chkRegisterTerms;
    EditText edtUsername, edtEmail, edtPassword, edtConfirmPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        addViews();
        addEvents();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnRegister.setEnabled(false);
        btnRegister.setAlpha(0.5f);

        chkRegisterTerms.setOnCheckedChangeListener((buttonView, isChecked) -> {
            btnRegister.setEnabled(isChecked);
            btnRegister.setAlpha(isChecked ? 1.0f : 0.5f);
        });
    }

    private void addViews() {
        btnRegister = findViewById(R.id.btnRegister);
        imgBackRegister = findViewById(R.id.imgBackRegister);
        txtRegisterBottom = findViewById(R.id.txtRegisterBottom);
        chkRegisterTerms = findViewById(R.id.chkRegisterTerms);

        edtUsername = findViewById(R.id.edtRegisterUsername);
        edtEmail = findViewById(R.id.edtRegisterEmail);
        edtPassword = findViewById(R.id.edtRegisterPassword);
        edtConfirmPassword = findViewById(R.id.edtRegisterConfirmPassword);
    }

    private void addEvents() {
        imgBackRegister.setOnClickListener(view -> {
            Intent intent = getIntent();
            String previousActivity = intent.getStringExtra("previous_activity");

            if ("main".equals(previousActivity)) {
                startActivity(new Intent(this, MainActivity.class));
            } else {
                startActivity(new Intent(this, WelcomeActivity.class));
            }
            finish();
        });

        txtRegisterBottom.setOnClickListener(view -> startActivity(new Intent(this, MainActivity.class)));

        btnRegister.setOnClickListener(v -> {
            String username = edtUsername.getText().toString().trim();
            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();
            String confirm = edtConfirmPassword.getText().toString().trim();

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, getString(R.string.invalid_email_format), Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirm)) {
                Toast.makeText(this, getString(R.string.passwords_do_not_match), Toast.LENGTH_SHORT).show();
                return;
            }

            if (!chkRegisterTerms.isChecked()) {
                Toast.makeText(this, getString(R.string.agree_terms_to_continue), Toast.LENGTH_SHORT).show();
                return;
            }

            // Only check username/email, then pass all info to CompleteProfileActivity
            FirebaseCustomerChecker.isUsernameTaken(username, new FirebaseCustomerChecker.TakenCallback() {
                @Override
                public void onResult(boolean isTaken) {
                    if (isTaken) {
                        Toast.makeText(RegisterActivity.this, getString(R.string.username_exists), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    FirebaseCustomerChecker.isEmailTaken(email, new FirebaseCustomerChecker.TakenCallback() {
                        @Override
                        public void onResult(boolean isTaken) {
                            if (isTaken) {
                                Toast.makeText(RegisterActivity.this, getString(R.string.email_exists), Toast.LENGTH_SHORT).show();
                                return;
                            }
                            // Pass all info to CompleteProfileActivity
                            Intent intent = new Intent(RegisterActivity.this, CompleteProfileActivity.class);
                            intent.putExtra("username", username);
                            intent.putExtra("email", email);
                            intent.putExtra("password", password);
                            startActivity(intent);
                        }
                        @Override
                        public void onError(String error) {
                            Toast.makeText(RegisterActivity.this, error, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                @Override
                public void onError(String error) {
                    Toast.makeText(RegisterActivity.this, error, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
