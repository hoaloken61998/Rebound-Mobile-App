package com.rebound.main;
import com.rebound.ar.ArCameraActivity;
import com.rebound.login.ProfileFragment;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;
import com.rebound.models.Customer.Customer;
import com.rebound.utils.SharedPrefManager;
import com.rebound.utils.CartManager;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.rebound.R;

public class NavBarActivity extends AppCompatActivity {
    FloatingActionButton fab;
    DrawerLayout drawerLayout;
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navbar);
        bottomNavigationView = findViewById(R.id.layoutMainBottomNavigationView);
        drawerLayout = findViewById(R.id.layoutMainDrawer);
        fab = findViewById(R.id.imgMainButtonScan);
        // Transparent status bar setup
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.TRANSPARENT);
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            );
        }

        if (savedInstanceState == null) {
            replaceFragment(new MainPageFragment());
        }

        bottomNavigationView.setBackground(null);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.txtNavigationShop) {
                replaceFragment(new MainPageFragment());
            } else if (id == R.id.txtNavigationSchedule) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.layoutMainFrame, new ScheduleServiceFragment())
                        .addToBackStack(null)
                        .commit();
            } else if (id == R.id.txtNavigationNews) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.layoutMainFrame, new NewsFragment())
                        .addToBackStack(null)
                        .commit();
            } else if (id == R.id.txtNavigationProfile) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.layoutMainFrame, new ProfileFragment())
                        .addToBackStack(null)
                        .commit();
            }
            return true;
        });

        // FAB mở camera
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(NavBarActivity.this, ArCameraActivity.class);
            startActivity(intent);
        });

        Customer current = SharedPrefManager.getCurrentCustomer(this);
        if (current != null) {
            CartManager.getInstance().setUserEmail(current.getEmail());
        }
    }

    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.layoutMainFrame, fragment)
                .commit();
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(cameraIntent, 101);
        } else {
            Toast.makeText(this, getString(R.string.camera_not_available), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 101 && resultCode == RESULT_OK && data != null) {
            Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
            Toast.makeText(this, getString(R.string.photo_captured_success), Toast.LENGTH_SHORT).show();
        }
    }

    // Optional: handle permission result
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            Toast.makeText(this, getString(R.string.camera_permission_denied), Toast.LENGTH_SHORT).show();
        }
    }
}
