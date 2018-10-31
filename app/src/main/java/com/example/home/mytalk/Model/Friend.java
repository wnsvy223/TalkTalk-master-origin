package com.example.home.mytalk.Model;

import java.security.PrivateKey;

/**
 * Created by Home on 2017-03-16.
 */

// [START comment_class]

public class Friend{

    private String email;
    private String photo;
    private String key;
    private String state;
    private String latitude;
    private String longitude;
    private String accept;
    private String name;
    private String phone;
    private boolean isCheck;
    private String deviceToken;

    public Friend(String email, String photo, String key, String state, String latitude, String longitude, String accept, boolean isCheck, String name, String phone,String deviceToken) {
        this.email = email;
        this.photo = photo;
        this.key = key;
        this.state = state;
        this.latitude = latitude;
        this.longitude = longitude;
        this.accept = accept;
        this.isCheck = isCheck;
        this.name = name;
        this.phone = phone;
        this.deviceToken = deviceToken;
    }

    public Friend() {
        // Default constructor required for calls to DataSnapshot.getValue(Comment.class)
    }



    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }


    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getAccept() {
        return accept;
    }

    public void setAccept(String accept) {
        this.accept = accept;
    }

    public boolean isCheck() {
        return isCheck;
    }

    public void setCheck(boolean check) {
        isCheck = check;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }
}
