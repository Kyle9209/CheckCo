package com.test.www.finalproject.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.otto.Subscribe;
import com.test.www.finalproject.R;
import com.test.www.finalproject.holder.MessageModelViewHolder;
import com.test.www.finalproject.model.ChannelModel;
import com.test.www.finalproject.model.MessageModel;
import com.test.www.finalproject.model.UserModel;
import com.test.www.finalproject.net.Net;
import com.test.www.finalproject.util.U;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.test.www.finalproject.R.id.recyclerview;

public class ChattingActivity extends AppCompatActivity {
    @BindView(R.id.toolbar_title_tv) TextView title;
    @BindView(recyclerview) RecyclerView recyclerView;
    @BindView(R.id.chatInput) AutoCompleteTextView chatInput;
    DatabaseReference databaseReference;
    FirebaseRecyclerAdapter firebaseRecyclerAdapter;
    String channel;
    String yourId;
    String uid;
    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatting);
        ButterKnife.bind(this);
        U.getInstance().getCustomBus().register(this);

        channel = getIntent().getStringExtra("channel");
        yourId = getIntent().getStringExtra("yourId");
        U.getInstance().setChattingYourID(yourId);
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        U.getInstance().getDr().child("users").child(yourId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                title.setText(dataSnapshot.getValue(UserModel.class).getName());
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

        U.getInstance().getDr().child("channels").child(uid).child(yourId).child("readCnt").setValue(0);

        recyclerView.setFocusable(true);
        databaseReference = FirebaseDatabase.getInstance().getReference();

        linearLayoutManager.setReverseLayout(false); // 데이터 받는 순서 == order by
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<MessageModel, MessageModelViewHolder>(
                MessageModel.class,
                R.layout.cell_message_layout,
                MessageModelViewHolder.class,
                databaseReference.child("chatting").child("rooms").child(channel)
        ){
            @Override
            protected void populateViewHolder(MessageModelViewHolder viewHolder, final MessageModel model, int position) {
                viewHolder.bindToItem(model);
                if(model.getSender().equals(yourId)) {
                    U.getInstance().getDr().child("chatting").child("rooms").child(channel)
                            .child(model.getTime()).child("readCnt").setValue(0);
                }
            }
        };

        recyclerView.setAdapter(firebaseRecyclerAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        U.getInstance().setChattingYourID("null");
        U.getInstance().getCustomBus().unregister(this);
    }

    public void sendMsg(View view) {
        final String msg = chatInput.getText().toString();
        if(TextUtils.isEmpty(msg)){
            chatInput.setError("채팅메세지를 입력 후 전송하세요");
            chatInput.setFocusable(true);
            return;
        }
        // 채팅 메세지 전송
        final long time = System.currentTimeMillis();
        MessageModel messageModel = new MessageModel(msg, time+"", uid, "t", 1);
        // 글을 쓰고 전송버튼을 누르면 디비에 기록 -> 채팅룸, 채팅채널 업데이트
        Map<String, Object> msgObj = messageModel.toMap();
        Map<String, Object> ups = new HashMap<>();
        ups.put( "/chatting/rooms/"+channel+"/"+time,  msgObj);
        databaseReference.updateChildren(ups, (databaseError, databaseReference1) -> {
            if(databaseError != null){
                Toast.makeText(ChattingActivity.this, "전송오류입니다. 잠시후 다시 이용해주세요.", Toast.LENGTH_SHORT).show();
            } else {
                // 채팅채널 lastmsg 업데이트
                ChannelModel myCm = new ChannelModel(channel, uid, msg, time, 0);
                ChannelModel yourCm = new ChannelModel(channel, uid, msg, time, 1);
                Map<String, Object> myMap = myCm.toMap();
                Map<String, Object> yourMap = yourCm.toMap();

                Map<String, Object> updates = new HashMap<>();
                updates.put("/channels/" + uid + "/" + yourId, myMap);
                updates.put("/channels/" + yourId + "/" + uid, yourMap);
                // 6. 업데이트
                databaseReference1.updateChildren(updates, (databaseError1, databaseReference11) -> {
                    if(databaseError1 != null){
                        Toast.makeText(ChattingActivity.this, "전송오류입니다. 잠시후 다시 이용해주세요.", Toast.LENGTH_SHORT).show();
                    } else {
                        // 초기화
                        chatInput.setText("");
                        recyclerView.scrollToPosition(firebaseRecyclerAdapter.getItemCount()-1);

                        databaseReference11.child("users").child(yourId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                UserModel userModel = dataSnapshot.getValue(UserModel.class);
                                // 레트로핏을 이용한 푸시발송
                                final Call<ResponseBody> res =  Net.getInstance().getFcmFactoryIm().sendFcm(
                                        userModel.getToken().trim(),
                                        "{\"sender\":\""+uid+"\",\"msg\":\""+msg+"\",\"channel\":\""+channel+"\"}",
                                        (int)(Math.random()*100),
                                        "high"
                                );
                                res.enqueue(new Callback<ResponseBody>() {
                                    @Override
                                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {}
                                    @Override
                                    public void onFailure(Call<ResponseBody> call, Throwable t) {}
                                });
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError1) {}
                        });
                    }
                });
            }
        });
    }

    @Subscribe
    public void ottoBus(String str){
        if(str.equals("listUp")) {
            recyclerView.scrollToPosition(firebaseRecyclerAdapter.getItemCount()-1);
        }
    }
}
