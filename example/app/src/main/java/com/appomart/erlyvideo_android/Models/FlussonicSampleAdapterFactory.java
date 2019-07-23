package com.appomart.erlyvideo_android.Models;

import com.google.gson.TypeAdapterFactory;
import com.ryanharter.auto.value.gson.GsonTypeAdapterFactory;

import flussonic.watcher.sdk.data.network.dto.AutoValueGson_FlussonicSdkAdapterFactory;

@GsonTypeAdapterFactory
public abstract class FlussonicSampleAdapterFactory implements TypeAdapterFactory {

    public static TypeAdapterFactory create() {
        return new AutoValueGson_FlussonicSdkAdapterFactory();
    }
}