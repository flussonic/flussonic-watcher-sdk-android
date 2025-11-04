# Flussonic Watcher SDK for Android

Android SDK library for playing live and archived video streams from Flussonic Media Server with timeline controls.

## Features

- Live video streaming playback
- Archive video playback with timeline
- Interactive timeline with video ranges
- Thumbnail preview support
- Multiple quality selection
- Playback speed control
- Audio controls
- Download archive selection
- Buffering management
- Screenshot capture
- React Native support

## Installation

Add the dependency to your `build.gradle`:

```gradle
dependencies {
    implementation 'com.flussonic:watcher-sdk:2.8.9'
}
```

Then update your `settings.gradle`:

```gradle
pluginManagement {
    repositories {
        mavenCentral()
        maven("https://flussonic-watcher-mobile-sdk.s3.eu-central-1.amazonaws.com/android/watcher-sdk/release")
    }
}
```

### Required Dependencies

The SDK requires the following dependencies:

- AndroidX Media3 (ExoPlayer)
- Glide (for thumbnails)
- RxJava 2
- Retrofit 2

**Important**: You must add `AppGlideModule` to your project for thumbnail functionality to work properly.

## Components

### FlussonicWatcherView

`FlussonicWatcherView` is the main custom composite view that combines a video player and an interactive timeline. It plays video from a specified camera stream and provides playback controls.

#### Basic Setup

Add the view to your XML layout:

```xml
<flussonic.watcher.sdk.presentation.watcher.FlussonicWatcherView
    android:id="@+id/watcher_view"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    app:allowDownload="true" />
```

Initialize in your Activity or Fragment:

```java
FlussonicWatcherView watcherView = findViewById(R.id.watcher_view);

// Initialize with Activity
watcherView.initialize(this);

// Or initialize with Fragment
watcherView.initialize(fragment);

// Set the stream URL
watcherView.setUrl("https://token@server:port/stream");
```

#### URL Format

The SDK supports the following URL format:

```
<protocol>://<token>@<server>:<port>/<stream>?<query>
```

Where:
- `<protocol>` - http or https
- `<token>` - login session (optional, substring `<token>@` can be omitted)
- `<server>` - watcher server name
- `<port>` - watcher server port (optional, substring `:<port>` can be omitted)
- `<stream>` - camera/stream name
- `<query>` - request parameters (optional, substring `?<query>` can be omitted)

If the query contains `from=<number>`, it will be interpreted as the start position.

#### Core Methods

##### Initialization

**`initialize(FragmentActivity activity)`**

Initializes the view with an Activity. Must be called before setting the URL and starting playback.

```java
watcherView.initialize(activity);
```

**`initialize(FragmentActivity activity, boolean reactNative)`**

Initializes the view with an Activity and React Native support flag. Pass `true` if using from React Native module, `false` for native Android apps.

```java
watcherView.initialize(activity, true); // For React Native
```

**`initialize(Fragment fragment)`**

Initializes the view with a Fragment.

```java
watcherView.initialize(fragment);
```

##### URL Configuration

**`setUrl(String url)`**

Sets the URL from which to retrieve watcher server parameters. The watcher server provides streamer parameters for generating URLs for timeline data and MP4 frames.

```java
watcherView.setUrl("https://token@watcher.example.com/camera1");
```

**`setUrl(String url, Long startPosition, Long endPosition)`**

Sets the URL with specific start and end positions for archive playback.

```java
watcherView.setUrl("https://token@watcher.example.com/camera1", 
    1634567890L, 1634568000L);
```

##### Playback Control

**`pause()`**

Pauses the current playback.

```java
watcherView.pause();
```

**`resume()`**

Resumes the playback.

```java
watcherView.resume();
```

**`seek(long seconds)`**

Seeks to a defined playback position in seconds. This starts playing from the specified position and rewinds the timeline accordingly.

```java
watcherView.seek(1634567900L);
```

**`setStartPosition(long dateTimeInSecs)`**

Sets the initial start position in seconds. When the view is initialized, it will start playing from this position.

```java
watcherView.setStartPosition(1634567890L);
```

##### Audio Control

**`disableAudio(boolean audioDisabled)`**

Disables or enables audio track rendering during video playback.

```java
watcherView.disableAudio(false); // Enable audio
watcherView.disableAudio(true);  // Disable audio
```

**`setAudioEnabled(boolean isEnabled)`**

Sets whether audio is enabled.

```java
watcherView.setAudioEnabled(true);
```

**`isAudioDisabled()`**

Returns whether audio is currently disabled.

