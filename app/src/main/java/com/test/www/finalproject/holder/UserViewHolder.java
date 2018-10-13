package com.test.www.finalproject.holder;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.test.www.finalproject.R;
import com.test.www.finalproject.model.UserModel;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class UserViewHolder extends RecyclerView.ViewHolder {
    @BindView(R.id.user_Img_civ) CircleImageView img;
    @BindView(R.id.state_tv) TextView state;
    @BindView(R.id.dept_tv) TextView dept;
    @BindView(R.id.position_tv) TextView position;
    @BindView(R.id.name_tv) TextView name;

    public UserViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void bindToItem(UserModel userModel, View.OnClickListener onClickListener){
        if(userModel.getCompany().getPosition().equals("관리자")){
            state.setVisibility(View.GONE);
        } else {
            if (userModel.getState() == 1) {
                state.setText("근무중");
                state.setBackgroundResource(R.drawable.state_background);
                state.setTextColor(Color.parseColor("#004e66"));
            } else {
                state.setText("퇴근");
                state.setBackgroundResource(R.drawable.state_background2);
                state.setTextColor(Color.parseColor("#d24f26"));
            }
        }
        dept.setText(userModel.getCompany().getDept());
        position.setText(userModel.getCompany().getPosition());
        name.setText(userModel.getName());
        Glide.with(img.getContext())
                .load(userModel.getImg())
                .error(R.mipmap.ic_account_circle_black_48dp)
                .into(img);

        itemView.setOnClickListener(onClickListener);
    }
}
