package com.example.home.mytalk.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.WindowManager;

import com.daimajia.androidanimations.library.Techniques;
import com.example.home.mytalk.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.viksaa.sssplash.lib.activity.AwesomeSplash;
import com.viksaa.sssplash.lib.cnst.Flags;
import com.viksaa.sssplash.lib.model.ConfigSplash;

public class SplashActivity extends AwesomeSplash {

    private static final String TAG = "SplashActivity" ;
    public String autoLoginID;
    public String autoLoginPW;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser user;
    private boolean isAutoLogin;
    private  String contactRequset;

    @Override
    public void initSplash(ConfigSplash configSplash) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();

        //Customize Circular Reveal
        configSplash.setBackgroundColor(R.color.fillColor); //any color you want form colors.xml
        configSplash.setAnimCircularRevealDuration(1500); //int ms
        configSplash.setRevealFlagX(Flags.REVEAL_RIGHT);  //or Flags.REVEAL_LEFT
        configSplash.setRevealFlagY(Flags.REVEAL_BOTTOM); //or Flags.REVEAL_TOP

        //Choose LOGO OR PATH; if you don't provide String value for path it's logo by default

        //Customize Logo
        configSplash.setLogoSplash(R.drawable.chaticon); //or any other drawable;
        configSplash.setAnimLogoSplashDuration(300); //int ms
        configSplash.setAnimLogoSplashTechnique(Techniques.SlideInUp); //choose one form Techniques (ref: https://github.com/daimajia/AndroidViewAnimations)
/*
        //Customize Path
        configSplash.setPathSplash(Constants.DROID_LOGO); //set path String
        configSplash.setOriginalHeight(400); //in relation to your svg (path) resource
        configSplash.setOriginalWidth(400); //in relation to your svg (path) resource
        configSplash.setAnimPathStrokeDrawingDuration(3000);
        configSplash.setPathSplashStrokeSize(3); //I advise value be <5
        configSplash.setPathSplashStrokeColor(R.color.accent); //any color you want form colors.xml
        configSplash.setAnimPathFillingDuration(3000);
        configSplash.setPathSplashFillColor(R.color.Wheat); //path object filling color
*/
        //Customize Title
        configSplash.setTitleSplash("");
        configSplash.setTitleTextColor(R.color.cardview_light_background);
        configSplash.setTitleTextSize(20f); //float value
        configSplash.setAnimTitleDuration(50);
        //configSplash.setAnimTitleTechnique(Techniques.Shake);
        //configSplash.setTitleFont("fonts/myfont.ttf"); //provide string to your font located in assets/fonts/

        SharedPreferences autoLogin = getSharedPreferences("autoLogin", MODE_PRIVATE);
        autoLoginID = autoLogin.getString("inputId", null);
        autoLoginPW = autoLogin.getString("inputPwd", null);
        isAutoLogin = false;

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    SharedPreferences sharedPreferences = getSharedPreferences("email", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("uid", user.getUid());
                    editor.putString("email", user.getEmail());
                    editor.apply();

                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };

        // firebase 로그인 체크 함수는 초기화 함수에서 확인하고  sharepreference 자동 로그인조건은 animationsFinished()에서 확인하여 시간 단축
        if(autoLoginID != null && autoLoginPW != null){
            Log.d("자동로그인 정보 ","아이디:"+ autoLoginID +" / "+"비번:"+ autoLoginPW);

            mAuth = FirebaseAuth.getInstance();
            mAuth.signInWithEmailAndPassword(autoLoginID, autoLoginPW)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                isAutoLogin = true;
                            }
                        }
                    });

        }else {
            isAutoLogin = false;
        };

        Intent intent = getIntent();
        contactRequset = intent.getStringExtra("FriendChatUid");
    }

    @Override
    public void animationsFinished() {
        // 스플래시 애니매이션 끝난후 자동로그인정보가
        // sharedpreference에 있으면 정보를 가지고 탭액티비로 이동
        // 정보가 없으면 메인액티비티로 이동
        if(isAutoLogin){
            goActivity(TabActivity.class);
        }else{
            goActivity(MainActivity.class);
        }
    }

    public void goActivity(Class c){
        Intent intent = new Intent(SplashActivity.this, c);
        if(c == TabActivity.class){
            intent.putExtra("FriendChatUid",contactRequset);
        } //친추요청 푸시메시지를 통해 탭액티비티 이동시 친구목록탭으로 설정을 위한 인탠트.
        startActivity(intent);
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

}