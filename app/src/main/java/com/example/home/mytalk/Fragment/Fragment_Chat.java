package com.example.home.mytalk.Fragment;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
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

        //mRecyclerView.setHasFixedSize(true);
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

                mRecyclerView.getRecycledViewPool().setMaxRecycledViews(4,0); // 2번째 인자를 0으로 주어 뷰 생성시에 재사용방지
                mRecyclerView.getRecycledViewPool().setMaxRecycledViews(0,0);
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

            @Override
            protected void populateViewHolder(final ChatViewHolder viewHolder, Chat chat, int position) {
                if (viewHolder.getAdapterPosition() != RecyclerView.NO_POSITION) {

                    final String list_room_id = getRef(viewHolder.getAdapterPosition()).getKey();

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

                    Query lastMessageQuery = mMessageDatabase.child(list_room_id).limitToLast(1);  //데이터 베이스 메시지 트리의 마지막메시지만 가져옴
                    lastMessageQuery.addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                            Chat chat = dataSnapshot.getValue(Chat.class);
                            String text = chat.getText();
                            String type = chat.getType();
                            String time = chat.getTime();
                            viewHolder.setMessage(text, type);
                            viewHolder.setTimeStamp(time);

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


                    Query lastMessageGroupQuery = mGroupMessageDatabase.child(list_room_id).limitToLast(1);
                    lastMessageGroupQuery.addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                            Chat chat = dataSnapshot.getValue(Chat.class);
                            String text = chat.getText();
                            String type = chat.getType();
                            String time = chat.getTime();
                            viewHolder.setMessage(text, type);
                            viewHolder.setTimeStamp(time);

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

                    mConversationMe.child(list_room_id).child("joinUserKey").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChildren()) {
                                String joinUserKey = dataSnapshot.getValue().toString();
                                long count = dataSnapshot.getChildrenCount();
                                if(isValidContextForGlide(getContext())) {
                                    viewHolder.setUserImageGroup(joinUserKey, getContext(), currentUid, count, metrics.densityDpi);
                                    //joinUserKey 노드로 부터 참가자들의 키값을 받아서 setUserImageGroup 메소드에 전달.
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


                    mConversationMe.child(list_room_id).child("join").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChildren()) {
                                String joinUser = dataSnapshot.getValue().toString();
                                viewHolder.setUserNameGroup(joinUser, currentName);
                                Log.d("그룹 참가 목록 : ", joinUser);
                                if (list_room_id.contains("Group")) {
                                    viewHolder.setUserNum(String.valueOf(dataSnapshot.getChildrenCount()));
                                    //참가유저 목록 노드를 참조해서 자식노드 수(채팅 참가 유저 수) 뷰 표시
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


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

        View mView;

        private ChatViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setMessage(String message, String type){
            TextView messageView = (TextView) mView.findViewById(R.id.user_last_message);
            switch (type){
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

        public void setName(String name){
            TextView userNameView = (TextView) mView.findViewById(R.id.user_group_name);
            userNameView.setText(name);
        }

        public void setUserNum(String num){
            TextView userNum = (TextView)mView.findViewById(R.id.textUserNum);
            userNum.setText(num);
            if(num.equals("2") ){
                userNum.setVisibility(View.INVISIBLE);
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

        public void setUserNameGroup(String name, String myName){
            TextView userNameView = (TextView) mView.findViewById(R.id.user_group_name);
            int index = name.indexOf("[");
            int index2 = name.indexOf("]");
            String split = name.substring(index + 1);
            String split2 = split.substring(0,index2 - 1);

            String userNameList[] = split2.split(", ");
            List<String> newList = new ArrayList<>();
            Collections.addAll(newList,userNameList);
            for(int i=0; i<userNameList.length - 1; i++){
                if (userNameList[i].equals(myName)){
                    newList.remove(userNameList[i]);
                }
                userNameView.append(newList.get(i) + "  ");
                //DB로 부터 받아온 참가리스트중 나의 이름은 삭제후 append를 이용해 리스트값을 텍스트뷰에 세팅
            }

        }

        public void setUserImageGroup(String user_key, Context context, String myPhoto, long count, int dpi){

            CircleImageView userImageView1 = (CircleImageView) mView.findViewById(R.id.user_single_image1);
            CircleImageView userImageView2 = (CircleImageView) mView.findViewById(R.id.user_single_image2);
            CircleImageView userImageView3 = (CircleImageView) mView.findViewById(R.id.user_single_image3);
            CircleImageView userImageView4 = (CircleImageView) mView.findViewById(R.id.user_single_image4);
            CircleImageView arrayUserImage[] = {userImageView1,userImageView2,userImageView3,userImageView4};
            arrayUserImage[0].setVisibility(View.VISIBLE);
            arrayUserImage[1].setVisibility(View.VISIBLE);
            arrayUserImage[2].setVisibility(View.VISIBLE);
            arrayUserImage[3].setVisibility(View.VISIBLE);
            TextView userNameView = (TextView) mView.findViewById(R.id.user_group_name);
            TextView userStatusView = (TextView) mView.findViewById(R.id.user_last_message);

            int index = user_key.indexOf("[");
            int index2 = user_key.indexOf("]");
            String split = user_key.substring(index + 1);
            String split2 = split.substring(0,index2 - 1);
            String userImageList[] = split2.split(", ");
            //인자로 받은 키값을 콤마 기준을 각각 하나씩 자른뒤 리스트에 넣음.
            //키값 문자열(콤마가 포함된 문자열)의 경우 split으로 자를때 콤마앞부분의 공백값이 있어 공백값을 포함해서 자름.

            List<String> newList = new ArrayList<>();
            if (context != null) {
                if (userImageList.length > 4) {   //그룹채팅 인원이 4명 넘으면 사진 4개만 받기
                    for (int i = 0; i < 4; i++) {
                        Log.d("4이상" + "사진" + i + "번", userImageList[i]);
                        Collections.addAll(newList, userImageList);
                        if (userImageList[i].equals(myPhoto)) {
                            newList.remove(userImageList[i]);
                        } //리스트 원소중 내 키값은 삭제.
                        setImage(newList.get(i), context, arrayUserImage, i);

                        switch (dpi) {
                            case 480:
                                if (arrayUserImage[0].getY() == 100) {
                                    arrayUserImage[0].setTranslationY(45);
                                }
                                if (arrayUserImage[1].getY() == 100) {
                                    arrayUserImage[1].setTranslationY(45);
                                }
                                arrayUserImage[2].setTranslationX(0);

                                break;
                            case 560:
                                if (arrayUserImage[0].getY() == 120) {
                                    arrayUserImage[0].setTranslationY(53);
                                }
                                if (arrayUserImage[1].getY() == 120) {
                                    arrayUserImage[1].setTranslationY(53);
                                }
                                arrayUserImage[2].setTranslationX(0);
                                break;
                            case 640:
                                Log.d("뷰테스트", String.valueOf(arrayUserImage[0].getY()));
                                if (arrayUserImage[0].getY() == 120) {
                                    arrayUserImage[0].setTranslationY(53);
                                }
                                if (arrayUserImage[1].getY() == 120) {
                                    arrayUserImage[1].setTranslationY(53);
                                }
                                arrayUserImage[2].setTranslationX(0);
                                break;
                            default:
                        }
                    }
                } else {  //4명 이하면 인원만큼만 가져오기
                    for (int i = 0; i < userImageList.length - 1; i++) {
                        Log.d("4이하" + "사진" + i + "번", userImageList[i] + "길이:" + count);
                        Collections.addAll(newList, userImageList);
                        if (userImageList[i].equals(myPhoto)) {
                            newList.remove(userImageList[i]);
                        }
                        setImage(newList.get(i), context, arrayUserImage, i);

                        switch (dpi) {
                            case 480:
                                if(count == 3) {  //이미지뷰2개
                                    arrayUserImage[0].getLayoutParams().height = 90;
                                    arrayUserImage[0].getLayoutParams().width = 90;
                                    arrayUserImage[1].getLayoutParams().height = 90;
                                    arrayUserImage[1].getLayoutParams().width = 90;
                                    arrayUserImage[0].setTranslationY(52);
                                    arrayUserImage[1].setTranslationY(52);
                                    userNameView.setTranslationX(-5);
                                    userStatusView.setTranslationX(-5);
                                    arrayUserImage[2].setVisibility(View.INVISIBLE);
                                    arrayUserImage[3].setVisibility(View.INVISIBLE);
                                }else if (count == 4) { //이미지뷰3개
                                    arrayUserImage[0].setTranslationY(0);
                                    arrayUserImage[1].setTranslationY(0);
                                    arrayUserImage[2].setTranslationX(52);
                                    arrayUserImage[3].setVisibility(View.INVISIBLE);
                                }else if(count == 2){ //이미지뷰1개
                                    arrayUserImage[0].setTranslationX(3);
                                    arrayUserImage[0].setTranslationY(3);
                                    arrayUserImage[0].getLayoutParams().height = 160;
                                    arrayUserImage[0].getLayoutParams().width = 160;
                                    arrayUserImage[1].setVisibility(View.INVISIBLE);
                                    arrayUserImage[2].setVisibility(View.INVISIBLE);
                                    arrayUserImage[3].setVisibility(View.INVISIBLE);
                                    userNameView.setTranslationX(-70);
                                    userStatusView.setTranslationX(-70);
                                } //채팅방 유저 이미지뷰들을 인원수에 따라 동적으로 INVISIBLE처리 및 위치 이동
                                break;
                            case 560:
                                if(count == 3) { //이미지뷰2개
                                    arrayUserImage[0].getLayoutParams().height = 110;
                                    arrayUserImage[0].getLayoutParams().width = 110;
                                    arrayUserImage[1].getLayoutParams().height = 110;
                                    arrayUserImage[1].getLayoutParams().width = 110;
                                    arrayUserImage[0].setTranslationY(55);
                                    arrayUserImage[1].setTranslationY(55);
                                    userNameView.setTranslationX(-10);
                                    userStatusView.setTranslationX(-10);
                                    arrayUserImage[2].setVisibility(View.INVISIBLE);
                                    arrayUserImage[3].setVisibility(View.INVISIBLE);
                                }else if (count == 4) {//이미지뷰3개
                                    arrayUserImage[0].setTranslationY(0);
                                    arrayUserImage[1].setTranslationY(0);
                                    arrayUserImage[2].setTranslationX(55);
                                    arrayUserImage[3].setVisibility(View.INVISIBLE);
                                }else if(count == 2){ //이미지뷰1개
                                    arrayUserImage[0].setTranslationX(5);
                                    arrayUserImage[0].setTranslationY(5);
                                    arrayUserImage[0].getLayoutParams().height = 195;
                                    arrayUserImage[0].getLayoutParams().width = 195;
                                    arrayUserImage[1].setVisibility(View.INVISIBLE);
                                    arrayUserImage[2].setVisibility(View.INVISIBLE);
                                    arrayUserImage[3].setVisibility(View.INVISIBLE);
                                    userNameView.setTranslationX(-80);
                                    userStatusView.setTranslationX(-80);
                                } //채팅방 유저 이미지뷰들을 인원수에 따라 동적으로 INVISIBLE처리 및 위치 이동
                                break;
                            case 640:
                                if(count == 3) { //이미지뷰2개
                                    Log.d("2개", String.valueOf(arrayUserImage[0].getY()));
                                    arrayUserImage[0].getLayoutParams().height = 120;
                                    arrayUserImage[0].getLayoutParams().width = 120;
                                    arrayUserImage[1].getLayoutParams().height = 120;
                                    arrayUserImage[1].getLayoutParams().width = 120;
                                    arrayUserImage[0].setTranslationY(60);
                                    arrayUserImage[1].setTranslationY(60);
                                    userNameView.setTranslationX(-10);
                                    userStatusView.setTranslationX(-10);
                                    arrayUserImage[2].setVisibility(View.INVISIBLE);
                                    arrayUserImage[3].setVisibility(View.INVISIBLE);
                                }else if (count == 4) {//이미지뷰3개
                                    Log.d("3개", String.valueOf(arrayUserImage[0].getY()));
                                    arrayUserImage[0].setTranslationY(0);
                                    arrayUserImage[1].setTranslationY(0);
                                    arrayUserImage[2].setTranslationX(60);
                                    arrayUserImage[3].setVisibility(View.INVISIBLE);
                                }else if(count == 2){ //이미지뷰1개
                                    Log.d("1개", String.valueOf(arrayUserImage[0].getY()));
                                    arrayUserImage[0].setTranslationX(5);
                                    arrayUserImage[0].setTranslationY(5);
                                    arrayUserImage[0].getLayoutParams().height = 220;
                                    arrayUserImage[0].getLayoutParams().width = 220;
                                    arrayUserImage[1].setVisibility(View.INVISIBLE);
                                    arrayUserImage[2].setVisibility(View.INVISIBLE);
                                    arrayUserImage[3].setVisibility(View.INVISIBLE);
                                    userNameView.setTranslationX(-80);
                                    userStatusView.setTranslationX(-80);
                                } //채팅방 유저 이미지뷰들을 인원수에 따라 동적으로 INVISIBLE처리 및 위치 이동
                                break;
                            default:
                        }
                    }
                }
            }

        }

        public void setTimeStamp(String time){
            TextView TimeStamp = (TextView) mView.findViewById(R.id.time_stamp);
            if(time.isEmpty()){
                TimeStamp.setText("");
            }else {
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
            }
        }

        private void setImage(String key, final Context context, final CircleImageView arrayUserImage[], final int i){

            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");
            databaseReference.child(key).child("photo").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    //Glide.clear(arrayUserImage[i]);
                    //Glide.get(context).clearMemory();
                    String image = dataSnapshot.getValue().toString();
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

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
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