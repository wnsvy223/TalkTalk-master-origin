package com.example.home.mytalk.Activity;

import android.animation.Animator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.drm.DrmStore;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.OvershootInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.home.mytalk.Fragment.Fragment_Chat;
import com.example.home.mytalk.Fragment.Fragment_Friend;
import com.example.home.mytalk.Fragment.Fragment_Home;
import com.example.home.mytalk.Fragment.Fragment_Profile;
import com.example.home.mytalk.R;
import com.example.home.mytalk.Service.LogOutTaskService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rahimlis.badgedtablayout.BadgedTabLayout;

import javax.xml.datatype.Duration;


public class TabActivity extends AppCompatActivity{

    private static final String TAG = "TabActivity" ;
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser user;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    public ViewPager mViewPager;
    private final long	FINSH_INTERVAL_TIME = 2000;
    private long	backPressedTime = 0;
    public Toolbar toolbar;
    public String currentUid;
    public static Context mContext;
    public BadgedTabLayout tabLayout;
    public CoordinatorLayout coordinatorLayout;
    public com.melnykov.fab.FloatingActionButton fab;
    public com.melnykov.fab.FloatingActionButton fab_child_one;
    public com.melnykov.fab.FloatingActionButton fab_child_two;
    public Intent intent;
    private AppBarLayout appBarLayout;
    private boolean isFabOpen =false;
    public TextView fab_text_1;
    public TextView fab_text_2;
    public FrameLayout overlay;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab);

        mContext = this;
        startService(new Intent(this, LogOutTaskService.class));
        //앱 강제종료 모니터링 서비스 실행. (실행중인 앱 보기에서 앱을 강제 종료시 DB와 사용자 인증서비스에 로그아웃상태로 변경해주는 서비스)
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        final SharedPreferences sharedPreferences = getSharedPreferences("email",MODE_PRIVATE );
        currentUid = sharedPreferences.getString("uid", "");

        databaseReference =  FirebaseDatabase.getInstance().getReference("users");
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
            }
        };

        fab = (com.melnykov.fab.FloatingActionButton)findViewById(R.id.fab);
        fab_child_one =(com.melnykov.fab.FloatingActionButton)findViewById(R.id.fab_1);
        fab_child_two =(com.melnykov.fab.FloatingActionButton)findViewById(R.id.fab_2);
        fab_text_1 =(TextView)findViewById(R.id.fab_text_1);
        fab_text_2 =(TextView)findViewById(R.id.fab_text_2);
        setGoneFab();

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(3);
        tabLayout = (BadgedTabLayout) findViewById(R.id.tabs);  //배지 세팅 가능한 탭 레이아웃 라이브러리
        tabLayout.setupWithViewPager(mViewPager);
        tabLayout.setIcon(0, R.drawable.home);
        tabLayout.setIcon(1, R.drawable.friend);
        tabLayout.setIcon(2, R.drawable.chat);
        tabLayout.setIcon(3, R.drawable.user_profile_edit2);

        overlay =(FrameLayout)findViewById(R.id.overlay);
        overlay.setVisibility(View.GONE);
        overlay.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideFabMenu();
                rotateFabBackward();
                appBarLayout.setEnabled(true);
                overlay.setVisibility(View.GONE);
                overlay.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
                return true;
            }
        });


        appBarLayout = (AppBarLayout)findViewById(R.id.appbar);
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                // verticalOffset 이 음수일때는 toolbar가 접힌 상태값 -> 스크롤업하여 toolbar를 접었을 경우 플로팅액션 버튼 안보이게
                if(verticalOffset< 0 && !toolbar.getTitle().equals("홈")){
                    fab.hide();
                    fab_child_one.hide();
                    fab_child_two.hide();
                }else{
                    fab.show();
                    fab_child_one.show();
                    fab_child_two.show();
                }
            }
        });


        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                switch (tab.getPosition()){
                    case 0:
                        toolbar.setTitle("홈");
                        setGoneFab();
                        break;
                    case 1:
                        toolbar.setTitle("친구");
                        setVisibleFab(1);
                        break;
                    case 2:
                        toolbar.setTitle("채팅");
                        setVisibleFab(2);
                        break;
                    case 3:
                        toolbar.setTitle("프로필");
                        setGoneFab();
                        break;
                    default:
                }
                fabShowHide(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        Intent intent = getIntent();
        String contactRequest = intent.getStringExtra("FriendChatUid");
        String isPush = intent.getStringExtra("isPush");
        if(!TextUtils.isEmpty(contactRequest)){
            mViewPager.setCurrentItem(1); // 친추요청 푸시메시지를 통해 탭액티비티로 들어오면 친구목록 탭으로 이동
        }else if(!TextUtils.isEmpty(isPush)){
            mViewPager.setCurrentItem(2); // 채팅메시지 푸시를 통해 탭액티비티로 들어오면 채팅목록 탭으로 이동
        }else{
            mViewPager.setCurrentItem(0); // 나머지의 경우는 맨 처음 탭으로 이동
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(getApplicationContext());
        notificationManagerCompat.cancelAll(); //탭 화면 생성되면 날아와있던 노티들 삭제.

        //Set_On(); //로그인

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("friendChatRoom").child(currentUid);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                        long totalCount = 0;
                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                            if(postSnapshot.hasChild("badgeCount")) {
                                long count = (long) postSnapshot.child("badgeCount").getValue();
                                totalCount += count;
                            }
                        }
                        if (totalCount == 0) {
                            tabLayout.setBadgeText(2, null); //배지 카운트 0이면 2번째 인자값 null로 주면 VISIBLE GONE(8)으로 됨.
                        } else {
                            tabLayout.setBadgeText(2, String.valueOf(totalCount)); //탭레이아웃 배지세팅 각 채팅방의 배지의 총합값
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
    protected void onDestroy() {
        super.onDestroy();
        Set_Off(); //로그아웃
    }

    public static void translateAnim(float xStart, float xEnd, float yStart, float yEnd, int duration, RelativeLayout layout) {
        TranslateAnimation translateAnimation = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF,xStart,
                Animation.RELATIVE_TO_SELF, xEnd,
                Animation.RELATIVE_TO_SELF,yStart,
                Animation.RELATIVE_TO_SELF,yEnd);
        translateAnimation.setDuration(duration);
        translateAnimation.setFillAfter(true);
        layout.startAnimation(translateAnimation);
    }

    @Override
    public void onBackPressed() {
        long tempTime = System.currentTimeMillis();
        long intervalTime = tempTime - backPressedTime;

        if ( 0 <= intervalTime && FINSH_INTERVAL_TIME >= intervalTime ) {
            super.onBackPressed();
            finish();
        }
        else {
            backPressedTime = tempTime;
            Toast.makeText(getApplicationContext(),"'뒤로'버튼을 한번 더 누르면 종료됩니다.",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_tab, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            //로그아웃버튼 클릭 시 자동로그인 정보 제거 후 메인액티비티로

            SharedPreferences autoLogin = getSharedPreferences("autoLogin", MODE_PRIVATE);
            SharedPreferences.Editor editor = autoLogin.edit();
            editor.clear();
            editor.apply();

            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            //return PlaceholderFragment.newInstance(position + 1);

            switch (position) {
                case 0:
                    return new Fragment_Home();
                case 1:
                    return new Fragment_Friend();
                case 2:
                    return new Fragment_Chat();
                case 3:
                    return new Fragment_Profile();
            }
            return null;
        }

        @Override
        public int getCount() {
            return 4;
        }

    }

    public void Set_On() {
        //String Online = "접속중";
        //databaseReference.child(currentUid).child("state").setValue(Online); //DB에서의 명시적 로그인 상태 세팅
        //mAuth.signIn()은 메인액티비티에서 로그인되는 형태이기 때문에 메인액티비티에서 체크
    }

    public void Set_Off() {
        //String Offline = "접속종료";
        //databaseReference.child(currentUid).child("state").setValue(Offline); //DB에서의 명시적 로그아웃 상태 세팅.
        mAuth.signOut(); //Firebase 유저 상태 모니터링 리스너를 이용한 암묵적 로그아웃상태 세팅.
    }

    private void fabShowHide(int position){
        if(position == 1 || position == 2){
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!isFabOpen){
                        overlay.setVisibility(View.VISIBLE);
                        overlay.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
                        appBarLayout.setEnabled(false);
                        showFabMenu();
                        rotateFabForward();
                    }else{
                        overlay.setVisibility(View.GONE);
                        overlay.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
                        appBarLayout.setEnabled(true);
                        hideFabMenu();
                        rotateFabBackward();
                    }
                }
            });
        }
    }

    public void rotateFabForward() {
        ViewCompat.animate(fab)
                .rotation(45.0F)
                .withLayer()
                .setDuration(300)
                .setInterpolator(new OvershootInterpolator())
                .start();
    }

    public void rotateFabBackward() {
        ViewCompat.animate(fab)
                .rotation(0.0F)
                .withLayer()
                .setDuration(300)
                .setInterpolator(new OvershootInterpolator())
                .start();
    }

    public void showFabMenu(){
        isFabOpen=true;
        fab_text_1.setVisibility(View.VISIBLE);
        fab_text_2.setVisibility(View.VISIBLE);
        fab_text_1.bringToFront();
        fab_text_2.bringToFront(); //텍스트뷰를 오버레이보다 앞으로 끌어냄(fab는 원래 오버레이보다 앞쪽이라 필요X)
        fabTextsAni(fab_text_1,1);
        fabTextsAni(fab_text_2,1);
        fab_text_1.animate().translationY(-getResources().getDimension(R.dimen.dp55));
        fab_text_2.animate().translationY(-getResources().getDimension(R.dimen.dp105));
        fab_child_one.animate().translationY(-getResources().getDimension(R.dimen.dp55));
        fab_child_two.animate().translationY(-getResources().getDimension(R.dimen.dp105));
    }

    public void hideFabMenu(){
        isFabOpen=false;
        fabTextsAni(fab_text_1,0);
        fabTextsAni(fab_text_2,0);
        fab_child_one.animate().translationY(0);
        fab_child_two.animate().translationY(0);
    }

    public void fabTextsAni(final TextView textView, final float i){
        textView.animate().setDuration(300);
        textView.animate().translationY(i).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if(i == 0) {
                    textView.animate().alpha(0);
                }else{
                    textView.animate().alpha(1);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    public void setVisibleFab(int position){
        fab.setVisibility(View.VISIBLE);
        fab_child_one.setVisibility(View.VISIBLE);
        fab_child_two.setVisibility(View.VISIBLE);
        switch (position){
            case 1:
                fab_text_1.setText("이메일로 검색");
                fab_text_2.setText("이름으로 검색");
                fab_child_one.setImageDrawable(getResources().getDrawable(R.drawable.ic_email_black_24dp ,null));
                fab_child_one.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        intent = new Intent(getApplicationContext(), SearchFriendActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.putExtra("searchType", "email");
                        startActivity(intent);
                    }
                });
                fab_child_two.setImageDrawable(getResources().getDrawable(R.drawable.ic_person_add_black_24dp ,null));
                fab_child_two.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        intent = new Intent(getApplicationContext(), SearchFriendActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.putExtra("searchType", "name");
                        startActivity(intent);
                    }
                });
                break;
            case 2:
                fab_text_1.setText("그룹채팅");
                fab_text_2.setText("오픈채팅");
                fab_child_one.setImageDrawable(getResources().getDrawable(R.drawable.ic_group_black_24dp,null));
                fab_child_one.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        intent = new Intent(getApplicationContext(), GroupChatActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                });
                fab_child_two.setImageDrawable(getResources().getDrawable(R.drawable.ic_chat_bubble_black_24dp ,null));
                fab_child_two.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        intent = new Intent(getApplicationContext(), OpenChatActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                });
                break;
            default:
        }
    }

    public void setGoneFab(){
        fab.setVisibility(View.GONE);
        fab_child_one.setVisibility(View.GONE);
        fab_child_two.setVisibility(View.GONE);
        fab_text_1.setVisibility(View.GONE);
        fab_text_2.setVisibility(View.GONE);
    }

}
