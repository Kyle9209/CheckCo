package com.test.www.finalproject.fragment;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.miguelbcr.ui.rx_paparazzo2.RxPaparazzo;
import com.miguelbcr.ui.rx_paparazzo2.entities.FileData;
import com.squareup.otto.Subscribe;
import com.test.www.finalproject.R;
import com.test.www.finalproject.RootFragment;
import com.test.www.finalproject.activity.BeaconActivity;
import com.test.www.finalproject.activity.ChangeActivity;
import com.test.www.finalproject.activity.MainActivity;
import com.test.www.finalproject.model.CompanyModel;
import com.test.www.finalproject.model.UserModel;
import com.test.www.finalproject.util.U;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import cn.pedant.SweetAlert.SweetAlertDialog;
import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static android.app.Activity.RESULT_OK;

public class SettingFragment extends RootFragment {
    @BindView(R.id.user_Img_civ) CircleImageView profile;
    @BindView(R.id.dept_btn) Button dept;
    @BindView(R.id.position_btn) Button position;
    @BindView(R.id.name_btn) Button name;
    @BindView(R.id.number_btn) Button tel;
    @BindView(R.id.company_btn) Button companyBtn;
    @BindView(R.id.company_line) LinearLayout companyLine;
    @BindView(R.id.admin_line) LinearLayout adminLine;
    @BindView(R.id.toolbar_title_tv) TextView title;
    UserModel userModel;
    View view;
    Unbinder unbinder;

    public SettingFragment() {}

    @Subscribe
    public void ottoBus(String str){
        if(str.equals("updateUserData")) {
            userModel = U.getInstance().getUserModel();
            if(userModel.getLevel() == 1){ // 사원
                if(!userModel.getCompany().getcName().equals("null")){
                    // 회사가 있거나 생김 -> 회사, 직급, 부서 보임 & 출퇴근 알람 셋팅 & 회사에 내 정보 업데이트
                    companyLine.setVisibility(View.VISIBLE);

                    String myCompany = userModel.getCompany().getcName();

                    // 회사 출퇴근 알람 셋팅
                    U.getInstance().getDr().child("companies").child(myCompany).child("time")
                            .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            CompanyModel.Time time = dataSnapshot.getValue(CompanyModel.Time.class);
                            setAlarmManager(
                                    time.getInTime().getHour(),
                                    time.getInTime().getMinute(), "출근", 1001);
                            setAlarmManager(
                                    time.getOutTime().getHour(),
                                    time.getOutTime().getMinute(), "퇴근", 1002);
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });

                    // 회사에 내 정보 업데이트
                    U.getInstance().getDr().child("companies").child(myCompany).child("userList").child(userModel.getUid())
                            .setValue(userModel).addOnCompleteListener(task -> {});
                }
            } else { // 관리자
                adminLine.setVisibility(View.VISIBLE);
                companyLine.setVisibility(View.VISIBLE);
            }

            companyBtn.setText(userModel.getCompany().getcName());
            dept.setText(userModel.getCompany().getDept());
            position.setText(userModel.getCompany().getPosition());
            name.setText(userModel.getName());
            tel.setText(userModel.getTel());

