package com.example.home.mytalk.Service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.RemoteInput;
import android.text.TextUtils;

import com.example.home.mytalk.Appliccation.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class DirectReplyReceiver extends BroadcastReceiver{

    public String KEY_REPLY = "key_reply";

    @Override
    public void onReceive(final Context context, final Intent intent) {
        String room = intent.getStringExtra("room");
        SharedPreferences sharedPreferences = context.getSharedPreferences("email", Context.MODE_PRIVATE);
        String currentUid = sharedPreferences.getString("uid", "");

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("friendChatRoom").child(currentUid).child(room).child("joinUserKey");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                FirebaseApp firebaseApp = ((FirebaseApp)context.getApplicationContext());
                ArrayList<String> userList = (ArrayList<String>) dataSnapshot.getValue();

                String action = intent.getStringExtra("action_reply");
                String tag = intent.getStringExtra("tag");
                String roomValue = intent.getStringExtra("room");

                Bundle bundle = RemoteInput.getResultsFromIntent(intent);
                if(!TextUtils.isEmpty(action)) {
                    if (bundle != null && action.equals("reply")) {
                        String messageText = bundle.getString(KEY_REPLY);

                        if(tag.equals("one")){
                            firebaseApp.setOneToOneMessage(messageText,"text",roomValue);
                        }else if(tag.equals("group")){
                            firebaseApp.setGroupMessage(messageText,"text",roomValue, userList);
                        }

                        clearExistingNotifications(context);
                    }else if(action.equals("cancel")){
                        clearExistingNotifications(context);
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    private void clearExistingNotifications(Context context) { // reply 입력후 노티 제거
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancelAll();
    }

}
