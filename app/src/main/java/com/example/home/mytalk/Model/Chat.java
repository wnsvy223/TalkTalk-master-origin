package com.example.home.mytalk.Model;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;


//모델클래스의 멤버변수 이름은 데이터베이스에 저장되어 있는 json트리의 key 이름값과 동일해야 함.
public class Chat {

    private String email;
    private String text;
    private String photo;
    private String key;
    private String time;
    private String name;
    private String userNumber;
    private String type;
    private List<String> unReadUserList;
    private int unReadCount;
    private String messageID;
    private boolean seen;

    public Chat(String email, String text, String photo, String time, String key, String userNumber,String type, ArrayList<String> unReadUserList, int unReadCount,String messageID,boolean seen) {
        this.email = email;
        this.text = text;
        this.photo = photo;
        this.time = time;
        this.key = key;
        this.userNumber = userNumber;
        this.type = type;
        this.unReadUserList = unReadUserList;
        this.unReadCount = unReadCount;
        this.messageID = messageID;
        this.seen = seen;
    }

    public Chat() {
        // Default constructor required for calls to DataSnapshot.getValue(Comment.class)
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

    public List<String> getUnReadUserList() {
        return unReadUserList;
    }

    public void setUnReadUserList(List<String> unReadUserList) {
        this.unReadUserList = unReadUserList;
    }

    public int getUnReadCount() {
        return unReadCount;
    }

    public void setUnReadCount(int unReadCount) {
        this.unReadCount = unReadCount;
    }

    public String getMessageID() {
        return messageID;
    }

    public void setMessageID(String messageID) {
        this.messageID = messageID;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }
}
