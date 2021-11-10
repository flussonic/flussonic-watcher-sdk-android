package flussonic.watcher.sample.data.network.services;

import androidx.annotation.NonNull;

import java.util.List;

import flussonic.watcher.sample.data.network.dto.LoginDto;
import flussonic.watcher.sample.data.network.dto.LoginRequestDto;
import flussonic.watcher.sdk.data.network.dto.CameraDto;
import io.reactivex.Single;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface SampleNetworkService {

    @POST("auth/login")
    @Headers({"Content-Type: application/json"})
    Single<LoginDto> login(@Body @NonNull LoginRequestDto request);

    @GET("cameras?limit=65")
    @Headers("Content-Type: application/json")
    Single<List<CameraDto>> cameras(@Header("X-Vsaas-Session") @NonNull String token);
}
