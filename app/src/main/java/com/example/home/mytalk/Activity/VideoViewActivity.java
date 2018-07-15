package com.example.home.mytalk.Activity;

import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.home.mytalk.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class VideoViewActivity extends AppCompatActivity {


    private VideoView videoView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_view);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(0xFF22CEF1));

        videoView = (VideoView) findViewById(R.id.view);
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                //동영상 재생이 완료된 후 호출되는 메소드
                Toast.makeText(VideoViewActivity.this, "동영상 재생이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                finish();
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
        String urlVideoClick = getIntent().getStringExtra("urlVideoClick");
        Log.d("URL",urlVideoClick);
        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(urlVideoClick);
        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                MediaController controller = new MediaController(VideoViewActivity.this);
                videoInit(uri, controller);
            }
        });
    }

    private void videoInit(Uri uri, MediaController mediaController){
        videoView.setVideoURI(uri);
        videoView.setMediaController(mediaController);
        videoView.requestFocus();
        videoView.start();
    }
}
