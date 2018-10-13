package com.test.www.finalproject.activity;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.test.www.finalproject.R;
import com.test.www.finalproject.RootActivity;
import com.test.www.finalproject.model.UserModel;
import com.test.www.finalproject.util.U;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.text.InputType.TYPE_CLASS_PHONE;
import static android.text.InputType.TYPE_CLASS_TEXT;
import static android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD;

public class ChangeActivity extends RootActivity {
    @BindView(R.id.toolbar_title_tv) TextView title;
    @BindView(R.id.toolbar_right_btn) Button rightBtn;
    @BindView(R.id.card) CardView cardView;
    ClearEditText clearEditText;
    UserModel userModel;
    String key;
    String value;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change);
        ButterKnife.bind(this);

        userModel = U.getInstance().getUserModel();

        if (getIntent().getStringExtra("dept") != null) {
            key = "dept";
            value = getIntent().getStringExtra("dept");
        } else if (getIntent().getStringExtra("position") != null) {
            key = "position";
            value = getIntent().getStringExtra("position");
        } else if (getIntent().getStringExtra("name") != null) {
            key = "name";
            value = getIntent().getStringExtra("name");
        } else if (getIntent().getStringExtra("tel") != null) {
            key = "tel";
            value = getIntent().getStringExtra("tel");
        } else if (getIntent().getStringExtra("pwd") != null) {
            key = "pwd";
            value = getIntent().getStringExtra("pwd");
        }

        title.setText("변경");
        rightBtn.setText("완료");
        rightBtn.setVisibility(View.VISIBLE);

        rightBtn.setOnClickListener(view -> onClickRightBtn());

        clearEditText = new ClearEditText(this);
        clearEditText.setTextSize(20);

        if (key.equals("tel")) {
            clearEditText.setText(value);
            clearEditText.setInputType(TYPE_CLASS_PHONE);
        } else {
            if (key.equals("pwd")) {
                rightBtn.setText("다음");
                clearEditText.setHint(value);
                clearEditText.setInputType( TYPE_CLASS_TEXT | TYPE_TEXT_VARIATION_PASSWORD);
            } else {
                clearEditText.setText(value);
            }
        }
        clearEditText.setSelection(clearEditText.length());

        cardView.addView(clearEditText);
    }

    public void onClickRightBtn() {
        if (TextUtils.isEmpty(clearEditText.getText())) {
            clearEditText.setError("입력해주세요");
            clearEditText.requestFocus();
        } else {
            Map<String, Object> update = new HashMap<>();
            String getData = clearEditText.getText().toString();
            switch (key) {
                case "pwd":  // 비밀번호
                    if (getData.length() < 6) {
                        clearEditText.setError("6자리 이상");
                        clearEditText.requestFocus();
                        return;
                    }
                    showPD(this);
                    AuthCredential credential = EmailAuthProvider
                            .getCredential(U.getInstance().getFirebaseAuth().getCurrentUser().getEmail(), getData);

                    U.getInstance().getFirebaseAuth().getCurrentUser()
                            .reauthenticate(credential).addOnCompleteListener(task -> {
                        stopPD();
                        if (task.isSuccessful()) {
                            clearEditText.setText("");
                            clearEditText.setHint("새로운 비밀번호 입력 6자리 이상");
                            rightBtn.setText("완료");
                            rightBtn.setOnClickListener(view -> {
                                if (clearEditText.getText().length() < 6) {
                                    clearEditText.setError("6자리 이상");
                                    clearEditText.requestFocus();
                                    return;
                                }
                                showPD(this);
                                U.getInstance().getFirebaseAuth().getCurrentUser()
                                        .updatePassword(clearEditText.getText().toString()).addOnCompleteListener(task1 -> {
                                    stopPD();
                                    if (task1.isSuccessful()) {
                                        Toast.makeText(ChangeActivity.this, "비밀번호가 변경되었습니다", Toast.LENGTH_SHORT).show();
                                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                        imm.hideSoftInputFromWindow(clearEditText.getWindowToken(), 0);
                                        finish();
                                    } else {
                                        Toast.makeText(ChangeActivity.this, "에러", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            });
                        } else {
                            Toast.makeText(ChangeActivity.this, "비밀번호가 틀렸습니다", Toast.LENGTH_SHORT).show();
                        }
                    });
                    return;
                case "dept":
                case "position":  // 직급, 부서
                    update.put("/users/" + userModel.getUid() + "/company/" + key, getData);
                    update.put("/companies/" + userModel.getCompany().getcName() + "/userList/" + userModel.getUid() + "/company/" + key, getData);
                    break;
                case "name":
                case "tel":  // 이름, 전화번호
                    if (!userModel.getCompany().getcName().equals("null")) { // 회사가 있다면
                        update.put("/companies/" + userModel.getCompany().getcName() + "/userList/" + userModel.getUid() + "/" + key, getData);
                    }
                    update.put("/users/" + userModel.getUid() + "/" + key, getData);
                    break;
            }

            U.getInstance().getDr().updateChildren(update, (databaseError, databaseReference) -> {
                if (databaseError != null) {
                    Toast.makeText(ChangeActivity.this, "에러", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ChangeActivity.this, "변경되었습니다", Toast.LENGTH_SHORT).show();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(clearEditText.getWindowToken(), 0);
                    finish();
                }
            });
        }
    }

    public class ClearEditText extends AppCompatEditText
            implements TextWatcher, View.OnTouchListener, View.OnFocusChangeListener {
        private Drawable clearDrawable;
        private OnFocusChangeListener onFocusChangeListener;
        private OnTouchListener onTouchListener;

        public ClearEditText(final Context context) {
            super(context);
            init();
        }

        public ClearEditText(final Context context, final AttributeSet attrs) {
            super(context, attrs);
            init();
        }

        public ClearEditText(final Context context, final AttributeSet attrs, final int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            init();
        }

        @Override
        public void setOnFocusChangeListener(OnFocusChangeListener onFocusChangeListener) {
            this.onFocusChangeListener = onFocusChangeListener;
        }

        @Override
        public void setOnTouchListener(OnTouchListener onTouchListener) {
            this.onTouchListener = onTouchListener;
        }

        private void init() {
            Drawable tempDrawable = ContextCompat.getDrawable(getContext(), R.mipmap.ic_clear_black_18dp);
            clearDrawable = DrawableCompat.wrap(tempDrawable);
            DrawableCompat.setTintList(clearDrawable, getHintTextColors());
            clearDrawable.setBounds(0, 0, clearDrawable.getIntrinsicWidth(), clearDrawable.getIntrinsicHeight());
            setClearIconVisible(false);
            super.setOnTouchListener(this);
            super.setOnFocusChangeListener(this);
            addTextChangedListener(this);
        }

        @Override
        public void onFocusChange(final View view, final boolean hasFocus) {
            if (hasFocus) {
                setClearIconVisible(getText().length() > 0);
            } else {
                setClearIconVisible(false);
            }
            if (onFocusChangeListener != null) {
                onFocusChangeListener.onFocusChange(view, hasFocus);
            }
        }

        @Override
        public boolean onTouch(final View view, final MotionEvent motionEvent) {
            final int x = (int) motionEvent.getX();
            if (clearDrawable.isVisible() && x > getWidth() - getPaddingRight() - clearDrawable.getIntrinsicWidth()) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    setError(null);
                    setText(null);
                }
                return true;
            }
            if (onTouchListener != null) {
                return onTouchListener.onTouch(view, motionEvent);
            } else {
                return false;
            }
        }

        @Override
        public final void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
            if (isFocused()) {
                setClearIconVisible(s.length() > 0);
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
        }

        private void setClearIconVisible(boolean visible) {
            clearDrawable.setVisible(visible, false);
            setCompoundDrawables(null, null, visible ? clearDrawable : null, null);
        }
    }
}
