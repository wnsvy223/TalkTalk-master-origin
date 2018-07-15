package com.example.home.mytalk.Adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.example.home.mytalk.Activity.MapActivity;
import com.example.home.mytalk.Model.Friend;
import com.example.home.mytalk.R;
import com.example.home.mytalk.Utils.CircleTransform;
import com.example.home.mytalk.Utils.RoundedTransformation;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.List;

import static android.content.Context.MODE_PRIVATE;


public class FriendMapAdapter extends RecyclerView.Adapter<FriendMapAdapter.ViewHolder> {

    private List<Friend> mFriend;
    public Context context;
    private String stPhoto;
    private Query conversationQuery;
    private Query groupConversationQuery;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView tvName;
        public ImageView ivUser;
        public TextView tvJoinUser;
        public TextView message;

        public ViewHolder(View item) {
            super(item);

            tvName = (TextView)item.findViewById(R.id.tvEmail);
            ivUser = (ImageView)item.findViewById(R.id.ivUser);
            tvJoinUser = (TextView)item.findViewById(R.id.tvJoinUser);
            message = (TextView)item.findViewById(R.id.message);
        }
    }

    public FriendMapAdapter(List<Friend> mFriend, Context context ) {
        this.mFriend = mFriend;
        this.context = context;
    }


    @Override
    public FriendMapAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                       int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_friend_map, parent, false);


        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        if(holder.getAdapterPosition() != RecyclerView.NO_POSITION) {
            String ID = mFriend.get(position).getName();
            holder.tvName.setText(ID);
            holder.tvJoinUser.setText(mFriend.get(position).getState());
            String s = holder.tvJoinUser.getText().toString();
            if (s.equals("접속중")) {
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
                StorageReference storageReference = FirebaseStorage.getInstance()
                        .getReference()
                        .child("users").child(mFriend.get(holder.getAdapterPosition()).getKey() + ".jpg");
                Glide.with(context)
                        .using(new FirebaseImageLoader())
                        .load(storageReference)
                        .transform(new CircleTransform(context), new FitCenter(context)).into(holder.ivUser);
                //이미지 로딩 속도 개선
            }
            holder.ivUser.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //사진클릭시 해당 유저의 위치로 17레벨 줌으로 맵 카메라 1회 이동(animateCamera - 1회 포커싱  , moveCamera - 지속적으로 포커싱)
                    LatLng latLng = new LatLng(
                            Double.parseDouble(mFriend.get(holder.getAdapterPosition()).getLatitude())
                            , Double.parseDouble(mFriend.get(holder.getAdapterPosition()).getLongitude()));
                    ((MapActivity) context).mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17.0f));

                }
            });

            SharedPreferences sharedPreferences = context.getSharedPreferences("email", MODE_PRIVATE);
            final String currentUid = sharedPreferences.getString("uid", "");

            Query conversationQuery = FirebaseDatabase.getInstance().getReference().child("friendChatRoom").child(currentUid);
            conversationQuery.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    String roomName = dataSnapshot.getKey(); //방 목록

                    getMessage(roomName, currentUid, holder);
                    Log.d("테스트", String.valueOf(roomName));
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
        }
    }

    // Return the size of your dataset (invoked by the layout manager)

    @Override
    public int getItemCount() {
        return mFriend.size();
    }

    private void getMessage(String key, String currentUid, final ViewHolder holder){


        groupConversationQuery = FirebaseDatabase.getInstance().getReference().child("groupMessage").child(key).limitToLast(1);
        groupConversationQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String chat = dataSnapshot.child("text").getValue().toString();
                Log.d("그룹메시지", chat);
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

        conversationQuery = FirebaseDatabase.getInstance().getReference().child("oneToOneMessage").child(currentUid).child(key).limitToLast(1);
        conversationQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String chat = dataSnapshot.child("text").getValue().toString();
                Log.d("1:1메시지", chat);
                String fromUser = dataSnapshot.child("from").getValue().toString();

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
    }

}

