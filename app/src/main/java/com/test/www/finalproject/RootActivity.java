package com.test.www.finalproject;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class RootActivity extends AppCompatActivity {
    SweetAlertDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void showPD(Context context){
        progressDialog = new SweetAlertDialog(context, SweetAlertDialog.PROGRESS_TYPE);
        progressDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        progressDialog.setTitleText("잠시만 기다려주세요...");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    public void stopPD() {
        if(progressDialog != null && progressDialog.isShowing()){
            progressDialog.dismissWithAnimation();
        }
    }
}
