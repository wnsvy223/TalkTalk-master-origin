package com.example.home.mytalk.Appliccation;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.example.home.mytalk.Model.Friend;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static com.example.home.mytalk.Activity.ChatActivity.OpenChatRoom;

public class FirebaseApp extends Application {

    private FirebaseDatabase database;
    private DatabaseReference mChatDisplayReference;
    private DatabaseReference FriendChatRoom;
    private DatabaseReference mUsersDatabase;
    private String currentUid;
    private String currentName;
    private String currentPhoto;
    private String currentEmail;
    private String formattedDate;

    @Override
    public void onCreate() {
        super.onCreate();

        database = FirebaseDatabase.getInstance();
        FriendChatRoom = FirebaseDatabase.getInstance().getReference("friendChatRoom");
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("users");
        SharedPreferences sharedPreferences = getSharedPreferences("email", MODE_PRIVATE); //유저정보 저장된 프레퍼런스
        currentUid = sharedPreferences.getString("uid", "");
        currentEmail = sharedPreferences.getString("email", "");
        getCurrentValue();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    public void getCurrentValue() {
       mUsersDatabase.child(currentUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Friend friend = dataSnapshot.getValue(Friend.class);
                currentName = friend.getName();
                currentPhoto = friend.getPhoto();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void setOneToOneMessage(String messageText, String type, final String FriendChatUid) {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA);
        formattedDate = simpleDateFormat.format(calendar.getTime());

        String current_user_ref = "oneToOneMessage/" + currentUid + "/" + FriendChatUid;
        String chat_user_ref = "oneToOneMessage/" + FriendChatUid + "/" + currentUid;
        DatabaseReference mChatReference = database.getReference("oneToOneMessage");
        String push_id = mChatReference.getKey();
        String messageId = mChatReference.push().getKey();

        HashMap message = new HashMap();
        message.put("name", currentName);
        message.put("email", currentEmail);
        message.put("text", messageText);
        message.put("from",currentUid);
        message.put("type", type);
        message.put("photo", currentPhoto);
        message.put("time", formattedDate);
        message.put("seen", false);
        message.put("messageID",messageId);

        HashMap messageUserMap = new HashMap();
        messageUserMap.put(current_user_ref + "/" + push_id, message);
        messageUserMap.put(chat_user_ref + "/" + push_id, message);

        FriendChatRoom.child(currentUid).child(FriendChatUid).child("seen").setValue(false);
        FriendChatRoom.child(currentUid).child(FriendChatUid).child("timestamp").setValue(formattedDate);
        FriendChatRoom.child(currentUid).child(FriendChatUid).child("Roomtype").setValue("OneToOne");
        FriendChatRoom.child(currentUid).child(FriendChatUid).child("lastMessage").setValue(messageText);
        FriendChatRoom.child(currentUid).child(FriendChatUid).child("type").setValue(type);
        mChatReference.child(currentUid).child(FriendChatUid).child(messageId).setValue(message);

        FriendChatRoom.child(FriendChatUid).child(currentUid).child("seen").setValue(false);
        FriendChatRoom.child(FriendChatUid).child(currentUid).child("timestamp").setValue(formattedDate);
        FriendChatRoom.child(FriendChatUid).child(currentUid).child("Roomtype").setValue("OneToOne");
        FriendChatRoom.child(FriendChatUid).child(currentUid).child("lastMessage").setValue(messageText);
        FriendChatRoom.child(FriendChatUid).child(currentUid).child("type").setValue(type);
        mChatReference.child(FriendChatUid).child(currentUid).child(messageId).setValue(message);

        // 해당유저와 1:1채팅 메시지를 전송하면 해당유저와의 대화창 목록을
        // FriendChatRoom노드에 추가해서 리스트에 표시되도록 하는 형태.

        FriendChatRoom.child(FriendChatUid).child(currentUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild("badgeCount")) { //배지node 있을 때
                    long currentCount = (long) dataSnapshot.child("badgeCount").getValue();
                    currentCount++;
                    FriendChatRoom.child(FriendChatUid).child(currentUid).child("badgeCount").setValue(currentCount, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            Log.d("대화상대 배지 리셋 =>  현재접속자", currentUid + "방이름 : " + FriendChatUid);
                        }
                    });
                } else { //배지node 없을 때 (= 처음 대화 시작 시)
                    FriendChatRoom.child(FriendChatUid).child(currentUid).child("badgeCount").setValue(1, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            Log.d("대화상대 배지 초기 생성", "");
                        }
                    });
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void setGroupMessage(final String messageText, final String type, final String FriendChatUid, ArrayList<String> unReadUserList) {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA);
        formattedDate = simpleDateFormat.format(calendar.getTime());

