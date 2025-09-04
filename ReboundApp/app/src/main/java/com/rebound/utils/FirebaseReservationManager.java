package com.rebound.utils;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.firebase.database.*;
import com.rebound.models.Customer.Customer;
import com.rebound.models.Main.NotificationItem;
import com.rebound.models.Reservation.BookingSchedule;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FirebaseReservationManager {

    // Interface callback nội bộ
    public interface OnBookingCompleteListener {
        void onComplete(long bookingID);
        void onError(String message);
    }

    // Tạo đặt lịch
    public static void createBooking(Context context, Customer customer, String dateTime,
                                     Object locationID, Object serviceID, OnBookingCompleteListener listener) {

        DatabaseReference bookingRef = FirebaseDatabase.getInstance().getReference("BookingSchedule");

        bookingRef.orderByChild("BookingID").limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long newID = 1;
                for (DataSnapshot child : snapshot.getChildren()) {
                    BookingSchedule last = child.getValue(BookingSchedule.class);
                    if (last != null && last.getBookingID() != null)
                        newID = last.getBookingID() + 1;
                }

                final long bookingID = newID;
                Object userID = convertToObject(customer.getUserID());

                BookingSchedule booking = new BookingSchedule(
                        bookingID, dateTime, locationID, serviceID, "Pending", userID
                );

                bookingRef.child(String.valueOf(bookingID)).setValue(booking)
                        .addOnSuccessListener(unused -> {
                            createNotification(customer, dateTime, serviceID);
                            listener.onComplete(bookingID);
                        })
                        .addOnFailureListener(e -> listener.onError(e.getMessage()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onError(error.getMessage());
            }
        });
    }

    // Tạo thông báo sau khi đặt lịch thành công
    private static void createNotification(Customer customer, String dateTime, Object serviceID) {
        DatabaseReference notiRef = FirebaseDatabase.getInstance().getReference("Notification");

        notiRef.orderByChild("NotificationID").limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long newNotiID = 1;
                for (DataSnapshot child : snapshot.getChildren()) {
                    NotificationItem last = child.getValue(NotificationItem.class);
                    if (last != null && last.getNotificationID() != null)
                        newNotiID = last.getNotificationID() + 1;
                }

                String sentAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                String message = "You have successfully booked an appointment on " + dateTime;

                DatabaseReference childRef = notiRef.child(String.valueOf(newNotiID));
                childRef.child("NotificationID").setValue(newNotiID);
                childRef.child("UserID").setValue(convertToObject(customer.getUserID()));
                childRef.child("Message").setValue(message);
                childRef.child("SentAt").setValue(sentAt);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    // Chuyển String ID về Object để Firebase nhận đúng
    private static Object convertToObject(String value) {
        if (value == null) return null;
        try {
            double d = Double.parseDouble(value);
            long l = (long) d;
            return (d == l) ? l : d;
        } catch (NumberFormatException e) {
            return value;
        }
    }
}
