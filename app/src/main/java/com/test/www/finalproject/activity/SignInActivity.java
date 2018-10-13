package com.test.www.finalproject.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.test.www.finalproject.R;
import com.test.www.finalproject.RootActivity;
import com.test.www.finalproject.model.CompanyModel;
import com.test.www.finalproject.model.UserModel;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SignInActivity extends RootActivity {

    @BindView(R.id.toolbar_title_tv) TextView title;
    @BindView(R.id.name) EditText name;
    @BindView(R.id.phone) EditText phone;
    @BindView(R.id.eid) EditText eid;
    @BindView(R.id.password1) EditText password1;
    @BindView(R.id.password2) EditText password2;
    @BindView(R.id.level) RadioGroup level;
    @BindView(R.id.company_til) TextInputLayout company_til;
    @BindView(R.id.company) EditText company;
    @BindView(R.id.company_cv) CardView company_cv;
    String name_str, phone_str, company_str;
    boolean levelFlag = false; // 사원이라면 false, 관리자라면 true
    SignInActivity self;
    // 파이어베이스 인증 객체 획득
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    // 파이어베이스 데이터베이스 객체 획득
    DatabaseReference dr = FirebaseDatabase.getInstance().getReference();
    int level_int;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);
        // 버터나이프 바이딩
        ButterKnife.bind(this);
        self = this;

        title.setText("회원가입");

        // 비밀번호 확인에 완료버튼 클릭 시 회원가입 진행
        password2.setImeOptions(EditorInfo.IME_ACTION_DONE);
        password2.setOnEditorActionListener((textView, i, keyEvent) -> {
            onSignInBtn(null);
            return true;
        });

        // 관리자 버튼 클릭 시 회사명 입력란 보이기
        level.setOnCheckedChangeListener((radioGroup, i) -> {
            if(radioGroup.getCheckedRadioButtonId() == R.id.master) {
                company_cv.setVisibility(View.VISIBLE);
                company_til.requestFocus();
                levelFlag = true;
            } else {
                company_cv.setVisibility(View.GONE);
                company.setText("");
                levelFlag = false;
            }
        });
    }

    public void onSignInBtn(View view) {
        String eid_str  = eid.getText().toString();
        String pwd1_str = password1.getText().toString();
        String pwd2_str = password2.getText().toString();
        name_str    = name.getText().toString();
        phone_str   = phone.getText().toString();
        company_str = company.getText().toString();

        // 유효성 검사
        View focus = null;
        Boolean state = false;
        if(!pwd1_str.equals(pwd2_str)){
            password2.setError("비밀번호가 다릅니다");
            focus = password2;
            state = true;
        }
        if(TextUtils.isEmpty(pwd2_str) || pwd2_str.length() < 6){
            password2.setError("비밀번호를 확인해주세요");
            focus = password2;
            state = true;
        }
        if(TextUtils.isEmpty(pwd1_str) || pwd1_str.length() < 6){
            password1.setError("비밀번호를 확인해주세요");
            focus = password1;
            state = true;
        }
        if(TextUtils.isEmpty(eid_str)){
            eid.setError("사번을 입력해주세요");
            focus = eid;
            state = true;
        }
        if(TextUtils.isEmpty(phone_str)){
            phone.setError("휴대폰 번호를 입력해주세요");
            focus = phone;
            state = true;
        }

        if(levelFlag){
            if(TextUtils.isEmpty(company_str)){
                company.setError("회사명을 입력해주세요");
                focus = company;
                state = true;
            }
        } else {
            company_str = "null";
        }

        if(state){
            focus.requestFocus();
            return;
        } else {
            // 회원가입 처리
            String eid_email = eid_str + "@email.com";
            createUser(eid_email, pwd1_str);
        }
    }

    public void createUser(final String id, final String pwd){
        showPD(this);
        // 파이어베이스 계정 생성
        firebaseAuth.createUserWithEmailAndPassword(id,pwd).addOnCompleteListener(task -> {
            if(task.isSuccessful()) { // 성공 -> 데이터 저장
                // 사용자데이터 업로드
                uploadUserData(id);
            } else { // 실패
                Toast.makeText(SignInActivity.this, "계정이 생성이 실패하였습니다.", Toast.LENGTH_SHORT).show();
                stopPD();
            }
        });
    }

    public void uploadUserData(String id){
        // 로그인된 계정의 uid 획득
        String key = firebaseAuth.getCurrentUser().getUid();
        // 레벨 획득(사원 or 관리자)
        level_int = 1;
        String position = "관리";
        if(level.getCheckedRadioButtonId() == R.id.master) {
            level_int = 0;
            position = "관리자";
        }
        // 데이터 셋팅
        UserModel userModel = new UserModel(
                key,
                id.split("@")[0],
                name_str,
                phone_str,
                "img",
                FirebaseInstanceId.getInstance().getToken(),
                new UserModel.Company(company_str, "관리", position),
                level_int, // 관리자 : 0 , 일반 : 1
                0 // 일반상태 : 0 , 출근상태 : 1
        );
        // 유저모델을 해시맵 형태로 변환
        Map<String, Object> data = userModel.toMap();
        // 저장될 위치(키값)과 저장될 데이터(내용) 셋팅
        Map<String, Object> update = new HashMap<>();
        update.put("/users/" + key, data);
        if(!company_str.equals("null")) { // 관리자 가입만 해당
            CompanyModel companyModel = new CompanyModel(
                    FirebaseInstanceId.getInstance().getToken(),
                    new CompanyModel.Beacon("null", "null", 0, 0),
                    new CompanyModel.Time(new CompanyModel.Time.InTime(9,0), new CompanyModel.Time.OutTime(18,0)),
                    null
            );
            data = companyModel.toMap();
            update.put("/companies/" + company_str, data);
        }
        // 파이어베이스 데이터베이스 유저 정보 저장
        dr.updateChildren(update, (databaseError, databaseReference) -> {
            if(databaseError != null){ // 실패
                Toast.makeText(self, "데이터 저장 실패", Toast.LENGTH_SHORT).show();
                stopPD();
            } else{ // 성공
                if(level_int == 0) {
                    dr.child("companies").child(company_str).child("userList")
                            .child(key).setValue(userModel).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) Log.i("kkmjar", "성공");
                    });
                }
                Intent intent = new Intent(self, MainServiceActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
