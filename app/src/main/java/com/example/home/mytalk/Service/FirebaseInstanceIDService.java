package com.example.home.mytalk.Service;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by xnote on 2017-10-21.
 */

public class FirebaseInstanceIDService extends FirebaseInstanceIdService{
    private final static String TAG = "FCM_ID";


    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "FirebaseInstanceId Refreshed token: " + refreshedToken);

    }
}
