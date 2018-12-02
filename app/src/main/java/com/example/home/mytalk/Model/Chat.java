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
    private long badgeCount;
    private String timestamp;
    private String lastMessage;
    private List<String> join;
    private List<String> joinUserKey;
    private List<String> joinUserPhoto;

    public Chat(String email, String text, String photo, String time,
                String key, String userNumber,String type, List<String> unReadUserList,
                int unReadCount,String messageID,boolean seen,long badgeCount,String timestamp,String lastMessage,
                List<String> join , List<String> joinUserKey, List<String> joinUserPhoto) {
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
        this.badgeCount = badgeCount;
        this.timestamp = timestamp;
        this.lastMessage = lastMessage;
        this.join = join;
        this.joinUserKey = joinUserKey;
        this.joinUserPhoto = joinUserPhoto;
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

    public long getBadgeCount() {
        return badgeCount;
    }

    public void setBadgeCount(long badgeCount) {
        this.badgeCount = badgeCount;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public List<String> getJoin() {
        return join;
    }

    public void setJoin(List<String> join) {
        this.join = join;
    }

    public List<String> getJoinUserKey() {
        return joinUserKey;
    }

    public void setJoinUserKey(List<String> joinUserKey) {
        this.joinUserKey = joinUserKey;
    }

    public List<String> getJoinUserPhoto() {
        return joinUserPhoto;
    }

    public void setJoinUserPhoto(List<String> joinUserPhoto) {
        this.joinUserPhoto = joinUserPhoto;
    }
}
