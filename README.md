# Flussonic Watcher SDK

## Installation instructions:

### Add maven repo url at the top of `android/app/build.gradle` right after "apply plugin" string:

```groovy
  repositories {
    maven { url 'https://flussonic-watcher-mobile-sdk.s3.eu-central-1.amazonaws.com/android/watcher-sdk/release'
  }
```

### Add dependencies to `android/app/build.gradle`:

```groovy
android {
  /// your other options

  packagingOptions {
    pickFirst 'lib/**/libcrypto.so'
    pickFirst 'lib/**/libssl.so'
    pickFirst 'lib/**/libswscale.so'
    pickFirst 'lib/**/libswresample.so'
    pickFirst 'lib/**/libavcodec.so'
    pickFirst 'lib/**/libavformat.so'
    pickFirst 'lib/**/libavutil.so'
  }
}
// glideCompiler annotationProcessor needed for LibraryGlideModule integration
annotationProcessor "com.github.bumptech.glide:compiler:4.11.0"

implementation 'com.flussonic:watcher-sdk:2.8.3'
```

### Create stub GlideModule for enabling software decoding of mp4 thumbnails:

```java
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;

@GlideModule
public class GlideStubModule extends AppGlideModule {
}
```

##### Androidx support has been added since 2.0.0 version. For integration without androidx you could use legacy version of Flussonic Watcher SDK, for example: `com.flussonic:watcher-sdk:1.7`.

### Information about usage and documentation [here](https://flussonic.com/doc/watcher/sdk-android/integration-of-flussonic-watcher-sdk-into-apps-for-android)