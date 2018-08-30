package com.example.home.mytalk.Adapter;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.example.home.mytalk.Activity.ChatActivity;
import com.example.home.mytalk.Activity.MapActivity;
import com.example.home.mytalk.Activity.TabActivity;
import com.example.home.mytalk.R;
import com.example.home.mytalk.Utils.CircleTransform;
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

import static android.content.ContentValues.TAG;
import static android.content.Context.MODE_PRIVATE;

/**
 * Created by anandbose on 09/06/15.
 */
public class FriendAdapterExpandable extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final int HEADER = 0;
    public static final int CHILD = 1;
    private Context context;
    private List<Item> data;
    private DatabaseReference DeleteRef;
    private DatabaseReference DeleteFriendRef;

    public FriendAdapterExpandable(List<Item> data, Context context) {
        this.data = data;
        this.context =  context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int type) {

        switch (type) {
            case HEADER:
                LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View headerView = inflater.inflate(R.layout.list_header, parent, false);
                ListHeaderViewHolder header = new ListHeaderViewHolder(headerView);
                return header;
            case CHILD:
                LayoutInflater inflater2 = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View childView = inflater2.inflate(R.layout.list_child, parent, false);
                ListHeaderViewHolder child = new ListHeaderViewHolder(childView);
                return child;
        }
        return null;
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        final Item item = data.get(position);
        SharedPreferences sharedPreferences = context.getSharedPreferences("email", MODE_PRIVATE);
        final String currentUid = sharedPreferences.getString("uid", "");

        switch (item.type) {
            case HEADER:
                final ListHeaderViewHolder listHeaderViewHolder = (ListHeaderViewHolder) holder;
                listHeaderViewHolder.refferalItem = item;
                listHeaderViewHolder.tvName.setText(item.name);
                listHeaderViewHolder.tvJoinUser.setText(item.state);

                // 유저사진
                if (TextUtils.isEmpty(item.photo)) {
                    Glide.with(context)
                            .load(R.drawable.ic_unknown_user)
                            .transform(new CircleTransform(context), new FitCenter(context)).into(listHeaderViewHolder.ivUser);
                } else {
                    Glide.with(context)
                            .load(item.photo)
                            .dontAnimate()
                            .transform(new CircleTransform(context), new FitCenter(context)).into(listHeaderViewHolder.ivUser);
                }

                // 접속상태
                String state = listHeaderViewHolder.tvJoinUser.getText().toString();
                if (state.equals("접속중")) {
                    listHeaderViewHolder.tvJoinUser.setTextColor(0xFF04D924);
                } else {
                    listHeaderViewHolder.tvJoinUser.setTextColor(0xFFF12922);
                }

                // 확장버튼
                if (item.invisibleChildren == null) {
                    listHeaderViewHolder.btn_expand_toggle.setImageResource(R.drawable.ic_arrow_up);
                } else {
                    listHeaderViewHolder.btn_expand_toggle.setImageResource(R.drawable.ic_arrow_down);
                }
                listHeaderViewHolder.btn_expand_toggle.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (item.invisibleChildren == null) {
                            item.invisibleChildren = new ArrayList<Item>();
                            int count = 0;
                            int pos = data.indexOf(listHeaderViewHolder.refferalItem);
                            while (data.size() > pos + 1 && data.get(pos + 1).type == CHILD) {
                                item.invisibleChildren.add(data.remove(pos + 1));
                                count++;
                            }
                            notifyItemRangeRemoved(pos + 1, count);
                            listHeaderViewHolder.btn_expand_toggle.setImageResource(R.drawable.ic_arrow_down);
                        } else {
                            int pos = data.indexOf(listHeaderViewHolder.refferalItem);
                            int index = pos + 1;
                            for (Item i : item.invisibleChildren) {
                                data.add(index, i);
                                index++;
                            }
                            notifyItemRangeInserted(pos + 1, index - pos - 1);
                            listHeaderViewHolder.btn_expand_toggle.setImageResource(R.drawable.ic_arrow_up);
                            item.invisibleChildren = null;
                        }
                    }
                });

                // 친추상태
                DatabaseReference AcceptpRef = FirebaseDatabase.getInstance().getReference("AddFriend")
                        .child(currentUid)
                        .child(item.key);
                AcceptpRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //친추노드의 현재 접속유저 UID노드 하위노드인 accept 노드를 탐색해서
                        if (dataSnapshot.hasChildren()) {
                            String accept = dataSnapshot.child("accept").getValue().toString();
                            Log.d(TAG, "Accept : " + accept);
                            if (accept.equals("N")) {    //N값이면 친추받아서 승인 이전 상태이므로 button_lay -> VISIBLE
                                listHeaderViewHolder.button_lay.setVisibility(View.VISIBLE);
                            } else {   //else (= F)이면 승인된 상태이므로 button_lay -> GONE  (레이아웃 객체까지 제거)
                                listHeaderViewHolder.button_lay.setVisibility(View.GONE);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                // 수락버튼
                listHeaderViewHolder.bt_agree.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        DatabaseReference SendRef = FirebaseDatabase.getInstance().getReference("AddFriend")
                                .child(currentUid)
                                .child(item.key).child("accept");
                        SendRef.setValue("F");
                        //버튼레이 사라지면서 친추노드에 F값 세팅.
                        TabActivity.translateAnim(0, 1f, 0, 0, 1000,listHeaderViewHolder.button_lay );
                    }
                });

                //거절버튼
                listHeaderViewHolder.bt_disagree.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        //AddFriend 노드 에서 현재 접속 유저의 자식노드(친추된 유저)를 삭제
                        DeleteRef = FirebaseDatabase.getInstance().getReference("AddFriend")
                                .child(currentUid)
                                .child(item.key);
                        DeleteRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                dataSnapshot.getRef().removeValue();  //해당 유저의 친추 데이터베이스 노드 제거
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                        DeleteFriendRef = FirebaseDatabase.getInstance().getReference("AddFriend")
                                .child(item.key)
                                .child(currentUid);
                        DeleteFriendRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                dataSnapshot.getRef().removeValue();  //친구 하위노드의 내 정보 삭제
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                });
                break;

            case CHILD:
                final ListHeaderViewHolder viewHolder = (ListHeaderViewHolder) holder;

                // 채팅버튼
                viewHolder.bt_chat.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String FriendChatUid = item.key;
                        Intent intent = new Intent(context, ChatActivity.class);
                        intent.putExtra("FriendChatUid", FriendChatUid);
                        context.startActivity(intent);

                        Calendar calendar = Calendar.getInstance();
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA);
                        String formattedDate = simpleDateFormat.format(calendar.getTime());
                        HashMap room = new HashMap();
                        room.put("seen", true);
                        room.put("timestamp", formattedDate);
                        room.put("Roomtype", "OneToOne");
                        room.put("badgeCount", 0);
                        DatabaseReference chatReference = FirebaseDatabase.getInstance().getReference("friendChatRoom");
                        chatReference.child(currentUid).child(FriendChatUid).setValue(room);
                    }
                });

                // 위치찾기버튼
                viewHolder.bt_map.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, MapActivity.class);
                        double lat = Double.parseDouble(item.latitude);
                        double lon = Double.parseDouble(item.longitude);
                        intent.putExtra("latitude", lat);
                        intent.putExtra("longitude", lon);
                        context.startActivity(intent);
                    }
                });
                break;
        }
    }

    @Override
    public int getItemViewType(int position) {
        return data.get(position).type;
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    private static class ListHeaderViewHolder extends RecyclerView.ViewHolder {

        public ImageView btn_expand_toggle;
        public Item refferalItem;
        public TextView tvName;
        public ImageView ivUser;
        public TextView tvJoinUser;
        public ImageButton bt_map;
        public ImageButton bt_chat;
        public Button bt_agree;
        public Button bt_disagree;
        public RelativeLayout button_lay;

        public ListHeaderViewHolder(View itemView) {
            super(itemView);
            btn_expand_toggle = (ImageView) itemView.findViewById(R.id.btn_expand_toggle);
            tvName = (TextView)itemView.findViewById(R.id.tvEmail);
            ivUser = (ImageView)itemView.findViewById(R.id.ivUser);
            tvJoinUser = (TextView)itemView.findViewById(R.id.tvJoinUser);
            bt_map = (ImageButton)itemView.findViewById(R.id.bt_map);
            bt_chat = (ImageButton)itemView.findViewById(R.id.bt_chat);
            bt_agree = (Button)itemView.findViewById(R.id.button_agree);
            bt_disagree = (Button)itemView.findViewById(R.id.button_disagree);
            button_lay = (RelativeLayout)itemView.findViewById(R.id.button_lay);
        }
    }

    public static class Item {
        public int type;
        private String name; //name
        private String photo;
        private String state;
        public List<Item> invisibleChildren;
        private String latitude;
        private String longitude;
        private String key;

        public Item() {
        }

        public Item(int type, String name, String photo, String state, String latitude, String longitude, String key) {
            this.type = type;
            this.name = name;
            this.photo = photo;
            this.state = state;
            this.latitude = latitude;
            this.longitude = longitude;
            this.key = key;
        }
    }
}
