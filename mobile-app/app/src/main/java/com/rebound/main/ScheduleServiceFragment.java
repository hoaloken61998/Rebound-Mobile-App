package com.rebound.main;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.rebound.R;
import com.rebound.connectors.BranchConnector;
import com.rebound.data.BranchData;
import com.rebound.main.ReservationDialog;
import com.rebound.models.Customer.Customer;
import com.rebound.utils.FirebaseReservationManager;
import com.rebound.utils.SharedPrefManager;

import java.text.SimpleDateFormat;
import java.util.*;

public class ScheduleServiceFragment extends Fragment {

    private final long[] selectedDateMillis = {0};
    private String selectedService = "";
    private Object selectedLocationID = null;
    private Object selectedServiceID = null;

    public ScheduleServiceFragment() {}

    private void setSelectedButton(MaterialButton button) {
        button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F2F0D4")));
        button.setTextColor(Color.WHITE);
        button.setStrokeWidth(0);
    }

    private void setUnselectedButton(MaterialButton button) {
        button.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
        button.setTextColor(Color.parseColor("#22000000"));
        button.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#22000000")));
        button.setStrokeWidth(1);
    }

    private void setSelectedServiceButton(Button button) {
        button.setBackgroundTintList(ColorStateList.valueOf(Color.BLACK));
        button.setTextColor(Color.WHITE);
    }

    private void setUnselectedServiceButton(Button button) {
        button.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
        button.setTextColor(Color.BLACK);
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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_schedule_service, container, false);

        TextView txtScheduleSelectedTime = view.findViewById(R.id.txtScheduleSelectedTime);
        MaterialButton btnScheduleBook = view.findViewById(R.id.btnScheduleBook);
        ImageView imgBell = view.findViewById(R.id.imgBell);

