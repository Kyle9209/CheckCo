package com.test.www.finalproject.fragment;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.test.www.finalproject.R;
import com.test.www.finalproject.activity.ChattingActivity;
import com.test.www.finalproject.holder.ChatViewHolder;
import com.test.www.finalproject.model.ChannelModel;
import com.test.www.finalproject.util.U;

public class ChattingListFragment extends ListFragment {

    public ChattingListFragment() {}

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        U.getInstance().getBus().register(this);
        this.title.setText("채팅리스트");
        Drawable drawable = ContextCompat.getDrawable(getContext(), R.mipmap.ic_chat_bubble_outline_white_24dp);
        drawable.setBounds(0,0,drawable.getIntrinsicWidth(),drawable.getIntrinsicHeight());
        this.title.setCompoundDrawables(drawable, null, null, null);
        this.title.setCompoundDrawablePadding(10);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(false); // 데이터 받는 순서 == order by
        recyclerView.setLayoutManager(linearLayoutManager);

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<ChannelModel, ChatViewHolder>(
                ChannelModel.class,
                R.layout.cell_list_layout,
                ChatViewHolder.class,
                U.getInstance().getDr().child("channels").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
        ){
            @Override
            protected void populateViewHolder(ChatViewHolder viewHolder, final ChannelModel model, int position) {
                // 상대방 아이디 구하기
                DatabaseReference databaseReference = getRef(position);
                final String key = databaseReference.getKey();
                viewHolder.bindToItem(model, key, view1 -> goChatRoom(model.getChannel(), key));
            }
        };
        recyclerView.setAdapter(firebaseRecyclerAdapter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        U.getInstance().getBus().unregister(this);
    }

    public void goChatRoom(String key, String yourId){
        Intent intent = new Intent(getActivity(), ChattingActivity.class);
        intent.putExtra("channel", key);
        intent.putExtra("yourId", yourId);
        startActivity(intent);
    }
}
