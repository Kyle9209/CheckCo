package com.test.www.finalproject.fragment;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.squareup.otto.Subscribe;
import com.test.www.finalproject.R;
import com.test.www.finalproject.activity.ChattingActivity;
import com.test.www.finalproject.holder.UserViewHolder;
import com.test.www.finalproject.model.ChannelModel;
import com.test.www.finalproject.model.MessageModel;
import com.test.www.finalproject.model.UserModel;
import com.test.www.finalproject.util.U;

import java.util.HashMap;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class MemberListFragment extends ListFragment {
    DatabaseReference dr;

    public MemberListFragment() {}

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        U.getInstance().getBus().register(this);
        this.title.setText("사원리스트");
        Drawable drawable = ContextCompat.getDrawable(getContext(), R.mipmap.ic_format_list_bulleted_white_24dp);
        drawable.setBounds(0,0,drawable.getIntrinsicWidth(),drawable.getIntrinsicHeight());
        this.title.setCompoundDrawables(drawable, null, null, null);
        this.title.setCompoundDrawablePadding(10);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        U.getInstance().getBus().unregister(this);
    }

    @Subscribe
    public void ottoBus(String str) {
        if (str.equals("updateUserData")) {
            dr = U.getInstance().getDr();
            UserModel userModel = U.getInstance().getUserModel();

            recyclerView.setFocusable(true);

            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
            linearLayoutManager.setReverseLayout(false); // 데이터 받는 순서 == order by
            recyclerView.setLayoutManager(linearLayoutManager);

            firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<UserModel, UserViewHolder>(
                    UserModel.class,
                    R.layout.cell_list_layout,
                    UserViewHolder.class,
                    dr.child("companies").child(userModel.getCompany().getcName()).child("userList")
            ){
                @Override
                protected void populateViewHolder(UserViewHolder viewHolder, final UserModel model, int position) {
                    if(model.getUid().equals(U.getInstance().getFirebaseAuth().getCurrentUser().getUid())) {
                        viewHolder.itemView.setEnabled(false);
                    }
                    viewHolder.bindToItem(model, view1 -> {
                        U.getInstance().showPopup(getContext(), SweetAlertDialog.SUCCESS_TYPE,
                                "선택하세요",
                                null,
                                "전화",
                                sweetAlertDialog -> {
                                    Intent intent = new Intent(Intent.ACTION_DIAL);
                                    intent.setData(Uri.parse("tel:"+model.getTel()));
                                    try {
                                        startActivity(intent);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    sweetAlertDialog.dismiss();
                                },
                                "채팅",
                                sweetAlertDialog -> {
                                    // 채팅 채널이 존재하는가? : channel > 내아이디 > 유저아이디 > 값이 존재하는가?
                                    final String myId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                    final String yourId = model.getUid();
                                    dr.child("channels").child(myId).child(yourId)
                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    ChannelModel channelModel = dataSnapshot.getValue(ChannelModel.class);
                                                    if (channelModel != null && channelModel.getChannel() != null) {
                                                        // 채팅으로 바로 이동
                                                        goChatRoom(channelModel.getChannel(), yourId);
                                                    } else {
                                                        // 채널 생성
                                                        makeChannel(myId, yourId);
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {
                                                    // 채널생성
                                                    makeChannel(myId, yourId);
                                                }
                                            });
                                    sweetAlertDialog.dismissWithAnimation();
                                }
                        );
                    });
                }
            };
            recyclerView.setAdapter(firebaseRecyclerAdapter);
        }
    }

    public void makeChannel(final String myId, final String yourId){
        // 신청자의 입장과 받는 사람 입장으로 각각 채널 정보를 생성
        // /channels/내아이디/상대방아이디/... : 채팅 신청중
        // /channels/상대방아이디/내아이디/... : ??님이 채팅을 신청하였습니다.
        // 1. 키 획득(채팅 채널 : 고유한 값)
        //String key = databaseReference.child("channels").child(myId).child(yourId).push().getKey();
        // 고유한 채팅채널을 확보하는 위치는 채팅룸을 관리하는 곳이므로 키를 획득하는 위치를 이쪽으로 셋팅
        final String key = dr.child("chatting").child("rooms").push().getKey();
        // 채팅방에 실제 메시지를 입력
        // 2. 데이터 셋팅
        final long time = System.currentTimeMillis();
        MessageModel messageModel = new MessageModel("채팅 오픈", time+"", FirebaseAuth.getInstance().getCurrentUser().getUid(), "t", 1);
        Map<String, Object> msgObj = messageModel.toMap();
        Map<String, Object> ups = new HashMap<>();
        ups.put( "/chatting/rooms/"+key+"/"+time,  msgObj);
        dr.updateChildren(ups, (databaseError, databaseReference) -> {
            if(databaseError != null){
                Toast.makeText(getActivity(), "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            } else {
                ChannelModel myCm = new ChannelModel(
                        key,
                        yourId,
                        "채팅 오픈",
                        time,
                        1
                );
                ChannelModel yourCm = new ChannelModel(
                        key,
                        myId,
                        "채팅 오픈",
                        time,
                        1
                );
                // 3. toMap 획득
                Map<String, Object> myMap = myCm.toMap();
                Map<String, Object> yourMap = yourCm.toMap();
                Map<String, Object> updates = new HashMap<>();
                updates.put("/channels/" + myId + "/" + yourId, myMap);
                updates.put("/channels/" + yourId + "/" + myId, yourMap);
                // 6. 업데이트
                databaseReference.updateChildren(updates, (databaseError1, databaseReference1) -> {
                    if(databaseError1 != null){
                        Toast.makeText(getActivity(), "데이터 저장 실패 : " + databaseError1.getMessage(), Toast.LENGTH_SHORT).show();
                    } else {
                        // 7. 채팅방으로 이동
                        goChatRoom(key, yourId);
                    }
                });
            }
        });
    }

    public void goChatRoom(String key, String yourId){
        Intent intent = new Intent(getActivity(), ChattingActivity.class);
        intent.putExtra("channel", key);
        intent.putExtra("yourId", yourId);
        startActivity(intent);
    }
}
