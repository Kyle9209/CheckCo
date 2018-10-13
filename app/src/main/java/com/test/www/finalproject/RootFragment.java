package com.test.www.finalproject;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.app.Fragment;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class RootFragment extends Fragment {
    SweetAlertDialog progressDialog;

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
