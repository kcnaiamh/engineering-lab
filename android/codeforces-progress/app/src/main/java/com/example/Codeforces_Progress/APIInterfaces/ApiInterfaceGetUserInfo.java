package com.example.Codeforces_Progress.APIInterfaces;

import com.example.Codeforces_Progress.UserInfoClasses.UserInfo;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiInterfaceGetUserInfo {
    /*
     * retrofit will use relative URL to fetch data and
     * it will add body to the following abstract method
     */
    @GET("user.info")
    /*
     * the parameters of getUserInfo function:
     * handle: Codeforces user handle.
     */
    Call<UserInfo> getUserInfo(@Query("handles") String handle);
}
