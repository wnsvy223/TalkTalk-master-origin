package com.example.home.mytalk.Service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class ContactActionReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getStringExtra("action_contact");
        if(action.equals("approve")){
            Toast.makeText(context,"친추 수락 됨",Toast.LENGTH_SHORT).show();
        }else if(action.equals("refuse")){
            Toast.makeText(context,"친추 거절 됨",Toast.LENGTH_SHORT).show();
        }
        Intent broadIntent = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        context.sendBroadcast(broadIntent);
    }
}
