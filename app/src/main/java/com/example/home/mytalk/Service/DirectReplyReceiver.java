package com.example.home.mytalk.Service;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.RemoteInput;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.example.home.mytalk.Model.FirebaseApp;

import java.util.ArrayList;


public class DirectReplyReceiver extends BroadcastReceiver{

    public String KEY_REPLY = "key_reply";

    @Override
    public void onReceive(Context context, Intent intent) {

        FirebaseApp firebaseApp = ((FirebaseApp)context.getApplicationContext());
        String action = intent.getStringExtra("action_reply");
        String tag = intent.getStringExtra("tag");
        //String photoUrl = intent.getStringExtra("photoUrl");
        //String title = intent.getStringExtra("title");
        //String body = intent.getStringExtra("body");
        String room = intent.getStringExtra("room");

        Bundle bundle = RemoteInput.getResultsFromIntent(intent);
        if(!TextUtils.isEmpty(action)) {
            if (bundle != null && action.equals("reply")) {
                String messageText = bundle.getString(KEY_REPLY);
                Toast.makeText(context, "다이렉트메시지:" + messageText, Toast.LENGTH_SHORT).show();

                if(tag.equals("one")){
                    firebaseApp.setOneToOneMessage(messageText,"text",room);
                }else if(tag.equals("group")){
                   // firebaseApp.setGroupMessage(messageText,"text",room,unReadUser);
                }

                clearExistingNotifications(context);
            }else if(action.equals("cancel")){
                clearExistingNotifications(context);
            }
        }
    }

    private void clearExistingNotifications(Context context) { // reply 입력후 노티 제거
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancelAll();
    }

}
