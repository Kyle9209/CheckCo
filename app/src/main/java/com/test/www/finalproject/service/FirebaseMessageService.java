package com.test.www.finalproject.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.test.www.finalproject.R;
import com.test.www.finalproject.activity.MainActivity;
import com.test.www.finalproject.model.PushModel;
import com.test.www.finalproject.model.UserModel;
import com.test.www.finalproject.util.U;

import java.util.List;

public class FirebaseMessageService extends FirebaseMessagingService {
    FirebaseMessageService self = this;
    private static final String TAG = "MyFirebaseMsgService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        PushModel pushModel = new Gson().fromJson(remoteMessage.getData().get("data"), PushModel.class);

        // 관리자가 사원의 출퇴근 푸시를 받을떄
        if(pushModel.getChannel().equals("admin")){
            sendNotification(pushModel.getSender(), pushModel.getMsg());
            return;
        }
        // 채팅중이라면
        if(U.getInstance().getChattingYourID().equals(pushModel.getSender())){
            U.getInstance().getDr().child("channels").child(U.getInstance().getFirebaseAuth().getCurrentUser().getUid())
                    .child(pushModel.getSender()).child("readCnt").setValue(0).addOnCompleteListener(task -> {});
            U.getInstance().getCustomBus().post("listUp");
            return;
        }
        // 배지추가===================================================================================
/*        int count = U.getInstance().getInt(this, "BADGE_COUNT");
        Intent intent = new Intent("android.intent.action.BADGE_COUNT_UPDATE");
        intent.putExtra("badge_count", count+1);
        intent.putExtra("badge_count_package_name", getPackageName());
        intent.putExtra("badge_count_class_name", getLunchClass(this));
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
            intent.setFlags(0x00000020);
        }
        sendBroadcast(intent);
        U.getInstance().setInt(this, "BADGE_COUNT", count+1);*/
        // =========================================================================================
        // 노티 발생
        sendNotification(pushModel.getSender(), pushModel.getMsg());
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

    private void sendNotification(String sender, String messageBody) {
        FirebaseDatabase.getInstance().getReference().child("users").child(sender)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UserModel userModel = dataSnapshot.getValue(UserModel.class);

                Intent intent = new Intent(self, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                PendingIntent pendingIntent = PendingIntent.getActivity(self, 0 /* Request code */, intent,
                        PendingIntent.FLAG_ONE_SHOT);

                Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(self)
                        .setSmallIcon(R.mipmap.ic_chat_bubble_outline_white_24dp)
                        .setContentTitle(userModel.getName())
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

                NotificationManager notificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }
}
