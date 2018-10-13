package com.test.www.finalproject;

import android.support.multidex.MultiDexApplication;

import com.miguelbcr.ui.rx_paparazzo2.RxPaparazzo;

public class MyApplication extends MultiDexApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        // 파파라조 라이브러리 등록
        RxPaparazzo.register(this);
    }
}
