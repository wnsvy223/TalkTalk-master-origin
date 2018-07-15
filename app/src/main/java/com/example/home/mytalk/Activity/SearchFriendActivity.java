package com.example.home.mytalk.Activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.home.mytalk.Model.Friend;
import com.example.home.mytalk.R;
import com.example.home.mytalk.Utils.RoundedTransformation;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import java.util.HashMap;


public class SearchFriendActivity extends AppCompatActivity {

    public EditText searchtext;
    public ImageView searchbutton;
    public RecyclerView mRecyclerView;
    public TextView searchUser;
    public LinearLayoutManager mLayoutManager;
    private DatabaseReference mDatabaseReference;
    private DatabaseReference mNotificationReference;
    private String currentEmail;
    private String currentUid;
    public String type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_friend);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(0xFF22CEF1));
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mDatabaseReference = FirebaseDatabase.getInstance().getReference("users");
        mNotificationReference = FirebaseDatabase.getInstance().getReference().child("notification");

        searchUser = (TextView)findViewById(R.id.searchUser);
        searchtext = (EditText)findViewById(R.id.searchText);
        searchbutton = (ImageView)findViewById(R.id.searchbutton);
        mRecyclerView = (RecyclerView)findViewById(R.id.rvFriendList);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        SharedPreferences sharedPreferences = this.getSharedPreferences("email", MODE_PRIVATE );
        currentEmail = sharedPreferences.getString("email", null);
        currentUid = sharedPreferences.getString("uid",null);
        type = getIntent().getStringExtra("searchType");

        if(type.equals("email")){
            searchUser.setText("Email을 입력하세요");
        }else{
            searchUser.setText("이름을 입력하세요");
        }

        searchbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String editTextSearch = searchtext.getText().toString();
                if(editTextSearch.isEmpty()){
                    if(type.equals("email")){
                        Toast.makeText(getApplicationContext(), "검색할 친구의 이메일을 입력하세요", Toast.LENGTH_SHORT).show();
                    }else {
                        Toast.makeText(getApplicationContext(), "검색할 친구의 이름을 입력하세요", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    firebaseUserSearch(editTextSearch, type);
                }
            }
        });

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
                if (message.length() > 0)
                    searchUser.setVisibility(View.INVISIBLE);
                else
                    searchUser.setVisibility(View.VISIBLE);
            }
        };
        searchtext.addTextChangedListener(textWatcher);
    }

    private void firebaseUserSearch(String editTextSearch, String type) {
        //인탠트로 넘어오는 type변수의값에 따라 이름검색, 이메일 검색 분류.  orderByChild가 type변수값에 따라 구분되어 정렬.
        Query firebaseSearchQuery = mDatabaseReference.orderByChild(type).startAt(editTextSearch).endAt(editTextSearch + "\uf8ff"); // 검색문자가 같은부분있을경우 쿼리
        //Query firebaseSearchQuery = mDatabaseReference.orderByChild("email").equalTo(editTextSearch); //정확하게 같은 경우만 쿼리
        FirebaseRecyclerAdapter<Friend, UserViewHolder> firebaseRecyclerAdapter =  new FirebaseRecyclerAdapter<Friend, UserViewHolder>(
                Friend.class,
                R.layout.list_add_friend,
                UserViewHolder.class,
                firebaseSearchQuery
        ) {
            @Override
            protected void populateViewHolder(UserViewHolder viewHolder, final Friend friend, int position) {
                viewHolder.setDatils(getApplicationContext(), friend.getEmail(), friend.getName(), friend.getPhoto());
                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        DatabaseReference AddFriendRef = FirebaseDatabase.getInstance().getReference("AddFriend");

                        String Email = friend.getEmail();
                        String Uid = friend.getKey();
                        String Email_R = currentEmail;
                        String Uid_R = currentUid;

                        HashMap<String, String> profile = new HashMap<String, String>();
                        profile.put("email",Email);
                        profile.put("accept","N");
                        AddFriendRef.child(Uid_R).child(Uid).setValue(profile, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            }
                        });    //내정보 하위에 친구정보 세팅

                        HashMap<String, String> profile_r = new HashMap<String, String>();
                        profile_r.put("email",Email_R);
                        profile_r.put("accept","N");
                        AddFriendRef.child(Uid).child(Uid_R).setValue(profile_r, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                HashMap<String, String> notification = new HashMap<>();
                                notification.put("from",currentUid);
                                notification.put("type","request");
                                mNotificationReference.child(friend.getKey()).push().setValue(notification);
                            }
                        });  //친구정보 하위에 내정보 세팅

                        finish();
                    }
                });

            }
        };
        mRecyclerView.setAdapter(firebaseRecyclerAdapter);
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder{  //static 선언 안하면 NoSuchMethodException 발생

        View mView;
        public UserViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

        }

        public  void setDatils(Context context, String userEmail, String userName, String userPhoto ){
            TextView user_name = (TextView)mView.findViewById(R.id.tvName);
            TextView user_email = (TextView)mView.findViewById(R.id.tvEmail);
            ImageView user_photo = (ImageView)mView.findViewById(R.id.ivUser);

            user_email.setText(userEmail);
            user_name.setText(userName);

            if(TextUtils.isEmpty(userPhoto)){
                Picasso.with(context)
                        .load(R.drawable.ic_unknown_user)
                        .transform(new RoundedTransformation(200, 0))
                        .fit().centerCrop()
                        .into(user_photo);
            }else {
                Picasso.with(context)
                        .load(userPhoto)
                        .transform(new RoundedTransformation(200, 0))
                        .fit().centerCrop()
                        .error(R.drawable.ic_error)
                        .into(user_photo);
            }
        }

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
