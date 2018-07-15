package com.example.home.mytalk.Activity;


import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.home.mytalk.Adapter.FriendMapAdapter;
import com.example.home.mytalk.Model.Friend;
import com.example.home.mytalk.R;
import com.example.home.mytalk.Utils.CircleTransform;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.os.Build.VERSION_CODES.M;


public class MapActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener{


    private GoogleApiClient mGoogleApiClient = null;
    public GoogleMap mGoogleMap = null;
    private Marker currentMarker = null;

    //디폴트 위치, Seoul
    private static final LatLng DEFAULT_LOCATION = new LatLng(37.56, 126.97);
    private static final String TAG = "googlemap_example";
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 2002;
    private static final int UPDATE_INTERVAL_MS = 1000;  // 1초
    private static final int FASTEST_UPDATE_INTERVAL_MS = 1000; // 1초
    private AppCompatActivity mActivity;
    private boolean askPermissionOnceAgain = false;
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    public String email;
    private String stUid;
    private ImageView imageView;
    public RecyclerView mRecyclerView;
    public LinearLayoutManager mLayoutManager;
    public List<Friend> mFriend;
    private FriendMapAdapter mAdapter;
    private ValueEventListener valueEventListener;
    private ValueEventListener mFriendValueListener;
    private String contact;
    private String myName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_map);
        mActivity = this;
        MapFragment mapFragment = (MapFragment)getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        SharedPreferences sharedPreferences = this.getSharedPreferences("email", MODE_PRIVATE );
        stUid = sharedPreferences.getString("uid", "");
        email = sharedPreferences.getString("email", "");
        myRef = FirebaseDatabase.getInstance().getReference("users");
        mRecyclerView = (RecyclerView)findViewById(R.id.mapFriend);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mFriend = new ArrayList<>();
        mAdapter = new FriendMapAdapter(mFriend , this);
        mRecyclerView.setAdapter(mAdapter);


    }

    @Override
    public void onMapReady(GoogleMap map) {

        Log.d(TAG, "onMapReady");
        mGoogleMap = map;

        setCurrentLocation(DEFAULT_LOCATION, "위치정보 가져올 수 없음", "위치 권한과 GPS 활성 여부 확인하세요",null);
        setRunTimePermission();

        final double F_latitude = getIntent().getDoubleExtra("latitude", 37.56);
        final double F_longitude = getIntent().getDoubleExtra("longitude", 126.97);
        final LatLng F_Latlng = new LatLng(F_latitude, F_longitude);

        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(F_Latlng, 17.0f));  //맵 로딩시 친구목록에서 위치요청한 친구의 위치로 이동
        mGoogleMap.getUiSettings().setCompassEnabled(true);
        mGoogleMap.getUiSettings().setZoomControlsEnabled(true);
        mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        getFriebaseAddUserReference(); //친추목록 참조값

    }

    private void setRunTimePermission(){
        if (Build.VERSION.SDK_INT >= M) {
            //API 23 이상이면 런타임 퍼미션 처리 필요
            int hasFineLocationPermission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);

            if (hasFineLocationPermission == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions( mActivity,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            } else {
                if (mGoogleApiClient == null) {
                    buildGoogleApiClient();
                }
                if (ActivityCompat.checkSelfPermission(this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    mGoogleMap.setMyLocationEnabled(true);
                }
            }
        } else {
            if (mGoogleApiClient == null) {
                buildGoogleApiClient();
            }
            mGoogleMap.setMyLocationEnabled(true);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mGoogleApiClient != null)
            mGoogleApiClient.connect();
    }


    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient!=null)
            mGoogleApiClient.connect();

        //앱 정보에서 퍼미션을 허가했는지를 다시 검사해봐야 한다.

        if (askPermissionOnceAgain) {
            if (Build.VERSION.SDK_INT >= M) {
                askPermissionOnceAgain = false;
                checkPermissions();

            }
        }

    }


    @Override
    protected void onStop() {
        super.onStop();
        //myRef.removeEventListener(childEventListener);   // 리스너 제거
        myRef.removeEventListener(valueEventListener);

        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.unregisterConnectionCallbacks(this);
            mGoogleApiClient.unregisterConnectionFailedListener(this);
            if (mGoogleApiClient.isConnected()) {
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,  this);
                mGoogleApiClient.disconnect();
            }
        }
    }



//////-----------------------------------------------------------------------------------------------------------------------------------/////////


    @Override
    public void onLocationChanged(Location location) {
        Log.d( TAG, "onLocationChanged");
        setGPS(location); //좌표 DB SET
    }


