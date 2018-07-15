package com.example.home.mytalk.Activity;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.home.mytalk.Adapter.OpenChatAdapter;
import com.example.home.mytalk.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import static com.example.home.mytalk.R.id.roomName;

public class OpenChatActivity extends AppCompatActivity {

    public RecyclerView mRecyclerView;
    public LinearLayoutManager mLayoutManager;
    // public String TAG = getClass().getSimpleName();
    private List<String> mChatRoom;
    private OpenChatAdapter mAdapter;
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    //public ValueEventListener valueReferenceListener;
    public Button makeRoom;
    public EditText makeRoomName;
    public String room_name;
    public TextView textView;
    public TextView textView2;
    public ImageView image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_chat);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(0xFF45A1F5));
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mRecyclerView = (RecyclerView)findViewById(R.id.ChatRoom);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mChatRoom = new ArrayList<>();
        mAdapter = new OpenChatAdapter(mChatRoom , this);
        mRecyclerView.setAdapter(mAdapter);

        makeRoomName = (EditText)findViewById(roomName);
        makeRoom = (Button)findViewById(R.id.makeRoom);
        textView = (TextView)findViewById(R.id.textView);
        textView2 = (TextView)findViewById(R.id.textView2);
        image = (ImageView)findViewById(R.id.image);
        getRoom();

        makeRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //버튼 누르면 채팅룸(리스트 아이템 1개) 생성  , 채팅룸 클릭시 ChatActivity 로 이동.
                database = FirebaseDatabase.getInstance();
                myRef = database.getReference("openChatRoom");
                room_name = makeRoomName.getText().toString();

                if (room_name.equals("") || room_name.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "채팅방 제목을 입력하세요.", Toast.LENGTH_SHORT).show();
                }else {
                    myRef.child(room_name).setValue("");
                    makeRoomName.setText("");
                }
            }
        });
    }
    public void getRoom(){
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("openChatRoom");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mChatRoom.clear();
                for(DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    String room = postSnapshot.getKey();
                    mChatRoom.add(room);
                    textView.setVisibility(View.GONE);
                    textView2.setVisibility(View.GONE);
                    image.setVisibility(View.GONE);
                    mAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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
