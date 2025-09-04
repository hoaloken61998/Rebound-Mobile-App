package com.rebound.checkout;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.rebound.R;
import com.rebound.main.NavBarActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PaymentSuccessActivity extends AppCompatActivity {

    TextView txtTransactionId, txtPaymentTime, txtAmountSent, txtAmountReceived;
    ImageView imgBack, imgLogo;
    Button btnDownload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_success);

        txtTransactionId = findViewById(R.id.txtPaymentTransactionId);
        txtPaymentTime = findViewById(R.id.txtPaymentTime);
        txtAmountSent = findViewById(R.id.txtPaymentAmountSent);
        txtAmountReceived = findViewById(R.id.txtPaymentAmountReceived);
        imgBack = findViewById(R.id.imgPaymentBack);
        imgLogo = findViewById(R.id.imgPaymentLogo);
        btnDownload = findViewById(R.id.btnPaymentDownloadReceipt);

        String transactionId = getIntent().getStringExtra("transactionId");
        int amount = getIntent().getIntExtra("amount", 0);

        String formattedAmount = String.format("%,d VND", amount).replace(',', '.');
        String currentTime = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());

        txtTransactionId.setText(transactionId != null ? transactionId : getString(R.string.transaction_unknown));
        txtPaymentTime.setText(currentTime);
        txtAmountSent.setText(formattedAmount);
        txtAmountReceived.setText(formattedAmount);

        imgBack.setOnClickListener(v -> {
            Intent intent = new Intent(this, NavBarActivity.class);
            intent.putExtra("targetFragment", "main");
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        btnDownload.setOnClickListener(v ->
                Toast.makeText(this, getString(R.string.downloading_receipt), Toast.LENGTH_SHORT).show()
        );
    }
}
