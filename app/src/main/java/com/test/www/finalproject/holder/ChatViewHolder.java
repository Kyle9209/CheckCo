package com.test.www.finalproject.holder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.test.www.finalproject.R;
import com.test.www.finalproject.model.ChannelModel;
import com.test.www.finalproject.model.UserModel;
import com.test.www.finalproject.util.U;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class ChatViewHolder extends RecyclerView.ViewHolder {
    @BindView(R.id.user_Img_civ) CircleImageView img;
    @BindView(R.id.name_tv) TextView name;
    @BindView(R.id.readCnt) ImageView readCnt;
    @BindView(R.id.lastMsg) TextView lastMsg;

    @BindView(R.id.state_tv) TextView state;
    @BindView(R.id.dept_tv) TextView dept;
    @BindView(R.id.position_tv) TextView position;

    public ChatViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void bindToItem(ChannelModel channelModel,String yourId, View.OnClickListener onClickListener){
        lastMsg.setText(channelModel.getLastMsg());
        lastMsg.setVisibility(View.VISIBLE);
        readCnt.setVisibility(View.VISIBLE);
        state.setVisibility(View.GONE);
        dept.setVisibility(View.GONE);
        position.setVisibility(View.GONE);

        if(channelModel.getReadCnt() == 1) {
            readCnt.setImageResource(android.R.drawable.presence_online);
        } else {
            readCnt.setImageResource(android.R.drawable.presence_invisible);
        }

        U.getInstance().getDr().child("users").child(yourId)
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UserModel userModel = dataSnapshot.getValue(UserModel.class);
                name.setText(userModel.getName());
                Glide.with(img.getContext())
                        .load(userModel.getImg())
                        .error(R.mipmap.ic_account_circle_black_48dp)
                        .into(img);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

        itemView.setOnClickListener(onClickListener);
    }
}
