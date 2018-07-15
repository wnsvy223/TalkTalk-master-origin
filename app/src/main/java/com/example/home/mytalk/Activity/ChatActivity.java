package com.example.home.mytalk.Activity;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.home.mytalk.Adapter.ChatAdapter;
import com.example.home.mytalk.Adapter.ImageAdapter;
import com.example.home.mytalk.Model.Chat;
import com.example.home.mytalk.Model.Friend;
import com.example.home.mytalk.R;
import com.example.home.mytalk.Service.MyLocationService;
import com.example.home.mytalk.Service.UploadService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE;


public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "ChatActivity";
    public EditText etText;
    public Button button_send;
    public ImageButton buttonSendFile;
    public ImageView imageView_back;
    public RecyclerView mRecyclerView;
    public ChatAdapter mChatAdapter;
    public RecyclerView.LayoutManager mLayoutManager;
    public List<Chat> mChat;
    private FirebaseDatabase database;
    private String currentEmail;
    private String currentUid;
    private String currentName;
    private String currentPhoto;
    public String formattedDate;
    public SharedPreferences sharedPreferences_img;
    public ImageAdapter imageAdapter;
    private static int Set_position;
    public static final int UPLOAD_REQUEST_CODE_IMAGE = 1;
    public static final int UPLOAD_REQUEST_CODE_VIDEO = 2;
    private static final int ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 3;
    public Context mContext;
    public static String OpenChatRoom;
    private String FriendChatUid;
    private DatabaseReference ChatDisplayReference;
    private DatabaseReference ChatReference;
    private DatabaseReference mRootRef;
    private DatabaseReference FriendChatRoom;
    private ProgressBar uploadProgress;
    private TextView progressStatus;
    private TextView sendImage;
    private TextView sendVideo;
    public RelativeLayout root_view;
    public ActionBar actionBar;
    public Switch locationSwitch;
    private SharedPreferences locationSwitchPref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(0xFF22CEF1));
        mContext = this;

        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        button_send = (Button) findViewById(R.id.send);
        button_send.setVisibility(View.GONE);
        uploadProgress = (ProgressBar) findViewById(R.id.uploadProgress);
        uploadProgress.setVisibility(View.GONE);
        progressStatus = (TextView) findViewById(R.id.progressStatus);
        progressStatus.setVisibility(View.GONE);
        etText = (EditText) findViewById(R.id.ettext);
        buttonSendFile = (ImageButton) findViewById(R.id.set);
        sendImage = (TextView) findViewById(R.id.sendImage);
        sendImage.setVisibility(View.GONE);
        sendVideo = (TextView) findViewById(R.id.sendVideo);
        sendVideo.setVisibility(View.GONE);
        root_view = (RelativeLayout) findViewById(R.id.root_view);
        locationSwitch = (Switch)findViewById(R.id.locSwitch);
        locationSwitch.setVisibility(View.GONE);

        database = FirebaseDatabase.getInstance();
        FriendChatRoom = FirebaseDatabase.getInstance().getReference("friendChatRoom");

        SharedPreferences sharedPreferences = getSharedPreferences("email", MODE_PRIVATE); //유저정보 저장된 프레퍼런스
        currentUid = sharedPreferences.getString("uid", "");
        currentEmail = sharedPreferences.getString("email", "");
        locationSwitchPref = getSharedPreferences("switch", MODE_PRIVATE); //위치정보스위치 상태값
        boolean switchStatus = locationSwitchPref.getBoolean("locStatus",false);
        Intent intent = getIntent();
        OpenChatRoom = intent.getStringExtra("OpenChat"); //공개방으로 부터 생성된 채팅액티비티
        FriendChatUid = intent.getStringExtra("FriendChatUid"); //1:1 또는 그룹 채팅방으로 부터 생성된 채팅액티비티

        String MyKey = intent.getStringExtra("MyKey");
        if (MyKey != null) {
            currentUid = MyKey;
        } //푸시 메시지를 클릭해서 생성된 채팅 액티비티이면 푸시메시지로부터 넘어온 Uid정보로 화면 생성

        if(switchStatus){
            locationSwitch.setChecked(true);
        }else{
            locationSwitch.setChecked(false);
        } //쉐어드프레퍼런스에 저장된 bool값에 의해 위치정보제공자가 켜져있으면 체크된상태, 켜져있지않으면 체크안된상태로 생성.


        getCurrentValue(); // 채팅방 유저 이름, 사진 참조값
        goSendFile(); // 사진, 동영상 전송을 위해 각 버튼 활성화 함수
        goSendImage(); // 사진 메시지 전송을 위한 갤러리 호출 함수
        goSendVideo(); // 동영상 메시지 전송을 위한 갤러리 호출 함수
        sendButtonHide(); // 채팅창 메시지 없을때  전송 버튼 숨김기능 함수
        setActionbarTitle(); //액션바에 참가인원수 표시
        setOnLocSwitch(); //위치정보제공 스위치

        /*
        imageView_back = (ImageView) findViewById(R.id.back_ground);
        sharedPreferences_img = PreferenceManager.getDefaultSharedPreferences(this);

        if(sharedPreferences_img != null) {
            Set_position = sharedPreferences_img.getInt("img_set", -1);
            if (Set_position != -1) {
                imageView_back.setBackgroundResource(Set_position);
            }else{
                imageView_back.setBackgroundColor(Color.rgb(255,249,159));
            }
        }
        */

        // 최상단 뷰에 TreeObserver를 이용해 키보드의 화면표시 상태를 루트뷰의 높이값 변화로 감지
        root_view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int mRootViewHeight = root_view.getRootView().getHeight();
                int mRelativeRootViewHeight = root_view.getHeight();
                int mDiff = mRootViewHeight - mRelativeRootViewHeight;
                if (mDiff > dpTopx(200) && mRecyclerView.getScrollState() == SCROLL_STATE_IDLE) {
                    mRecyclerView.scrollToPosition(mChat.size() - 1);
                    //키보드가 올라오면 스크롤 위치를 채팅메시지 최하단으로 이동
                }
            }
        });

        button_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String stText = etText.getText().toString();
                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA);
                formattedDate = simpleDateFormat.format(calendar.getTime());

                if (stText.equals("") || stText.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "내용을 입력하세요", Toast.LENGTH_SHORT).show();
                } else {
                    etText.setText("");
                    if (OpenChatRoom != null) {    //공개채팅방  Message Node Set
                        setOpenMessage(stText, "text");
                    } else if (FriendChatUid != null) {
                        if (FriendChatUid.contains("Group@")) { //채팅 리스트 노드 참조값에 Group 문자가 포함되어 있으면 그룹채팅
                            setGroupMessage(stText, "text");
                        } else { //채팅 리스트 노드 참조값에 Group 문자가 포함되어있지 않으면 1:1 채팅
                            setOneToOneMessage(stText, "text");
                        }

                    }
                }
            }
        });
        // 채팅 메시지 함수들의 메시지 해시맵 setValue 함수에서 각 메시지 자식노드구분을
        // 타임스탬프 --> push() 로 바꾼 이유 : 타임스탬프를 사용하면 초단위보다 빠르게 메시지를 작성할경우 구분을 하지못해서
        // 너무 빠르게 채팅메시지를 입력할 경우 이전 메시지가 전송버튼을 눌렀음에도 적용되지않는 경우 발생.


        //------------------------------------채팅화면 생성되면 메시지들 참조하는 부분--------------------------------//
        if (!TextUtils.isEmpty(OpenChatRoom)) { //채팅액티비티로 넘어올때 인텐트값이 공개방 생성값이면 공개채팅 참조
            ChatDisplayReference = database.getReference("openChatRoom").child(OpenChatRoom);
        } else if (!TextUtils.isEmpty(FriendChatUid)) { //채팅액티비티로 넘어올때 인텐트값이 친구대화방 생성값이면 친구대화방 참조
            if (FriendChatUid.contains("Group@")) {
                ChatDisplayReference = database.getReference("groupMessage").child(FriendChatUid); // 그룹 메시지 참조
            } else {
                ChatDisplayReference = database.getReference("oneToOneMessage").child(currentUid).child(FriendChatUid); // 1:1 메시지 참조
            }
        }
        ChatDisplayReference.addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Chat chat = dataSnapshot.getValue(Chat.class);
                mChat.add(chat);
                mRecyclerView.scrollToPosition(mChat.size() - 1);
                //mRecyclerView.smoothScrollToPosition(mChat.size() - 1);
                mChatAdapter.notifyItemInserted(mChat.size());
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mChat = new ArrayList<>();
        mChatAdapter = new ChatAdapter(mChat, currentEmail, mContext);
        mRecyclerView.setAdapter(mChatAdapter);

    }

    private void goSendFile() {
        buttonSendFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendImage.setVisibility(View.VISIBLE);
                sendVideo.setVisibility(View.VISIBLE);
                locationSwitch.setVisibility(View.VISIBLE);
                mRecyclerView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        sendImage.setVisibility(View.GONE);
                        sendVideo.setVisibility(View.GONE);
                        locationSwitch.setVisibility(View.GONE);
                        return false;
                    }

                });
                checkPermission(); //윈도우 매니저 권한체크
            }
        });
    }

    private void setOnLocSwitch(){
        locationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked){
                    startService(new Intent(getApplicationContext(), MyLocationService.class));
                    Toast.makeText(getApplicationContext(),"백그라운드 위치정보를 제공을 시작합니다.",Toast.LENGTH_SHORT).show();
                    SharedPreferences.Editor editor = locationSwitchPref.edit();
                    editor.clear();
                    editor.putBoolean("locStatus",true);
                    editor.apply();
                    //위치정보 제공 스위치 ON시 쉐어드프레퍼런스에 상태값 ON으로 저장.
                }else{
                    stopService(new Intent(getApplicationContext(), MyLocationService.class));
                    Toast.makeText(getApplicationContext(),"백그라운드 위치정보 제공이 중지되었습니다.",Toast.LENGTH_SHORT).show();
                    SharedPreferences.Editor editor = locationSwitchPref.edit();
                    editor.clear();
                    editor.putBoolean("locStatus",false);
                    editor.apply();
                    //위치정보 제공 스위치 OFF시 쉐어드프레퍼런스에 상태값 OFF로 저장.
                }
            }
        });
    }

    private void goSendImage() {
        sendImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, UPLOAD_REQUEST_CODE_IMAGE);
                sendImage.setVisibility(View.GONE);
                sendVideo.setVisibility(View.GONE);
                locationSwitch.setVisibility(View.GONE);
            }
        });

    }

    private void goSendVideo() {
        sendVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, UPLOAD_REQUEST_CODE_VIDEO);
                sendImage.setVisibility(View.GONE);
                sendVideo.setVisibility(View.GONE);
                locationSwitch.setVisibility(View.GONE);
            }
        });
    }

    private void sendButtonHide() {
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String message = editable.toString();
                if (!message.isEmpty())
                    button_send.setVisibility(View.VISIBLE);
                else
                    button_send.setVisibility(View.INVISIBLE);
            }
        };
        etText.addTextChangedListener(textWatcher);
    }

    public float dpTopx(float valueInDp) {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, valueInDp, metrics);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (FriendChatUid != null && FriendChatUid.contains("Group@")) {
            getMenuInflater().inflate(R.menu.menu_invite, menu); //그룹채팅일때만 초대 버튼 활성화
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_button_invite) {
            //대화 상대 추가 초대
            Intent intent = new Intent(getApplicationContext(), GroupChatActivity.class);
            intent.putExtra("invite", FriendChatUid);
            startActivity(intent);

        }
        return super.onOptionsItemSelected(item);
    }

    private void getCurrentValue() {
        DatabaseReference nameReference = database.getReference("users").child(currentUid);
        nameReference.addValueEventListener(new ValueEventListener() {
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


    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == UPLOAD_REQUEST_CODE_IMAGE && resultCode == RESULT_OK) { //사진 업로드
            //사진 업로드 백그라운드 서비스 시작
            Intent intent = new Intent(getApplicationContext(), UploadService.class);
            intent.putExtra("storagePath", "message_images");
            intent.putExtra("fileType", ".jpg");
            intent.putExtra("messageType", "image");
            intent.putExtra("progressText", "사진 전송중");
            intent.putExtra("uri",data.getData());
            intent.putExtra("room",FriendChatUid);
            startService(intent); // 갤러리로 부터 받은 데이터들과 각 타입 구분 String 값을 업로드 서비스 클래스에 전달 및 실행
        } else if (requestCode == UPLOAD_REQUEST_CODE_VIDEO && resultCode == RESULT_OK) { // 동영상 업로드
            //동영상 업로드 백그라운드 서비스 시작
            Intent intent = new Intent(getApplicationContext(), UploadService.class);
            intent.putExtra("storagePath", "message_video");
            intent.putExtra("fileType", ".mp4");
            intent.putExtra("messageType", "video");
            intent.putExtra("progressText", "동영상 전송중");
            intent.putExtra("uri",data.getData());
            intent.putExtra("room",FriendChatUid);
            startService(intent);  // 갤러리로 부터 받은 데이터들과 각 타입 구분 String 값을 업로드 서비스 클래스에 전달 및 실행
        } else if (requestCode == ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE) { // 화면 매니저 권한
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(getApplicationContext(), "화면매니저 권한 거부되었습니다.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "화면매니저 권한 승인되었습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setOneToOneMessage(String messageText, String type) {

        String current_user_ref = "oneToOneMessage/" + currentUid + "/" + FriendChatUid;
        String chat_user_ref = "oneToOneMessage/" + FriendChatUid + "/" + currentUid;
        ChatReference = database.getReference("oneToOneMessage");
        String push_id = ChatReference.getKey();

        HashMap message = new HashMap();
        message.put("name", currentName);
        message.put("email", currentEmail);
        message.put("text", messageText);
        message.put("type", type);
        message.put("photo", currentPhoto);
        message.put("time", formattedDate);
        message.put("seen", false);
        message.put("from", currentUid);

        HashMap messageUserMap = new HashMap();
        messageUserMap.put(current_user_ref + "/" + push_id, message);
        messageUserMap.put(chat_user_ref + "/" + push_id, message);

        mRootRef = database.getReference();
        mRootRef.child("friendChatRoom").child(currentUid).child(FriendChatUid).child("seen").setValue(true);
        mRootRef.child("friendChatRoom").child(currentUid).child(FriendChatUid).child("timestamp").setValue(formattedDate);
        mRootRef.child("friendChatRoom").child(currentUid).child(FriendChatUid).child("Roomtype").setValue("OneToOne");
        ChatReference.child(currentUid).child(FriendChatUid).push().setValue(message);

        mRootRef.child("friendChatRoom").child(FriendChatUid).child(currentUid).child("seen").setValue(true);
        mRootRef.child("friendChatRoom").child(FriendChatUid).child(currentUid).child("timestamp").setValue(formattedDate);
        mRootRef.child("friendChatRoom").child(FriendChatUid).child(currentUid).child("Roomtype").setValue("OneToOne");
        ChatReference.child(FriendChatUid).child(currentUid).push().setValue(message);

        // 해당유저와 1:1채팅 메시지를 전송하면 해당유저와의 대화창 목록을
        // FriendChatRoom노드에 추가해서 리스트에 표시되도록 하는 형태.

        Log.d("테스트",  FriendChatUid);
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

    private void setGroupMessage(String messageText, String type) {
        ChatReference = database.getReference("groupMessage").child(FriendChatUid);
        HashMap message = new HashMap();
        message.put("name", currentName);
        message.put("email", currentEmail);
        message.put("text", messageText);
        message.put("type", type);
        message.put("photo", currentPhoto);
        message.put("time", formattedDate);
        message.put("key", currentUid);
        ChatReference.push().setValue(message);

        FriendChatRoom.child(currentUid).child(FriendChatUid).child("joinUserKey").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        List<String> MergeListKey = new ArrayList<>();
                        if (dataSnapshot.hasChildren()) {
                            MergeListKey.clear();
                            for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                String key = postSnapshot.getValue().toString();
                                resetTimeStamp(key);
                                // 채팅이 쓰여질때마다 현재 채팅방의 참가자들의 키값을 받아와서
                                // 각 참가자들의 채팅방 노드의 타임스템프값을 현재시간으로 바꿔주어
                                // 타임스탬프값이 가장 최근인 채팅방을 맨 위쪽으로 정렬.

                                if(!key.equals(currentUid)){
                                    MergeListKey.add(key);
                                    for(int i=0; i<MergeListKey.size(); i++){
                                        resetBadgeCount(MergeListKey.get(i)); //메시지 보낸 사람을 제외한 참가자들의 노드의 배지카운트 세팅
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

    private void setOpenMessage(String messageText, String type){
        ChatReference = database.getReference("openChatRoom").child(OpenChatRoom);

        HashMap message = new HashMap();
        message.put("name", currentName);
        message.put("email", currentEmail);
        message.put("text", messageText);
        message.put("type", type);
        message.put("photo",currentPhoto);
        message.put("time", formattedDate);
        message.put("key", currentUid);
        ChatReference.push().setValue(message);
    }

    private void resetBadgeCount(final String key){
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

    private void resetTimeStamp(String key) { // 타임스탬프 시간값 최신값으로 재세팅 함수
        FriendChatRoom
                .child(key)
                .child(FriendChatUid)
                .child("timestamp")
                .setValue(formattedDate);
    }


    private void setActionbarTitle(){
        if(!TextUtils.isEmpty(OpenChatRoom)){
            actionBar.setTitle("공개채팅");
        }else {
            FriendChatRoom.child(currentUid)
                    .child(FriendChatUid)
                    .child("join").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (FriendChatUid.contains("Group@")) {
                        long count = dataSnapshot.getChildrenCount();
                        actionBar.setTitle("그룹채팅" + count);
                        //카운트값을 받아와서 액션바에 세팅. join 노드의 하위요소 카운트값(참가인원 수)이므로 인원변경 시 변경값으로 자동세팅
                    } else {
                        actionBar.setTitle("1:1채팅");
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        sharedPreferences_img = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences_img.edit();
        editor.putInt("img_set", Set_position);
        editor.apply();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        sharedPreferences_img = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences_img.edit();
        editor.putInt("img_set", Set_position);
        editor.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
     /*
        if(Set_position != -1){
            imageView_back.setBackgroundResource(Set_position);
        }
     */

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Toast.makeText(getApplicationContext(), "온뉴인탠트호출",Toast.LENGTH_SHORT).show();
        if(intent != null) {
            setIntent(intent);
            /*
            int position = intent.getIntExtra("id", 0);
            imageAdapter = new ImageAdapter(this);
            Set_position = imageAdapter.mThumbIds[position];
            */

        }
    }

    public void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {   // 마시멜로우 이상일 경우
            if (!Settings.canDrawOverlays(this)) {              // 체크
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE);
            }
        }
    }

}



