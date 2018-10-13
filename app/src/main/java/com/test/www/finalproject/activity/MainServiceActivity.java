package com.test.www.finalproject.activity;

import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.test.www.finalproject.R;
import com.test.www.finalproject.RootActivity;
import com.test.www.finalproject.fragment.ChattingListFragment;
import com.test.www.finalproject.fragment.HomeFragment;
import com.test.www.finalproject.fragment.MemberListFragment;
import com.test.www.finalproject.fragment.SettingFragment;
import com.test.www.finalproject.model.UserModel;
import com.test.www.finalproject.util.U;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainServiceActivity extends RootActivity {
    @BindView(R.id.viewpager) ViewPager viewPager;
    @BindView(R.id.navigation) BottomNavigationView navigation;
    MainServiceActivity self;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_service);
        ButterKnife.bind(this);
        self = this;

        initUI();

        getUserData();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // 앱 바로 종료
        ActivityCompat.finishAffinity(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        U.getInstance().setUserModel(null);
    }

    public void initUI(){
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        SectionsPagerAdapter adapter = new SectionsPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(4);
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
            @Override
            public void onPageSelected(int position) {
                switch (position){
                    case 0:
                        navigation.setSelectedItemId(R.id.navigation_home);
                        break;
                    case 1:
                        navigation.setSelectedItemId(R.id.navigation_member);
                        break;
                    case 2:
                        navigation.setSelectedItemId(R.id.navigation_chatting);
                        break;
                    case 3:
                        navigation.setSelectedItemId(R.id.navigation_setting);
                        break;
                }
            }
            @Override
            public void onPageScrollStateChanged(int state) {}
        });
    }

    public void getUserData(){
        showPD(self);
        String uid = U.getInstance().getFirebaseAuth().getCurrentUser().getUid();
        // 프로필 데이터 가져오기
        U.getInstance().getDr().child("users").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UserModel userModel = dataSnapshot.getValue(UserModel.class);
                U.getInstance().setUserModel(userModel);
                U.getInstance().getBus().post("updateUserData");
                stopPD();
        }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                //Toast.makeText(self, "메인서비스 에러", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class SectionsPagerAdapter extends FragmentPagerAdapter {

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0:
                    return new HomeFragment();
                case 1:
                    return new MemberListFragment();
                case 2:
                    return new ChattingListFragment();
                default:
                    return new SettingFragment();
            }
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "홈";
                case 1:
                    return "사원리스트";
                case 2:
                    return "채팅리스트";
                case 3:
                    return "설정";
            }
            return null;
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
                switch (item.getItemId()) {
                    case R.id.navigation_home:
                        viewPager.setCurrentItem(0, true);
                        return true;
                    case R.id.navigation_member:
                        viewPager.setCurrentItem(1, true);
                        return true;
                    case R.id.navigation_chatting:
                        viewPager.setCurrentItem(2, true);
                        return true;
                    case R.id.navigation_setting:
                        viewPager.setCurrentItem(3, true);
                        return true;
                }
                return false;
            };
}