```java
boolean isAudioOff = watcherView.isAudioDisabled();
```

**`disableSoundBtn()`**

Disables the sound button in the UI.

```java
watcherView.disableSoundBtn();
```

##### Quality Control

**`setQuality(Quality quality)`**

Sets the initial quality for player startup. Users can change quality through the UI after playback starts.

```java
watcherView.setQuality(Quality.LOW);
```

**`getQuality()`**

Returns the current quality setting.

```java
Quality currentQuality = watcherView.getQuality();
```

**`showQualityMenu(Quality quality)`**

Programmatically shows the quality selection menu with the specified quality pre-selected.

```java
watcherView.showQualityMenu(Quality.MEDIUM);
```

**`disableQualityBtn()`**

Disables the quality selection button in the UI.

```java
watcherView.disableQualityBtn();
```

##### Speed Control

**`getSpeed()`**

Returns the current playback speed. Returns 1.0 for live streams. Users can change speed when playing archive.

```java
float speed = watcherView.getSpeed();
```

**`showSpeedMenu(PlaybackSpeed checkedSpeed)`**

Programmatically shows the speed selection menu with the specified speed pre-selected.

```java
watcherView.showSpeedMenu(PlaybackSpeed.SPEED_2X);
```

**`disableSpeedBtn()`**

Disables the speed selection button in the UI.

```java
watcherView.disableSpeedBtn();
```

##### Download/Archive Selection

**`setAllowDownload(boolean allowDownload)`**

Controls whether the scissors icon is visible. When enabled, users can select a range on the timeline to download archive footage.

```java
watcherView.setAllowDownload(true);
```

**`showTrim()`**

Programmatically enters range selection mode for archive download.

```java
watcherView.showTrim();
```

**`hideTrim()`**

Exits range selection mode.

```java
watcherView.hideTrim();
```

**`disableCutBtn()`**

Disables the trim/cut button in the UI.

```java
watcherView.disableCutBtn();
```

##### Screenshot

**`captureScreenshot(String relativePath, String fileName)`**

Captures a screenshot of the current playback and saves it to the specified path. The image is also scanned by MediaScanner. Returns an RxJava `Single` that performs the work when subscribed.

```java
watcherView.captureScreenshot("Screenshots", "screenshot.png")
    .subscribe(
        path -> Log.d("Screenshot", "Saved to: " + path),
        error -> Log.e("Screenshot", "Error: " + error.getMessage())
    );
```

#### Event Listeners

##### setBufferingListener

**`setBufferingListener(FlussonicBufferingListener listener)`**

Receives buffering start and stop events from the player. Use this to show/hide loading indicators during buffering.

```java
watcherView.setBufferingListener(new FlussonicBufferingListener() {
    @Override
    public void onBufferingStart() {
        // Show buffering indicator
    }

    @Override
    public void onBufferingStop(long duration) {
        // Hide buffering indicator
    }
});
```

##### setFlussonicWatcherLifecycleListener

**`setFlussonicWatcherLifecycleListener(FlussonicWatcherLifecycleListener listener)`**

Receives lifecycle events from the watcher view.

```java
watcherView.setFlussonicWatcherLifecycleListener(new FlussonicWatcherLifecycleListener() {
    @Override
    public void onDestroy() {
        // Cleanup when view is destroyed
    }
});
```

##### setPlayerSessionListener

**`setPlayerSessionListener(FlussonicPlayerSessionListener listener)`**

Receives player session-related events.

```java
watcherView.setPlayerSessionListener(sessionListener);
```

##### setQualityListener

**`setQualityListener(FlussonicQualityListener listener)`**

Receives quality change events when the user or system changes video quality.

```java
watcherView.setQualityListener(qualityListener);
```

##### setExoPlayerErrorListener

**`setExoPlayerErrorListener(FlussonicExoPlayerErrorListener listener)`**

Receives player error events when errors occur during playback.

```java
watcherView.setExoPlayerErrorListener(new FlussonicWatcherView.FlussonicExoPlayerErrorListener() {
    @Override
    public void onExoPlayerError(String code, String message, String url) {
        // Handle player errors
    }
});
```

---

### FlussonicThumbnailView

`FlussonicThumbnailView` is designed to display single frame thumbnails from video streams. It's optimized for use in lists (RecyclerView) to show lightweight preview images.

#### Basic Setup

Add the view to your XML layout:

```xml
<flussonic.watcher.sdk.presentation.thumbnail.FlussonicThumbnailView
    android:id="@+id/thumbnail_view"
    android:layout_width="match_parent"
    android:layout_height="wrap_content" />
```

