package com.example.home.mytalk.Service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.home.mytalk.Model.FirebaseApp;
import com.example.home.mytalk.Model.Friend;
import com.example.home.mytalk.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static com.example.home.mytalk.Activity.ChatActivity.OpenChatRoom;

//like miller 님의 최상단 뷰 서비스 이용 (http://blog.daum.net/_blog/BlogTypeView.do?blogid=0NADc&articleno=35&categoryId=3&regdt=20121022143331&totalcnt=20)
// 파일 업로드를 서비스를 이용해서 백그라운드로 진행되도록 함
public class UploadService extends Service{

    private WindowManager wm;
    private View mView;
    private TextView progressStatus;
    private ProgressBar uploadProgress;
    private UploadTask uploadTask;
    public String currentUid;
    public FirebaseApp firebaseApp;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) { //startService 로 서비스 호출시 이 함수를 탐
        String path = intent.getStringExtra("storagePath");
        String fileType = intent.getStringExtra("fileType");
        String  messageType = intent.getStringExtra("messageType");
        String progressText = intent.getStringExtra("progressText");
        Uri uri = intent.getParcelableExtra("uri"); //uri는 Parcelable 객체로 받아야 한다고 함. String으로 못받음.
        String room = intent.getStringExtra("room");
        ArrayList<String> unReadUserLIst = intent.getStringArrayListExtra("unReadUserList");

        Log.d("업로드테스크", String.valueOf(uri)+path+fileType+messageType+progressText+room);
        uploadStorage(path,fileType,uri,messageType,progressText, room, unReadUserLIst);

        return super.onStartCommand(intent, flags, startId);
    }

    private void uploadStorage(String storagePath, final String fileType, Uri imageUri, final String messageType, final String progressText, final String room, final ArrayList<String> unReadUserList) {

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA);
        String formattedDate = simpleDateFormat.format(calendar.getTime());

        uploadTask = FirebaseStorage.getInstance().getReference()
                .child(storagePath).child(currentUid + "/" + formattedDate + fileType).putFile(imageUri);

        uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    @SuppressWarnings("VisibleForTests") String download_url = task.getResult().getDownloadUrl().toString();

                    if (!TextUtils.isEmpty(OpenChatRoom)) {
                        firebaseApp.setOpenMessage(download_url,messageType);
                    } else if (!TextUtils.isEmpty(room)) {
                        if (room.contains("Group@")) {
                            firebaseApp.setGroupMessage(download_url,messageType,room,unReadUserList);
                        } else {
                            firebaseApp.setOneToOneMessage(download_url,messageType,room);
                        }
                    }
                }
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() { //사진 메시지 전송중일때 프로그래스바를 이용해 상태 표시
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                @SuppressWarnings("VisibleForTests") long completedUnits = taskSnapshot.getBytesTransferred(); //현재 진행된 값
                @SuppressWarnings("VisibleForTests") long totalUnits = taskSnapshot.getTotalByteCount(); //전체 값
                showProgressbar(progressText, completedUnits, totalUnits);
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() { //성공하면 프로그래스바와 텍스트 뷰 제거
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(getApplicationContext(), "전송 완료", Toast.LENGTH_LONG).show();
                progressStatus.setVisibility(View.GONE);
                uploadProgress.setVisibility(View.GONE);
            }
        }).addOnFailureListener(new OnFailureListener() { //실패하면 프로그래스바와 텍스트 뷰 제거와 동시에 실패 토스트메시지 출력
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "전송 실패"+e, Toast.LENGTH_LONG).show();
                progressStatus.setVisibility(View.GONE);
                uploadProgress.setVisibility(View.GONE);
            }
        }).addOnPausedListener(new OnPausedListener<UploadTask.TaskSnapshot>() { //업로드 중지되면 중지 토스트메시지 출력
            @Override
            public void onPaused(UploadTask.TaskSnapshot taskSnapshot) {
                if (uploadTask.isPaused()) {
                    @SuppressWarnings("VisibleForTests") long completedUnits = taskSnapshot.getBytesTransferred(); //현재 진행된 값
                    @SuppressWarnings("VisibleForTests") long totalUnits = taskSnapshot.getTotalByteCount(); //전체 값
                    Log.d("진행상태", "전체 : " + totalUnits + " //// " + "현재 : " + completedUnits);

                    Toast.makeText(getApplicationContext(), "전송이 중지 되었습니다.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void showProgressbar(String text, long completedUnits, long totalUnits) { //전송상태 프로그래스 바
        uploadProgress.setVisibility(View.VISIBLE);
        progressStatus.setVisibility(View.VISIBLE);
        progressStatus.setText(text);
        int percentComplete = 0;
        if (totalUnits > 0) {
            percentComplete = (int) (100 * completedUnits / totalUnits);
            uploadProgress.setProgress(percentComplete);
            uploadProgress.getProgressDrawable().setColorFilter(
                    Color.BLUE, android.graphics.PorterDuff.Mode.SRC_IN);
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();

        firebaseApp = (FirebaseApp)getApplicationContext();

        LayoutInflater inflate = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        |WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        |WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.CENTER | Gravity.TOP;
        mView = inflate.inflate(R.layout.view_upload_service, null);
        progressStatus = (TextView) mView.findViewById(R.id.progressStatus);
        uploadProgress =  (ProgressBar) mView.findViewById(R.id.uploadProgress);
        progressStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressStatus.setText("on click!!");
            }
        });
        wm.addView(mView, params);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(wm != null) {
            if(mView != null) {
                wm.removeView(mView);
                mView = null;
            }
            wm = null;
        }
        stopSelf();
        Log.d("업로드 서비스 종료","");
    }

}
