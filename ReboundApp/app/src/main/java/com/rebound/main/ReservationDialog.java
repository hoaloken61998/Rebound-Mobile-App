package com.rebound.main;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.button.MaterialButton;
import com.rebound.R;

public class ReservationDialog extends DialogFragment {

    public static ReservationDialog newInstance(String date, String time, long serviceId, long bookingId) {
        ReservationDialog dialog = new ReservationDialog();
        Bundle args = new Bundle();
        args.putString("selectedDate", date);
        args.putString("selectedTime", time);
        args.putLong("serviceId", serviceId);
        args.putLong("bookingId", bookingId);
        dialog.setArguments(args);
        return dialog;
    }

    private String getServiceName(long id) {
        switch ((int) id) {
            case 1: return "First Piercing Experience";
            case 2: return "The Quick And Simple Piercing Experience";
            case 3: return "The Extra Piercing Experience";
            case 4: return "Piercing Consulting Styling";
            default: return "Unknown Service";
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.reservation_popup, container, false);

        // Handle close button
        ImageView imgClose = view.findViewById(R.id.imgClose);
        imgClose.setOnClickListener(v -> dismiss());

        Bundle args = getArguments();
        if (args != null) {
            String selectedDate = args.getString("selectedDate", "");
            String selectedTime = args.getString("selectedTime", "");
            long serviceId = args.getLong("serviceId", 0);
            long bookingId = args.getLong("bookingId", 0);

            String selectedService = getServiceName(serviceId);

            TextView txtDate = view.findViewById(R.id.txtAppointmentDateValue);
            TextView txtTime = view.findViewById(R.id.txtAppointmentTimeValue);
            TextView txtService = view.findViewById(R.id.txtAppointmentServiceValue);
            TextView txtId = view.findViewById(R.id.txtAppointmentIdValue);

            txtDate.setText(selectedDate);
            txtTime.setText(selectedTime);
            txtService.setText(selectedService);
            txtId.setText("#" + bookingId);
        }

        MaterialButton btnRemindMe = view.findViewById(R.id.btnReservedSuccessfullyRemindMe);
        btnRemindMe.setOnClickListener(v -> {
            btnRemindMe.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#BEB488")));
            btnRemindMe.setTextColor(Color.WHITE);
            btnRemindMe.setStrokeWidth(0);

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                btnRemindMe.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
                btnRemindMe.setTextColor(Color.BLACK);
                btnRemindMe.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#BEB488")));
                btnRemindMe.setStrokeWidth(1);

                dismiss();
            }, 300);
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            Window window = getDialog().getWindow();
            window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }
}
