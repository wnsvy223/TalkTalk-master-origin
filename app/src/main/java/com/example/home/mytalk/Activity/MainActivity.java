package com.example.home.mytalk.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.example.home.mytalk.Appliccation.FirebaseApp;
import com.example.home.mytalk.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;


public class MainActivity extends AppCompatActivity {

    public String TAG = "MainActivity";
    public Button button_login;
    public Button button_reg;
    public EditText editText_email;
    public EditText editText_password;
    public ProgressBar PBLogin;
    public String stEmail;
    public String stPassword;
    public FirebaseDatabase database;
    private DatabaseReference myRef;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser user;
    public CheckBox checkBox;
    public String currentUid;
    public static Context mContext;
    private FirebaseApp firebaseApp;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;

        getSupportActionBar().hide();

        editText_email = (EditText) findViewById(R.id.email);
        editText_password = (EditText) findViewById(R.id.password);
        button_reg = (Button) findViewById(R.id.reg);
        button_login = (Button) findViewById(R.id.login);
        PBLogin = (ProgressBar)findViewById(R.id.PBLogin);
        PBLogin.setVisibility(View.GONE);
        checkBox = (CheckBox) findViewById(R.id.checkBox);

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("users");
        firebaseApp = (FirebaseApp)getApplicationContext(); //공통 클래스 초기화

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
                    currentUid = user.getUid();

                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };


        button_reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                intent.setFlags(intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.setFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        button_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stEmail = editText_email.getText().toString();
                stPassword = editText_password.getText().toString();
                if (stEmail.isEmpty() || stEmail.equals("") || stPassword.isEmpty() || stPassword.equals(""))
                {
                    Toast.makeText(MainActivity.this, "로그인 정보를 입력하세요", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    userLogin(stEmail, stPassword);
                    PBLogin.setVisibility(View.VISIBLE);
                }
            }

        });


    }

    public void userLogin(String email, String password) {
        mAuth = FirebaseAuth.getInstance();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithEmail:failed", task.getException());
                            firebaseApp.checkAccount(task, editText_email, editText_password); // 계정 유효성 체크
                            PBLogin.setVisibility(View.GONE);
                        } else {
                            if(checkBox.isChecked()) { //자동로그인 체크박스 체크되있으면 자동로그인 정보 저장
                                SharedPreferences autoLogin = getSharedPreferences("autoLogin", MODE_PRIVATE);
                                SharedPreferences.Editor editor = autoLogin.edit();
                                editor.putString("inputId", editText_email.getText().toString().trim());
                                editor.putString("inputPwd", editText_password.getText().toString().trim());
                                editor.apply();
                            }
                            String deviceToken = FirebaseInstanceId.getInstance().getToken();
                            String current_user_id = mAuth.getCurrentUser().getUid();
                            myRef.child(current_user_id).child("deviceToken").setValue(deviceToken, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                    Toast.makeText(MainActivity.this, "로그인 성공", Toast.LENGTH_SHORT).show();
                                    PBLogin.setVisibility(View.GONE);
                                    Intent intent_login = new Intent(getApplicationContext(), TabActivity.class);
                                    intent_login.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent_login);
                                    finish();
                                }
                            });

                        }
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

    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if(mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }


}
