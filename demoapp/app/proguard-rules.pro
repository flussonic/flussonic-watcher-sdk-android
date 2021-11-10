# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Keep exceptions
-keep public class * extends java.lang.Exception

-dontwarn javax.annotation.**

## Retrofit ##
## http://square.github.io/retrofit/ ##

# Platform calls Class.forName on types which do not exist on Android to determine platform.
-dontnote retrofit2.Platform
# Platform used when running on Java 8 VMs. Will not be used at runtime.
-dontwarn retrofit2.Platform$Java8
# Retain generic type information for use by reflection by converters and adapters.
-keepattributes Signature
# Retain declared checked exceptions for use by a Proxy instance.
-keepattributes Exceptions

## OkHttp3 ##
## https://github.com/krschultz/android-proguard-snippets/blob/master/libraries/proguard-square-okhttp3.pro ##

-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**

## Okio ##
## https://github.com/square/okio#proguard ##

-dontwarn okio.**

## Gson ##
## https://github.com/google/gson/blob/master/examples/android-proguard-example/proguard.cfg ##

# Gson specific classes
-dontwarn sun.misc.**
#-keep class com.google.gson.stream.** { *; }

# Application classes that will be serialized/deserialized over Gson
-keep class flussonic.watcher.sample.data.network.dto.** { *; }

# Prevent proguard from stripping interface information from TypeAdapterFactory,
# JsonSerializer, JsonDeserializer instances (so they can be used in @JsonAdapter)
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

## Android architecture components: Lifecycle ##

# LifecycleObserver's empty constructor is considered to be unused by proguard
-keepclassmembers class * implements androidx.lifecycle.LifecycleObserver {
    <init>(...);
}

# ViewModel's empty constructor is considered to be unused by proguard
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# Keep Lifecycle State and Event enums values
-keepclassmembers class androidx.lifecycle.Lifecycle$State { *; }
-keepclassmembers class androidx.lifecycle.Lifecycle$Event { *; }

# Keep methods annotated with @OnLifecycleEvent even if they seem to be unused
# (Mostly for LiveData.LifecycleBoundObserver.onStateChange(), but who knows)
-keepclassmembers class * {
    @androidx.lifecycle.OnLifecycleEvent *;
}

-keepclassmembers class * implements androidx.lifecycle.LifecycleObserver {
    <init>(...);
}

-keep class * implements androidx.lifecycle.LifecycleObserver {
    <init>(...);
}

-keepclassmembers class androidx.arch.** { *; }
-keep class androidx.arch.** { *; }
-dontwarn androidx.arch.**

# Keep methods annotated with @OnLifecycleEvent even if they seem to be unused
# (Mostly for LiveData.LifecycleBoundObserver.onStateChange(), but who knows)
-keepclassmembers class * {
    @androidx.lifecycle.OnLifecycleEvent *;
}

## AutoValue ##
## https://github.com/rharter/auto-value-gson ##

# Retain generated classes that end in the suffix
-keepnames class **_GsonTypeAdapter

# Prevent obfuscation of types which use @GenerateTypeAdapter since the simple name
# is used to reflectively look up the generated adapter.
-keepnames @com.ryanharter.auto.value.gson.GenerateTypeAdapter class *

-dontwarn com.ryanharter.auto.value.gson.GenerateTypeAdapter
-dontwarn com.google.gson.TypeAdapterFactory

## Glide ##
## https://github.com/bumptech/glide#proguard ##

-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
