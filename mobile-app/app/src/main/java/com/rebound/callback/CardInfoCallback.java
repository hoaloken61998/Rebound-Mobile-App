package com.rebound.callback;

import com.rebound.models.Cart.CardInfo;

import java.util.List;

public interface CardInfoCallback {
    void onCardInfoLoaded(List<CardInfo> cardList);
    void onError(String error);
}
