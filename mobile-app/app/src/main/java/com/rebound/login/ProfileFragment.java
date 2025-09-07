package com.rebound.login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.rebound.R;
import com.rebound.checkout.CreateNewCardActivity;
import com.rebound.main.LanguageSelectorActivity;
import com.rebound.main.OrdersActivity;
import com.rebound.main.PrivacyPolicyActivity;
import com.rebound.models.Customer.Customer;
import com.rebound.utils.SharedPrefManager;
import com.rebound.login.WishlistActivity;
import com.rebound.connectors.CustomerConnector;

public class ProfileFragment extends Fragment {

    private LinearLayout optionHelpCenter;
    private LinearLayout optionYourProfile;
    private LinearLayout optionLogOut;
    private LinearLayout optionWishList;
    private ImageView imgBackProfile;
    private ImageView imgAvatar;
    private TextView txtProfileUserName;
    private LinearLayout optionMyOrders;
    private LinearLayout optionPrivacyPolicy;
    private LinearLayout optionLanguage;

    private Customer currentCustomer;

    private LinearLayout optionPaymentMethod;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        addViews(view);
        addEvents();
        loadUserInfo();

        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        return view;
    }

    private void addViews(View view) {
        optionHelpCenter = view.findViewById(R.id.optionHelpCenter);
        optionYourProfile = view.findViewById(R.id.optionYourProfile);
        optionLogOut = view.findViewById(R.id.optionLogOut);
        optionWishList = view.findViewById(R.id.optionWishList);
        imgBackProfile = view.findViewById(R.id.imgBackProfile);
        imgAvatar = view.findViewById(R.id.imgAvatar);
        txtProfileUserName = view.findViewById(R.id.txtProfileUserName);
        optionMyOrders = view.findViewById(R.id.optionMyOrders);
        optionPrivacyPolicy = view.findViewById(R.id.optionPrivacyPolicy);
        optionLanguage = view.findViewById(R.id.optionLanguage);
        optionPaymentMethod = view.findViewById(R.id.optionPaymentMethod);
    }

    private void addEvents() {
        optionHelpCenter.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), HelpCenterActivity.class));
        });

        optionWishList.setOnClickListener(v -> {
            currentCustomer = SharedPrefManager.getCurrentCustomer(requireContext());
            if (currentCustomer == null) {
                Toast.makeText(getContext(), getString(R.string.please_login_wishlist), Toast.LENGTH_SHORT).show();
                return;
            }
            startActivity(new Intent(getActivity(), WishlistActivity.class));
        });

        optionYourProfile.setOnClickListener(v -> {
            // Always get the latest currentCustomer before checking
            currentCustomer = SharedPrefManager.getCurrentCustomer(requireContext());
            if (currentCustomer == null) {
                Toast.makeText(getContext(), getString(R.string.please_login), Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(getActivity(), EditProfileActivity.class);
            intent.putExtra("username", currentCustomer.getUsername());
            startActivity(intent);
        });

        optionLogOut.setOnClickListener(v -> {
            // ✨ Added: AlertDialog xác nhận đăng xuất
            View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_logout, null);
            AlertDialog alertDialog = new AlertDialog.Builder(requireContext())
                    .setView(dialogView)
                    .setCancelable(false)
                    .create();

            Button btnYes = dialogView.findViewById(R.id.btnLogOutYes);
            Button btnNo = dialogView.findViewById(R.id.btnLogOutNo);

            btnYes.setOnClickListener(v1 -> {
                // Clear current user session
                SharedPrefManager.logoutCurrentCustomer(requireContext());
                // Optionally clear other session data if needed
                SharedPreferences.Editor editor = requireActivity()
                        .getSharedPreferences("auth", Context.MODE_PRIVATE).edit();
                editor.clear();
                editor.apply();

                alertDialog.dismiss();

                Intent intent = new Intent(getActivity(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            });

            btnNo.setOnClickListener(v1 -> alertDialog.dismiss());

            if (alertDialog.getWindow() != null) {
                alertDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            }

            alertDialog.show();
        });

        optionMyOrders.setOnClickListener(v -> {
            if (!isAdded() || getContext() == null || getFragmentManager() == null || getFragmentManager().isDestroyed()) return;
            currentCustomer = SharedPrefManager.getCurrentCustomer(requireContext());
            if (currentCustomer == null) {
                Toast.makeText(getContext(), getString(R.string.please_login_orders), Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(getActivity(), OrdersActivity.class);
                startActivity(intent);
            }
        });

        optionPrivacyPolicy.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), PrivacyPolicyActivity.class));
        });

        optionLanguage.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), LanguageSelectorActivity.class));
        });

        optionPaymentMethod.setOnClickListener(v -> {
            Customer currentCustomer = SharedPrefManager.getCurrentCustomer(requireContext());

            if (currentCustomer == null) {
                Toast.makeText(getContext(), getString(R.string.please_login_payment), Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(getActivity(), CreateNewCardActivity.class);
            intent.putExtra("cardType", "Credit Card");
            intent.putExtra("from", "profile");
            startActivity(intent);
        });


        imgBackProfile.setVisibility(View.VISIBLE);
        imgBackProfile.setOnClickListener(v -> requireActivity().onBackPressed());
    }

    private void loadUserInfo() {
        currentCustomer = SharedPrefManager.getCurrentCustomer(requireContext());
        if (currentCustomer == null) {
            // Not logged in, hide profile info or show login prompt
            txtProfileUserName.setText(getString(R.string.please_login));
            imgAvatar.setImageResource(R.drawable.ic_placeholder); // Use your default avatar resource
            return;
        }
        // Fetch latest info from Firebase
        CustomerConnector connector = new CustomerConnector();
        com.rebound.connectors.FirebaseConnector.getAllItems(
            "User",
            Customer.class,
            new com.rebound.callback.FirebaseListCallback<Customer>() {
                @Override
                public void onSuccess(java.util.ArrayList<Customer> customers) {
                    if (!isAdded()) return;
                    for (Customer c : customers) {
                        if (c != null && c.getEmail() != null && c.getEmail().equalsIgnoreCase(currentCustomer.getEmail())) {
                            txtProfileUserName.setText(c.getFullName());
                            if (c.getAvatarURL() != null && !c.getAvatarURL().isEmpty()) {
                                Glide.with(requireContext()).load(c.getAvatarURL()).into(imgAvatar);
                            } else {
                                imgAvatar.setImageResource(R.drawable.ic_placeholder);
                            }
                            break;
                        }
                    }
                }
                @Override
                public void onFailure(String errorMessage) {
                    txtProfileUserName.setText(currentCustomer.getFullName());
                    imgAvatar.setImageResource(R.drawable.ic_placeholder);
                }
            }
        );
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserInfo();
    }
}