            Glide.with(profile.getContext())
                    .load(userModel.getImg())
                    .error(R.mipmap.ic_account_circle_white_48dp)
                    .into(profile);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_setting, container, false);
        unbinder = ButterKnife.bind(this, view);
        U.getInstance().getBus().register(this);

        title.setText("마이페이지");
        Drawable drawable = ContextCompat.getDrawable(getContext(), R.mipmap.ic_settings_white_24dp);
        drawable.setBounds(0,0,drawable.getIntrinsicWidth(),drawable.getIntrinsicHeight());
        title.setCompoundDrawables(drawable, null, null, null);
        this.title.setCompoundDrawablePadding(10);

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
        U.getInstance().getBus().unregister(this);
    }

    @OnClick(R.id.logoutBtn)
    public void logOut() {
        if(U.getInstance().getFirebaseAuth().getCurrentUser() != null){
            U.getInstance().showPopup(getContext(), SweetAlertDialog.WARNING_TYPE,  "로그아웃 하시겠습니까?", null,
                    "예", sweetAlertDialog -> {
                        // 로그인 세션 종료 처리
                        U.getInstance().getFirebaseAuth().signOut();
                        startActivity(new Intent(getContext(), MainActivity.class));
                        getActivity().finish();
                    },
                    "아니오", SweetAlertDialog::dismissWithAnimation);
        }
    }

    @OnClick(R.id.user_Img_civ)
    public void onClickImg(){
        U.getInstance().showPopup(getContext(), SweetAlertDialog.SUCCESS_TYPE,
                "사진을 가져올 방법을 선택하세요",
                null,
                "파일",
                sweetAlertDialog -> {
                    onFile();
                    sweetAlertDialog.dismiss();
                },
                "사진",
                sweetAlertDialog -> {
                    onPhoto();
                    sweetAlertDialog.dismissWithAnimation();
                }
        );
    }

    public void onPhoto(){
        UCrop.Options options = new UCrop.Options();
        options.setToolbarColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryDark));

        RxPaparazzo.single(this)
                .crop(options)
                .usingGallery()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    // See response.resultCode() doc
                    if (response.resultCode() != RESULT_OK) {
                        return;
                    }
                    bind(response.data());
                });
    }

    public void onFile(){
        UCrop.Options options = new UCrop.Options();
        options.setToolbarColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryDark));

        RxPaparazzo.single(this)
                .crop(options)
                .usingFiles()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    // See response.resultCode() doc
                    if (response.resultCode() != RESULT_OK) {
                        return;
                    }
                    bind(response.data());
                });
    }

    void bind(FileData fileData) {
        // 이미지를 서버로 전송 -> firebase
        showPD(getContext());
        // 스토리지 획득
        FirebaseStorage storage = FirebaseStorage.getInstance();
        // 저장소의 경로
        StorageReference reference = storage.getReference().child("user_imgs");
        // 업로드할 파일 준비
        File uploadFile = fileData.getFile();
        // Uri 전환
        Uri uri = Uri.fromFile(uploadFile);
        // 업로드 경로 설정
        StorageReference uploadPath = reference.child(userModel.getUid());
        // 업로드
        UploadTask uploadTask = uploadPath.putFile(uri);
        // 성공과 실패
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            // 성공하면
            // 다운로드URL 획득
            String downloadUrl = taskSnapshot.getDownloadUrl().toString();
            // 다운로드URL을 유저 데이터에 업로드
            U.getInstance().getDr().child("users").child(userModel.getUid()).child("img")
                    .setValue(downloadUrl).addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    Glide.with(profile.getContext())
                            .load(downloadUrl)
                            .error(R.mipmap.ic_account_circle_black_48dp)
                            .into(profile);
                    if(!userModel.getCompany().getcName().equals("null")){
                        U.getInstance().getDr().child("companies").child(userModel.getCompany().getcName())
                                .child("userList").child(userModel.getUid()).child("img")
                                .setValue(downloadUrl).addOnCompleteListener(task1 -> {});
                    }
                } else {
                    Toast.makeText(getContext(), "데이터 등록 실패", Toast.LENGTH_SHORT).show();
                }
                stopPD();
            });
        }).addOnFailureListener(e -> {
            // 실패하면
            stopPD();
            Toast.makeText(getContext(), "업로드 실패", Toast.LENGTH_SHORT).show();
        });
    }

    public void setAlarmManager(int hour, int minute, String state, int requestCode){
        Calendar calendar = Calendar.getInstance();
        Log.i("@#@#@#@#@#@#", calendar.getTime().toString());
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        if(System.currentTimeMillis() > calendar.getTimeInMillis()){
            calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR)+1);
        }

        Intent intent = new Intent("ALARM_RECEIVER");
        intent.putExtra("state", state);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                getContext(),
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY,
                pendingIntent
        );
    }

    @OnClick(R.id.beaconSettingBtn)
    public void onSettingBeacon(View view) {
        Intent intent = new Intent(getContext(), BeaconActivity.class);
        intent.putExtra("companyName", userModel.getCompany().getcName());
        startActivity(intent);
    }

    @OnClick({R.id.inTimeBtn, R.id.outTimeBtn})
    public void onClickTimeSetting(View view){
        new TimePickerDialog(getContext(), (timePicker, i, i1) -> {
            if(view.getId() == R.id.inTimeBtn){
                CompanyModel.Time.InTime setTime = new CompanyModel.Time.InTime(i,i1);
                timeChange(setTime, "inTime",  "출근");

            } else {
                CompanyModel.Time.OutTime setTime = new CompanyModel.Time.OutTime(i,i1);
                timeChange(setTime, "outTime", "퇴근");
            }
        }, 0, 0, false).show();
    }

    public void timeChange(Object value, String when, String state){
        U.getInstance().getDr().child("companies").child(userModel.getCompany().getcName())
                .child("time").child(when).setValue(value).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                U.getInstance().showPopup(getContext(), SweetAlertDialog.SUCCESS_TYPE,
                        state+"시간이 변경되었습니다.", null, "확인", SweetAlertDialog::dismissWithAnimation);
            } else {
                U.getInstance().showPopup(getContext(), SweetAlertDialog.ERROR_TYPE,
                        "잠시후 다시 시도해주세요", null, "확인", SweetAlertDialog::dismissWithAnimation);
            }
        });
    }

    @OnClick({R.id.dept_btn, R.id.position_btn, R.id.name_btn, R.id.number_btn, R.id.pwd_btn})
    public void moveToChange(View view){
        Intent intent = new Intent(getContext(), ChangeActivity.class);
        switch (view.getId()){
            case R.id.dept_btn:
                intent.putExtra("dept", userModel.getCompany().getDept());
                break;
            case R.id.position_btn:
                intent.putExtra("position", userModel.getCompany().getPosition());
                break;
            case R.id.name_btn:
                intent.putExtra("name", userModel.getName());
                break;
            case R.id.number_btn:
                intent.putExtra("tel", userModel.getTel());
                break;
            case R.id.pwd_btn:
                intent.putExtra("pwd", "현재 비밀번호 입력");
                break;
        }
        startActivity(intent);
    }
}
