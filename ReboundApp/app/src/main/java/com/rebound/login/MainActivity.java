package com.rebound.login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.rebound.R;
import com.rebound.connectors.CustomerConnector;
import com.rebound.callback.FirebaseLoginCallback;
import com.rebound.main.NavBarActivity;
import com.rebound.models.Customer.Customer;
import com.rebound.utils.CartManager;
import com.rebound.utils.SharedPrefManager;
import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

public class MainActivity extends AppCompatActivity {

    // ... (Your existing views are fine)
    TextView txtLoginForgotPassword;
    TextView txtBottomLoginRegister;
    ImageView imgBackLogin;
    ImageView imgGoogleSignIn;
    EditText edtLoginEmail;
    EditText edtLoginPassword;
    MaterialButton btnLogin;
    MaterialCheckBox chkTerms;
    // Add a ProgressBar to your layout XML for user feedback
    // ProgressBar progressBar;

    private CustomerConnector customerConnector; // Connector instance

    private GoogleSignInClient mGoogleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        addViews();
        addEvents();

        // Initialize the new Firebase-enabled connector
        customerConnector = new CustomerConnector();

        CartManager.init(getApplicationContext());

        // ... (Your existing onCreate code is fine)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Remove disabling of login button based on checkbox
        btnLogin.setEnabled(true);
        btnLogin.setAlpha(1.0f);

        // Optionally, pre-fill email/password if saved
        SharedPreferences prefs = getSharedPreferences("login_prefs", MODE_PRIVATE);
        String savedEmail = prefs.getString("email", "");
        String savedPwd = prefs.getString("password", "");
        if (!savedEmail.isEmpty() && !savedPwd.isEmpty()) {
            edtLoginEmail.setText(savedEmail);
            edtLoginPassword.setText(savedPwd);
            chkTerms.setChecked(true);
        }

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                        handleSignInResult(task);
                    } else {
                        Toast.makeText(this, "Google Sign-In canceled", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    // THIS IS THE MAIN CHANGED METHOD
    public void do_login(View view) {
        String email = edtLoginEmail.getText().toString().trim();
        String pwd = edtLoginPassword.getText().toString().trim();

        if (email.isEmpty() || pwd.isEmpty()) {
            Toast.makeText(this, "Please enter both Email and Password.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid email format.", Toast.LENGTH_SHORT).show();
            return;
        }

        // --- Start of Asynchronous Login ---
        // progressBar.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);

        customerConnector.loginWithFirebase(email, pwd, new com.rebound.callback.FirebaseLoginCallback() {
            @Override
            public void onSuccess(com.rebound.models.Customer.Customer customer) {
                Toast.makeText(MainActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                SharedPreferences.Editor authEditor = getSharedPreferences("auth", MODE_PRIVATE).edit();
                authEditor.putString("current_user", customer.getUsername());
                authEditor.apply();
                SharedPrefManager.setCurrentCustomer(MainActivity.this, customer);
                SharedPrefManager.addCustomer(MainActivity.this, customer);
                CartManager.getInstance().setUserEmail(customer.getEmail());
                // Save login info if checkbox is checked
                if (chkTerms.isChecked()) {
                    SharedPreferences.Editor editor = getSharedPreferences("login_prefs", MODE_PRIVATE).edit();
                    editor.putString("email", email);
                    editor.putString("password", pwd);
                    editor.apply();
                } else {
                    // Clear saved login info
                    SharedPreferences.Editor editor = getSharedPreferences("login_prefs", MODE_PRIVATE).edit();
                    editor.remove("email");
                    editor.remove("password");
                    editor.apply();
                }
                // Save userId to SharedPreferences for order queries
                SharedPreferences userPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                long userId = -1;
                try {
                    userId = Long.parseLong(String.valueOf(customer.getUserID()));
                } catch (Exception e) {
                    // handle error or keep userId as -1
                }
                userPrefs.edit().putLong("user_id", userId).apply();
                Intent intent = new Intent(MainActivity.this, NavBarActivity.class);
                startActivity(intent);
                finish();
            }
            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(MainActivity.this, "Login failed: " + errorMessage, Toast.LENGTH_LONG).show();
                btnLogin.setEnabled(true);
            }
        });
    }

    private void addViews() {
        // ... (No changes needed here)
        txtLoginForgotPassword = findViewById(R.id.txtLoginForgotPassword);
        txtBottomLoginRegister = findViewById(R.id.txtBottomLoginRegister);
        imgBackLogin = findViewById(R.id.imgBackLogin);
        edtLoginEmail = findViewById(R.id.edtLoginEmail);
        edtLoginPassword = findViewById(R.id.edtLoginPassword);
        btnLogin = findViewById(R.id.btnLogin);
        chkTerms = findViewById(R.id.chkTerms);
        imgGoogleSignIn = findViewById(R.id.imgGoogleSignIn);
        // progressBar = findViewById(R.id.progressBar); // Initialize your progress bar
    }

    // ... (rest of your methods like addEvents, handleSignInResult, etc. can remain as they are)
    private void addEvents() {
        imgBackLogin.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
            startActivity(intent);
        });

        txtBottomLoginRegister.setOnClickListener(v -> openRegisterActivity());

        txtLoginForgotPassword.setOnClickListener(v -> openForgotPasswordActivity());

        imgGoogleSignIn.setOnClickListener(v -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });

    }

    private void openRegisterActivity() {
        Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
        intent.putExtra("previous_activity", "main");
        startActivity(intent);
        finish();
    }

    private void openForgotPasswordActivity() {
        Intent intent = new Intent(MainActivity.this, ForgotPasswordActivity.class);
        startActivity(intent);
        finish();
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            if (account != null) {
                String email = account.getEmail();
                String name = account.getDisplayName();
                String avatarUrl = account.getPhotoUrl() != null ? account.getPhotoUrl().toString() : "";

                Customer customer = new Customer();
                customer.setEmail(email);
                customer.setFullName(name);
                customer.setAvatarURL(avatarUrl);
                customer.setUsername(email); // Or generate a username

                // NOTE: You should also save this new user to your Firebase Database here!
                // This ensures that a user signing in with Google for the first time
                // gets an entry in your "User" table.

                SharedPrefManager.setCurrentCustomer(this, customer);
                SharedPrefManager.addCustomer(this, customer);
                CartManager.getInstance().setUserEmail(email);

                Intent intent = new Intent(MainActivity.this, NavBarActivity.class);
                startActivity(intent);
                finish();
            }

        } catch (ApiException e) {
            Toast.makeText(this, "Google Sign-In failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}