        DatabaseReference mChatReference = database.getReference("groupMessage").child(FriendChatUid);
        String messageId = mChatReference.push().getKey();
        HashMap messageGroup = new HashMap();
        messageGroup.put("name", currentName);
        messageGroup.put("email", currentEmail);
        messageGroup.put("text", messageText);
        messageGroup.put("type", type);
        messageGroup.put("photo", currentPhoto);
        messageGroup.put("time", formattedDate);
        messageGroup.put("key", currentUid);
        messageGroup.put("unReadUserList",unReadUserList);
        messageGroup.put("unReadCount",unReadUserList.size());
        messageGroup.put("messageID",messageId);
        mChatReference.child(messageId).setValue(messageGroup);

        FriendChatRoom.child(currentUid).child(FriendChatUid).child("joinUserKey").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<String> MergeListKey = new ArrayList<>();
                if (dataSnapshot.hasChildren()) {
                    MergeListKey.clear();
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        String key = postSnapshot.getValue().toString();
                        resetTimeStamp(key,FriendChatUid);
                        setLastMessage(key,FriendChatUid,messageText,type);
                        // 채팅이 쓰여질때마다 현재 채팅방의 참가자들의 키값을 받아와서
                        // 각 참가자들의 채팅방 노드의 타임스템프값을 현재시간으로 바꿔주어
                        // 타임스탬프값이 가장 최근인 채팅방을 맨 위쪽으로 정렬.

                        if(!key.equals(currentUid)){
                            MergeListKey.add(key);
                            for(int i=0; i<MergeListKey.size(); i++){
                                resetBadgeCount(MergeListKey.get(i),FriendChatUid); //메시지 보낸 사람을 제외한 참가자들의 노드의 배지카운트 세팅
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void setOpenMessage(String messageText, String type){
        DatabaseReference mChatReference = database.getReference("openChatRoom").child(OpenChatRoom);

        HashMap message = new HashMap();
        message.put("name", currentName);
        message.put("email", currentEmail);
        message.put("text", messageText);
        message.put("type", type);
        message.put("photo",currentPhoto);
        message.put("time", formattedDate);
        message.put("key", currentUid);
        mChatReference.push().setValue(message);
    }

    public void resetBadgeCount(final String key, final String FriendChatUid){
        FriendChatRoom.child(key).child(FriendChatUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("badgeCount")) {

                    long currentCountGroup = (long) dataSnapshot.child("badgeCount").getValue();
                    currentCountGroup++;

                    FriendChatRoom.child(key).child(FriendChatUid).child("badgeCount").setValue(currentCountGroup, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            Log.d("FCM 로그(그룹) => 현재접속자", currentUid + "방참가자 : " + key + "방이름 : " + FriendChatUid);
                        }//그룹채팅의 경우 배지카운트 노드는 방 생성과 동시에 참가자 모두에게 생성되므로 조건 따로 필요없음
                    });
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void resetTimeStamp(String key, String FriendChatUid) { // 타임스탬프 시간값 최신값으로 Reset 함수
        FriendChatRoom.child(key).child(FriendChatUid).child("timestamp").setValue(formattedDate);
    }

    public void setLastMessage(String key, String FriendChatUid, String lastMessage, String type){
        FriendChatRoom.child(key).child(FriendChatUid).child("lastMessage").setValue(lastMessage);
        FriendChatRoom.child(key).child(FriendChatUid).child("type").setValue(type);
        // 채팅방 목록화면에서 채팅메시지 참조를 위해 메시지 입력 시 채팅방 노드에도 마지막메시지만 갱신
    }

    public void checkAccount(Task<AuthResult> task, EditText email, EditText password){ // Firebase auth의 예외처리 에러코드를 이용하여 계정 유효성 체크
        String errorCode = ((FirebaseAuthException) task.getException()).getErrorCode();

        switch (errorCode) {
            case "ERROR_INVALID_CUSTOM_TOKEN":
                Toast.makeText(getApplicationContext(), "The custom token format is incorrect. Please check the documentation.", Toast.LENGTH_LONG).show();
                break;

            case "ERROR_CUSTOM_TOKEN_MISMATCH":
                Toast.makeText(getApplicationContext(), "The custom token corresponds to a different audience.", Toast.LENGTH_LONG).show();
                break;

            case "ERROR_INVALID_CREDENTIAL":
                Toast.makeText(getApplicationContext(), "The supplied auth credential is malformed or has expired.", Toast.LENGTH_LONG).show();
                break;

            case "ERROR_INVALID_EMAIL":
                Toast.makeText(getApplicationContext(), "The email address is badly formatted.", Toast.LENGTH_LONG).show();
                email.setError("잘못된 형식의 이메일 입니다.");
                email.requestFocus();
                break;

            case "ERROR_WRONG_PASSWORD":
                Toast.makeText(getApplicationContext(), "The password is invalid or the user does not have a password.", Toast.LENGTH_LONG).show();
                password.setError("비밀번호가 틀렸습니다.");
                password.requestFocus();
                password.setText("");
                break;

            case "ERROR_USER_MISMATCH":
                Toast.makeText(getApplicationContext(), "The supplied credentials do not correspond to the previously signed in user.", Toast.LENGTH_LONG).show();
                break;

            case "ERROR_REQUIRES_RECENT_LOGIN":
                Toast.makeText(getApplicationContext(), "This operation is sensitive and requires recent authentication. Log in again before retrying this request.", Toast.LENGTH_LONG).show();
                break;

            case "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL":
                Toast.makeText(getApplicationContext(), "An account already exists with the same email address but different sign-in credentials. Sign in using a provider associated with this email address.", Toast.LENGTH_LONG).show();
                break;

            case "ERROR_EMAIL_ALREADY_IN_USE":
                Toast.makeText(getApplicationContext(), "The email address is already in use by another account.   ", Toast.LENGTH_LONG).show();
                email.setError("동일한 이메일이 이미 사용중입니다.");
                email.requestFocus();
                break;

            case "ERROR_CREDENTIAL_ALREADY_IN_USE":
                Toast.makeText(getApplicationContext(), "This credential is already associated with a different user account.", Toast.LENGTH_LONG).show();
                break;

            case "ERROR_USER_DISABLED":
                Toast.makeText(getApplicationContext(), "The user account has been disabled by an administrator.", Toast.LENGTH_LONG).show();
                break;

            case "ERROR_USER_TOKEN_EXPIRED":
                Toast.makeText(getApplicationContext(), "The user\\'s credential is no longer valid. The user must sign in again.", Toast.LENGTH_LONG).show();
                break;

            case "ERROR_USER_NOT_FOUND":
                Toast.makeText(getApplicationContext(), "There is no user record corresponding to this identifier. The user may have been deleted.", Toast.LENGTH_LONG).show();
                email.setError("가입된 사용자가 아닙니다. 가입을 진행해 주세요.");
                email.requestFocus();
                break;

            case "ERROR_INVALID_USER_TOKEN":
                Toast.makeText(getApplicationContext(), "The user\\'s credential is no longer valid. The user must sign in again.", Toast.LENGTH_LONG).show();
                break;

            case "ERROR_OPERATION_NOT_ALLOWED":
                Toast.makeText(getApplicationContext(), "This operation is not allowed. You must enable this service in the console.", Toast.LENGTH_LONG).show();
                break;

            case "ERROR_WEAK_PASSWORD":
                Toast.makeText(getApplicationContext(), "The given password is invalid.", Toast.LENGTH_LONG).show();
                password.setError("비밀번호는 6글자 이상으로 만들어 주세요.");
                password.requestFocus();
                break;

        }
    }


}
