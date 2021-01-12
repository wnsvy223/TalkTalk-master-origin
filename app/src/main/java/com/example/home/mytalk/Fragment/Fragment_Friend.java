package com.example.home.mytalk.Fragment;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.home.mytalk.Adapter.FriendAdapterExpandable;
import com.example.home.mytalk.Adapter.FriendAdapter;
import com.example.home.mytalk.Model.Friend;
import com.example.home.mytalk.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;
import static android.content.Context.MODE_PRIVATE;


public class Fragment_Friend extends  android.support.v4.app.Fragment {

    private static final String TAG = "FriendFragment";
    public RecyclerView mRecyclerView;
    public LinearLayoutManager mLayoutManager;
    private List<Friend> mFriend;
    private FriendAdapter mAdapter;
    private String userID;
    private ValueEventListener mFriendValueListener;
    private ValueEventListener mUserValueListener;
    CoordinatorLayout rootLayout;
    private DatabaseReference rootRef;
    private DatabaseReference FriendRef;
    private DatabaseReference UserDataRef;
    private View view;
    public Intent intent;
    public FriendAdapterExpandable friendAdapterExpandable; //확장 리사이클러뷰
    private List<FriendAdapterExpandable.Item> friendListData;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_friend, container, false);

        mRecyclerView = (RecyclerView)view.findViewById(R.id.rvFriend) ;
        mRecyclerView.setHasFixedSize(true);
        //mLayoutManager = new LinearLayoutManager(getContext());
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        rootLayout = (CoordinatorLayout)view.findViewById(R.id.coordinatorLayout);
        mFriend = new ArrayList<>();    //새 리스트 만들고
        friendListData = new ArrayList<>();

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("email",MODE_PRIVATE );
        String userID = sharedPreferences.getString("uid", "");
        getFriebaseAddUserReference(userID);

        friendAdapterExpandable = new FriendAdapterExpandable(friendListData, getContext());
        mRecyclerView.setAdapter(friendAdapterExpandable);

        return view;
    }

    private void getFriebaseAddUserReference(String userID){

            FriendRef = FirebaseDatabase.getInstance().getReference("AddFriend").child(userID);
            FriendRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.hasChildren()){   //추가된 친구가 있으면
                        String contact = dataSnapshot.getValue().toString(); //추가된 친구의 UID정보(= 추가된 친구 목록)가져옴
                        getFirebaseUserReference(contact); //친추목록 참조값을 가져오면 유저정보값을 참조해서 가져옴
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

    }

    private void getFirebaseUserReference(final String contact){

        UserDataRef = FirebaseDatabase.getInstance().getReference("users");
        UserDataRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                friendListData.clear();
                for(DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Friend friend = postSnapshot.getValue(Friend.class);
                    String key = friend.getKey();
                    if(!TextUtils.isEmpty(key) && contact.contains(key)){
                        FriendAdapterExpandable.Item friendData = new FriendAdapterExpandable.Item(FriendAdapterExpandable.HEADER,  friend.getName(),friend.getPhoto(),friend.getState(),null,null,friend.getKey());
                        friendData.invisibleChildren = new ArrayList<>();
                        friendData.invisibleChildren.add(new FriendAdapterExpandable.Item(FriendAdapterExpandable.CHILD, null,null,null,friend.getLatitude(), friend.getLongitude(),friend.getKey()));
                        friendListData.add(friendData);
                        friendAdapterExpandable.notifyDataSetChanged();
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    @Override
    public void onStart() {
        super.onStart();
        //SharedPreferences sharedPreferences = getActivity().getSharedPreferences("email",MODE_PRIVATE );
        //String uid = sharedPreferences.getString("uid", "");
        //getFriebaseAddUserReference(uid);
    }

    @Override
    public void onPause() {
        super.onPause();
        //FriendRef.removeEventListener(mFriendValueListener);
        //UserDataRef.removeEventListener(mUserValueListener);
    }

}
