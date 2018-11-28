package com.example.home.mytalk.Service;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LogOutTaskService extends Service {


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);

        DatabaseReference databaseReference =  FirebaseDatabase.getInstance().getReference("users");
        SharedPreferences sharedPreferences = getSharedPreferences("email",MODE_PRIVATE );
        String currentUid = sharedPreferences.getString("uid", "");
        //databaseReference.child(currentUid).child("state").setValue("접속종료"); //DB에서의 명시적 로그아웃 상태 세팅.

        Log.d("강제종료 모니터링 서비스", String.valueOf(currentUid));

        stopSelf(); // LogOutTaskService 종료
    }
}
