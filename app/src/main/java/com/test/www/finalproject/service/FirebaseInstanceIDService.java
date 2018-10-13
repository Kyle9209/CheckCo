package com.test.www.finalproject.service;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * 사용자별 고유 토큰 발급
 * 토큰이 변경되면 서버측에 제공하여 갱신
 */

public class FirebaseInstanceIDService extends FirebaseInstanceIdService {
    private static final String TAG = "MyFirebaseIIDService";

    @Override // 최초 발급 및 갱신
    public void onTokenRefresh() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);

        sendRegistrationToServer(refreshedToken);
    }

    private void sendRegistrationToServer(String token) {
        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            // 서버로 토큰 재전송
        }
    }
}