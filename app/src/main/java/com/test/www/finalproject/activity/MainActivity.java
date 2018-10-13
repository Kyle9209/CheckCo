package com.test.www.finalproject.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.test.www.finalproject.BuildConfig;
import com.test.www.finalproject.R;
import com.test.www.finalproject.net.Net;
import com.test.www.finalproject.util.U;

import java.util.List;

import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {
    MainActivity self;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 버터나이프 바이딩
        ButterKnife.bind(this);

        self = this;

        // 네트워크 체크
        if(U.getInstance().getNetworkType(self) == BuildConfig.NETWORK_NOT_AVAILABLE) {
            finish();
            return;
        }

        // 배지제거===================================================================================
        Intent intent = new Intent("android.intent.action.BADGE_COUNT_UPDATE");
        intent.putExtra("badge_count", 0);
        intent.putExtra("badge_count_package_name", getPackageName());
        intent.putExtra("badge_count_class_name", getLunchClass(self));
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
            intent.setFlags(0x00000020);
        }
        sendBroadcast(intent);
        U.getInstance().setInt(self, "BADGE_COUNT", 0);
        // =========================================================================================

        // 푸시 서버 전송 모듈 초기화
        Net.getInstance().launch(self);

        intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    public String getLunchClass(Context context){
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setPackage(getPackageName());
        List<ResolveInfo> resolveInfos = getPackageManager().queryIntentActivities(intent, 0);
        if(resolveInfos != null && resolveInfos.size() > 0){
            return resolveInfos.get(0).activityInfo.name;
        }
        return "";
    }
}
