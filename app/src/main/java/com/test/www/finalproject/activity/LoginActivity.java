package com.test.www.finalproject.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.test.www.finalproject.R;
import com.test.www.finalproject.RootActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LoginActivity extends RootActivity {
    @BindView(R.id.eid) EditText eid;
    @BindView(R.id.password1) EditText password1;
    // 파이어베이스 인증 객체 획득
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // 버터나이프 바이딩
        ButterKnife.bind(this);

        // 비밀번호 완료버튼 클릭시 로그인
        password1.setImeOptions(EditorInfo.IME_ACTION_GO);
        password1.setOnEditorActionListener((textView, i, keyEvent) -> {
            onLoginBtn(null);
            return true;
        });
    }

    @Override
    protected void onStart(){
        super.onStart();

        // 로그인이 되어있다면 바로 마이페이지로
        if(firebaseAuth.getCurrentUser() != null){
            moveToMainService();
        }
    }

    public void onLoginBtn(View view) {
        String eid_str = eid.getText().toString();
        String pwd1_str = password1.getText().toString();

        // 유효성 검사
        View focus = null;
        Boolean state = false;
        if(TextUtils.isEmpty(pwd1_str) || pwd1_str.length() < 6){
            password1.setError("비밀번호를 확인해주세요");
            focus = password1;
            state = true;
        }
        if(TextUtils.isEmpty(eid_str)){
            eid.setError("사번 또는 ID를 입력해주세요");
            focus = eid;
            state = true;
        }

        if(state){
            focus.requestFocus();
            return;
        } else {
            // 로그인 처리
            String eid_email = eid_str + "@email.com";
            login(eid_email, pwd1_str);
        }
    }

    public void login(String id, String pwd){
        showPD(this);
        // 로그인 처리
        firebaseAuth.signInWithEmailAndPassword(id,pwd).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                moveToMainService();
            } else {
                Toast.makeText(LoginActivity.this, "로그인 실패", Toast.LENGTH_SHORT).show();
                stopPD();
            }
        });
    }

    public void moveToMainService(){
        Intent intent = new Intent(this, MainServiceActivity.class);
        startActivity(intent);
        finish();
    }

    public void onGoSignInBtn(View view) {
        Intent intent = new Intent(this, SignInActivity.class);
        startActivity(intent);
    }
}
