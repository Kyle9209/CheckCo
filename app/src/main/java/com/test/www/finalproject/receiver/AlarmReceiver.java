package com.test.www.finalproject.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.test.www.finalproject.service.AlarmService;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent mIntent = new Intent(context, AlarmService.class);
        mIntent.putExtra("state", intent.getStringExtra("state"));
        context.startService(mIntent);
    }
}
