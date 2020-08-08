package com.example.ar8ee.fragments;

import com.example.ar8ee.notification.MyResponse;
import com.example.ar8ee.notification.Sender;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAE8hP6Q0:APA91bGJPoq8U1gMOZgDy74abrAxeF20CCK_9wmv_Pe8DWJ7SrVAInGlG6zBCHqFBlF_Ej_zc4fD9nm7Sp4B6lN_zdlsx0w4IC6tREjqxsQ8ZShIpLd5hGPqGftc47c5U1KZjypUoDrJ"
            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
