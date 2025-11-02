package com.example.Codeforces_Progress.APIInterfaces;

import com.example.Codeforces_Progress.UserStatusClasses.UserStatus;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiInterfaceGetStatus {
    /*
     * retrofit will use relative URL to fetch data and
     * it will add body to the following abstract method
     */
    @GET("user.status")
    /*
     * the parameters of getUserStatus function:
     * handle: Codeforces user handle.
     */
    Call<UserStatus> getUserStatus(@Query("handle") String handle);

}
