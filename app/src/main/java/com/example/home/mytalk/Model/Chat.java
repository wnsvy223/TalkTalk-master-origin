package com.example.home.mytalk.Model;




public class Chat {

    private String email;
    private String text;
    private String photo;
    private String key;
    private String time;
    private String name;
    private String userNumber;
    private String type;

    public Chat() {
        // Default constructor required for calls to DataSnapshot.getValue(Comment.class)
    }



    public Chat(String email, String text, String photo, String time, String key, String userNumber,String type) {
        this.email = email;
        this.text = text;
        this.photo = photo;
        this.time = time;
        this.key = key;
        this.userNumber = userNumber;
        this.type = type;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
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

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserNumber() {
        return userNumber;
    }

    public void setUserNumber(String userNum) {
        this.userNumber = userNum;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}
