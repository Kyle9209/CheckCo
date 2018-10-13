package com.test.www.finalproject.fragment;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.SystemRequirementsChecker;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.otto.Subscribe;
import com.test.www.finalproject.R;
import com.test.www.finalproject.RootFragment;
import com.test.www.finalproject.activity.MainActivity;
import com.test.www.finalproject.holder.UserViewHolder;
import com.test.www.finalproject.holder.WorkViewHolder;
import com.test.www.finalproject.model.CompanyModel;
import com.test.www.finalproject.model.UserModel;
import com.test.www.finalproject.model.WorkModel;
import com.test.www.finalproject.net.Net;
import com.test.www.finalproject.util.U;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import cn.pedant.SweetAlert.SweetAlertDialog;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends RootFragment {
    @BindView(R.id.inOutList_rcv) RecyclerView listRcv;
    @BindView(R.id.inBtn) Button inBtn;
    @BindView(R.id.outBtn) Button outBtn;
    @BindView(R.id.inoutCv) LinearLayout inoutCv;
    @BindView(R.id.search_company) CardView searchCompany;
    @BindView(R.id.company_et) EditText companyEt;
    @BindView(R.id.company_lv) ListView companyLv;
    @BindView(R.id.company_tv) TextView companyTv;
    @BindView(R.id.toolbar_title_tv) TextView title;
    @BindString(R.string.app_title) String appTitle;
    @BindView(R.id.admin_init_rl) RelativeLayout adminInit;
    @BindView(R.id.admin_company_line) LinearLayout adminCompanyLine;
    @BindView(R.id.admin_company_name_tv) TextView adminCompanyName;
    @BindView(R.id.admin_company_count_tv) TextView adminCompanyCount;
    UserModel userModel;
    View view;
    Unbinder unbinder;
    // 파이어베이스 리사이클러 뷰 어댑터 객체 생성
    FirebaseRecyclerAdapter fAdapter;
    // 비콘매니저 객체 생성
    BeaconManager bm;
    // 비콘 셋팅 플래그
    boolean beaconFlag = true;
    // 회사리스트 & 어댑터
    ArrayAdapter adapter;
    List companyList;

    public HomeFragment() {}

    @Subscribe
    public void ottoBus(String str) {
        if (str.equals("updateUserData")) {
            userModel = U.getInstance().getUserModel();
            if (userModel.getLevel() == 1) { // 사원
                if (!userModel.getCompany().getcName().equals("null")) { // 회사가 있다면
                    searchCompany.setVisibility(View.GONE);
                    inoutCv.setVisibility(View.VISIBLE);
                    companyTv.setText(userModel.getCompany().getcName());

                    if (beaconFlag && !userModel.getCompany().getcName().equals("null")) {
                        beaconFlag = false;
                        // 회사 비콘 셋팅 1번만!
                        U.getInstance().getDr().child("companies").child(userModel.getCompany().getcName())
                                .child("beacon").addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        CompanyModel.Beacon beacon = dataSnapshot.getValue(CompanyModel.Beacon.class);

                                        if(beacon.getUuid() == null || beacon.getUuid().equals("null")){
                                            Toast.makeText(getContext(), "회사 비콘이 아직 설정되어있지 않습니다", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Region region = new Region(
                                                    beacon.getbName(),
                                                    UUID.fromString(beacon.getUuid()),
                                                    beacon.getMajor(),
                                                    beacon.getMinor());
                                            initBeacon(region);
                                        }
                                    }
                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {}
                                });
                        listRcv.addItemDecoration(new DividerItemDecoration(getContext(),new LinearLayoutManager(getContext()).getOrientation()));
                    }

                    // 회사에 내 정보 업데이트
                    U.getInstance().getDr().child("companies").child(userModel.getCompany().getcName())
                            .child("userList").child(userModel.getUid())
                            .setValue(userModel).addOnCompleteListener(task -> {});
                } else { // 회사가 없다면
                    inoutCv.setVisibility(View.GONE);
                    searchCompany.setVisibility(View.VISIBLE);
                }
                initRcv(false);
            } else { // 관리자
                adminCompanyLine.setVisibility(View.VISIBLE);
                adminCompanyName.setText(userModel.getCompany().getcName());
                initRcv(true);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home, container, false);
        unbinder = ButterKnife.bind(this, view);
        U.getInstance().getBus().register(this);

        // 화면 상단 타이틀
        title.setText(appTitle);

        // 회사검색란에 검색 누르면 찾기버튼 클릭
        companyEt.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        companyEt.setOnEditorActionListener((textView, i, keyEvent) -> {
            onSearchCompany(null);
            return true;
        });

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
        U.getInstance().getBus().unregister(this);
        if(bm != null){
            bm.disconnect();
        }
    }

    public void initBeacon(Region myBeacon){
        bm = new BeaconManager(getContext());

        SystemRequirementsChecker.checkWithDefaultDialogs(getActivity());

        bm.connect(() -> bm.startMonitoring(myBeacon));

        bm.setMonitoringListener(new BeaconManager.MonitoringListener() {
            @Override
            public void onEnteredRegion(Region region, List<Beacon> list) {
                Toast.makeText(getContext(), "비콘과 연결되었습니다", Toast.LENGTH_SHORT).show();
                showNotification("알림", "비콘과 연결되었습니다");
                if(userModel.getState() == 0){
                    inBtn.setEnabled(true);
                } else {
                    outBtn.setEnabled(true);
                }
            }
            @Override
            public void onExitedRegion(Region region) {
                Toast.makeText(getContext(), "비콘과 연결이 해제되었습니다", Toast.LENGTH_SHORT).show();
                showNotification("알림", "비콘과 연결이 해제되었습니다");
                inBtn.setEnabled(false);
                outBtn.setEnabled(false);
            }
        });
    }

    public void showNotification(String title, String message) {
        Intent notifyIntent = new Intent(getContext(), MainActivity.class);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivities(getContext(), 0,
                new Intent[] { notifyIntent }, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new Notification.Builder(getContext())
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build();
        notification.defaults |= Notification.DEFAULT_SOUND;
        NotificationManager notificationManager =
                (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);
    }

    @OnClick({R.id.inBtn, R.id.outBtn})
    public void onClickInOut(View view){
        final String state;
        if(view.getId() == R.id.inBtn){
            state = "출근";
        } else {
            state = "퇴근";
        }
        // 데이터 셋팅
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        String dateStr = new SimpleDateFormat("yy-MM-dd HH:mm:ss", Locale.KOREA).format(date);
        WorkModel workModel = new WorkModel(state, dateStr);
        // 유저모델을 해시맵 형태로 변환
        Map<String, Object> data = workModel.toMap();
        // 저장될 위치(키값)과 저장될 데이터(내용) 셋팅
        Map<String, Object> update = new HashMap<>();
        // 키값 획득
        String key = U.getInstance().getDr().child("mywork").child(userModel.getUid()).push().getKey();
        // 관리자용 works에 저장
        update.put("/works/" + key + "/" + userModel.getUid(), data);
        // 사원용 mywork-자신의 uid - key 밑에 저장
        update.put("/mywork/" + userModel.getUid() + "/" + key, data);
        // 사원의 현재 상태 변경
        if(state.equals("출근")) {
            update.put("/users/" + userModel.getUid() + "/state", 1);
            update.put("/companies/" + userModel.getCompany().getcName() + "/userList/" + userModel.getUid() + "/state", 1);
        } else {
            update.put("/users/" + userModel.getUid() + "/state", 0);
            update.put("/companies/" + userModel.getCompany().getcName() + "/userList/" + userModel.getUid() + "/state", 0);
        }
        // 파이어베이스 출퇴근 정보 저장
        U.getInstance().getDr().updateChildren(update, (databaseError, databaseReference) -> {
            if(databaseError != null){ // 실패
                U.getInstance().showPopup(getContext(), SweetAlertDialog.ERROR_TYPE, "실패", "잠시후 다시 시도해주세요",
                        "확인", SweetAlertDialog::dismissWithAnimation);
            } else{ // 성공
                U.getInstance().showPopup(getContext(), SweetAlertDialog.SUCCESS_TYPE, state, dateStr,
                        "확인", SweetAlertDialog::dismissWithAnimation);
                String msg;
                if(state.equals("출근")) {
                    inBtn.setEnabled(false);
                    outBtn.setEnabled(true);
                    msg = "님이 출근하였습니다";
                } else {
                    inBtn.setEnabled(true);
                    outBtn.setEnabled(false);
                    msg = "님이 퇴근하였습니다";
                }

                U.getInstance().getDr().child("companies").child(userModel.getCompany().getcName())
                        .child("adminToken").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String adminToken = dataSnapshot.getValue(String.class);

                        Call<ResponseBody> res =  Net.getInstance().getFcmFactoryIm().sendFcm(
                                adminToken.trim(),
                                "{\"sender\":\""+userModel.getUid()+"\",\"msg\":\""+userModel.getName()+msg+"\",\"channel\":\""+"admin"+"\"}",
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
                    public void onCancelled(DatabaseError databaseError) {}
                });
            }
        });
    }

    public void initRcv(boolean levelFlag){
        if(levelFlag){ // 관리자 - 사원 리스트
            GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 3);
            listRcv.setLayoutManager(gridLayoutManager);

            fAdapter = new FirebaseRecyclerAdapter<UserModel, UserViewHolder>(
                    UserModel.class,
                    R.layout.cell_member_layout,
                    UserViewHolder.class,
                    U.getInstance().getDr().child("companies").child(userModel.getCompany().getcName()).child("userList").orderByChild("level").equalTo(1)
            ) {
                @Override
                protected void populateViewHolder(UserViewHolder viewHolder, UserModel model, int position) {
                    adminInit.setVisibility(View.GONE);
                    //adminCompanyCount.setText("<"+(position+1)+">");
                    viewHolder.bindToItem(model, view1 -> {
                        LinearLayout linearLayout = (LinearLayout) View.inflate(getContext(), R.layout.work_list_layout, null);
                        RecyclerView list = linearLayout.findViewById(R.id.inOutList_rcv);
                        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
                        linearLayoutManager.setReverseLayout(true); // 데이터 받는 순서 == order by
                        linearLayoutManager.setStackFromEnd(true);
                        list.setLayoutManager(linearLayoutManager);
                        FirebaseRecyclerAdapter frAdapter;
                        frAdapter = new FirebaseRecyclerAdapter<WorkModel, WorkViewHolder>(
                                WorkModel.class,
                                R.layout.cell_work_layout,
                                WorkViewHolder.class,
                                U.getInstance().getDr().child("mywork").child(model.getUid())
                        ) {
                            @Override
                            protected void populateViewHolder(WorkViewHolder viewHolder1, WorkModel model1, int position1) {
                                viewHolder1.bindToItem(model1);
                            }
                        };
                        list.setAdapter(frAdapter);

                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
                        alertDialog
                                .setView(linearLayout)
                                .setTitle("출퇴근리스트")
                                .setCancelable(true);
                        alertDialog.create().show();
                    });
                }
            };
        } else { // 사원 - 출퇴근 리스트
            adminInit.setVisibility(View.GONE);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
            linearLayoutManager.setReverseLayout(true); // 데이터 받는 순서 == order by
            linearLayoutManager.setStackFromEnd(true);
            listRcv.setLayoutManager(linearLayoutManager);

            fAdapter = new FirebaseRecyclerAdapter<WorkModel, WorkViewHolder>(
                    WorkModel.class,
                    R.layout.cell_work_layout,
                    WorkViewHolder.class,
                    U.getInstance().getDr().child("mywork").child(userModel.getUid())
            ) {
                @Override
                protected void populateViewHolder(WorkViewHolder viewHolder, WorkModel model, int position) {
                    viewHolder.bindToItem(model);
                }
            };
        }
        listRcv.setAdapter(fAdapter);
    }

    @OnClick(R.id.searchCompanyBtn)
    public void onSearchCompany(View view) {
        // 유효성 검사
        String company_str = companyEt.getText().toString();
        if(TextUtils.isEmpty(company_str)) {
            companyEt.setError("회사명을 입력해주세요");
            companyEt.requestFocus();
            return;
        }

        showPD(getContext());

        // 회사명 리스트 출력
        companyList = new ArrayList();
        adapter = new ArrayAdapter(getContext(), android.R.layout.simple_list_item_1, companyList);
        companyLv.setAdapter(adapter);
        U.getInstance().getDr().child("companies").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    String companyName = data.getKey();
                    if(companyName.contains(company_str))
                        companyList.add(companyName);
                }
                adapter.notifyDataSetChanged();
                stopPD();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                stopPD();
            }
        });

        companyLv.setOnItemClickListener((adapterView, view1, i, l) -> {
            String select_company = companyList.get(i).toString();
            U.getInstance().showPopup(getContext(), SweetAlertDialog.SUCCESS_TYPE, select_company, "선택한 회사로 계속하시겠습니까?",
                    "예", sweetAlertDialog -> {
                        sweetAlertDialog.dismissWithAnimation();
                        showAlertDialog(select_company);
                    },
                    "아니오", SweetAlertDialog::dismissWithAnimation);
        });
    }

    public void showAlertDialog(String company){
        LinearLayout linearLayout = (LinearLayout) View.inflate(getContext(), R.layout.get_dept_position_layout, null);
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
        alertDialog
                .setView(linearLayout)
                .setTitle("직급과 부서를 입력해주세요")
                .setPositiveButton("완료", (dialogInterface, i12) -> {
                    EditText deptEt = linearLayout.findViewById(R.id.dept_et);
                    EditText positionEt = linearLayout.findViewById(R.id.position_et);
                    String dept_str = deptEt.getText().toString();
                    String position_str = positionEt.getText().toString();
                    boolean flag = false;
                    View focus = null;

                    if(TextUtils.isEmpty(position_str)){
                        positionEt.setError("직급을 입력해주세요");
                        flag = true;
                        focus = positionEt;
                    }
                    if(TextUtils.isEmpty(dept_str)){
                        deptEt.setError("부서를 입력해주세요");
                        flag = true;
                        focus = deptEt;
                    }

                    if(flag){
                        focus.requestFocus();
                        return;
                    } else {
                        showPD(getContext());
                        // 데이터 셋팅
                        UserModel.Company companyModel = new UserModel.Company(company, dept_str, position_str);
                        Map<String, Object> data = companyModel.toMap();
                        Map<String, Object> update = new HashMap<>();
                        update.put("/users/" + userModel.getUid() + "/company", data);
                        U.getInstance().getDr().updateChildren(update, (databaseError, databaseReference) -> {
                            stopPD();
                            if(databaseError != null){
                                Toast.makeText(getContext(), "잠시후 다시 시도해주세요", Toast.LENGTH_SHORT).show();
                            } else {
                                companyLv.setAdapter(null);
                                //initUI();
                            }
                        });
                    }
                })
                .setNegativeButton("취소", (dialogInterface, i1) -> dialogInterface.dismiss());
        alertDialog.create().show();
    }
}
