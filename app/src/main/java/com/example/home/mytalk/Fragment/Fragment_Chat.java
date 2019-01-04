package com.example.home.mytalk.Fragment;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.example.home.mytalk.Activity.ChatActivity;
import com.example.home.mytalk.Model.Chat;
import com.example.home.mytalk.R;
import com.example.home.mytalk.Utils.CircleTransform;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import de.hdodenhof.circleimageview.CircleImageView;
import me.leolin.shortcutbadger.ShortcutBadger;
import static android.content.Context.MODE_PRIVATE;

public class Fragment_Chat extends android.support.v4.app.Fragment {

    public RecyclerView mRecyclerView;
    public LinearLayoutManager mLayoutManager;
    public String TAG = getClass().getSimpleName();
    private DatabaseReference mConversationMe;
    private DatabaseReference mConversationFriend;
    private DatabaseReference mMessageDatabase;
    private DatabaseReference mUsersDatabase;
    private DatabaseReference mGroupMessageDatabase;
    private DatabaseReference mSystemMessageDatabase;
    private String currentUid;
    private List<String> newListKey;
    private List<String> newListName;
    private List<String> newListPhoto;
    public String currentName;
    public String currentPhoto;
    private String NodeUid;
    public Intent intent;
    public ActivityManager activityManager;
    public int type;
    private FirebaseRecyclerAdapter<Chat,ChatViewHolder> firebaseConvAdapter;
    public DisplayMetrics metrics;


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        mRecyclerView = (RecyclerView)view.findViewById(R.id.ChatRoom);

        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);
        //Firebase RealTimeDatabase에는 내림차순 정렬이 없음. 그래서 위 함수2줄로 레이아웃에서 역순정렬 설정.
        mRecyclerView.setLayoutManager(mLayoutManager);


        metrics = new DisplayMetrics();
        WindowManager mgr = (WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE);
        mgr.getDefaultDisplay().getMetrics(metrics); //기기별 dpi값 가져옴
        Log.d("TAG", "디바이스DPI = " + metrics.densityDpi);

        return view;
    }

    @Override
    public void onStop() {
        super.onStop();
        firebaseConvAdapter.cleanup();
    }

    @Override
    public void onStart() {
        super.onStart(); //FirebaseUI는 onStart()에 구현.

        activityManager = (ActivityManager)getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("email",MODE_PRIVATE );
        currentUid = sharedPreferences.getString("uid", "");

        mConversationMe = FirebaseDatabase.getInstance().getReference().child("friendChatRoom").child(currentUid);
        mConversationMe.keepSynced(true);
        mConversationFriend = FirebaseDatabase.getInstance().getReference().child("friendChatRoom");
        mConversationFriend.keepSynced(true);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("users");
        mUsersDatabase.keepSynced(true);
        mMessageDatabase = FirebaseDatabase.getInstance().getReference().child("oneToOneMessage").child(currentUid);
        mGroupMessageDatabase = FirebaseDatabase.getInstance().getReference().child("groupMessage");
        getCurrentValue();

        Query conversationQuery = mConversationMe.orderByChild("timestamp");
        firebaseConvAdapter = new FirebaseRecyclerAdapter<Chat, ChatViewHolder>(
                Chat.class,
                R.layout.list_friendchat_user,
                ChatViewHolder.class,
                conversationQuery)
        {

            @Override
            public ChatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

                //mRecyclerView.getRecycledViewPool().setMaxRecycledViews(4,0); // 2번째 인자를 0으로 주어 뷰 생성시에 재사용방지
                //mRecyclerView.getRecycledViewPool().setMaxRecycledViews(0,0);
                if(viewType == 4){ //4명 또는 그 이상
                    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_friendchat_groupuser_four,parent,false);
                    return new ChatViewHolder(view);
                }else{
                    View view2 = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_friendchat_user,parent,false);
                    return new ChatViewHolder(view2);
                }
            }


            @Override
            public int getItemViewType(int position) {
                NodeUid = getRef(position).getKey();

                if(NodeUid.contains("Group")){
                    return 4;
                }else{
                    return 0;
                }

            }

            // firebase ui 에서는 이미 생성자에서 query로 참조 부분이 있기 때문에 같은 참조위치를 populateViewHolder 에서 새로 참조해서
            // 값을 가져올 경우 뷰 위치 꼬임현상 발생.
            @Override
            protected void populateViewHolder(final ChatViewHolder viewHolder, Chat chat, int position) {

                final String list_room_id = getRef(viewHolder.getAdapterPosition()).getKey();
                if (viewHolder.getAdapterPosition() != RecyclerView.NO_POSITION) {
                    viewHolder.setTimeStamp(chat.getTimestamp()); // 타임스탬프 세팅
                    viewHolder.setBadgeCount(String.valueOf(chat.getBadgeCount())); // 배지카운트 세팅
                    viewHolder.setMessage(chat.getLastMessage(),chat.getType()); // 마지막 메시지 세팅 및 타입값
                    if (list_room_id.contains("Group")) {
                        viewHolder.setUserNameGroup(chat.getJoin(), currentName); // 참가자 리스트
                        viewHolder.setUserNum(String.valueOf(chat.getJoinUserKey().size())); // 참가자 수
                        List<String> joinUserKey = chat.getJoinUserKey();
                        joinUserKey.remove(currentUid); // 리스트에서 내 키값은 삭제
                        viewHolder.setGroupPhoto(joinUserKey, getContext()); // 참가자 프로필사진 세팅
                    }else{
                        mUsersDatabase.child(list_room_id).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.hasChildren()) {
                                    String userName = dataSnapshot.child("name").getValue().toString();
                                    viewHolder.setName(userName);
                                    String userPhoto = dataSnapshot.child("photo").getValue().toString();
                                    if(isValidContextForGlide(getContext())) {
                                        viewHolder.setUserImage(userPhoto, getContext());
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }


/*
                    mConversationMe.child(list_room_id).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild("badgeCount")) {
                                String count = dataSnapshot.child("badgeCount").getValue().toString();
                                viewHolder.setBadgeCount(count);
                                Log.d("채팅방배찌", count);
                                if (!TextUtils.isEmpty(list_room_id)) {
                                    List<ActivityManager.RunningTaskInfo> taskInfo = activityManager.getRunningTasks(1);
                                    ComponentName topActivity = taskInfo.get(0).topActivity;
                                    if (topActivity.getClassName().equals("com.example.home.mytalk.Activity.ChatActivity")) {
                                        mConversationMe.child(list_room_id).child("badgeCount").setValue(0);
                                    }
                                    // 현재 띄워진 액티비티의 값을 받아와서 채팅액티비티가 띄워져 있으면
                                    // 현재 들어온 채팅방의 배지카운트 0으로 세팅(이미 보고있으니 배지카운트가 올라갈 필요없음.)
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
*/

                    //채팅방 이동
                    viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            mConversationMe.child(list_room_id).child("joinUserKey").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Intent intent = new Intent(getActivity(), ChatActivity.class);
                                    intent.putExtra("FriendChatUid", list_room_id);//채팅방제목 키값
                                    intent.putExtra("position", viewHolder.getAdapterPosition());
                                    List<String> unReadUserLIst = (List<String>) dataSnapshot.getValue();
                                    intent.putStringArrayListExtra("unReadUserList", (ArrayList<String>) unReadUserLIst);// 참가자들 목록
                                    startActivity(intent);
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });


                            mConversationMe.child(list_room_id).child("badgeCount").setValue(0); //채팅방 리스트 뱃지카운트 리셋
                            mConversationMe.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    long totalCount = 0;
                                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                        if (postSnapshot.hasChild("badgeCount")) {
                                            long count = (long) postSnapshot.child("badgeCount").getValue();
                                            totalCount += count;
                                        }
                                    }
                                    ShortcutBadger.applyCount(getActivity(), (int) totalCount);
                                    // 탭 배지와 마찬가지로 아이콘 배지는 배지 총합값,
                                    // 해당 채팅방에 들어가면 그 채팅방에 있던 배지수만큼 아이콘뱃지에서 차감되어 다시 표시
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                        }
                    });

                    //채팅방 나가기
                    viewHolder.mView.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View view) {

                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                            builder.setTitle("채팅방 나가기");
                            builder.setMessage("채팅방을 나가시겠습니까?");
                            builder.setPositiveButton("확인",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (list_room_id.contains("Group")) {
                                                //해당 채팅방에 참가된 유저들의 키값을 받아 Key ->그룹방노드->joinUser에 내 이름 삭제해야함
                                                newListKey = new ArrayList<>();
                                                newListName = new ArrayList<>();
                                                newListPhoto = new ArrayList<>();

                                                makeNewList(list_room_id, "join", currentName, newListName);
                                                makeNewList(list_room_id, "joinUserPhoto", currentPhoto, newListPhoto);
                                                makeNewList(list_room_id, "joinUserKey", currentUid, newListKey);

                                                mConversationMe.child(list_room_id).child("joinUserKey").addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        if (dataSnapshot.hasChildren()) {
                                                            newListKey.clear();
                                                            for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                                                String key = postSnapshot.getValue().toString();
                                                                newListKey.add(key);
                                                                if (key.equals(currentUid)) {
                                                                    newListKey.remove(key);
                                                                }
                                                                setNewList(key, list_room_id, "joinUserKey", newListKey);
                                                                setNewList(key, list_room_id, "join", newListName);
                                                                setNewList(key, list_room_id, "joinUserPhoto", newListPhoto);
                                                            }
                                                            removeNode(list_room_id, mConversationMe); //내 채팅리스트에서 해당 채팅목록 삭제
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError) {

                                                    }
                                                });
                                                setSystemMessage(list_room_id, "groupMessage", null); //유저 퇴장 시스템메시지
                                            } else {
                                                removeNode(list_room_id, mConversationMe); //1:1 대화목록에서 MY NODE 삭제
                                                removeNode(list_room_id, mMessageDatabase); //1:1 채팅에서 MY 메시지 삭제
                                                setSystemMessage(list_room_id, "oneToOneMessage", currentUid); //유저 퇴장 시스템메시지
                                            }
                                        }
                                    });
                            builder.setNegativeButton("취소",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    });
                            builder.show();

                            return true;
                        }
                    });
                }
            }
        };

        mRecyclerView.setAdapter(firebaseConvAdapter);

    }


    public static class ChatViewHolder extends RecyclerView.ViewHolder {

        private View mView;

        private ChatViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setMessage(String message, String type){
            TextView messageView = (TextView) mView.findViewById(R.id.user_last_message);
            if(TextUtils.isEmpty(message) || TextUtils.isEmpty(type)){
                messageView.setVisibility(View.INVISIBLE);
            }else {
                messageView.setVisibility(View.VISIBLE);
                switch (type) {
                    case "image":
                        messageView.setText("(사진)");
                        break;
                    case "video":
                        messageView.setText("(동영상)");
                        break;
                    case "text":
                        messageView.setText(message);
                        break;
                    case "System":
                        messageView.setText(message);
                        break;
                    default:
                }
            }
        }

        public void setName(String name){
            TextView userNameView = (TextView) mView.findViewById(R.id.user_group_name);
            if(TextUtils.isEmpty(name)){
                userNameView.setVisibility(View.INVISIBLE);
            }else{
                userNameView.setVisibility(View.VISIBLE);
                userNameView.setText("["+name+"]");
            }
        }

        public void setUserNum(String num){
            TextView userNum = (TextView)mView.findViewById(R.id.textUserNum);
            if(num.equals("2") || TextUtils.isEmpty(num) ){
                userNum.setVisibility(View.INVISIBLE);
            }else{
                userNum.setVisibility(View.VISIBLE);
                userNum.setText(num);
            }
        }

        public void setUserImage(String thumb_image, Context context) {
            if(context != null) {
                CircleImageView userImageView = (CircleImageView) mView.findViewById(R.id.user_single_image);
                //Glide.clear(userImageView);
                //Glide.get(context).clearMemory();
                if (TextUtils.isEmpty(thumb_image)) {
                    Glide.with(context)
                            .load(R.drawable.ic_unknown_user)
                            .dontAnimate()
                            .transform(new CircleTransform(context), new FitCenter(context)).into(userImageView);
                } else {
                    Glide.with(context)
                            .load(thumb_image)
                            .dontAnimate()
                            .transform(new CircleTransform(context), new FitCenter(context)).into(userImageView);
                }
            }
        }

        public void setUserNameGroup(List<String> name, String myName){
            TextView userNameView = (TextView) mView.findViewById(R.id.user_group_name);
            if(name == null || name.size() <= 0){
                userNameView.setVisibility(View.INVISIBLE);
            }else {
                userNameView.setVisibility(View.VISIBLE);
                for (int i = 0; i < name.size(); i++) {
                    if (name.get(i).equals(myName)) {
                        name.remove(myName);
                    }
                }
                userNameView.setText(String.valueOf(name));
            }
        }

        // 리사이클러뷰 재사용시 VISIBLE/INVISIBLE 처리는 바로 적용되는데, setXY, setTranslatinXY같은 뷰 이동 메소드는
        // 뷰홀더 바인딩 시 바로 적용이안되고 뷰가 꼬임. ( => 이미지뷰를 추가해서 INVISIBLE처리후 위치가 변경/추가된 이미지에 세팅하는방법으로 임시 구현
        public void setGroupPhoto(List<String> joinUserKey, Context context){
            CircleImageView userImageView0 = userImageView0 = (CircleImageView) mView.findViewById(R.id.user_single_image1);
            CircleImageView userImageView1 = userImageView1 = (CircleImageView) mView.findViewById(R.id.user_single_image2);
            CircleImageView userImageView2 = userImageView2 = (CircleImageView) mView.findViewById(R.id.user_single_image3);
            CircleImageView userImageView3 = userImageView3 = (CircleImageView) mView.findViewById(R.id.user_single_image4);
            CircleImageView userImageViewTemp1 = userImageViewTemp1 = (CircleImageView) mView.findViewById(R.id.user_single_image_temp1);
            CircleImageView userImageViewTemp2 = userImageViewTemp2 = (CircleImageView) mView.findViewById(R.id.user_single_image_temp2);
            CircleImageView userImageViewTemp3 = userImageViewTemp3 = (CircleImageView) mView.findViewById(R.id.user_single_image_temp3);
            CircleImageView userImageViewTempOne = userImageViewTempOne = (CircleImageView) mView.findViewById(R.id.user_single_image_temp_one);

            CircleImageView arrayUserImage[] = {userImageView0,userImageView1,userImageView2,userImageView3,
                    userImageViewTemp1, userImageViewTemp2, userImageViewTemp3, userImageViewTempOne};

            for(CircleImageView circleImageView : arrayUserImage){
                circleImageView.setVisibility(View.INVISIBLE); // 배열안의 모든 이미지뷰 invisible
            }
            if(joinUserKey.size() >= 4) {
                arrayUserImage[0].setVisibility(View.VISIBLE);
                arrayUserImage[1].setVisibility(View.VISIBLE);
                arrayUserImage[2].setVisibility(View.VISIBLE);
                arrayUserImage[3].setVisibility(View.VISIBLE);
                for (int i = 0; i < 4; i++) {
                    setImage(joinUserKey.get(i), context, arrayUserImage, i , joinUserKey.size());
                }
            }else{
                switch (joinUserKey.size()){
                    case 3:
                        arrayUserImage[0].setVisibility(View.VISIBLE);
                        arrayUserImage[1].setVisibility(View.VISIBLE);
                        arrayUserImage[2].setVisibility(View.INVISIBLE);
                        arrayUserImage[3].setVisibility(View.INVISIBLE);
                        arrayUserImage[4].setVisibility(View.VISIBLE); // temp_1번
                        break;
                    case 2:
                        arrayUserImage[0].setVisibility(View.INVISIBLE);
                        arrayUserImage[1].setVisibility(View.INVISIBLE);
                        arrayUserImage[5].setVisibility(View.VISIBLE); //temp_2번
                        arrayUserImage[6].setVisibility(View.VISIBLE); //temp_3번
                        break;
                    case 1:
                        arrayUserImage[0].setVisibility(View.INVISIBLE);
                        arrayUserImage[7].setVisibility(View.VISIBLE); // temp_one
                        break;
                    default:
                        arrayUserImage[0].setVisibility(View.VISIBLE);
                        arrayUserImage[1].setVisibility(View.VISIBLE);
                        arrayUserImage[2].setVisibility(View.VISIBLE);
                        arrayUserImage[3].setVisibility(View.VISIBLE);
                }

                for (int i = 0; i < joinUserKey.size(); i++) {
                    setImage(joinUserKey.get(i), context, arrayUserImage, i , joinUserKey.size());
                }
            }
        }

        public void setTimeStamp(String time){
            TextView TimeStamp = (TextView) mView.findViewById(R.id.time_stamp);
            if(TextUtils.isEmpty(time)){
                TimeStamp.setVisibility(View.INVISIBLE);
            }else {
                TimeStamp.setVisibility(View.VISIBLE);
                TimeStamp.setText(time);
            }
        }

        public void setUserOnline(String online_status) {
            ImageView userOnlineView = (ImageView) mView.findViewById(R.id.user_single_online_icon);
            if(online_status.equals("true")){
                userOnlineView.setVisibility(View.VISIBLE);
            } else {
                userOnlineView.setVisibility(View.INVISIBLE);
            }
        }

        public void setBadgeCount(String count){
            TextView badgeCount = (TextView) mView.findViewById(R.id.badgecount);
            if(!count.equals("0")) {
                badgeCount.setVisibility(View.VISIBLE);
                badgeCount.setText(count);
            }else{
                badgeCount.setVisibility(View.INVISIBLE);
            }
        }

        private void setImage(String key, final Context context, final CircleImageView arrayUserImage[], final int i, final int size){

            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");
            databaseReference.child(key).child("photo").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String image = dataSnapshot.getValue().toString();
                    setGlideImage(image,context,arrayUserImage,i);
                    switch (size){
                        case 3:
                            setGlideImage(image,context,arrayUserImage,i,2);
                            break;
                        case 2:
                            setGlideImage(image,context,arrayUserImage,i,5);
                            break;
                        case 1:
                            setGlideImage(image,context,arrayUserImage,i,7);
                            break;
                        default:
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        private void setGlideImage(String image, Context context, CircleImageView arrayUserImage[], int i){
            if(TextUtils.isEmpty(image)){
                Glide.with(context)
                        .load(R.drawable.ic_unknown_user)
                        .dontAnimate()
                        .transform(new CircleTransform(context), new FitCenter(context)).into(arrayUserImage[i]);
            }else {
                Glide.with(context)
                        .load(image)
                        .dontAnimate()
                        .transform(new CircleTransform(context), new FitCenter(context)).into(arrayUserImage[i]);
                // 리스트의 각 원소들의 값(참가 유저들의 키값)의 사진값을 Glide를 사용하여
                // CircleImageView 배열에 하나씩 순서대로 넣음
                // 리스너 내부에서 세팅되기때문에 해당유저가 프로필사진 변경시 채팅목록의 해당유저의 이미지뷰도 갱신
            }
        }

        private void setGlideImage(String image, Context context, CircleImageView arrayUserImage[], int i, int moveIndex){
            if(TextUtils.isEmpty(image)){
                Glide.with(context)
                        .load(R.drawable.ic_unknown_user)
                        .dontAnimate()
                        .transform(new CircleTransform(context), new FitCenter(context)).into(arrayUserImage[i + moveIndex]);
            }else {
                Glide.with(context)
                        .load(image)
                        .dontAnimate()
                        .transform(new CircleTransform(context), new FitCenter(context)).into(arrayUserImage[i + moveIndex]);
                // 리스트의 각 원소들의 값(참가 유저들의 키값)의 사진값을 Glide를 사용하여
                // CircleImageView 배열에 하나씩 순서대로 넣음
                // 리스너 내부에서 세팅되기때문에 해당유저가 프로필사진 변경시 채팅목록의 해당유저의 이미지뷰도 갱신
            }
        }


    }

    private void setSystemMessage(String listRoom, String roomType, String myNode ){
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA);
        String formattedDate = simpleDateFormat.format(calendar.getTime());

        if(roomType.equals("groupMessage")){
            mSystemMessageDatabase = FirebaseDatabase.getInstance().getReference("groupMessage").child(listRoom);
        }else {
            mSystemMessageDatabase = FirebaseDatabase.getInstance().getReference("oneToOneMessage").child(listRoom).child(myNode);
        }
        String messageID = mSystemMessageDatabase.push().getKey();

        HashMap message = new HashMap();
        message.put("text", "[" + currentName + "]" + "님이 대화방을 나갔습니다.");
        message.put("name", "System");
        message.put("email", "System");
        message.put("type","System");
        message.put("photo","System");
        message.put("time", formattedDate);
        message.put("key","System");
        message.put("unReadCount", 0);
        message.put("unReadUserList", null);
        message.put("messageID", messageID);
        mSystemMessageDatabase.child(messageID).setValue(message);
    }

    public void getCurrentValue(){
        mUsersDatabase.child(currentUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentName = dataSnapshot.child("name").getValue().toString();
                currentPhoto = dataSnapshot.child("photo").getValue().toString();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void makeNewList(String listNode, String valueNode, final String currentUserData, final List<String> newList){
        mConversationMe.child(listNode).child(valueNode).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren()){
                    newList.clear();
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        String newData = postSnapshot.getValue().toString();
                        newList.add(newData);
                        if(newData.equals(currentUserData)){
                            newList.remove(newData);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void setNewList(String key, String listNode, String valueNode, final List<String> newList){
        mConversationFriend.child(key).child(listNode).child(valueNode).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                dataSnapshot.getRef().setValue(newList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void removeNode(String listNode, DatabaseReference databaseReference){
        databaseReference.child(listNode).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                dataSnapshot.getRef().removeValue();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        }); //내가 참가했던 해당 채팅방 리스트 및 노드 삭제.(= 난 채팅방을 나갔기때문에 리스트와 데이터베이스 노드가 삭제되야함)

    }

    public static boolean isValidContextForGlide(final Context context) {
        if (context == null) {
            return false;
        }
        if (context instanceof Activity) {
            final Activity activity = (Activity) context;
            if (activity.isDestroyed() || activity.isFinishing()) {
                return false;
            }
        }
        return true;
    }

}