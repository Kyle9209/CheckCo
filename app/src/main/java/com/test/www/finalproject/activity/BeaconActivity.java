package com.test.www.finalproject.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.test.www.finalproject.R;
import com.test.www.finalproject.RootActivity;
import com.test.www.finalproject.model.CompanyModel;
import com.test.www.finalproject.util.U;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.pedant.SweetAlert.SweetAlertDialog;

public class BeaconActivity extends RootActivity {
    @BindView(R.id.toolbar_title_tv) TextView title;
    @BindView(R.id.bName) EditText bName;
    @BindView(R.id.uuid) EditText uuid;
    @BindView(R.id.major) EditText major;
    @BindView(R.id.minor) EditText minor;
    DatabaseReference dr = FirebaseDatabase.getInstance().getReference();
    String companyName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beacon);
        ButterKnife.bind(this);

        title.setText("비콘설정");

        companyName = getIntent().getStringExtra("companyName");
    }

    public void onSaveBeacon(View view) {
        String bName_str = bName.getText().toString();
        String uuid_str  = uuid.getText().toString();
        int major_int    = Integer.parseInt(major.getText().toString());
        int minor_int    = Integer.parseInt(minor.getText().toString());

        CompanyModel.Beacon beaconModel = new CompanyModel.Beacon(
                bName_str,
                uuid_str,
                major_int,
                minor_int
        );

        showPD(this);
        dr.child("companies").child(companyName).child("beacon").setValue(beaconModel).addOnCompleteListener(task -> {
            stopPD();
            if(task.isSuccessful()){
                U.getInstance().showPopup(this, SweetAlertDialog.SUCCESS_TYPE, "비콘 설정이 완료되었습니다.", null, "확인",
                        sweetAlertDialog -> {
                            sweetAlertDialog.dismissWithAnimation();
                            finish();
                        });
            } else {
                U.getInstance().showPopup(this, SweetAlertDialog.ERROR_TYPE, "잠시후 다시 시도해주세요", null, "확인",
                        SweetAlertDialog::dismissWithAnimation);
            }
        });
    }
}
