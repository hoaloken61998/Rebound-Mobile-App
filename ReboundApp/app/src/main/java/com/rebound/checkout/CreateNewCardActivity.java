package com.rebound.checkout;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.button.MaterialButton;
import com.rebound.R;
import com.rebound.adapters.ViewPaperAdapter;
import com.rebound.connectors.CardConnector;
import com.rebound.utils.SharedPrefManager;
import com.rebound.models.Customer.Customer;
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator;

import java.util.List;

public class CreateNewCardActivity extends AppCompatActivity {

    private ViewPager2 viewPagerCards;
    private DotsIndicator dotsIndicator;
    private EditText edtNameOnCard, edtCardNumber, edtExpMonth, edtExpYear, edtCVV;
    private MaterialButton btnCreateNewCardAddCard;

    private String cardType = ""; // "Credit Card" or "Debit Card"
    private String from = "";     // profile | checkout

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_card);

        // Nhận loại thẻ & nơi mở
        cardType = getIntent().getStringExtra("cardType");
        from = getIntent().getStringExtra("from"); // có thể là "profile" hoặc "checkout"

        viewPagerCards = findViewById(R.id.viewPagerCards);
        dotsIndicator = findViewById(R.id.dotsIndicator);
        edtNameOnCard = findViewById(R.id.edtNameOnCard);
        edtCardNumber = findViewById(R.id.edtCardNumber);
        edtExpMonth = findViewById(R.id.edtExpMonth);
        edtExpYear = findViewById(R.id.edtExpYear);
        edtCVV = findViewById(R.id.edtCVV);
        btnCreateNewCardAddCard = findViewById(R.id.btnCreateNewCardAddCard);

        int[] imageResIds = {
                R.mipmap.card1,
                R.mipmap.card2,
                R.mipmap.card3
        };

        ViewPaperAdapter adapter = new ViewPaperAdapter(this, imageResIds);
        viewPagerCards.setAdapter(adapter);
        dotsIndicator.setViewPager2(viewPagerCards);

        findViewById(R.id.imgBack).setOnClickListener(v -> finish());

        btnCreateNewCardAddCard.setOnClickListener(v -> {
            String nameOnCard = edtNameOnCard.getText().toString().trim();
            String cardNumber = edtCardNumber.getText().toString().trim();
            String expMonth = edtExpMonth.getText().toString().trim();
            // Remove the first two letters from expYear if length > 2, and update the EditText
            String expYear = edtExpYear.getText().toString().trim();
            String cvv = edtCVV.getText().toString().trim();

            if (nameOnCard.isEmpty() || cardNumber.isEmpty() ||
                    expMonth.isEmpty() || expYear.isEmpty() || cvv.isEmpty()) {
                Toast.makeText(this, getString(R.string.message_fill_all_fields), Toast.LENGTH_SHORT).show();
                return;
            }

            if (cardNumber.length() != 16) {
                Toast.makeText(this, getString(R.string.message_card_number_16_digits), Toast.LENGTH_SHORT).show();
                return;
            }

            int month = Integer.parseInt(expMonth);
            if (month < 1 || month > 12) {
                Toast.makeText(this, getString(R.string.message_invalid_exp_month), Toast.LENGTH_SHORT).show();
                return;
            }

            if (expYear.length() != 2) {
                Toast.makeText(this, getString(R.string.message_year_2_digits), Toast.LENGTH_SHORT).show();
                return;
            }

            if (cvv.length() < 3 || cvv.length() > 4) {
                Toast.makeText(this, getString(R.string.message_cvv_length), Toast.LENGTH_SHORT).show();
                return;
            }

            Customer currentCustomer = SharedPrefManager.getCurrentCustomer(this);
            if (currentCustomer != null) {
                String email = currentCustomer.getEmail();
                if ("Credit Card".equals(cardType)) {
                    SharedPrefManager.setCreditCard(this, email, nameOnCard, cardNumber);
                } else if ("Debit Card".equals(cardType)) {
                    SharedPrefManager.setDebitCard(this, email, nameOnCard, cardNumber);
                }
            }

            Toast.makeText(this, getString(R.string.message_card_added_success), Toast.LENGTH_SHORT).show();

            if ("profile".equals(from)) {
                finish();
            } else {
                int totalAmount = getIntent().getIntExtra("totalAmount", 0);
                Intent intent = new Intent(this, CheckOutActivity.class);
                intent.putExtra("totalAmount", totalAmount);
                intent.putExtra("cardType", cardType);
                startActivity(intent);
                finish();
            }
        });

        // Lấy UserID từ SharedPreferences
        long userId = getSharedPreferences("user_prefs", MODE_PRIVATE).getLong("user_id", -1L);
        if (userId != -1L) {
            CardConnector cardConnector = new CardConnector();
            cardConnector.getCardInfoForUser(userId, new com.rebound.callback.CardInfoCallback() {
                @Override
                public void onCardInfoLoaded(List<com.rebound.models.Cart.CardInfo> cardList) {
                    if (!cardList.isEmpty()) {
                        // Populate the EditText fields with the first card's info
                        com.rebound.models.Cart.CardInfo card = cardList.get(0);
                        edtNameOnCard.setText(card.getCardHolderName());
                        edtCardNumber.setText(String.valueOf(card.getCardNumber()));
                        edtExpMonth.setText(String.valueOf(card.getExpMonth()));
                        edtExpYear.setText(formatExpYear(String.valueOf(card.getExpYear())));
                        edtCVV.setText(String.valueOf(card.getCVV()));
                    }
                }

                @Override
                public void onError(String error) {
                    // Handle error (e.g., show a Toast)
                }
            });
        }
    }

    private String formatExpYear(String expYear) {
        if (expYear == null) return "";
        if (expYear.length() > 2) {
            return expYear.substring(2);
        }
        return expYear;
    }
}
