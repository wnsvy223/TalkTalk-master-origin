package com.example.home.mytalk.Activity;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.example.home.mytalk.R;
import com.example.home.mytalk.Utils.TouchImageView;

public class FullScreenActivity extends AppCompatActivity {

    private TouchImageView mImageView;
    private ProgressDialog progressDialog;
    private String urlPhotoClick;
    private String friendProfileImage;
    private String chatProfileImage;
    private ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen);
        actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(0xFF22CEF1));
        bindViews();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        System.gc();
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }


    private void bindViews(){
        progressDialog = new ProgressDialog(this);
        mImageView = (TouchImageView) findViewById(R.id.imageView);
    }

    private void setValues(){
        urlPhotoClick = getIntent().getStringExtra("urlPhotoClick");
        friendProfileImage = getIntent().getStringExtra("friendProfileImage");
        chatProfileImage = getIntent().getStringExtra("chatProfileImage");


        if(!TextUtils.isEmpty(urlPhotoClick)){
            setImage(urlPhotoClick);
            actionBar.setTitle("사진 메시지");
        }else if(!TextUtils.isEmpty(friendProfileImage)){
            setImage(friendProfileImage);
            actionBar.setTitle("프로필 사진");
        }else if(!TextUtils.isEmpty(chatProfileImage)){
            setImage(chatProfileImage);
            actionBar.setTitle("프로필 사진");
        }
    }

    private void setImage(String url){
        Glide.with(this).load( url).asBitmap().override(640,640).fitCenter().into(new SimpleTarget<Bitmap>() {

            @Override
            public void onLoadStarted(Drawable placeholder) {
                progressDialog.setMessage("로딩중...");
                progressDialog.show();
            }

            @Override
            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                progressDialog.dismiss();
                mImageView.setImageBitmap(resource);
            }

            @Override
            public void onLoadFailed(Exception e, Drawable errorDrawable) {
                Toast.makeText(getApplicationContext(),"로딩 에러",Toast.LENGTH_LONG).show();
                progressDialog.dismiss();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        urlPhotoClick = null;
        friendProfileImage = null;
        chatProfileImage = null;

    }

    @Override
    protected void onResume() {
        super.onResume();
        setValues();

    }
}
