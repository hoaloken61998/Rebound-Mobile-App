package com.rebound.models.Customer;

import com.google.firebase.database.PropertyName;

import java.io.Serializable;

public class Customer implements Serializable {
    @PropertyName("UserID")
    private Object UserID;
    @PropertyName("Email")
    private String Email;
    @PropertyName("Password")
    private Object Password;
    @PropertyName("FullName")
    private String FullName;
    @PropertyName("AvatarURL")
    private String AvatarURL;
    @PropertyName("PhoneNumber")
    private Object PhoneNumber;
    @PropertyName("RegistrationDate")
    private String RegistrationDate;
    @PropertyName("DateOfBirth")
    private String DateOfBirth;
    @PropertyName("Sex")
    private String Sex;
    @PropertyName("UserRanking")
    private String UserRanking;
    @PropertyName("Username")
    private String Username;

    public Customer() {}

    @PropertyName("UserID")
    public String getUserID() { return objectToString(UserID); }
    @PropertyName("UserID")
    public void setUserID(Object UserID) { this.UserID = UserID; }

    @PropertyName("Email")
    public String getEmail() { return Email; }
    @PropertyName("Email")
    public void setEmail(String Email) {
        if (Email == null) {
            android.util.Log.e("CustomerDebug", "Email is null or not a String");
        }
        this.Email = Email;
    }

    @PropertyName("Password")
    public String getPassword() { return objectToString(Password); }
    @PropertyName("Password")
    public void setPassword(Object Password) { this.Password = Password; }

    @PropertyName("FullName")
    public String getFullName() { return FullName; }
    @PropertyName("FullName")
    public void setFullName(String FullName) {
        if (FullName == null) {
            android.util.Log.e("CustomerDebug", "FullName is null or not a String");
        }
        this.FullName = FullName;
    }

    @PropertyName("AvatarURL")
    public String getAvatarURL() { return AvatarURL; }
    @PropertyName("AvatarURL")
    public void setAvatarURL(String AvatarURL) {
        if (AvatarURL == null) {
            android.util.Log.e("CustomerDebug", "AvatarURL is null or not a String");
        }
        this.AvatarURL = AvatarURL;
    }

    @PropertyName("PhoneNumber")
    public String getPhoneNumber() { return objectToString(PhoneNumber); }
    @PropertyName("PhoneNumber")
    public void setPhoneNumber(Object PhoneNumber) { this.PhoneNumber = PhoneNumber; }

    @PropertyName("RegistrationDate")
    public String getRegistrationDate() { return RegistrationDate; }
    @PropertyName("RegistrationDate")
    public void setRegistrationDate(String RegistrationDate) {
        if (RegistrationDate == null) {
            android.util.Log.e("CustomerDebug", "RegistrationDate is null or not a String");
        }
        this.RegistrationDate = RegistrationDate;
    }

    @PropertyName("DateOfBirth")
    public String getDateOfBirth() { return DateOfBirth; }
    @PropertyName("DateOfBirth")
    public void setDateOfBirth(String DateOfBirth) {
        if (DateOfBirth == null) {
            android.util.Log.e("CustomerDebug", "DateOfBirth is null or not a String");
        }
        this.DateOfBirth = DateOfBirth;
    }

    @PropertyName("Sex")
    public String getSex() { return Sex; }
    @PropertyName("Sex")
    public void setSex(String Sex) {
        if (Sex == null) {
            android.util.Log.e("CustomerDebug", "Sex is null or not a String");
        }
        this.Sex = Sex;
    }

    @PropertyName("UserRanking")
    public String getUserRanking() { return UserRanking; }
    @PropertyName("UserRanking")
    public void setUserRanking(String UserRanking) {
        if (UserRanking == null) {
            android.util.Log.e("CustomerDebug", "UserRanking is null or not a String");
        }
        this.UserRanking = UserRanking;
    }

    @PropertyName("Username")
    public String getUsername() { return Username; }
    @PropertyName("Username")
    public void setUsername(String Username) {
        if (Username == null) {
            android.util.Log.e("CustomerDebug", "Username is null or not a String");
        }
        this.Username = Username;
    }

    private String objectToString(Object obj) {
        if (obj == null) return null;
        if (obj instanceof String) return (String) obj;
        if (obj instanceof Number) return String.valueOf(obj);
        return obj.toString();
    }
}