        txtScheduleSelectedTime.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            new TimePickerDialog(requireContext(), (view1, h, m) -> {
                txtScheduleSelectedTime.setText(String.format(Locale.getDefault(), "%02d:%02d", h, m));
            }, hour, minute, true).show();
        });

        Button btn1 = view.findViewById(R.id.btnScheduleSelected1);
        Button btn2 = view.findViewById(R.id.btnScheduleSelected2);
        Button btn3 = view.findViewById(R.id.btnScheduleSelected3);
        Button btn4 = view.findViewById(R.id.btnScheduleSelected4);

        View.OnClickListener serviceClickListener = v -> {
            Button clicked = (Button) v;
            selectedService = clicked.getText().toString();

            setUnselectedServiceButton(btn1);
            setUnselectedServiceButton(btn2);
            setUnselectedServiceButton(btn3);
            setUnselectedServiceButton(btn4);
            setSelectedServiceButton(clicked);

            if (clicked == btn1) selectedServiceID = 1L;
            else if (clicked == btn2) selectedServiceID = 2L;
            else if (clicked == btn3) selectedServiceID = 3L;
            else if (clicked == btn4) selectedServiceID = 4L;
        };

        btn1.setOnClickListener(serviceClickListener);
        btn2.setOnClickListener(serviceClickListener);
        btn3.setOnClickListener(serviceClickListener);
        btn4.setOnClickListener(serviceClickListener);

        btnScheduleBook.setOnClickListener(v -> {
            Customer currentCustomer = SharedPrefManager.getCurrentCustomer(requireContext());
            if (currentCustomer == null) {
                Toast.makeText(requireContext(), "Please log in to continue.", Toast.LENGTH_SHORT).show();
                return;
            }

            String selectedTime = txtScheduleSelectedTime.getText().toString();
            if (selectedDateMillis[0] == 0 || selectedTime.isEmpty() || selectedServiceID == null || selectedLocationID == null) {
                Toast.makeText(requireContext(), "Please complete all fields.", Toast.LENGTH_SHORT).show();
                return;
            }

            String selectedDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    .format(new Date(selectedDateMillis[0]));
            String bookingTime = selectedDate + " " + selectedTime;

            FirebaseReservationManager.createBooking(
                    requireContext(),
                    currentCustomer,
                    bookingTime,
                    selectedLocationID,
                    selectedServiceID,
                    new FirebaseReservationManager.OnBookingCompleteListener() {
                        @Override
                        public void onComplete(long bookingID) {
                            ReservationDialog dialog = ReservationDialog.newInstance(
                                    selectedDate,
                                    selectedTime,
                                    ((Number) selectedServiceID).longValue(),
                                    bookingID
                            );
                            dialog.show(requireActivity().getSupportFragmentManager(), "ReservationDialog");

                            btnScheduleBook.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#BEB488")));
                            btnScheduleBook.setTextColor(Color.WHITE);
                            btnScheduleBook.setStrokeWidth(0);

                            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                btnScheduleBook.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
                                btnScheduleBook.setTextColor(Color.BLACK);
                                btnScheduleBook.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#BEB488")));
                                btnScheduleBook.setStrokeWidth(1);
                            }, 300);
                        }

                        @Override
                        public void onError(String message) {
                            Toast.makeText(getContext(), "Firebase Error: " + message, Toast.LENGTH_SHORT).show();
                        }
                    }
            );
        });

        CalendarView calendarView = view.findViewById(R.id.calendarView);
        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, dayOfMonth, 0, 0, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            selectedDateMillis[0] = calendar.getTimeInMillis();
        });

        MaterialButton btnHanoi = view.findViewById(R.id.btnScheduleHanoi);
        MaterialButton btnHCM = view.findViewById(R.id.btnScheduleHCM);

        TextView txtBranch1 = view.findViewById(R.id.txtScheduleBranch1);
        TextView txtAddress1 = view.findViewById(R.id.txtScheduleAddress1);
        TextView txtTime1 = view.findViewById(R.id.txtScheduleTime1);
        ImageView imgBranch1 = view.findViewById(R.id.imgBranch1);

        TextView txtBranch2 = view.findViewById(R.id.txtScheduleBranch2);
        TextView txtAddress2 = view.findViewById(R.id.txtScheduleAddress2);
        TextView txtTime2 = view.findViewById(R.id.txtScheduleTime2);
        ImageView imgBranch2 = view.findViewById(R.id.imgBranch2);

        btnHanoi.setOnClickListener(v -> {
            selectedLocationID = 1L;
            List<BranchConnector> branches = BranchData.getHanoiBranches();
            if (branches.size() >= 2) {
                txtBranch1.setText(branches.get(0).getName());
                txtAddress1.setText(branches.get(0).getAddress());
                txtTime1.setText(branches.get(0).getHours());
                imgBranch1.setImageResource(branches.get(0).getImageResId());

                txtBranch2.setText(branches.get(1).getName());
                txtAddress2.setText(branches.get(1).getAddress());
                txtTime2.setText(branches.get(1).getHours());
                imgBranch2.setImageResource(branches.get(1).getImageResId());
            }
            setSelectedButton(btnHanoi);
            setUnselectedButton(btnHCM);
        });

        btnHCM.setOnClickListener(v -> {
            selectedLocationID = 2L;
            List<BranchConnector> branches = BranchData.getHCMBranches();
            if (branches.size() >= 2) {
                txtBranch1.setText(branches.get(0).getName());
                txtAddress1.setText(branches.get(0).getAddress());
                txtTime1.setText(branches.get(0).getHours());
                imgBranch1.setImageResource(branches.get(0).getImageResId());

                txtBranch2.setText(branches.get(1).getName());
                txtAddress2.setText(branches.get(1).getAddress());
                txtTime2.setText(branches.get(1).getHours());
                imgBranch2.setImageResource(branches.get(1).getImageResId());
            }
            setSelectedButton(btnHCM);
            setUnselectedButton(btnHanoi);
        });

        imgBell.setOnClickListener(v -> {
            Customer currentCustomer = SharedPrefManager.getCurrentCustomer(requireContext());
            Intent intent = (currentCustomer != null)
                    ? new Intent(requireContext(), NotificationActivity.class)
                    : new Intent(requireContext(), NoNotificationActivity.class);
            startActivity(intent);
        });

        return view;
    }
}
