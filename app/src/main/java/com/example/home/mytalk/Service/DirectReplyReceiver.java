package com.example.home.mytalk.Service;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.RemoteInput;
import android.text.TextUtils;
import android.widget.Toast;

import org.w3c.dom.Text;

public class DirectReplyReceiver extends BroadcastReceiver{

    public String KEY_REPLY = "key_reply";

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getStringExtra("action_reply");
        Bundle bundle = RemoteInput.getResultsFromIntent(intent);
        if(!TextUtils.isEmpty(action)) {
            if (bundle != null && action.equals("reply")) {
                String messageText = bundle.getString(KEY_REPLY);
                Toast.makeText(context, "다이렉트메시지:" + messageText, Toast.LENGTH_SHORT).show();
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