//////-----------------------------------------------------------------------------------------------------------------------------------/////////

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }


    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d(TAG,"onConnected");
        if (!checkLocationServicesStatus()) {
            showDialogForLocationServiceSetting();
        }
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL_MS);
        locationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_MS);

        if (Build.VERSION.SDK_INT >= M) {
            if (ActivityCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, this);
            }
        }
        else{
            Log.d(TAG,"onConnected : call FusedLocationApi");
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, this);
            mGoogleMap.getUiSettings().setCompassEnabled(true);
            //mGoogleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult  connectionResult) {
        Location location = null;
        location.setLatitude(DEFAULT_LOCATION.latitude);
        location.setLongitude(DEFAULT_LOCATION.longitude);
        //setCurrentLocation(location, "위치정보 가져올 수 없음", "위치 권한과 GPS 활성 여부 확인하세요");
    }

    @Override

    public void onConnectionSuspended(int cause) {
        if ( cause ==  CAUSE_NETWORK_LOST )
            Log.e(TAG, "onConnectionSuspended(): Google Play services " + "connection lost.  Cause: network lost.");
        else if (cause == CAUSE_SERVICE_DISCONNECTED )
            Log.e(TAG,"onConnectionSuspended():  Google Play services " + "connection lost.  Cause: service disconnected");
    }


    public String getCurrentAddress(Location location){

        //지오코더... GPS를 주소로 변환
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

        } catch (IOException ioException) {
            //네트워크 문제
            Toast.makeText(this, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";
        }

        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";
        } else {
            Address address = addresses.get(0);
            return address.getAddressLine(0).toString();
        }
    }


    public boolean checkLocationServicesStatus(){
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    public void setCurrentLocation(final LatLng latlng, final String markerTitle, final String markerSnippet, String photo) {

        View view = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.view_custom_marker, null);
        imageView = (ImageView)view.findViewById(R.id.profile_image);
        //DB로 부터 넘어오는 이미지 URL을 Glide의 into()함수에 인자값으로 넣기위해 사용될 이미지뷰 초기화. 오버라이딩함수에서 사용되기 때문에 전역변수로 선언
        Glide.with(getApplicationContext())
                .load(photo)
                .asBitmap()
                .transform(new CircleTransform(getApplicationContext()),new FitCenter(getApplicationContext()))
                .override(200,200)
                .listener(new RequestListener<String, Bitmap>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap bitmap, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.position(latlng);
                        markerOptions.title(markerTitle);
                        markerOptions.snippet(markerSnippet);
                        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(bitmap, markerTitle)));
                        mGoogleMap.addMarker(markerOptions);
                        return false;
                    }
                }).into(imageView);
    }

    private Bitmap getMarkerBitmapFromView(Bitmap bitmap, String markerTitle) {
        View view = ((LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.view_custom_marker, null);
        ImageView backMarkerimageView = (ImageView)view.findViewById(R.id.profile_image); //XML 리소스로 때려박은 핀형태의 백그라운드 이미지 초기화.
        TextView textMe = (TextView)view.findViewById(R.id.textMe);
        textMe.setVisibility(View.GONE);
        if(markerTitle.equals(myName)){
            textMe.setVisibility(View.VISIBLE);  //내 위치 표시 마커에는 ME! 텍스트 표시
        }
        backMarkerimageView.setImageBitmap(bitmap);
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.buildDrawingCache();
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN);
        Drawable drawable = view.getBackground();
        if (drawable != null)
            drawable.draw(canvas);
        view.draw(canvas);
        return returnedBitmap;
    }


    public void setGPS(Location location){
        String latitude = String.valueOf(location.getLatitude());
        String longitude = String.valueOf(location.getLongitude());
        myRef.child(stUid).child("latitude").setValue(latitude);
        myRef.child(stUid).child("longitude").setValue(longitude);
    }


    // 런타임 퍼미션 처리을 위한 메소드
    @TargetApi(M)
    private void checkPermissions() {
        boolean fineLocationRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION);

        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);

        if (hasFineLocationPermission == PackageManager.PERMISSION_DENIED && fineLocationRationale )
            showDialogForPermission("앱을 실행하려면 권한을 허가하셔야합니다.");

        else if (hasFineLocationPermission == PackageManager.PERMISSION_DENIED && !fineLocationRationale ) {
            showDialogForPermissionSetting("퍼미션 거부 + Don't ask again(다시 묻지 않음) " + "체크 박스를 설정한 경우로 설정에서 권한 허가해야합니다.");
        }

        else if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED) {
            if (mGoogleApiClient == null) {
                buildGoogleApiClient();
            }
            mGoogleMap.setMyLocationEnabled(true);
        }

    }


    @Override
    public void onRequestPermissionsResult(int permsRequestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (permsRequestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION && grantResults.length > 0) {

            boolean permissionAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
            if (permissionAccepted) {
                if (mGoogleApiClient == null) {
                    buildGoogleApiClient();
                }
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    mGoogleMap.setMyLocationEnabled(true);
                }
            }
            else {
                checkPermissions();
            }
        }
    }


    @TargetApi(M)  //타겟 API 버전 - 마시멜로
    private void showDialogForPermission(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
        builder.setTitle("알림");
        builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int id) {
                ActivityCompat.requestPermissions( mActivity, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }

        });

        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        });
        builder.create().show();
    }

    private void showDialogForPermissionSetting(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
        builder.setTitle("알림");
        builder.setMessage(msg);
        builder.setCancelable(true);
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int id) {
                askPermissionOnceAgain = true;
                Intent myAppSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + mActivity.getPackageName()));
                myAppSettings.addCategory(Intent.CATEGORY_DEFAULT);
                myAppSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mActivity.startActivity(myAppSettings);
            }

        });

        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        });
        builder.create().show();
    }


    // GPS 활성화를 위한 메소드
    private void showDialogForLocationServiceSetting() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n" + "위치 설정을 수정하시겠습니까?" );
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });

        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id){
                dialog.cancel();
            }
        });
        builder.create().show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case GPS_ENABLE_REQUEST_CODE:
                //사용자가 GPS 활성 시켰는지 검사

                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {
                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        if (Build.VERSION.SDK_INT >= M) {
                            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                mGoogleMap.setMyLocationEnabled(true);
                            }
                        }
                        else  mGoogleMap.setMyLocationEnabled(true);

                        return;
                    }
                }
                else{
                    setCurrentLocation(DEFAULT_LOCATION, "위치정보 가져올 수 없음",null,"위치 권한과 GPS 활성 여부 확인하세요");
                }
                break;
        }
    }

    private void getFriebaseAddUserReference(){
        DatabaseReference FriendRef = FirebaseDatabase.getInstance().getReference("AddFriend").child(stUid);
        mFriendValueListener = FriendRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren()){   //추가된 친구가 있으면
                    contact = dataSnapshot.getValue().toString(); //추가된 친구의 UID정보(= 추가된 친구 목록)가져옴
                    getFreiendReference(contact); //친구 정보및 마커 참조값
                    Log.d(TAG, "친추목록값 : " + contact);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    private void getFreiendReference(final String contact) {
        valueEventListener = myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //데이터에 변화가 있으면 지도를 다 지우고 foreach loop를 통해 데이터 다시받아옴
                mGoogleMap.clear(); //지도 클리어
                mFriend.clear(); //하단 친구목록 리스트 클리어
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Friend friend = postSnapshot.getValue(Friend.class);
                    String key = friend.getKey(); //모든 유저의 키값을 가져옴
                    if (!TextUtils.isEmpty(contact) && contact.contains(key)) { // contact(친추목록값)에 포함된 유저의 키(key)만 뽑아서
                        String id = friend.getName();
                        String stPhoto = friend.getPhoto();
                        double lat = Double.parseDouble(friend.getLatitude());
                        double lon = Double.parseDouble(friend.getLongitude());
                        LatLng latlng = new LatLng(lat, lon);
                        setCurrentLocation(latlng, id, null, stPhoto); //이름, 사진, 위/경도를 가지고 마커 찍어줌.
                        mFriend.add(friend);
                        if (key.equals(stUid)) {
                            mFriend.remove(friend);
                        } //하단 친구목록 리스트 세팅
                        mAdapter.notifyDataSetChanged();
                    }
                }
                String myPhoto = dataSnapshot.child(stUid).child("photo").getValue().toString();
                myName = dataSnapshot.child(stUid).child("name").getValue().toString();
                double mylat = Double.parseDouble(dataSnapshot.child(stUid).child("latitude").getValue().toString());
                double mylon = Double.parseDouble(dataSnapshot.child(stUid).child("longitude").getValue().toString());
                LatLng myLatLon = new LatLng(mylat,mylon);
                setCurrentLocation(myLatLon, myName, null, myPhoto);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

    }

}