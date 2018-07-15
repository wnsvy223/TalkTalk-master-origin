package com.example.home.mytalk.Adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.signature.StringSignature;
import com.example.home.mytalk.Activity.ChatActivity;
import com.example.home.mytalk.Activity.FullScreenActivity;
import com.example.home.mytalk.Activity.MapActivity;
import com.example.home.mytalk.Activity.TabActivity;
import com.example.home.mytalk.Model.Friend;
import com.example.home.mytalk.R;
import com.example.home.mytalk.Utils.CircleTransform;
import com.example.home.mytalk.Utils.RoundedTransformation;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static android.content.ContentValues.TAG;
import static android.content.Context.MODE_PRIVATE;


public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.ViewHolder> {

    private List<Friend> mFriend;
    public Context context;
    private String stPhoto;
    private DatabaseReference DeleteRef;
    private DatabaseReference DeleteFriendRef;
    private String currentUid;


    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView tvName;
        public ImageView ivUser;
        public TextView tvJoinUser;
        public ImageButton bt_map;
        public ImageButton bt_chat;
        public ImageButton bt_q;
        public RelativeLayout back_lay;
        public RelativeLayout front_lay;
        public RelativeLayout button_lay;
        public Button bt_agree;
        public Button bt_disagree;

        private ViewHolder(View item) {
            super(item);

            tvName = (TextView)item.findViewById(R.id.tvEmail);
            ivUser = (ImageView)item.findViewById(R.id.ivUser);
            tvJoinUser = (TextView)item.findViewById(R.id.tvJoinUser);
            bt_map = (ImageButton)item.findViewById(R.id.bt_map);
            bt_chat = (ImageButton)item.findViewById(R.id.bt_chat);
            bt_q = (ImageButton)item.findViewById(R.id.bt_q);
            back_lay = (RelativeLayout)item.findViewById(R.id.back_lay);
            front_lay = (RelativeLayout)item.findViewById(R.id.front_lay);
            button_lay = (RelativeLayout)item.findViewById(R.id.button_lay);
            bt_agree = (Button)item.findViewById(R.id.button_agree);
            bt_disagree = (Button)item.findViewById(R.id.button_disagree);

        }
    }

    public FriendAdapter(List<Friend> mFriend, Context context) {
        this.mFriend = mFriend;
        this.context = context;

    }


    @Override
    public FriendAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                       int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_friend, parent, false);

        ViewHolder vh = new ViewHolder(v);

        return vh;

    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        if(holder.getAdapterPosition() != RecyclerView.NO_POSITION) {
            //*******onBindViewHolder내에서 리스트 위치값 받아올땐 맴버변수 position쓰면 안되고, holder.getAdapterposition()함수를 써야 정확한 위치받아옴********
            SharedPreferences sharedPreferences = context.getSharedPreferences("email", MODE_PRIVATE);
            currentUid = sharedPreferences.getString("uid", "");

            holder.button_lay.setVisibility(View.GONE);
            DatabaseReference AcceptpRef = FirebaseDatabase.getInstance().getReference("AddFriend")
                    .child(currentUid)
                    .child(mFriend.get(holder.getAdapterPosition()).getKey());

            AcceptpRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    //친추노드의 현재 접속유저 UID노드 하위노드인 accept 노드를 탐색해서
                    if (dataSnapshot.hasChildren()) {
                        String accept = dataSnapshot.child("accept").getValue().toString();
                        Log.d(TAG, "Accept : " + accept);
                        if (accept.equals("N")) {    //N값이면 친추받아서 승인 이전 상태이므로 button_lay -> VISIBLE
                            holder.button_lay.setVisibility(View.VISIBLE);
                        } else {   //else (= F)이면 승인된 상태이므로 button_lay -> GONE  (레이아웃 객체까지 제거)
                            holder.button_lay.setVisibility(View.GONE);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            holder.bt_agree.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    holder.button_lay.setVisibility(View.GONE);
                    DatabaseReference SendRef = FirebaseDatabase.getInstance().getReference("AddFriend")
                            .child(currentUid)
                            .child(mFriend.get(holder.getAdapterPosition()).getKey()).child("accept");
                    SendRef.setValue("F");
                    //버튼레이 사라지면서 친추노드에 F값 세팅.
                }
            });
            holder.bt_disagree.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    //AddFriend 노드 에서 현재 접속 유저의 자식노드(친추된 유저)를 삭제
                    DeleteRef = FirebaseDatabase.getInstance().getReference("AddFriend")
                            .child(currentUid)
                            .child(mFriend.get(holder.getAdapterPosition()).getKey());
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
                            .child(mFriend.get(holder.getAdapterPosition()).getKey())
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


            holder.tvName.setText(mFriend.get(position).getName());
            holder.tvJoinUser.setText(mFriend.get(position).getState());
            String state = holder.tvJoinUser.getText().toString();
            if (state.equals("접속중")) {
                holder.tvJoinUser.setTextColor(0xFF04D924);
            } else {
                holder.tvJoinUser.setTextColor(0xFFF12922);
            }

            stPhoto = mFriend.get(position).getPhoto();

            if (TextUtils.isEmpty(stPhoto)) {
                Glide.with(context)
                        .load(R.drawable.ic_unknown_user)
                        .transform(new CircleTransform(context), new FitCenter(context)).into(holder.ivUser);
            } else {
                Glide.with(context)
                        .load(stPhoto)
                        .dontAnimate()
                        .transform(new CircleTransform(context), new FitCenter(context)).into(holder.ivUser);
            }

            holder.back_lay.setVisibility(View.GONE);
            holder.bt_map.setClickable(false);
            holder.bt_q.setClickable(false);
            holder.bt_chat.setClickable(false);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (holder.back_lay.getVisibility() == View.VISIBLE) {
                        return;
                    }
                    TabActivity.translateAnim(1f, 0, 0, 0, 500, holder.back_lay);
                    holder.back_lay.setVisibility(View.VISIBLE);
                    holder.bt_map.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(context, MapActivity.class);
                            double lat = Double.parseDouble(mFriend.get(holder.getAdapterPosition()).getLatitude());
                            double lon = Double.parseDouble(mFriend.get(holder.getAdapterPosition()).getLongitude());
                            intent.putExtra("latitude", lat);
                            intent.putExtra("longitude", lon);
                            context.startActivity(intent);
                        }
                    });

                    holder.bt_chat.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            String FriendChatUid = mFriend.get(holder.getAdapterPosition()).getKey();
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

                    holder.bt_q.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            TabActivity.translateAnim(0, 1f, 0, 0, 500, holder.back_lay);
                            holder.front_lay.setVisibility(View.VISIBLE);
                            holder.back_lay.setVisibility(View.GONE);
                            holder.bt_map.setClickable(false);
                            holder.bt_q.setClickable(false);
                            holder.bt_chat.setClickable(false);
                        }
                    });
                }
            });
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    //롱 클릭 리스너를 아이템뷰에 달아서 길게 누르면 다이얼로그 띄워서 유저가 원할때 친추 삭제 가능하게 만들기.
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("친추 정보 변경");
                    builder.setMessage("친구를 삭제 하시겠습니까?");
                    builder.setPositiveButton("확인",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {

                                    DeleteRef = FirebaseDatabase.getInstance().getReference("AddFriend")
                                            .child(currentUid)
                                            .child(mFriend.get(holder.getAdapterPosition()).getKey());
                                    DeleteRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            dataSnapshot.getRef().removeValue();  //내 하위노드의 친구정보 삭제
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });

                                    DeleteFriendRef = FirebaseDatabase.getInstance().getReference("AddFriend")
                                            .child(mFriend.get(holder.getAdapterPosition()).getKey())
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
                    builder.setNegativeButton("취소",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });
                    builder.show();
                    return true;
                }
            });

            holder.ivUser.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String url = mFriend.get(holder.getAdapterPosition()).getPhoto();
                    Intent intent = new Intent(context, FullScreenActivity.class);
                    intent.putExtra("friendProfileImage", url);
                    context.startActivity(intent);
                }
            });
        }
    }


    @Override
    public int getItemCount() {
        return mFriend.size();
    }

}
