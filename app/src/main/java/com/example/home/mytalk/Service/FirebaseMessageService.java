package com.example.home.mytalk.Service;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.RemoteInput;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.example.home.mytalk.Activity.ChatActivity;
import com.example.home.mytalk.R;
import com.example.home.mytalk.Utils.RoundedTransformation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.squareup.picasso.Picasso;
import java.io.IOException;
import java.util.List;


import me.leolin.shortcutbadger.ShortcutBadger;


public class FirebaseMessageService extends FirebaseMessagingService {

    public  NotificationCompat.Builder notificationBuilder;
    public String KEY_REPLY = "key_reply";
    public String Body;
    private String currentUid;
    private String currentEmail;
    private NotificationManager notificationManager;

    @Override
    public void onMessageReceived(final RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        SharedPreferences sharedPreferences = getSharedPreferences("email", MODE_PRIVATE);
        currentUid = sharedPreferences.getString("uid", "");
        currentEmail =  sharedPreferences.getString("email", "");

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE );
        @SuppressLint("InvalidWakeLockTag") PowerManager.WakeLock wakeLock = pm.newWakeLock( PowerManager.PARTIAL_WAKE_LOCK
                | PowerManager.ACQUIRE_CAUSES_WAKEUP, "TAG" );
        wakeLock.acquire(3000);
        // PARTIAL_WAKE_LOCK : 화면을 깨우지 않고 잠금화면에 푸시 알림 표시 및 진동&소리 제공
        // SCREEN_DIM_WAKE_LOCK (deprecated ) : 화면을 깨우고 푸시 알림 표시 및 진동&소리 제공

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("friendChatRoom").child(currentUid);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long totalCount = 0;
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    if(postSnapshot.hasChild("badgeCount")) {
                        long count = (long) postSnapshot.child("badgeCount").getValue();
                        Log.d("FCM카운트", String.valueOf(count));
                        totalCount += count;
                    }
                }
                Log.d("FCM토탈카운트", String.valueOf(totalCount));
                ShortcutBadger.applyCount(getApplicationContext(), (int) totalCount);
                // 푸시메시지 날아오면 DB에서 현재 채팅방들의 배지 카운트 값을 가져와서 그값을 더해 총 값을
                // 홈 화면 아이콘에 카운트값 넣어 배지생성.
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if (remoteMessage.getData() != null ) {

            String tag = remoteMessage.getData().get("tag");
            // tag는 어떤 기능의 푸시인지 구분을 위한 변수
            // contact- 친추, one - 1:1채팅, group - 그룹채팅

            String from_user_id = remoteMessage.getData().get("from_user_id");
            String Title = remoteMessage.getData().get("title");
            String Body = remoteMessage.getData().get("body");
            String Photo = remoteMessage.getData().get("userPhoto");
            String click_action = remoteMessage.getData().get("click_action");
            //친구추가 푸시 데이터

            String name = remoteMessage.getData().get("name");
            String userPhotoImage = remoteMessage.getData().get("userPhotoImage");
            String message = remoteMessage.getData().get("message");
            String token = remoteMessage.getData().get("tokenId"); //채팅 메시지 받는사람 기기 토큰값
            String tokenSender = remoteMessage.getData().get("tokenIdSender"); //채팅 메시지 보내는 사람 기기 토큰값
            String click_action_message = remoteMessage.getData().get("click_action_message");
            String one_room_name = remoteMessage.getData().get("room");
            //1:1채팅 메시지 푸시 데이터

            String group_key = remoteMessage.getData().get("gsenderKey");
            String group_name = remoteMessage.getData().get("gname");
            String group_userPhotoImage = remoteMessage.getData().get("guserPhotoImage");
            String group_message = remoteMessage.getData().get("gmessage");
            String group_token = remoteMessage.getData().get("gtokenId"); //채팅 메시지 받는사람 기기 토큰값
            String sender_token = remoteMessage.getData().get("gtokenIdSender"); //채팅 메시지 보내는 사람 기기 토큰값
            String group_click_action = remoteMessage.getData().get("gclick_action_message");
            String group_room_name = remoteMessage.getData().get("groom");

            ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
            List<ActivityManager.RunningTaskInfo> info = activityManager.getRunningTasks(1);
            String activity = info.get(0).topActivity.getClassName();

            if (tag != null) {
                if((activity.equals("com.example.home.mytalk.Activity.ChatActivity"))){
                    return;
                }else{
                    switch (tag) {
                        case "contact":
                            setNotification("contact",Photo, Title, Body, click_action, from_user_id);
                            break;

                        case "one":
                            //setNotification(userPhotoImage, name, message, click_action_message, one_room_name)
                            if (!token.equals(tokenSender)) { //메시지 보낸사람 기기는 푸시X
                                setNotification("one",userPhotoImage, name, message, click_action_message, one_room_name);
                            }
                            break;

                        case "group":
                            //setNotification(group_userPhotoImage, group_name, group_message, group_click_action, group_room_name);
                            if (!group_token.equals(sender_token)) { //메시지 보낸사람 기기는 푸시X
                                setNotification("group",group_userPhotoImage, group_name, group_message, group_click_action, group_room_name);
                            }
                            break;
                        default:
                    }
                }
            }
        }

    }


    private void setNotification(String tag, String photoUrl, String title, String body, String clickAction, String from_value) {

        notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        String CHANNEL_ID = "my_channel_01";
        CharSequence name = "my_channel";
        String Description = "This is my channel";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            mChannel.setDescription(Description);
            mChannel.enableLights(true);
            mChannel.setLightColor(Color.RED);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            mChannel.setShowBadge(false);
            notificationManager.createNotificationChannel(mChannel);
        }

        Bitmap bitmap = null;
        try {
            if(TextUtils.isEmpty(photoUrl)){
                bitmap = Picasso.with(getApplicationContext())
                        .load(R.drawable.ic_unknown_user).transform(new RoundedTransformation(200, 0))
                        .centerCrop()
                        .resize(300, 300)
                        .get();
            }else{
                bitmap = Picasso.with(getApplicationContext())
                    .load(photoUrl).transform(new RoundedTransformation(200, 0))
                    .centerCrop()
                    .resize(300, 300)
                    .get();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (body.contains(".jpg")) {
            Body = "(사진)";
        }else if(body.contains(".mp4")){
            Body = "(동영상)";
        }else{
            Body = body;
        }

        SharedPreferences sharedPreferences = getSharedPreferences("email", MODE_PRIVATE );
        String currentUid = sharedPreferences.getString("uid", "");
        Intent resultIntent = new Intent(clickAction);
        resultIntent.putExtra("FriendChatUid", from_value);
        resultIntent.putExtra("MyKey",currentUid);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(
                this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT
        );  //노티로 채팅액티비티 호출 시 이미 채팅액티비티가 생성되어있으면 두개가 생성되므로 manifest에 채팅액티비티를 singleTask로 세팅해서 하나만 생성되도록함.

        if(tag.equals("contact")) { // 친추 푸시 메시지는 수락/거절 버튼 포함된 노티 생성.
            Intent accept = new Intent(getApplicationContext(), ContactActionReceiver.class);
            accept.putExtra("action_contact", "approve");
            accept.putExtra("from_user",from_value);
            PendingIntent approvePending = PendingIntent.getBroadcast(
                    this, 1, accept, PendingIntent.FLAG_UPDATE_CURRENT
            );
            Intent deccept = new Intent(getApplicationContext(), ContactActionReceiver.class);
            deccept.putExtra("action_contact", "refuse");
            accept.putExtra("from_user",from_value);
            PendingIntent refuePending = PendingIntent.getBroadcast(
                    this, 2, deccept, PendingIntent.FLAG_UPDATE_CURRENT
            );
            notificationBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(getApplicationContext())
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setLargeIcon(bitmap)
                    .setContentTitle(title)
                    .setContentText(Body)
                    .addAction(R.drawable.approve,"수락",approvePending)
                    .addAction(R.drawable.refuse,"거절",refuePending)
                    .setDefaults(NotificationCompat.DEFAULT_SOUND | NotificationCompat.DEFAULT_VIBRATE) //노티 사운드. 진동 설정
                    .setPriority(NotificationCompat.PRIORITY_HIGH) // 노티메시지시 팝업 추가
                    .setAutoCancel(true); //노티 클릭시 제거
        }else { // 1:1 또는 그룹채팅 노티의 경우 direct reply 포함된 노티 생성
            Intent directReply = new Intent(getApplicationContext(), DirectReplyReceiver.class);
            directReply.putExtra("action_reply", "reply");
            directReply.putExtra("tag", tag);
            directReply.putExtra("room", from_value);
            PendingIntent replyPending = PendingIntent.getBroadcast(
                    this, 3, directReply, PendingIntent.FLAG_UPDATE_CURRENT);

            Intent cancel = new Intent(getApplicationContext(),DirectReplyReceiver.class);
            cancel.putExtra("action_reply","cancel");
            cancel.putExtra("room", from_value);
            PendingIntent dismissPending = PendingIntent.getBroadcast(
                    this, 4, cancel, PendingIntent.FLAG_CANCEL_CURRENT);

            RemoteInput remoteInput = new RemoteInput.Builder(KEY_REPLY)
                    .setLabel("메시지를 입력하세요")
                    .build();
            NotificationCompat.Action replyAction = new NotificationCompat.Action.Builder(
                    android.R.drawable.sym_action_chat, "답장", replyPending)
                    .addRemoteInput(remoteInput)
                    .setAllowGeneratedReplies(true)
                    .build();

            NotificationCompat.Action dismissAction = new NotificationCompat.Action.Builder(
                    android.R.drawable.ic_menu_close_clear_cancel,"취소",dismissPending)
                    .build();


            notificationBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(getApplicationContext())
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setLargeIcon(bitmap)
                    .setContentTitle(title)
                    .setContentText(Body)
                    .setChannelId(CHANNEL_ID)
                    .addAction(replyAction)
                    .addAction(dismissAction)
                    .setDefaults(NotificationCompat.DEFAULT_SOUND | NotificationCompat.DEFAULT_VIBRATE) //노티 사운드. 진동 설정
                    .setPriority(NotificationCompat.PRIORITY_HIGH) // 노티메시지시 팝업 추가
                    .setAutoCancel(true); //노티 클릭시 제거
        }

        notificationBuilder.setContentIntent(resultPendingIntent);
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(getApplicationContext());
        notificationManagerCompat.notify(0x1001, notificationBuilder.build());
    }

}

