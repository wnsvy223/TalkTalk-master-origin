package com.example.home.mytalk.Fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.home.mytalk.Activity.FullScreenActivity;
import com.example.home.mytalk.BuildConfig;
import com.example.home.mytalk.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;
import static android.os.Build.VERSION_CODES.M;


public class Fragment_Profile extends android.support.v4.app.Fragment {
    public CircleImageView ivUser;
    public ImageView ivUserBack;
    private StorageReference mStorageRef;
    private DatabaseReference profileRef;
    public Bitmap bitmap;
    public ListView Profile_list;
    public ArrayAdapter<String> P_Adapter;
    private String currentUid;
    public String TAG = getClass().getSimpleName();
    public ProgressBar progressBar;
    public FloatingActionButton camera;
    private Uri fileUri;
    public  static final int UPLOAD_REQUEST_CODE = 1;
    public  static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 2;
    private boolean askPermissionOnceAgain = false;
    public TextView textId;
    public TextView textName;
    public TextView textPhoneNum;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_profile, container, false);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("email", Context.MODE_PRIVATE);
        currentUid = sharedPreferences.getString("uid", "");

        mStorageRef = FirebaseStorage.getInstance().getReference();
        profileRef = FirebaseDatabase.getInstance().getReference().child("users").child(currentUid);

        //P_Adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1);
        //Profile_list = (ListView) v.findViewById(R.id.Profile_list);
        //Profile_list.setAdapter(P_Adapter);
        //Profile_list.setOnItemClickListener(onClickListItem);

        textId = (TextView)v.findViewById(R.id.textId);
        textName = (TextView)v.findViewById(R.id.textName);
        textPhoneNum = (TextView)v.findViewById(R.id.textPhoneNum);
        editProfile(textId,"Id");
        editProfile(textName,"Name");
        editProfile(textPhoneNum,"PhoneNum");

        progressBar = (ProgressBar) v.findViewById(R.id.Progress);
        ivUserBack = (ImageView)v.findViewById(R.id.ivUserBack);
        ivUserBack.setColorFilter(Color.parseColor("#BDBDBD"), PorterDuff.Mode.MULTIPLY); //이미지뷰 어둡게 효과주기
        ivUser = (CircleImageView) v.findViewById(R.id.ivUser);
        ivUser.setBackground(new ShapeDrawable(new OvalShape()));
        ivUser.setOnClickListener(new View.OnClickListener() {
                                      @Override
                                      public void onClick(View v) {
                                          Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                          startActivityForResult(intent, UPLOAD_REQUEST_CODE );
                                      }
                                  }
        );

        camera = (FloatingActionButton) v.findViewById(R.id.camera);
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);  //내장 카메라 호출
                if(fileUri != null) {
                    fileUri = FileProvider.getUriForFile(getContext(), BuildConfig.APPLICATION_ID + ".provider", getOutputMediaFile());
                }
                intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivityForResult(intent,CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
            }
        });

        getProfilePhotoReference();
        getProfileListReference();
        return v;
    }


    public void editProfile(TextView textView, final String type){
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (type){
                    case "Id":
                        showDialog(textId);
                        break;
                    case "Name":
                        showDialog(textName);
                        break;
                    case "PhoneNum":
                        showDialog(textPhoneNum);
                        break;

                }
            }
        });
    }

    public void showDialog(final TextView textView) {
        final EditText edittext = new EditText(getActivity());

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("프로필 변경");
        builder.setMessage("변경 내용을 입력하세요");
        builder.setView(edittext);
        builder.setPositiveButton("입력",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String EditValue = edittext.getText().toString();
                        updateList(EditValue , textView);
                    }
                });
        builder.setNegativeButton("취소",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        builder.show();
    }

    public void updateList(String edit, TextView textView){
        switch (textView.getId()){
            case R.id.textId:
                profileRef.child("email").setValue(edit);
                break;
            case R.id.textName:
                profileRef.child("name").setValue(edit);
                break;
            case R.id.textPhoneNum:
                profileRef.child("phone").setValue(edit);
                break;
            default:
        }
    }

    private void getProfileListReference(){
        profileRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String id = dataSnapshot.child("email").getValue().toString();
                String name = dataSnapshot.child("name").getValue().toString();
                String phone = dataSnapshot.child("phone").getValue().toString();
                textId.setText("ID(E-mail) : "+id);
                textName.setText("이 름 : "+name);
                textPhoneNum.setText("휴대폰 번호 : "+phone);
                //P_Adapter.clear();
                //P_Adapter.add("ID(E-Mail) : "+ id);
                //P_Adapter.add("이름 : "+ name);
                //P_Adapter.add("휴대폰 번호 : "+phone);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getProfilePhotoReference(){
        profileRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String stPhoto = dataSnapshot.child("photo").getValue().toString();
                if(TextUtils.isEmpty(stPhoto)){
                    progressBar.setVisibility(View.GONE);
                }else{
                    progressBar.setVisibility(View.VISIBLE);
                    Picasso.with(getActivity()).load(stPhoto).fit().centerInside().into(ivUser, new Callback.EmptyCallback() {
                        @Override public void onSuccess() {
                            progressBar.setVisibility(View.GONE);
                            Log.d(TAG, "FireBaseStorage 사진 받아오기 성공");
                        }

                        @Override
                        public void onError() {
                            super.onError();
                            progressBar.setVisibility(View.GONE);
                            ivUser.setVisibility(View.GONE);
                        }
                    });
                    Picasso.with(getActivity()).load(stPhoto).fit().centerCrop().into(ivUserBack);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

    }


    private static File getOutputMediaFile() {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MyTalkCamera");

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs())
                Log.d("MyTalkCamera", "디렉토리 생성 실패");
            return null;
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp +".jpg");

        return mediaFile;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == UPLOAD_REQUEST_CODE && resultCode == RESULT_OK) {  //프로필 사진 업로드를 위해 호출된 갤러리 액티비티의 결과 콜백
            if (data != null) {
                Uri image = data.getData();
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), image);
                    ivUser.setImageBitmap(bitmap);
                    uploadImage();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(getActivity(), "프로필 사진 설정이 취소되었습니다", Toast.LENGTH_SHORT).show();
            }
        }

        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {  //사진촬영을 위한 내장카메라 액티비티의 결과 콜백
            if (fileUri != null) {
                File tmepImg = new File(fileUri.getPath());
                if (!tmepImg.exists()) {
                    Log.d(TAG, "이미지가 존재하지 않습니다." + tmepImg.toString());
                    try {
                        FileOutputStream fos = new FileOutputStream(new File(fileUri.getPath()));
                        try {
                            fos.write(data.getExtras().getByte("data"));
                            fos.close();
                        } catch (IOException e) {
                            Log.d(TAG, "IOException." + e.getMessage());
                            e.printStackTrace();
                        }
                    } catch (FileNotFoundException e) {
                        Log.d(TAG, "이미지가 존재하지 않습니다." + tmepImg.toString());
                        e.printStackTrace();
                    }
                }
            }
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            case 2:
            {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public void uploadImage(){
        StorageReference mountainsRef = mStorageRef.child("users").child(currentUid +".jpg");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        final byte[] data = baos.toByteArray();

        UploadTask uploadTask = mountainsRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads

            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @SuppressWarnings("VisibleForTests")
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                String photoUrl = String.valueOf(downloadUrl);
                Log.d("url", photoUrl);

                DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("users");
                myRef.child(currentUid).child("photo").setValue(photoUrl);
                myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String s = dataSnapshot.getValue().toString();
                        Log.d("Profile",s);
                        if(dataSnapshot != null){
                            Toast.makeText(getActivity(), "사진 업로드 성공",Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });

    }
/*
    public AdapterView.OnItemClickListener onClickListItem = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            switch (position){
                case 0:
                    showDialog(0);
                    break;
                case 1:
                    showDialog(1);
                    break;
                case 2:
                    showDialog(2);
                    break;

                default:
            }
        }
    };
*/





    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkPermission();
    }

    private void checkPermission(){
        if(Build.VERSION.SDK_INT >= M){
            if (ContextCompat.checkSelfPermission(getActivity(),
                    android.Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getActivity(),
                    android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                        android.Manifest.permission.CAMERA) && ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                        android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                } else {
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[]{android.Manifest.permission.CAMERA , android.Manifest.permission.READ_EXTERNAL_STORAGE},
                            CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
                }
            } // 내장카메라 호출 , 외부 저장소 접근 권한 체크
        }
    }

    @Override
    public void onResume() {  //--------- 권한설정 끄고 다시하면 체크를 안함 왜일까...-------------
        super.onResume();

        if (askPermissionOnceAgain) {
            if (Build.VERSION.SDK_INT >= M) {
                askPermissionOnceAgain = false;
                checkPermission();
            }
        }
    }
}