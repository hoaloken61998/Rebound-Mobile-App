package com.rebound.checkout;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.rebound.R;
import com.rebound.models.Cart.ShippingAddress;
import com.rebound.models.Customer.Customer;
import com.rebound.utils.SharedPrefManager;

public class CheckOutShippingActivity extends AppCompatActivity {

    Spinner spinnerShipping, spinnerPayment;
    MaterialButton btnCheckoutShipping;
    ImageView imgBack;
    LinearLayout layoutShippingInfo;
    TextView txtName, txtAddress, txtPhone;
    TextView txtCheckOutShippingPaymentMethod, txtDeliveryFee, txtTotal;

    private int deliveryFee = 0;
    private int subtotal = 0;
    private String selectedPaymentMethod = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_out_shipping);

        spinnerShipping = findViewById(R.id.spinnerShipping);
        spinnerPayment = findViewById(R.id.spinnerPaymentMethod);
        txtDeliveryFee = findViewById(R.id.txtDeliveryFee);
        txtTotal = findViewById(R.id.txtCheckOutInfoTotalAmount);
        btnCheckoutShipping = findViewById(R.id.btnCheckoutShipping);
        imgBack = findViewById(R.id.imgBackCheckOutShipping);
        layoutShippingInfo = findViewById(R.id.layoutCheckOutShippingInfo);
        txtName = findViewById(R.id.txtCheckOutInfoRecipientName);
        txtAddress = findViewById(R.id.txtCheckOutInfoAddress);
        txtPhone = findViewById(R.id.txtCheckOutInfoPhoneNumber);
        txtCheckOutShippingPaymentMethod = findViewById(R.id.txtCheckOutShippingPaymentMethod);

        subtotal = getIntent().getIntExtra("totalAmount", 0);
        updateTotalAndFee();

        ArrayAdapter<CharSequence> paymentAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                getResources().getStringArray(R.array.payment_method)) {
            @Override
            public boolean isEnabled(int position) {
                return position != 0;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                tv.setTextColor(position == 0 ? Color.GRAY : Color.BLACK);
                return view;
            }
        };
        paymentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPayment.setAdapter(paymentAdapter);

        spinnerPayment.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedPaymentMethod = position > 0 ? parent.getItemAtPosition(position).toString() : "";

                if (getString(R.string.cod).equals(selectedPaymentMethod)) {
                    String currentShipping = spinnerShipping.getSelectedItem().toString();
                    if (getString(R.string.pickup_at_store).equals(currentShipping)) {
                        showToast(getString(R.string.checkout_shipping_cod_not_supported));
                        spinnerShipping.setSelection(0);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedPaymentMethod = "";
            }
        });

        ArrayAdapter<CharSequence> shippingAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                getResources().getStringArray(R.array.shipping_methods)) {
            @Override
            public boolean isEnabled(int position) {
                return position != 0;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                tv.setTextColor(position == 0 ? Color.GRAY : Color.BLACK);
                return view;
            }
        };
        shippingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerShipping.setAdapter(shippingAdapter);

        spinnerShipping.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String method = parent.getItemAtPosition(position).toString();

                if (getString(R.string.pickup_at_store).equals(method) && getString(R.string.cod).equals(selectedPaymentMethod)) {
                    showToast(getString(R.string.checkout_shipping_pickup_not_supported));
                    spinnerShipping.setSelection(0);
                    return;
                }

                if (getString(R.string.pickup_at_store).equals(method)) {
                    deliveryFee = 0;
                } else if (getString(R.string.standard_delivery).equals(method)) {
                    deliveryFee = 22000;
                } else if (getString(R.string.express_delivery).equals(method)) {
                    deliveryFee = 30000;
                } else {
                    deliveryFee = 0;
                }

                updateTotalAndFee();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        findViewById(R.id.btnCheckOutInfoAddAddress).setOnClickListener(v ->
                startActivity(new Intent(this, AddShippingAddressActivity.class)));

        imgBack.setOnClickListener(v -> finish());

        btnCheckoutShipping.setOnClickListener(v -> {
            Customer currentCustomer = SharedPrefManager.getCurrentCustomer(this);
            if (currentCustomer == null) {
                showToast(getString(R.string.checkout_shipping_user_not_found));
                return;
            }

            ShippingAddress address = SharedPrefManager.getShippingAddress(this, currentCustomer.getEmail());
            if (address == null) {
                showToast(getString(R.string.checkout_shipping_add_address));
                return;
            }

            if (spinnerShipping.getSelectedItemPosition() == 0) {
                showToast(getString(R.string.checkout_shipping_select_shipping));
                return;
            }

            if (spinnerPayment.getSelectedItemPosition() == 0) {
                showToast(getString(R.string.checkout_shipping_select_payment));
                return;
            }

            String email = currentCustomer.getEmail();
            Intent intent;

            switch (selectedPaymentMethod) {
                case "Bank Transfer":
                    intent = new Intent(this, BankTransferQRCodeActivity.class);
                    intent.putExtra("totalAmount", subtotal + deliveryFee);
                    intent.putExtra("paymentMethod", "Bank Transfer");
                    startActivity(intent);
                    break;

                case "Credit Card":
                    if (!SharedPrefManager.getCreditCardNumber(this, email).isEmpty()) {
                        intent = new Intent(this, CheckOutActivity.class);
                    } else {
                        intent = new Intent(this, CreateNewCardActivity.class);
                    }
                    intent.putExtra("cardType", "Credit Card");
                    intent.putExtra("paymentMethod", "Credit Card");
                    intent.putExtra("totalAmount", subtotal + deliveryFee);
                    startActivity(intent);
                    break;

                case "Debit Card":
                    if (!SharedPrefManager.getDebitCardNumber(this, email).isEmpty()) {
                        intent = new Intent(this, CheckOutActivity.class);
                    } else {
                        intent = new Intent(this, CreateNewCardActivity.class);
                    }
                    intent.putExtra("cardType", "Debit Card");
                    intent.putExtra("paymentMethod", "Debit Card");
                    intent.putExtra("totalAmount", subtotal + deliveryFee);
                    startActivity(intent);
                    break;

                case "Cash on Delivery":
                    new AlertDialog.Builder(this)
                            .setTitle(getString(R.string.checkout_shipping_confirm_title))
                            .setMessage(getString(R.string.checkout_shipping_confirm_cod))
                            .setPositiveButton(getString(R.string.checkout_shipping_button_yes), (dialog, which) -> {
                                Intent codIntent = new Intent(this, CheckOutActivity.class);
                                codIntent.putExtra("totalAmount", subtotal + deliveryFee);
                                codIntent.putExtra("cardType", "");
                                codIntent.putExtra("paymentMethod", "Cash on Delivery");
                                startActivity(codIntent);
                            })
                            .setNegativeButton(getString(R.string.checkout_shipping_button_cancel), null)
                            .show();
                    break;

                default:
                    showToast(getString(R.string.checkout_shipping_invalid_payment));
                    break;
            }
        });

        updateShippingInfoAndCardName();
    }

    private void updateTotalAndFee() {
        txtDeliveryFee.setText(String.format("%,d VND", deliveryFee).replace(',', '.'));
        txtTotal.setText(String.format("%,d VND", subtotal + deliveryFee).replace(',', '.'));
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateShippingInfoAndCardName();
    }

    private void updateShippingInfoAndCardName() {
        Customer currentCustomer = SharedPrefManager.getCurrentCustomer(this);
        if (currentCustomer != null) {
            ShippingAddress address = SharedPrefManager.getShippingAddress(this, currentCustomer.getEmail());
            if (address != null) {
                layoutShippingInfo.setVisibility(View.VISIBLE);
                txtName.setText(address.getName());
                txtAddress.setText(address.getAddress());
                txtPhone.setText(address.getPhone());
            } else {
                layoutShippingInfo.setVisibility(View.GONE);
            }

            String email = currentCustomer.getEmail();
            String creditName = SharedPrefManager.getCreditCardName(this, email);
            String debitName = SharedPrefManager.getDebitCardName(this, email);

            if (!creditName.isEmpty()) {
                txtCheckOutShippingPaymentMethod.setText("Credit Card: " + creditName);
            } else if (!debitName.isEmpty()) {
                txtCheckOutShippingPaymentMethod.setText("Debit Card: " + debitName);
            } else {
                txtCheckOutShippingPaymentMethod.setText(getString(R.string.checkout_shipping_no_card_added));
            }

        } else {
            layoutShippingInfo.setVisibility(View.GONE);
            txtCheckOutShippingPaymentMethod.setText(getString(R.string.checkout_shipping_no_user));
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
