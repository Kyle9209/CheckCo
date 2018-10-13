package com.test.www.finalproject.holder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.test.www.finalproject.R;
import com.test.www.finalproject.model.MessageModel;
import com.test.www.finalproject.model.UserModel;
import com.test.www.finalproject.util.U;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class MessageModelViewHolder extends RecyclerView.ViewHolder {
    @BindView(R.id.left_container) LinearLayout left_container;
    @BindView(R.id.right_container) LinearLayout right_container;
    @BindView(R.id.img_left_thumbnail) CircleImageView img_left;
    @BindView(R.id.txt_left) TextView txt_left;
    @BindView(R.id.txt_right) TextView txt_right;
    @BindView(R.id.txt_left_name) TextView left_name;
    @BindView(R.id.txt_left_time) TextView txt_left_time;
    @BindView(R.id.txt_right_time) TextView txt_right_time;
    @BindView(R.id.txt_right_status) TextView txt_right_status;

    public MessageModelViewHolder(View itemView) {
        super(itemView);
        // 바인딩 설정
        ButterKnife.bind(this, itemView);
    }

    public void bindToItem(MessageModel messageModel){
        // long 상태의 시간값을 변환하여 보여줌
        long time = Long.parseLong(messageModel.getTime());
        Date date = new Date(time);
        // 시간을 나타냇 포맷을 정한다 ( yyyy/MM/dd 같은 형태로 변형 가능 )
        SimpleDateFormat sdfNow = new SimpleDateFormat("HH:mm:ss", Locale.KOREA);
        // nowDate 변수에 값을 저장한다.
        String formatDate = sdfNow.format(date);

        // 데이터 설정, 이벤트 설정
        if(messageModel.getSender().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){ // 내 글
            left_container.setVisibility(View.GONE);
            right_container.setVisibility(View.VISIBLE);
            txt_right.setText(messageModel.getMsg()); // 내 글 세팅
            txt_right_time.setText(formatDate);
            if(messageModel.getReadCnt() == 0) txt_right_status.setVisibility(View.INVISIBLE);
        } else { // 상대방 글
            right_container.setVisibility(View.GONE);
            left_container.setVisibility(View.VISIBLE);
            txt_left.setText(messageModel.getMsg()); // 상대방 글 세팅
            txt_left_time.setText(formatDate);

            U.getInstance().getDr().child("users").child(messageModel.getSender())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    UserModel userModel = dataSnapshot.getValue(UserModel.class);
                    left_name.setText(userModel.getName());// 상대방 이름 세팅
                    Glide.with(img_left.getContext())
                            .load(userModel.getImg())
                            .error(R.mipmap.ic_account_circle_black_48dp)
                            .into(img_left);
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {}
            });
        }
    }
}