Initialize and load a thumbnail:

```java
FlussonicThumbnailView thumbnailView = findViewById(R.id.thumbnail_view);
thumbnailView.setUrl("https://token@server:port/stream");
```

### Create stub GlideModule for enabling software decoding of mp4 thumbnails:

```kotlin
@GlideModule
class MyGlideModule : AppGlideModule() {
    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        val client = unsafeOkHttpClient
        registry.replace(
            GlideUrl::class.java, InputStream::class.java,
            OkHttpUrlLoader.Factory(client)
        )
    }
}
```

#### Methods

##### URL Configuration

**`setUrl(String url)`**

Sets the URL to load the thumbnail from. Parses watcher connection parameters, loads streamer parameters, and forms MP4 or JPEG URL for the thumbnail.

```java
thumbnailView.setUrl("https://token@watcher.example.com/camera1");
```

**`setUrl(String url, Headers headers)`**

Sets the URL with custom headers for the thumbnail request.

```java
Headers headers = MP4GlideModule.mapToHeaders(headerMap);
thumbnailView.setUrl("https://token@watcher.example.com/camera1", headers);
```

**`show(Camera camera, Date date)`**

Displays a thumbnail from a Camera object at a specific time position.

```java
thumbnailView.show(camera, new Date());
```

##### Base64 Image

**`setBase64Image(String base64)`**

Displays an image from a base64-encoded string. Useful for showing cached or pre-loaded images.

```java
thumbnailView.setBase64Image("data:image/png;base64,iVBORw0KGgoAAAANS...");
```

##### Size Configuration

**`setSize(int previewWidth, int previewHeight)`**

Sets the size for preloaded previews in the disk cache. Default is 848x480.

```java
thumbnailView.setSize(1280, 720);
```

##### Cache Configuration

**`setCacheKey(String cacheKey)`**

Sets a cache key for the preview. When the cache key changes, the preview will be reloaded from the specified URL.

```java
thumbnailView.setCacheKey("camera1_" + System.currentTimeMillis());
```

**`static clearBase64Cache()`**

Clears all cached Base64 bitmaps from memory.

```java
FlussonicThumbnailView.clearBase64Cache();
```

##### Auto-Update

**`startUpdatingPreview(int seconds)`**

Starts automatically updating the preview every specified number of seconds. Useful for live thumbnail updates.

```java
thumbnailView.startUpdatingPreview(5); // Update every 5 seconds
```

**`stopUpdatingPreview()`**

Stops the automatic preview updates.

```java
thumbnailView.stopUpdatingPreview();
```

##### Request Management

**`cancelRequest()`**

Cancels the current thumbnail loading request. Use this when removing the view or in RecyclerView's `onViewRecycled()`.

```java
@Override
public void onViewRecycled(ViewHolder holder) {
    holder.thumbnailView.cancelRequest();
}
```

##### Memory Management

**`static trimMemory(Context context, int level)`**

Clears part of Glide's memory cache based on memory pressure level.

```java
@Override
public void onTrimMemory(int level) {
    super.onTrimMemory(level);
    FlussonicThumbnailView.trimMemory(this, level);
}
```

**`static trimMemory(Context context)`**

Clears part of Glide's memory cache with default level.

```java
FlussonicThumbnailView.trimMemory(context);
```

##### Event Listeners

**`setStatusListener(StatusListener listener)`**

Sets a listener to receive thumbnail loading status updates.

```java
thumbnailView.setStatusListener(new FlussonicThumbnailView.StatusListener() {
    @Override
    public void onStatus(@NonNull Status status, FlussonicThumbnailError error) {
        switch (status) {
            case LOADING:
                // Show loading indicator
                break;
            case LOADED:
                // Hide loading indicator
                break;
            case ERROR:
                // Handle error
                Log.e("Thumbnail", "Error: " + error.message());
                break;
        }
    }
});
```

**`setImageLoadListener(ImageLoadListener listener)`**

Sets a simplified success/failure listener for image loading.

```java
thumbnailView.setImageLoadListener(new FlussonicThumbnailView.ImageLoadListener() {
    @Override
    public void onSuccess() {
        // Image loaded successfully
    }

    @Override
    public void onError(@NonNull FlussonicThumbnailError error) {
        // Handle error
        Log.e("Thumbnail", "Failed: " + error.message());
    }
});
```

**`getStatusListener()`**

Returns the current status listener.

```java
StatusListener listener = thumbnailView.getStatusListener();
```

#### Status Enum

- `LOADING` - Thumbnail is currently loading
- `LOADED` - Thumbnail loaded successfully
- `ERROR` - An error occurred during loading or rendering

