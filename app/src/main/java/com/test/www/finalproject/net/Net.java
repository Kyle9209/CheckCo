package com.test.www.finalproject.net;

import android.content.Context;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by kkmnb on 2017-09-08.
 */

public class Net {
    private static final Net ourInstance = new Net();

    public static Net getInstance() {
        return ourInstance;
    }

    public Net() {}

    // =============================================================================================
    // 보안, 타임아웃, 세션등 처리를위한 코드 => OKHttp3 내장하고 있어서 이를 커스터마이즈 하면 적용 가능
    public void launch(Context context)
    {
        OkHttpClient.Builder clientImp =  new OkHttpClient.Builder();
        // 통신시간, 연결수 제한등 구성처리
        //clientImp.interceptors().add(new AddHeaderInterceptor());      // 헤더
        clientImp.connectTimeout(30, TimeUnit.SECONDS); // 연결제한시간 : 30초이내에 연결되면 통과
        clientImp.readTimeout(30, TimeUnit.SECONDS);    // 응답제한시간 : 30초이내에 수신되면 통과

        retrofit = new Retrofit.Builder()
                .baseUrl("https://fcm.googleapis.com/fcm/")                          // 기본 도메인 설정
                .addConverterFactory(GsonConverterFactory.create()) // 응답데이터를 json 자동 변환
                .client(clientImp.build())                          // okhttp를 커스터마이즈 하여 적용
                .build();
    }
    // 레트로핏 생성
    private Retrofit retrofit;
    public Retrofit getRetrofit() {
        return retrofit;
    }
    // =============================================================================================

    FcmFactoryIm fcmFactoryIm;
    public FcmFactoryIm getFcmFactoryIm() {
        if(fcmFactoryIm == null) {
            fcmFactoryIm = retrofit.create(FcmFactoryIm.class);
        }
        return fcmFactoryIm;
    }
}
