package com.erlyvideo.sample.Control;

import androidx.annotation.Nullable;

import com.erlyvideo.sample.Api.ServerApi;
import com.erlyvideo.sample.BuildConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;


import flussonic.watcher.sdk.data.network.dto.FlussonicSdkAdapterFactory;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import timber.log.Timber;


public class ServerConnect {

    public ServerConnect(){

    }
    private static final long TIMEOUT_IN_SECONDS = 15;

    @Nullable
    private static Interceptor getLoggingInterceptor() {
        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor(Timber::d);
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            return interceptor;
        }
        return null;
    }

    private OkHttpClient provideOkHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        Interceptor loggingInterceptor = getLoggingInterceptor();
        if (loggingInterceptor != null) {
            builder.addInterceptor(loggingInterceptor);
        }
        builder.connectTimeout(TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);
        builder.writeTimeout(TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);
        builder.readTimeout(TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);
        return builder.build();
    }

    private Retrofit provideRetrofit(String baseUrl) {
        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(provideOkHttpClient())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(provideGsonConverterFactory(provideGson()))
                .build();
    }

    private GsonConverterFactory provideGsonConverterFactory(Gson gson) {
        return GsonConverterFactory.create(gson);
    }

    private Gson provideGson() {
        GsonBuilder gsonBuilder = new GsonBuilder()
                .serializeNulls()
                .disableHtmlEscaping()
                .registerTypeAdapterFactory(FlussonicSdkAdapterFactory.create());
        if (BuildConfig.DEBUG) {
            gsonBuilder.setPrettyPrinting();
        }
        return gsonBuilder.create();
    }

    public ServerApi provideSampleNetworkService(String baseUrl) {
        return provideRetrofit(baseUrl).create(ServerApi.class);
    }

    /*
    private Retrofit retrofit = null;

    private static final long TIMEOUT_IN_SECONDS = 15;

    @Nullable
    private static Interceptor getLoggingInterceptor() {
        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor(Timber::d);
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            return interceptor;
        }
        return null;
    }

    private OkHttpClient provideOkHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        Interceptor loggingInterceptor = getLoggingInterceptor();
        if (loggingInterceptor != null) {
            builder.addInterceptor(loggingInterceptor);
        }
        builder.connectTimeout(TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);
        builder.writeTimeout(TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);
        builder.readTimeout(TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);
        return builder.build();
    }

    public Retrofit getRetrofit(String url){
        if (retrofit == null){
            retrofit = new Retrofit.Builder()
                    .baseUrl(url)
                    .client(provideOkHttpClient())
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build();
        }
        return retrofit;
    }
    */

}
