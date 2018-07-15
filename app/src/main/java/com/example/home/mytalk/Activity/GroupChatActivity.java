package com.example.home.mytalk.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.home.mytalk.Adapter.GroupChatAdapter;
import com.example.home.mytalk.Model.Friend;
import com.example.home.mytalk.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

public class GroupChatActivity extends AppCompatActivity {

    private static final String TAG = "GroupChatActivity";
    public RecyclerView mRecyclerView;
    public LinearLayoutManager mLayoutManager;
    private List<Friend> mFriend;
    private GroupChatAdapter gAdapter;
    private String currentUid;
    private String currentUserName;
    private String currentUserPhoto;
    private String currentEmail;
    private ValueEventListener mRootValueListener;
    private ValueEventListener mFriendValueListener;
    private ValueEventListener mUserValueListener;
    public String formattedDate;
    public String inviteUserNum;
    public List<String> inviteList;
    public String invUser;
    public String invite;
    public HashMap AddHashMap;
    private DatabaseReference mRootRef;
    private DatabaseReference roomNodeReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(0xFF22CEF1));
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mRecyclerView = (RecyclerView) findViewById(R.id.rvFriend);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        SharedPreferences sharedPreferences = getSharedPreferences("email",MODE_PRIVATE );
        currentEmail = sharedPreferences.getString("email", "");
        currentUid = sharedPreferences.getString("uid","");

        roomNodeReference = FirebaseDatabase.getInstance().getReference().child("friendChatRoom");
        getRootReference();
        getCurrentUserValue();

        //생성된 그룹방에 추가 인원 초대를 위한 구간
        Intent intent = getIntent();
        invite = intent.getStringExtra("invite");
        if(!TextUtils.isEmpty(invite)) {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("friendChatRoom")
                    .child(currentUid).child(invite).child("join");
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    inviteList = new ArrayList<>();
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        invUser = postSnapshot.getValue().toString();
                        inviteList.add(invUser);
                    }
                    mFriend = new ArrayList<>();
                    gAdapter = new GroupChatAdapter(mFriend,inviteList,getApplicationContext());
                    //이미 생성된 그룹방에서 추가 초대를 위해 호출된 액티비티 조건식이므로 2번째 인자값으로
                    //현재 초대된 인원들 리스트를 넘겨주어 어댑터에서 이미 초대된 인원은 체크박스 disable
                    mRecyclerView.setAdapter(gAdapter);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        mFriend = new ArrayList<>();
        gAdapter = new GroupChatAdapter(mFriend, null, this);
        // 신규 그룹방 생성이므로 2번쨰 인자값인 초대한 유저리스트는 null
        mRecyclerView.setAdapter(gAdapter);
        //새 리스트 만들고 리스트에 나타낼 재료들 준비하고 어댑터 객체생성후 어댑터 연결
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_button, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA);
            formattedDate = simpleDateFormat.format(calendar.getTime());

            int id = item.getItemId();
            if (id == R.id.action_button_ok) {
                final List<String> checkUser = new ArrayList<>();
                final List<String> checkPhoto = new ArrayList<>();
                final List<String> checkKey = new ArrayList<>();
                checkUser.add(currentUserName);
                checkKey.add(currentUid);
                checkPhoto.add(currentUserPhoto);
                //리스트 생성과 동시에 현재접속자의 정보는 리스트에 삽입.
                mFriend = gAdapter.getFriendList();
                for (int i = 0; i < mFriend.size(); i++) {
                    Friend friend = mFriend.get(i);
                    if (friend.isCheck()) {
                        checkUser.add(friend.getName());
                        checkKey.add(friend.getKey());
                        checkPhoto.add(friend.getPhoto());
                        //체크된 유저들의 정보를 리스트에 삽입
                    }
                }

                switch (checkKey.size()) { //그룹 채팅방 인원 수(체크인원 + 나)
                    case 3:
                        inviteUserNum = "3People";
                        break;
                    case 4:
                        inviteUserNum = "4People";
                        break;
                    default:
                        inviteUserNum = String.valueOf(checkKey.size()) + "People";
                }
                for (int i = 0; i < checkKey.size(); i++) {

                    HashMap checkedUser = new HashMap();
                    //checkedUser.put("seen", false);
                    checkedUser.put("timestamp", formattedDate);
                    checkedUser.put("join", checkUser);
                    checkedUser.put("joinUserPhoto", checkPhoto);
                    checkedUser.put("joinUserKey", checkKey);
                    checkedUser.put("badgeCount", 0);

                    if (TextUtils.isEmpty(invite)) { //추가 인원 초대를 위한 액티비티 호출이 아닐 경우
                        if (checkKey.size() <= 2) {//채팅방 인원이 2인 이하면  1:1채팅이므로 1:1채팅형태로 노드 생성

                            // 1:1채팅은 친구리스트에서 대화시작버튼으로도 생성되므로 그룹 초대 리스트에서
                            // 1인선택시 이미 1:1대화방생성되있으면 그 대화방으로 이동
                            checkKey.remove(currentUid);
                            String current_user_ref = "oneToOneMessage/" + currentUid + "/" + checkKey.get(i);
                            String chat_user_ref = "oneToOneMessage/" + checkKey.get(i) + "/" + currentUid;
                            DatabaseReference ChatReference = FirebaseDatabase.getInstance().getReference("oneToOneMessage").child(currentUid).child(checkKey.get(i)).child(formattedDate);
                            String push_id = ChatReference.getKey();

                            HashMap message = new HashMap();
                            message.put("name", currentUid);
                            message.put("email", currentEmail );
                            message.put("text", null);
                            message.put("photo", currentUserPhoto);
                            message.put("time", formattedDate);
                            message.put("seen", false);

                            HashMap messageUserMap = new HashMap();
                            messageUserMap.put(current_user_ref + "/" + push_id, message);
                            messageUserMap.put(chat_user_ref + "/" + push_id, message);

                            mRootRef = FirebaseDatabase.getInstance().getReference();
                            mRootRef.child("friendChatRoom").child(currentUid).child(checkKey.get(i)).child("seen").setValue(true);
                            mRootRef.child("friendChatRoom").child(currentUid).child(checkKey.get(i)).child("timestamp").setValue(formattedDate);
                            mRootRef.child("friendChatRoom").child(currentUid).child(checkKey.get(i)).child("Roomtype").setValue("OneToOne");

                            mRootRef.child("friendChatRoom").child(checkKey.get(i)).child(currentUid).child("seen").setValue(true);
                            mRootRef.child("friendChatRoom").child(checkKey.get(i)).child(currentUid).child("timestamp").setValue(formattedDate);
                            mRootRef.child("friendChatRoom").child(checkKey.get(i)).child(currentUid).child("Roomtype").setValue("OneToOne");

                        }else{ //2인보다 많으면 그룹채팅이므로 그룹채팅 노드 생성

                            DatabaseReference GroupReference_Current = FirebaseDatabase.getInstance().getReference().child("friendChatRoom").child(currentUid);
                            GroupReference_Current.child("Group@" + "+" + inviteUserNum + "+" + currentUid + "+" + formattedDate).setValue(checkedUser);

                            DatabaseReference GroupReference_Invite = FirebaseDatabase.getInstance().getReference().child("friendChatRoom").child(checkKey.get(i));
                            GroupReference_Invite.child("Group@" + "+" + inviteUserNum + "+" + currentUid + "+" + formattedDate).setValue(checkedUser);

                        }
                    }else{ //추가 인원 초대를 위한 액티비티 호출일 경우
                        if(invite.contains("Group@")) { //그룹방에서 호출된 초대창일 경우에만
                            List<String> MergeListKey = new ArrayList<>();
                            List<String> MergeListUser = new ArrayList<>();
                            List<String> MergeListPhoto = new ArrayList<>();
                            AddHashMap = new HashMap<>();

                            makeMergeList("join", checkUser, MergeListUser, currentUserName, checkKey);
                            makeMergeList("joinUserKey", checkKey, MergeListKey, currentUid, checkKey);
                            makeMergeList("joinUserPhoto", checkPhoto, MergeListPhoto, currentUserPhoto, checkKey); //초대인원을 포함한 새 리스트생성
                        }
                    }
                }

                if(!TextUtils.isEmpty(invite)) {  //시스템 메시지는 생성되어있는 대화방으로의 초대 경우에만 호출
                    checkUser.remove(currentUserName);
                    setSystemMessage(checkUser);
                }

                this.finish();
                return true;
            }

        return super.onOptionsItemSelected(item);
    }

    private void setSystemMessage(List<String> joinUser){
        if(joinUser.size() > 0) {
            HashMap message = new HashMap();
            message.put("text", joinUser + "님이 대화에 참가했습니다.");
            message.put("name", "System");
            message.put("email", "System");
            message.put("photo", "System");
            message.put("type","System");
            message.put("time", formattedDate);
            message.put("key", "System");
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("groupMessage")
                    .child(invite);
            databaseReference.push().setValue(message);
        }
    }

    private void getRootReference() {
        //최상위 루트노드를 참조하면 users 노드의 로그 인/아웃 데이터 변화 감지되므로 사용자가 접속하면 친추목록참조(AddFriend)에서
        //친추목록값 contact를 받아오고 users노드의 사용자 정보값 가져옴.
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference().getRoot();
        mRootValueListener = rootRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild("AddFriend")) {
                    String Uid = currentUid;
                    if (dataSnapshot.child("AddFriend").getValue().toString().contains(Uid)) {
                        getFriebaseAddUserReference();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getFriebaseAddUserReference() {

        DatabaseReference FriendRef = FirebaseDatabase.getInstance().getReference("AddFriend").child(currentUid);

        mFriendValueListener = FriendRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {   //추가된 친구가 있으면
                    String contact = dataSnapshot.getValue().toString(); //추가된 친구의 UID정보(= 추가된 친구 목록)가져옴
                    getFirebaseUserReference(contact); //친추목록 참조값을 가져오면 유저정보값을 참조해서 가져옴
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void getFirebaseUserReference(final String contact) {

        DatabaseReference UserDataRef = FirebaseDatabase.getInstance().getReference("users");
        mUserValueListener = UserDataRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mFriend.clear();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Friend friend = postSnapshot.getValue(Friend.class);
                    String key = friend.getKey();
                    if (!TextUtils.isEmpty(contact) && contact.contains(key)) {
                        mFriend.add(friend);
                        if (key.equals(currentUid)) {
                            mFriend.remove(friend);
                        }
                        gAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getCurrentUserValue(){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users").child(currentUid);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Friend friend = dataSnapshot.getValue(Friend.class);
                currentUserName = friend.getName();
                currentUserPhoto = friend.getPhoto();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void makeMergeList(final String valueNode, final List<String> addList, final List<String> MergeList, final String currentValue, final List<String> KeyList){
        roomNodeReference.child(currentUid).child(invite).child(valueNode).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                addList.remove(currentValue);
                MergeList.clear();
                MergeList.addAll(addList);
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    String value = postSnapshot.getValue().toString();
                    MergeList.add(value);
                }
                Log.d("Merge", String.valueOf(MergeList));
                setMergeList(valueNode, MergeList,KeyList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });  //추가 인원이 포함된 MergeList생성
    }

    private void setMergeList(final String valueNode, final List<String> mergeList, final List<String> keyList){
        roomNodeReference.child(currentUid).child(invite).child("joinUserKey").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    String key = postSnapshot.getValue().toString();
                    roomNodeReference.child(key).child(invite).child(valueNode).setValue(mergeList);
                }
                roomNodeReference.child(currentUid).child(invite).child(valueNode).setValue(mergeList);
                setHashMap(valueNode, keyList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setHashMap(String valueNode, final List<String> keyList){
        DatabaseReference Merge = FirebaseDatabase.getInstance().getReference().child("friendChatRoom").child(currentUid).child(invite);
        Merge.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                AddHashMap = (HashMap) dataSnapshot.getValue();
                for(int index = 0; index<keyList.size(); index++){
                    DatabaseReference hashMapRef = FirebaseDatabase.getInstance().getReference().child("friendChatRoom").child(keyList.get(index)).child(invite);
                    hashMapRef.setValue(AddHashMap);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        }); //추가 초대된 인원들의 노드에 채팅방 노드 추가
    }


    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onResume() {
        super.onResume();

    }
}