---

## ProGuard Rules

If you're using ProGuard, the SDK includes consumer ProGuard rules automatically. No additional configuration is needed.

---

## Debugging and Logging

### initLogger

**`initLogger(String sessionId, String operatorId, String watcherVersion, String appFlavor, String appVersion, String login, String streamName, String streamTitle)`**

Initializes the internal logger with session and application information. This method helps developers debug issues by providing detailed logs with context about the current session, user, and stream being played.

```java
watcherView.initLogger(
    "session_123456",           // Unique session ID
    "operator_001",             // Operator ID
    "2.8.7",                    // Watcher version
    "production",               // App flavor (debug/production)
    "1.0.0",                    // App version
    "user@example.com",         // User login
    "camera1",                  // Stream name
    "Front Door Camera"         // Stream title/description
);
```

**Parameters:**
- `sessionId` - Unique identifier for the current session
- `operatorId` - Operator or organization identifier
- `watcherVersion` - Version of the watcher SDK being used
- `appFlavor` - Build flavor (e.g., "debug", "production", "staging")
- `appVersion` - Your application version
- `login` - User login or identifier
- `streamName` - Name of the stream/camera
- `streamTitle` - Human-readable title or description of the stream

This logger information is particularly useful when troubleshooting playback issues, timeline problems, or when reporting bugs to [Flussonic support](https://flussonic.com/contacts).

---

## Requirements

- **Minimum SDK**: 21 (Android 5.0)
- **Target SDK**: See gradle.properties
- **Java Version**: 11
- **Glide**: You must add `AppGlideModule` to your project

---

## React Native Support

The SDK supports React Native integration. When initializing, pass `true` for the `reactNative` parameter:

```java
watcherView.initialize(activity, true);
```

This enables special redrawing behavior required for React Native views.

---

## Example Usage

### Complete Activity Example

```java
public class VideoPlayerActivity extends AppCompatActivity {
    
    private FlussonicWatcherView watcherView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);
        
        watcherView = findViewById(R.id.watcher_view);
        
        // Initialize
        watcherView.initialize(this);
        
        // Configure
        watcherView.setHideToolbarInPortrait(true);
        watcherView.setAllowDownload(true);
        watcherView.setQuality(Quality.AUTO);
        
        // Set listeners
        watcherView.setBufferingListener(new FlussonicBufferingListener() {
            @Override
            public void onBufferingStart() {
                // Show loading
            }
            
            @Override
            public void onBufferingStop(long duration) {
                // Hide loading
            }
        });
        
        watcherView.setUpdateProgressEventListener(event -> {
            long currentTime = event.currentUtcInSeconds();
            // Update UI with current time
        });
        
        // Set URL and start playback
        watcherView.setUrl("https://demo:demo@demo.example.com/camera1");
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (watcherView != null) {
            watcherView.release();
        }
    }
    
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (watcherView != null) {
            watcherView.clearCache();
        }
    }
}
```

### RecyclerView Adapter with Thumbnails

```java
public class CameraAdapter extends RecyclerView.Adapter<CameraAdapter.ViewHolder> {
    
    private List<Camera> cameras;
    
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Camera camera = cameras.get(position);
        
        holder.thumbnailView.setSize(640, 360);
        holder.thumbnailView.setUrl(camera.getUrl());
        holder.thumbnailView.startUpdatingPreview(10); // Update every 10 seconds
        
        holder.thumbnailView.setImageLoadListener(new FlussonicThumbnailView.ImageLoadListener() {
            @Override
            public void onSuccess() {
                holder.progressBar.setVisibility(View.GONE);
            }
            
            @Override
            public void onError(@NonNull FlussonicThumbnailError error) {
                holder.progressBar.setVisibility(View.GONE);
                holder.errorText.setVisibility(View.VISIBLE);
            }
        });
    }
    
    @Override
    public void onViewRecycled(ViewHolder holder) {
        holder.thumbnailView.stopUpdatingPreview();
        holder.thumbnailView.cancelRequest();
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        FlussonicThumbnailView thumbnailView;
        ProgressBar progressBar;
        TextView errorText;
        
        ViewHolder(View view) {
            super(view);
            thumbnailView = view.findViewById(R.id.thumbnail);
            progressBar = view.findViewById(R.id.progress);
            errorText = view.findViewById(R.id.error);
        }
    }
}
```

---

## License

See LICENSE file for details.

---

## Support

For issues, questions, or contributions, please visit the project repository or contact [Flussonic support](https://flussonic.com/contacts).

  