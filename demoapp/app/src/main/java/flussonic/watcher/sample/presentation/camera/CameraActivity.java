package flussonic.watcher.sample.presentation.camera;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import flussonic.watcher.sample.BuildConfig;
import flussonic.watcher.sample.R;
import flussonic.watcher.sample.presentation.core.BaseActivity;
import flussonic.watcher.sample.presentation.core.Settings;
import flussonic.watcher.sdk.domain.pojo.Camera;
import flussonic.watcher.sdk.domain.pojo.PlaybackStatus;
import flussonic.watcher.sdk.domain.pojo.Track;
import flussonic.watcher.sdk.domain.pojo.UpdateProgressEvent;
import flussonic.watcher.sdk.domain.utils.CalendarUtils;
import flussonic.watcher.sdk.domain.utils.FlussonicUtils;
import flussonic.watcher.sdk.presentation.core.listeners.FlussonicBufferingListener;
import flussonic.watcher.sdk.presentation.core.listeners.FlussonicCollapseExpandTimelineListener;
import flussonic.watcher.sdk.presentation.core.listeners.FlussonicDownloadRequestListener;
import flussonic.watcher.sdk.presentation.core.listeners.FlussonicPlayerSessionListener;
import flussonic.watcher.sdk.presentation.core.listeners.FlussonicUpdateProgressEventListener;
import flussonic.watcher.sdk.presentation.timeline.animation.ToolbarAnimator;
import flussonic.watcher.sdk.presentation.utils.DialogUtils;
import flussonic.watcher.sdk.presentation.watcher.FlussonicColorTheme;
import flussonic.watcher.sdk.presentation.watcher.FlussonicWatcherView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class CameraActivity extends BaseActivity implements FlussonicCollapseExpandTimelineListener {

    private static final String SERVER = BuildConfig.SERVER;

    private static final String EXTRA_CAMERA = "EXTRA_CAMERA";
    private static final String EXTRA_SESSION = "EXTRA_SESSION";
    private static final String EXTRA_CAMERAS = "EXTRA_CAMERAS";
    private static final String EXTRA_START_POSITION = "EXTRA_START_POSITION";
    private static final String DATE_TIME_PICKER_SUFFIX = CameraActivity.class.getName();

    private static final int REQUEST_WRITE_STORAGE_PERMISSION = 1;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private int toolbarHeight;
    private ToolbarAnimator toolbarAnimator;
    @ColorInt
    private int toolbarColor;
    @ColorInt
    private int toolbarColorTransparent;
    private Toolbar toolbar;
    private View container;
    private TextView textViewInfoLeft;
    private TextView textViewUtc,
            textViewStatus,
            textViewSpeed,
            textViewTracks,
            textViewCurrentTrack,
            textViewStartPosition;
    private FlussonicWatcherView flussonicWatcherView;
    private Camera camera;
    private String session;
    private List<Camera> cameras;
    private long startPosition;

    public static Intent getStartIntent(@NonNull Context context,
                                        @NonNull Camera camera,
                                        @NonNull String session,
                                        @NonNull List<Camera> cameras,
                                        long startPosition) {
        Intent intent = new Intent(context, CameraActivity.class);
        intent.putExtra(EXTRA_CAMERA, camera);
        intent.putExtra(EXTRA_SESSION, session);
        intent.putExtra(EXTRA_CAMERAS, new ArrayList<>(cameras));
        intent.putExtra(EXTRA_START_POSITION, startPosition);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        container = findViewById(R.id.container);
        textViewInfoLeft = findViewById(R.id.text_view_info_left);
        textViewUtc = findViewById(R.id.text_view_utc);
        textViewStatus = findViewById(R.id.text_view_status);
        textViewSpeed = findViewById(R.id.text_view_speed);
        textViewTracks = findViewById(R.id.text_view_tracks);
        textViewCurrentTrack = findViewById(R.id.text_view_current_track);
        textViewStartPosition = findViewById(R.id.text_view_start_position);

        toolbarHeight = getResources().getDimensionPixelSize(R.dimen.toolbar_height);
        toolbarAnimator = new ToolbarAnimator(toolbarHeight);

        camera = savedInstanceState == null
                ? getIntent().getParcelableExtra(EXTRA_CAMERA)
                : savedInstanceState.getParcelable(EXTRA_CAMERA);
        session = getIntent().getStringExtra(EXTRA_SESSION);
        cameras = getIntent().getParcelableArrayListExtra(EXTRA_CAMERAS);
        startPosition = getIntent().getLongExtra(EXTRA_START_POSITION, 0);
        textViewStartPosition.setText(String.valueOf(startPosition));

        setupToolbar();
        setupTitle();
        setupWatcher();


        toolbarColor = ContextCompat.getColor(this, R.color.colorPrimary);
        TypedValue outValue = new TypedValue();
        getResources().getValue(flussonic.watcher.sdk.R.dimen.fs_timeline_transparent_factor, outValue, true);
        float timelineTransparentFactor = outValue.getFloat();
        int alpha = Math.round(255 * timelineTransparentFactor);
        toolbarColorTransparent = ColorUtils.setAlphaComponent(toolbarColor, alpha);

        DialogUtils.hideDateTimePicker(getSupportFragmentManager(), DATE_TIME_PICKER_SUFFIX);

        boolean isLandscape = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
        setConfiguration(isLandscape);

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(EXTRA_CAMERA, camera);
    }

    private void setupToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) {
            return;
        }
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    private void setupTitle() {
        String title = camera.title();
        if (!TextUtils.isEmpty(title)) {
            setTitle(title);
        }
    }

    private void setupWatcher() {
        // Чтобы плеер не пересоздавался (и в частности чтобы пауза плеера сохранялась)
        // при повороте экрана, добавьте в манифест активити, использующей
        // FlussonicWatcherView, атрибут android:configChanges="orientation|screenSize"

        // Инициализация компонента Watcher
        // Watcher представляет собой player + timeline
        flussonicWatcherView = findViewById(R.id.watcher_view);

        Map<String, String> colors = getCustomColorsMap();
        FlussonicColorTheme flussonicColorTheme = FlussonicColorTheme.getInstance();
        // flussonicColorTheme.setColorThemeHex(flussonicWatcherView, colors);
        // Параметр allowDownload можно задать как в xml-разметке, так и в коде
        // allow download -- разрешить загрузку части архива
        flussonicWatcherView.setAllowDownload(Settings.allowDownload(this));
        flussonicWatcherView.setHideToolbarInPortrait(true);

        flussonicWatcherView.setStartPosition(startPosition);

        // Установка слушателя, чтобы запускать анимацию тулбара синхронно анимации таймлайна
        flussonicWatcherView.setCollapseExpandTimelineListener(this);
        flussonicWatcherView.setToolbarHeight(toolbarHeight);
        flussonicWatcherView.disableAudio(true);

        // Инициализация параметров подключения к камере
        flussonicWatcherView.initialize(this);
        setUrl(false);
        flussonicWatcherView.enableTimelineMarkersV2(true);

        // Примеры вызовов: см. onOptionsItemSelected, onLowMemory

        // Установка слушателя на события буферизации
        flussonicWatcherView.setBufferingListener(new FlussonicBufferingListener() {

            @Override
            public void onBufferingStart() {
                Timber.d("onBufferingStart");
                textViewInfoLeft.setText(R.string.buffering);
            }

            @Override
            public void onBufferingStop() {
                Timber.d("onBufferingStop");
                textViewInfoLeft.setText("");
            }
        });

        // Установка слушателя запроса на сохранения части архива
        //noinspection Convert2Lambda
        flussonicWatcherView.setDownloadRequestListener(new FlussonicDownloadRequestListener() {

            @Override
            public void onDownloadRequest(long from, long to) {
                Timber.d("onDownloadRequest: from %d to %d", from, to);
                String fromString = CalendarUtils.toString(from, CalendarUtils.DATE_TIME_PATTERN);
                String toString = CalendarUtils.toString(to, CalendarUtils.DATE_TIME_PATTERN);
                showToast(getString(R.string.download_video_request, fromString, toString));
            }
        });

        // Установка слушателя, который вызывается раз в секунду, в параметре передается текущее
        // время проигрывания, UTC, в секундах
        //noinspection Convert2Lambda
        flussonicWatcherView.setUpdateProgressEventListener(new FlussonicUpdateProgressEventListener() {

            @Override
            public void onUpdateProgress(@NonNull UpdateProgressEvent event) {
                // event.currentUtcInSeconds() is the same as flussonicWatcherView.getCurrentUtcInSeconds()
                // event.playbackStatus() is the same as flussonicWatcherView.getPlaybackStatus()
                // event.speed() is the same as flussonicWatcherView.getSpeed()
                List<Track> tracks = flussonicWatcherView.getAvailableTracks();
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < tracks.size(); i++) {
                    sb.append(tracks.get(i).trackId());
                    if (i < tracks.size() - 1) {
                        sb.append(", ");
                    }
                }
                String tracksString = sb.toString();
                Track currentTrack = flussonicWatcherView.getCurrentTrack();
                String videoTrack = currentTrack == null ? "NO" : currentTrack.trackId();
                String audioDisabled = event.audioDisabled() ? "Audio OFF" : "Audio ON";

                textViewUtc.setText(String.valueOf(event.currentUtcInSeconds()));
                textViewStatus.setText(String.valueOf(event.playbackStatus()));
                textViewSpeed.setText(String.format(Locale.US, "%.1f", event.speed()));
                textViewTracks.setText(tracksString.isEmpty() ? "NO" : tracksString);
                textViewCurrentTrack.setText(String.format("Video: %s %s", videoTrack, audioDisabled));
            }
        });
        flussonicWatcherView.setExoPlayerErrorListener(new FlussonicWatcherView.FlussonicExoPlayerErrorListener() {
            @Override
            public void onExoPlayerError(String code, String message, String url) {
                Timber.e("onExoPlayerError code: %s, message: %s, player_url: %s", code, message, url);

            }
        });
        flussonicWatcherView.setPlayerSessionListener(new FlussonicPlayerSessionListener() {
            @Override
            public void onPlaybackSessionCreated(@NonNull String playbackSession) {
                Timber.d("onPlaybackSessionCreated playbackSession: %s", playbackSession);
            }

            @Override
            public void onPlaybackSessionActive(@NonNull String playbackSession) {
                Timber.d("onPlaybackSessionActive playbackSession: %s", playbackSession);
            }

            @Override
            public void onPlaybackSessionFinished(@NonNull String playbackSession) {
                Timber.d("onPlaybackSessionFinished playbackSession: %s", playbackSession);
            }

            @Override
            public void onMediaSessionChanged(@NonNull String mediaSession) {
                Timber.d("onPlaybackSession mediaSession: %s", mediaSession);
            }
        });
    }

    private String colorToString(int colorId) {
        return "#" + Integer.toHexString(ContextCompat.getColor(this, colorId)).substring(2);
    }

    private Map<String, String> getCustomColorsMap() {
        Map<String, String> colors = new HashMap();
        // vsaasio theme
        colors.put("fs_chart_background", "#aac");
       /// ruler color white
        colors.put("fs_ruler_line", "#fff");
        colors.put("fs_loading_range", "#ccc");
       /// btnColor
        colors.put("fs_range", "#d0577c");
       /// transparent background for cutOpen timeline
        colors.put("fs_cut_range", "#00000046");
       /// backgroundCommon
        colors.put("fs_pause_button_pressed", "#fff");
       /// btnColor
        colors.put("fs_progress_bar", "#d0577c");
        colors.put("fs_camera_status_icon_active", "#21d921");
        colors.put("fs_camera_status_icon_inactive", "#f2183d");
        // colors.put("fs_bottom_bar_gradient1", "#00000044");
        // colors.put("fs_bottom_bar_gradient2", "#00000055");
        // colors.put("fs_bottom_bar_date_text", "#fff");
        // colors.put("fs_bottom_bar_icon", "#fff");
        colors.put("fs_event", "#f2183d");
        colors.put("fs_event_vehicle", "#f2183d");
        colors.put("fs_event_face", "#f2183d");
        // colors.put("fs_time_label", "#fff");
        // colors.put("fs_cut_value", "#fff");
        // colors.put("fs_cut_label", "#ffffffaa");
        // colors.put("fs_cut_timestamp_background", "#ffffffaa");
        // colors.put("fs_bottom_bar_divider", "#ffffffaa");
        // colors.put("fs_dash", "#ffffffaa");
        // vsaasio theme
        
        /// numbers color on timeline
        colors.put("fs_time_label", "#000");
        /// time color for cutOpen
        colors.put("fs_cut_value", "#000");
        /// onSurfaceBgColor
        colors.put("fs_cut_label", "#000a");
        /// surfaceColorVariant + iconOpacity
        colors.put("fs_cut_timestamp_background", "#000");
        /// navbarColor
        colors.put("fs_bottom_bar_gradient1", "#fff4");
        colors.put("fs_bottom_bar_gradient2", "#fff5");
        /// onNavbarTitleColor
        colors.put("fs_bottom_bar_date_text", "#000");
        /// onNavbarIconColor
        colors.put("fs_bottom_bar_icon", "#000");
        /// borderColor
        colors.put("fs_bottom_bar_divider", "#000a");
        /// onSurfaceBgColor
        colors.put("fs_dash", "#000a");
        return colors;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.camera, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item;
        item = menu.findItem(R.id.clear_cache);
        item.setVisible(BuildConfig.DEBUG);
        item = menu.findItem(R.id.next_camera);
        item.setVisible(BuildConfig.DEBUG);
        item = menu.findItem(R.id.prev_camera);
        item.setVisible(BuildConfig.DEBUG);
        item = menu.findItem(R.id.allow_download);
        boolean allowDownload = Settings.allowDownload(this);
        item.setChecked(allowDownload);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.capture_screenshot:
                if (isWriteStoragePermissionGranted()) {
                    // Сделать скриншот с камеры и сохранить его в папку фотографий на устройстве
                    captureScreenshot();
                }
                return true;
            case R.id.pause:
                // Пауза проигрывания
                flussonicWatcherView.pause();
                return true;
            case R.id.resume:
                // Возобновление проигрывания
                flussonicWatcherView.resume();
                return true;
            case R.id.seek:
                // Начать проигрывание с выбранного времени
                DialogUtils.showDateTimePicker(
                        getSupportFragmentManager(),
                        DATE_TIME_PICKER_SUFFIX,
                        0, 0, FlussonicUtils.utcTimeSeconds(),
                        flussonicWatcherView::seek);
                return true;
            case R.id.allow_download:
                // Разрешить загрузку части архива
                boolean allowDownload = Settings.allowDownload(this);
                allowDownload = !allowDownload;
                Settings.setAllowDownload(this, allowDownload);
                flussonicWatcherView.setAllowDownload(allowDownload);
                return true;
            case R.id.clear_cache:
                // Очистить кэш загруженных диапазонов
                flussonicWatcherView.clearCache();
                return true;
            case R.id.prev_camera: {
                if (cameras.size() < 2) {
                    showToast("No prev camera");
                    return true;
                }
                int index = 0;
                for (int i = 0; i < cameras.size(); ++i) {
                    if (TextUtils.equals(camera.name(), cameras.get(i).name())) {
                        index = i == 0 ? cameras.size() - 1 : i - 1;
                        break;
                    }
                }
                camera = cameras.get(index);
                setupTitle();
                setUrl(true);
                return true;
            }
            case R.id.next_camera: {
                if (cameras.size() < 2) {
                    showToast("No next camera");
                    return true;
                }
                int index = 0;
                for (int i = 0; i < cameras.size(); ++i) {
                    if (TextUtils.equals(camera.name(), cameras.get(i).name())) {
                        index = i == cameras.size() - 1 ? 0 : i + 1;
                        break;
                    }
                }
                camera = cameras.get(index);
                setupTitle();
                setUrl(true);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void setUrl(boolean setStartPositionFromUrl) {
        try {
            URL serverUrl = new URL(SERVER);
            String urlString = String.format(Locale.US, "%s://%s@%s/%s%s",
                    serverUrl.getProtocol(),
                    session,
                    serverUrl.getAuthority(),
                    camera.name(),
                    setStartPositionFromUrl ? "?from=" + startPosition : "");
            flussonicWatcherView.setUrl(urlString);
        } catch (MalformedURLException e) {
            throw new RuntimeException("failed to set url", e);
        }
    }

    private void captureScreenshot() {
        String state = Environment.getExternalStorageState();
        boolean isExternalStorageWritable = Environment.MEDIA_MOUNTED.equals(state);
        if (!isExternalStorageWritable) {
            Timber.e("External storage isn't mounted");
            showToast(getString(R.string.failed_to_save_screenshot));
            return;
        }

        PlaybackStatus playbackStatus = flussonicWatcherView.getPlaybackStatus();
        if (playbackStatus == PlaybackStatus.PREPARING
                || playbackStatus == PlaybackStatus.IDLE) {
            Timber.e("Playing not started");
            showToast(getString(R.string.please_wait));
            return;
        }
        String picturesSubdirectoryName = Environment.DIRECTORY_DOWNLOADS + File.separator + "FlussonicWatcherSample";
        String fileName = CalendarUtils.toString(
                flussonicWatcherView.getCurrentUtcInSeconds(),
                CalendarUtils.DATE_TIME_FILE_NAME_PATTERN)
                + ".png";

        compositeDisposable.add(
                flussonicWatcherView.captureScreenshot(picturesSubdirectoryName, fileName)
                        .doOnSuccess((path) -> Timber.d("Screenshot saved as %s", path))
                        .doOnError(throwable -> Timber.e(throwable, "Failed to save screenshot"))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                (path) -> showToast(getString(R.string.screenshot_saved, path)),
                                throwable -> showToast(getString(R.string.failed_to_save_screenshot))
                        ));
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Timber.w("onLowMemory");
        // При нехватке памяти можно попытаться очистить кэш с загруженными зонами
        flussonicWatcherView.clearCache();
    }

    private boolean isWriteStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                // TODO shouldShowRequestPermissionRationale
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_STORAGE_PERMISSION);
                return false;
            }
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_WRITE_STORAGE_PERMISSION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    captureScreenshot();
                } else {
                    showToast(getString(R.string.write_storage_permission_is_not_granted));
                }
                break;
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        flussonicWatcherView.release();
        compositeDisposable.dispose();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Timber.d("onConfigurationChanged");

        boolean isLandscape = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE;
        setConfiguration(isLandscape);

        if (!isLandscape) {
            toolbarAnimator.cancelAnimation();
        }
    }

    private void setConfiguration(boolean isLandscape) {
        toolbar.setBackgroundColor(isLandscape ? toolbarColorTransparent : toolbarColor);

        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) container.getLayoutParams();
        layoutParams.topMargin = isLandscape ? 0 : getResources().getDimensionPixelSize(R.dimen.toolbar_height);
        container.setLayoutParams(layoutParams);

        if (isLandscape) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    @Override
    public void collapseToolbar(int animationDuration) {
        toolbarAnimator.collapseToolbar(toolbar, () -> toolbar.setVisibility(View.GONE));
    }

    @Override
    public void expandToolbar(int animationDuration) {
        toolbar.setVisibility(View.VISIBLE);
        toolbarAnimator.expandToolbar(toolbar);
    }

    @Override
    public void showToolbar(int animationDuration) {
        toolbar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideToolbar(int animationDuration) {
        toolbar.setVisibility(View.GONE);
    }
}
