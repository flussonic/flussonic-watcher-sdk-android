package flussonic.watcher.sample.data.network.services;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import flussonic.watcher.sample.BuildConfig;
import flussonic.watcher.sample.data.network.dto.FlussonicSampleAdapterFactory;
import flussonic.watcher.sdk.data.network.dto.FlussonicSdkAdapterFactory;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import timber.log.Timber;

public class SampleNetworkProvider {

    private static final long TIMEOUT_IN_SECONDS = 25;

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
                .registerTypeAdapterFactory(FlussonicSampleAdapterFactory.create())
                .registerTypeAdapterFactory(FlussonicSdkAdapterFactory.create());
        if (BuildConfig.DEBUG) {
            gsonBuilder.setPrettyPrinting();
        }
        return gsonBuilder.create();
    }

    public SampleNetworkService provideSampleNetworkService(String baseUrl) {
        return provideRetrofit(baseUrl).create(SampleNetworkService.class);
    }
}
