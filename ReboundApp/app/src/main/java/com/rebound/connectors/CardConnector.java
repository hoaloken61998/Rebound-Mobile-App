package com.rebound.connectors;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import androidx.annotation.NonNull;

import com.rebound.callback.CardInfoCallback;
import com.rebound.models.Cart.CardInfo;
import java.util.ArrayList;
import java.util.List;


public class CardConnector {
    public void getCardInfoForUser(long userId, CardInfoCallback callback) {
        DatabaseReference cardRef = FirebaseDatabase.getInstance().getReference("CardInfo");
        cardRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<CardInfo> cardList = new ArrayList<>();
                Long userIdLong = userId;
                for (DataSnapshot cardSnap : snapshot.getChildren()) {
                    Object userIdObj = cardSnap.child("UserID").getValue();
                    Long dbUserId = null;
                    if (userIdObj instanceof Long) {
                        dbUserId = (Long) userIdObj;
                    } else if (userIdObj instanceof String) {
                        try {
                            dbUserId = Long.parseLong((String) userIdObj);
                        } catch (Exception ignored) {}
                    }
                    if (dbUserId != null && dbUserId.equals(userIdLong)) {
                        CardInfo card = cardSnap.getValue(CardInfo.class);
                        if (card != null) {
                            cardList.add(card);
                        }
                    }
                }
                callback.onCardInfoLoaded(cardList);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }
}
