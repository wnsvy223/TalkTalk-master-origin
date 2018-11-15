package com.example.home.mytalk.Service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationManagerCompat;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ContactActionReceiver extends BroadcastReceiver{

    private DatabaseReference databaseReference;

    @Override
    public void onReceive(Context context, Intent intent) {
        String fromUserUid = intent.getStringExtra("from_user");
        String action = intent.getStringExtra("action_contact");
        SharedPreferences sharedPreferences = context.getSharedPreferences("email", Context.MODE_PRIVATE);
        String currentUid = sharedPreferences.getString("uid", "");
        databaseReference = FirebaseDatabase.getInstance().getReference("AddFriend");

        if(action.equals("approve")){
            setApprove(currentUid,fromUserUid,context);
        }else if(action.equals("refuse")){
            setRefuse(currentUid,fromUserUid,context);
        }
        Intent broadIntent = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        context.sendBroadcast(broadIntent);
    }

    public void setApprove(String myNode, String friendNode,Context context){
        databaseReference.child(myNode).child(friendNode).child("accept").setValue("F");
        databaseReference.child(friendNode).child(myNode).child("accept").setValue("F");
        clearExistingNotifications(context);
    }

    public void setRefuse(String myNode, String friendNode, final Context context){
        databaseReference.child(myNode).child(friendNode).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                dataSnapshot.getRef().removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(context,"친구 추가가 승인 되었습니다..",Toast.LENGTH_SHORT).show();
                    }
                });  //친구 하위노드의 내 정보 삭제
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        databaseReference.child(friendNode).child(myNode).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                dataSnapshot.getRef().removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(context,"친구 추가가 거절 되었습니다.",Toast.LENGTH_SHORT).show();
                    }
                });  //친구 하위노드의 내 정보 삭제
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        clearExistingNotifications(context);
    }

    private void clearExistingNotifications(Context context) { // reply 입력후 노티 제거
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancelAll();
    }
}
