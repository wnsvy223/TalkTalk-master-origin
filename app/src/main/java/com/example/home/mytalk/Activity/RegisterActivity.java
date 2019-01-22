package com.example.home.mytalk.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.home.mytalk.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.example.home.mytalk.Appliccation.FirebaseApp;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    public EditText Email;
    public EditText PassWord;
    public EditText Phone;
    public EditText Name;
    public TextView TxEmail;
    public TextView TxPW;
    public Button button_reg;
    public Button button_quit;
    public Button checkID;
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser user;
    public String TAG = getClass().getSimpleName();
    private String stEmail;
    private String stPassword;
    private String stPhone;
    private String stName;
    private FirebaseApp firebaseApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(0xFF000000));
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Phone = (EditText)findViewById(R.id.phone);
        Name = (EditText)findViewById(R.id.name);
        Email = (EditText)findViewById(R.id.email);
        PassWord = (EditText)findViewById(R.id.password);
        TxEmail = (TextView)findViewById(R.id.text_email);
        TxPW = (TextView)findViewById(R.id.text_password);
        button_reg = (Button)findViewById(R.id.reg);
        button_quit = (Button)findViewById(R.id.quit);
        checkID = (Button)findViewById(R.id.checkId);

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
                    editor.putString("uid",  user.getUid());
                    editor.putString("email",  user.getEmail());
                    editor.apply();

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
                stEmail =  Email.getText().toString();
                stPassword = PassWord.getText().toString();
                stPhone = Phone.getText().toString();
                stName = Name.getText().toString();
                if(stEmail.isEmpty() || stEmail.equals("") ||stPassword.isEmpty() || stPassword.equals("")) {
                    Toast.makeText(getApplicationContext(), "정보를 입력해주세요",Toast.LENGTH_SHORT).show();
                }else if(!isValidEmail(stEmail)){
                    Toast.makeText(getApplicationContext(), "이메일 형식으로 입력해주세요",Toast.LENGTH_SHORT).show();
                }else if (isValidPasswd(stPassword)){
                    Toast.makeText(getApplicationContext(), "비밀번호는 6자리 이상(한글미포함)으로 입력해주세요",Toast.LENGTH_SHORT).show();
                }else {
                    registerUser(stEmail, stPassword, stPhone , stName);
                }
            }
        });

        button_quit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        checkID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stEmail =  Email.getText().toString();
                checkId(stEmail);
            }
        });

    }

    public void registerUser(String email, String password, final String phone, final String name){
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                        if (!task.isSuccessful()) {
                            firebaseApp.checkAccount(task, Email, PassWord); // 계정 유효성 체크
                        }else{
                            String deviceToken = FirebaseInstanceId.getInstance().getToken();
                            if (user != null) {
                                HashMap<String, String> profile = new HashMap<String, String>();
                                profile.put("email", user.getEmail());
                                profile.put("photo", "");
                                profile.put("name", name);
                                profile.put("phone", phone);
                                profile.put("key",user.getUid());
                                profile.put("state","Unknown");
                                profile.put("latitude", String.valueOf(37.56));
                                profile.put("longitude", String.valueOf(126.97));
                                profile.put("deviceToken",deviceToken);
                                myRef.child(user.getUid()).setValue(profile, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                        Toast.makeText(RegisterActivity.this, "가입 성공", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                        intent.addFlags(intent.FLAG_ACTIVITY_SINGLE_TOP);
                                        intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(intent);
                                    }
                                });

                            }
                        }

                        // ...
                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);

    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    private boolean isValidEmail(String target) {
        if ( target.isEmpty() ||  TextUtils.isEmpty(target))
        {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }

    private boolean isValidPasswd(String target) {
        Pattern p = Pattern.compile("(^.*(?=.{6,100})(?=.*[0-9])(?=.*[a-zA-Z]).*$)");

        Matcher m = p.matcher(target);
        if (m.find() && !target.matches(".*[ㄱ-ㅎㅏ-ㅣ가-힣]+.*")){
            return true;
        }else{
            return false;
        }
    }

    private void checkId(final String text){
        Query query = myRef.orderByChild("email").equalTo(text); //users 노드에서 email 노드중 입력값과 같은값만 쿼리
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                boolean emailIsExist = dataSnapshot.exists(); //입력값과 같은 쿼리 데이터가 존재하면(= 이미 존재하는 이메일이면) true 반환
                if(text.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "아이디를 입력해야 합니다", Toast.LENGTH_SHORT).show();
                }else if(!text.contains("@")){
                    Toast.makeText(getApplicationContext(), "이메일 형식의 아이디를 입력해야 합니다", Toast.LENGTH_SHORT).show();
                }else if(emailIsExist){
                    Toast.makeText(getApplicationContext(), "동일한 아이디가 존재합니다", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getApplicationContext(), "가입 가능한 아이디입니다", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

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

}
