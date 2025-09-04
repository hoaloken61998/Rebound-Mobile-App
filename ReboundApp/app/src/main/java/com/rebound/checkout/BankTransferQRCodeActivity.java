package com.rebound.checkout;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;
import com.rebound.R;
import com.rebound.models.Customer.Customer;
import com.rebound.models.Main.NotificationItem;
import com.rebound.utils.NotificationStorage;
import com.rebound.utils.SharedPrefManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class BankTransferQRCodeActivity extends AppCompatActivity {

    ImageView imgQRCode;
    TextView txtTimer, btnRegenerate;
    Button btnConfirmSuccess;
    ImageView imgBack;

    CountDownTimer countDownTimer;
    static final long QR_DURATION = 5 * 60 * 1000;
    boolean isExpired = false;

    int totalAmount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bank_transfer_qrcode);

        imgQRCode = findViewById(R.id.imgBankTransferQRCode);
        txtTimer = findViewById(R.id.txtBankTransferTimer);
        btnRegenerate = findViewById(R.id.btnBankTransferRegenerate);
        btnConfirmSuccess = findViewById(R.id.btnConfirmPaymentSuccess);

        imgBack = findViewById(R.id.imgPaymentBack);
        imgBack.setOnClickListener(v -> onBackPressed());


        totalAmount = getIntent().getIntExtra("totalAmount", 0);

        createNotificationChannel();
        generateAndStartTimer();

        btnRegenerate.setOnClickListener(v -> generateAndStartTimer());

        btnConfirmSuccess.setOnClickListener(v -> {
            if (isExpired) {
                showToast(getString(R.string.qr_expired_message));
            } else {
                String transactionId = "#TXN" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
                String currentTime = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());

                Intent intent = new Intent(this, CheckOutActivity.class);
                intent.putExtra("fromBankTransfer", true);
                intent.putExtra("totalAmount", totalAmount);
                intent.putExtra("transactionId", transactionId);
                intent.putExtra("time", currentTime);
                intent.putExtra("paymentMethod", "Bank Transfer");
                intent.putExtra("cardType", "");
                startActivity(intent);
                finish();
            }
        });
    }

    private void generateAndStartTimer() {
        String qrContent = "PAYMENT_" + UUID.randomUUID().toString();
        generateQRCode(qrContent);

        isExpired = false;

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        countDownTimer = new CountDownTimer(QR_DURATION, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long sec = millisUntilFinished / 1000;
                long min = sec / 60;
                long s = sec % 60;
                txtTimer.setText(getString(R.string.qr_expire_countdown, min, s));
            }

            @Override
            public void onFinish() {
                isExpired = true;
                txtTimer.setText(getString(R.string.qr_expired_prompt));
            }
        };
        countDownTimer.start();
    }

    private void generateQRCode(String content) {
        QRCodeWriter writer = new QRCodeWriter();
        try {
            int size = 500;
            Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565);
            com.google.zxing.common.BitMatrix bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size);
            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? android.graphics.Color.BLACK : android.graphics.Color.WHITE);
                }
            }
            imgQRCode.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "order_channel",
                    getString(R.string.channel_order_title),
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription(getString(R.string.channel_order_description));

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private void sendPaymentSuccessNotification(String transactionId, int amount) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
                return;
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "order_channel")
                .setSmallIcon(R.mipmap.ic_order)
                .setContentTitle(getString(R.string.payment_success_title))
                .setContentText(getString(R.string.payment_success_text, transactionId, amount))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(2002, builder.build());

        Customer current = SharedPrefManager.getCurrentCustomer(this);
        if (current != null) {
            NotificationItem item = new NotificationItem(
                    NotificationItem.TYPE_NOTIFICATION,
                    getString(R.string.payment_success_title),
                    getString(R.string.payment_success_detailed, transactionId, amount),
                    getString(R.string.time_just_now),
                    System.currentTimeMillis()
            );
            NotificationStorage.saveNotification(this, current.getEmail(), item);
        }
    }
}
