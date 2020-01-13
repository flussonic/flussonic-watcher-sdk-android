package com.appomart.erlyvideo_android.Api;

import androidx.annotation.NonNull;

import com.appomart.erlyvideo_android.Models.LoginUser;

import java.util.List;

import flussonic.watcher.sdk.data.network.dto.CameraDto;
import io.reactivex.Single;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;


public interface ServerApi {

    @POST("/vsaas/api/v2/auth/check-login")
    Single<Boolean> checklogin(@Body Object user);

    @POST("/vsaas/api/v2/auth/login")
    Single<LoginUser> login(@Body Object user);

    @GET("/vsaas/api/v2/cameras")
    Single<List<CameraDto>> cameras(@Header("X-Vsaas-Session") @NonNull String token);

}
