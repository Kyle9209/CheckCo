package com.test.www.finalproject.net;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface FcmFactoryIm {
    //1. 푸시 전송
    @FormUrlEncoded
    @Headers({
            "Authorization:key=AAAA4OXo-TY:APA91bGF9GhRhdQbzrYPYlJ1tw88vLJgq2TGE-82vIJ7CuBywSUjC4IGj-2_tJSt8UtCZpTT8oP-VkKB4Aqhf0GVRkWbzhO8pxPeb-_oGvGDhURxmhfrbyAG02gcjvN3zit2kED8rpC0",
            "Content-Type:application/x-www-form-urlencoded"
    })
    @POST("send")
    Call<ResponseBody> sendFcm(@Field("to") String to, @Field("data") String data, @Field("collapse_key") int collapse_key, @Field("priority") String priority);
}
