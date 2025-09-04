package com.rebound.models.Cart;

import java.io.Serializable;

public class CardInfo implements Serializable {
    private Long CardID;
    private String CardHolderName;
    private Long CardNumber;
    private Object ExpMonth;
    private Long ExpYear;
    private Long CVV;
    private Object UserID;
    private Long UserPaymentID;

    public CardInfo() {}

    public CardInfo(Long cardID, String cardHolderName, Long cardNumber, Long expMonth, Long expYear, Long CVV, Object userID, Long userPaymentID) {
        CardID = cardID;
        CardHolderName = cardHolderName;
        CardNumber = cardNumber;
        ExpMonth = expMonth;
        ExpYear = expYear;
        this.CVV = CVV;
        UserID = userID;
        UserPaymentID = userPaymentID;
    }

    public Long getCardID() {
        return CardID;
    }

    public void setCardID(Long cardID) {
        CardID = cardID;
    }

    public String getCardHolderName() {
        return CardHolderName;
    }

    public void setCardHolderName(String cardHolderName) {
        CardHolderName = cardHolderName;
    }

    public Long getCardNumber() {
        return CardNumber;
    }

    public void setCardNumber(Long cardNumber) {
        CardNumber = cardNumber;
    }

    public Object getExpMonth() {
        return ExpMonth;
    }

    public void setExpMonth(Long expMonth) {
        ExpMonth = expMonth;
    }

    public Long getExpYear() {
        return ExpYear;
    }

    public void setExpYear(Long expYear) {
        ExpYear = expYear;
    }

    public Long getCVV() {
        return CVV;
    }

    public void setCVV(Long CVV) {
        this.CVV = CVV;
    }

    public Object getUserID() {
        return UserID;
    }

    public void setUserID(Object userID) {
        UserID = userID;
    }

    public Long getUserPaymentID() {
        return UserPaymentID;
    }

    public void setUserPaymentID(Long userPaymentID) {
        UserPaymentID = userPaymentID;
    }
}
