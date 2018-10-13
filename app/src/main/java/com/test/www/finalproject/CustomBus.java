package com.test.www.finalproject;

import android.os.Handler;
import android.os.Looper;

import com.squareup.otto.Bus;

public class CustomBus extends Bus {
    private final Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    public void post(final Object event) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            super.post(event);
        } else {
            mHandler.post(() -> CustomBus.super.post(event));
        }
    }

}