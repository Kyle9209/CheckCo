package com.test.www.finalproject.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.otto.Bus;
import com.test.www.finalproject.BuildConfig;
import com.test.www.finalproject.CustomBus;
import com.test.www.finalproject.model.UserModel;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class U {
    // 싱글톤 기본 개체=====================================================
    private static final U ourInstance = new U();
    public static U getInstance() {
        return ourInstance;
    }
    private U() {}
    // 싱글톤 기본 개체=====================================================

    // 영속저장 =============================================================================
    String SAVE_TAG = "ref";
    // String data
    public void setString(Context context, String key, String value){
        SharedPreferences.Editor editor = context.getSharedPreferences(SAVE_TAG, 0).edit();
        editor.putString(key, value);
        editor.commit();
    }
    public String getString(Context context, String key){
        return context.getSharedPreferences(SAVE_TAG, 0).getString(key, "");
    }
    // Int data
    public void setInt(Context context, String key, int value){
        SharedPreferences.Editor editor = context.getSharedPreferences(SAVE_TAG, 0).edit();
        editor.putInt(key, value);
        editor.commit();
    }
    public int getInt(Context context, String key){
        return context.getSharedPreferences(SAVE_TAG, 0).getInt(key, 0);
    }
    // Boolean data
    public void setBoolean(Context context, String key, boolean value){
        SharedPreferences.Editor editor = context.getSharedPreferences(SAVE_TAG, 0).edit();
        editor.putBoolean(key, value);
        editor.commit();
    }
    public boolean getBoolean(Context context, String key){
        return context.getSharedPreferences(SAVE_TAG, 0).getBoolean(key, false);
    }
    // 영속저장 =============================================================================

    public void showPopup(Context context, int type, String title, String msg,
                           String cName, SweetAlertDialog.OnSweetClickListener cEvent,
                           String oName, SweetAlertDialog.OnSweetClickListener oEvent) {
        new SweetAlertDialog(context, type)
                .setTitleText(title)
                .setContentText(msg)
                .setConfirmText(cName)
                .setConfirmClickListener(cEvent)
                .setCancelText(oName)
                .setCancelClickListener(oEvent)
                .show();
    }

    public void showPopup(Context context, int type, String title, String msg,
                          String cName, SweetAlertDialog.OnSweetClickListener cEvent) {
        new SweetAlertDialog(context, type)
                .setTitleText(title)
                .setContentText(msg)
                .setConfirmText(cName)
                .setConfirmClickListener(cEvent)
                .show();
    }

    // 로그인 되었을때 가지고 있는 나의 데이터
    UserModel userModel;
    public UserModel getUserModel() {
        return userModel;
    }
    public void setUserModel(UserModel userModel) {
        this.userModel = userModel;
    }

    // 파이어베이스 인증 객체 획득
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    public FirebaseAuth getFirebaseAuth() {
        return firebaseAuth;
    }

    // 파이어베이스 데이터레퍼런스 객체 획득
    DatabaseReference dr = FirebaseDatabase.getInstance().getReference();
    public DatabaseReference getDr() {
        return dr;
    }

    // 오토버스 객체
    Bus bus = new Bus();
    public Bus getBus() {
        return bus;
    }
    CustomBus customBus = new CustomBus();
    public CustomBus getCustomBus() {
        return customBus;
    }

    // 네트워크 사용 여부
    public int getNetworkType(Context context){
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo.State wifi = connectivityManager.getNetworkInfo(1).getState(); // 와이파이 체크
        if(wifi == NetworkInfo.State.CONNECTED || wifi == NetworkInfo.State.CONNECTING){
            return BuildConfig.NETWORK_WIFI;
        }
        NetworkInfo.State mobile = connectivityManager.getNetworkInfo(0).getState(); // 엘티이, 쓰리지 체크
        if(mobile == NetworkInfo.State.CONNECTED || mobile == NetworkInfo.State.CONNECTING){
            return BuildConfig.NETWORK_MOBILE;
        }
        return BuildConfig.NETWORK_NOT_AVAILABLE;
    }

    String chattingYourID = "null";
    public String getChattingYourID() {
        return chattingYourID;
    }
    public void setChattingYourID(String chattingYourID) {
        this.chattingYourID = chattingYourID;
    }
}
