package flussonic.watcher.sample.presentation.camera_list;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import flussonic.watcher.sample.BuildConfig;
import flussonic.watcher.sample.R;
import flussonic.watcher.sample.data.network.dto.LoginRequestDto;
import flussonic.watcher.sample.data.network.services.SampleNetworkProvider;
import flussonic.watcher.sample.data.network.services.SampleNetworkService;
import flussonic.watcher.sample.presentation.camera.CameraActivity;
import flussonic.watcher.sample.presentation.core.BaseActivity;
import flussonic.watcher.sample.presentation.core.Settings;
import flussonic.watcher.sdk.data.network.mappers.CameraDtoToCameraMapper;
import flussonic.watcher.sdk.domain.pojo.Camera;
import flussonic.watcher.sdk.domain.utils.CalendarUtils;
import flussonic.watcher.sdk.domain.utils.FlussonicUtils;
import flussonic.watcher.sdk.domain.utils.RxUtils;
import flussonic.watcher.sdk.presentation.utils.DialogUtils;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class CameraListActivity extends BaseActivity
        implements SwipeRefreshLayout.OnRefreshListener, AbstractCameraViewHolder.OnCameraClickListener,
        DialogUtils.DateTimePickerListener {
    private static final long UPDATE_PREVIEW_PERIOD_MS = 5000;

    private static final String SERVER = BuildConfig.SERVER;
    private static final String API_VERSION = "/vsaas/api/v2/";
    private static final String BASE_URL = SERVER + API_VERSION;

    private static final String LOGIN = BuildConfig.LOGIN;
    private static final String PASSWORD = BuildConfig.PASSWORD;

    private static final String KEY_CAMERAS = "KEY_CAMERAS";
    private static final String KEY_SESSION = "KEY_SESSION";
    private static final String KEY_START_POSITION = "KEY_START_POSITION";
    private static final String KEY_START_PLAYING_FROM_START_POSITION = "KEY_START_PLAYING_FROM_START_POSITION";
    private static final String KEY_ITEM_TYPE = "KEY_ITEM_TYPE";
    private static final String KEY_PREVIEW_UPDATING = "KEY_P_UPDATING";

    private static final String DATE_TIME_PICKER_SUFFIX = CameraListActivity.class.getName();

    private final CameraDtoToCameraMapper cameraDtoToCameraMapper = new CameraDtoToCameraMapper();
    private final SampleNetworkService sampleNetworkService = new SampleNetworkProvider()
            .provideSampleNetworkService(BASE_URL);

    private AbstractCameraAdapter adapter;
    private ProgressBar progressBar;
    private SwipeRefreshLayout refreshLayout;

    @Nullable
    private Disposable loadDisposable;
    private ArrayList<Camera> cameras;
    private String session;
    private long startPosition;
    private boolean startPlayingFromStartPosition;
    private String itemType = "cameras";
    private boolean enableUpdating;
    private Bundle savedInstanceState;
    private LinearLayoutManager layoutManager;
    private RecyclerView recyclerView;

    private final CompositeDisposable compositeDisposableUpdatePreview = new CompositeDisposable();

    private void setCameras(@NonNull List<Camera> cameras) {
        this.cameras = new ArrayList<>(cameras);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.savedInstanceState = savedInstanceState;
        layoutManager = new LinearLayoutManager(this);
        onSavedInstanceStateLoaded(savedInstanceState);
        init();
    }



    private void init() {
        setContentView(R.layout.activity_camera_list);
        progressBar = findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.GONE);
        refreshLayout = findViewById(R.id.refresh_layout);
        refreshLayout.setOnRefreshListener(this);
        refreshLayout.setRefreshing(false);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(layoutManager);
        switch(itemType) {
            case "cameras":
                adapter = new CameraAdapter(Collections.emptyList(), this);
                break;
            case "mosaics":
                adapter = new CameraMosaicAdapter(Collections.emptyList(), session, this, this);
                break;
            case "streams":
                adapter = new CameraMosaicAdapter(Collections.emptyList(), this, this);
                break;
        }
        recyclerView.setAdapter(adapter);
        setupToolbar();
        DialogUtils.hideDateTimePicker(getSupportFragmentManager(), DATE_TIME_PICKER_SUFFIX);
    }

    private void onSavedInstanceStateLoaded(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            cameras = savedInstanceState.getParcelableArrayList(KEY_CAMERAS);
            session = savedInstanceState.getString(KEY_SESSION);
            startPosition = savedInstanceState.getLong(KEY_START_POSITION);
            startPlayingFromStartPosition = savedInstanceState.getBoolean(KEY_START_PLAYING_FROM_START_POSITION);
            itemType = savedInstanceState.getString(KEY_ITEM_TYPE, "cameras");
        }
    }

    private void toggleCameraTypes(String cameraTypes) {
        if (recyclerView != null) {
          // workaround for clearing Adapter memory. see https://stackoverflow.com/questions/35520946/leak-canary-recyclerview-leaking-madapter
            recyclerView.setAdapter(null);
        }
        layoutManager = new LinearLayoutManager(this);
        switch(cameraTypes) {
            case "cameras":
                itemType = cameraTypes;
                init();
                adapter.setItems(cameras);
                break;
            case "mosaics":
                itemType = cameraTypes;
                init();
                adapter.setItems(cameras.subList(0,5));
                break;
            case "streams":
                itemType = cameraTypes;
                init();
                adapter.setItems(cameras.subList(0,3));
                break;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(KEY_CAMERAS, cameras);
        outState.putSerializable(KEY_SESSION, session);
        outState.putLong(KEY_START_POSITION, startPosition);
        outState.putBoolean(KEY_START_PLAYING_FROM_START_POSITION, startPlayingFromStartPosition);
        outState.putSerializable(KEY_ITEM_TYPE, itemType);
        outState.putBoolean(KEY_PREVIEW_UPDATING, enableUpdating);
        this.savedInstanceState = outState;
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.camera_list, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item;
        item = menu.findItem(R.id.start_playing_from_start_position);
        item.setChecked(startPlayingFromStartPosition);
        item = menu.findItem(R.id.start_position);
        item.setTitle(startPositionString());
        item.setEnabled(startPlayingFromStartPosition);
        item = menu.findItem(R.id.allow_download);
        boolean allowDownload = Settings.allowDownload(this);
        item.setChecked(allowDownload);
        item = menu.findItem(R.id.use_cameras);
        item.setChecked(itemType.contains("cameras"));
        item = menu.findItem(R.id.use_mosaiks);
        item.setChecked(itemType.contains("mosaics"));
        item = menu.findItem(R.id.use_streams);
        item.setChecked(itemType.contains("streams"));
        item = menu.findItem(R.id.preview_updating);
        item.setChecked(enableUpdating);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.start_position:
                DialogUtils.showDateTimePicker(
                        getSupportFragmentManager(),
                        DATE_TIME_PICKER_SUFFIX,
                        0, 0,
                        startPosition > 0 ? startPosition : FlussonicUtils.utcTimeSeconds(),
                        this);
                return true;
            case R.id.start_playing_from_start_position:
                startPlayingFromStartPosition = !startPlayingFromStartPosition;
                return true;
            case R.id.use_cameras:
                itemType = "cameras";
                toggleCameraTypes(itemType);
                return true;
            case R.id.use_mosaiks:
                itemType = "mosaics";
                toggleCameraTypes(itemType);
                return true;
            case R.id.use_streams:
                itemType = "streams";
                toggleCameraTypes(itemType);
                return true;
            case R.id.preview_updating:
                enableUpdating = !enableUpdating;
                togglePreviewUpdating(enableUpdating);
                return true;
            case R.id.allow_download:
                boolean allowDownload = Settings.allowDownload(this);
                allowDownload = !allowDownload;
                Settings.setAllowDownload(this, allowDownload);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void togglePreviewUpdating(boolean enableUpdating) {
        if (enableUpdating) {
            startUpdatePreviewTimers();
        } else {
            stopUpdatePreviewTimers();
        }
    }

    @Override
    public void onDateTimeSelected(long dateTimeInSecs) {
        startPosition = dateTimeInSecs;
        showToast(startPositionString());
    }

    private String startPositionString() {
        return startPosition > 0
                ? getString(R.string.start_position, CalendarUtils.toString(startPosition, CalendarUtils.DATE_TIME_PATTERN))
                : getString(R.string.select_start_position);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (cameras == null) {
            loadCameras(
                    () -> progressBar.setVisibility(View.VISIBLE),
                    () -> {
                        progressBar.setVisibility(View.GONE);

                    });
        } else {
            toggleCameraTypes(itemType);
        }
        togglePreviewUpdating(enableUpdating);
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopUpdatePreviewTimers();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RxUtils.dispose(loadDisposable);
        compositeDisposableUpdatePreview.dispose();
    }

    @Override
    public void onRefresh() {
        loadCameras(
                () -> refreshLayout.setRefreshing(true),
                () -> refreshLayout.setRefreshing(false));
    }

    @Override
    public void onCameraClick(@NonNull Camera camera) {
        Timber.d("onCameraClick called with useMasaics %s", itemType);
            startActivity(CameraActivity.getStartIntent(this,
                    camera, session, cameras,
                    startPlayingFromStartPosition ? startPosition : 0));
    }

    private void startUpdatePreviewTimers() {
        // Updating progress every second
        compositeDisposableUpdatePreview.add(
                Observable.interval(UPDATE_PREVIEW_PERIOD_MS, TimeUnit.MILLISECONDS, Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::updatePreview, this::onPreviewError));
    }

    private void updatePreview(Long aLong) {
        Date currentTime = Calendar.getInstance().getTime();
        adapter.updateCacheKey(currentTime.toString());
    }

    private void onPreviewError(Throwable throwable) {
        Timber.e(throwable.toString());
    }

    private void stopUpdatePreviewTimers() {
        compositeDisposableUpdatePreview.clear();
    }

    private void loadCameras(@NonNull ProgressShowListener progressShowListener,
                             @NonNull ProgressHideListener progressHideListener) {
        RxUtils.dispose(loadDisposable);
        loadDisposable = sampleNetworkService.login(LoginRequestDto.create(LOGIN, PASSWORD))
                .observeOn(AndroidSchedulers.mainThread())
                .map(response -> {
                    session = response.session();
                    if (session == null) {
                        throw new NullPointerException("Session is null");
                    }
                    return session;
                })
                .observeOn(Schedulers.io())
                .flatMap(sampleNetworkService::cameras)
                .map(cameraDtoToCameraMapper::map)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(disposable -> progressShowListener.showProgress())
                .doFinally(progressHideListener::hideProgress)
                .doOnSuccess(this::setCameras)
                .subscribe(cameras -> {
                  this.setCameras(cameras);
                  toggleCameraTypes(this.itemType);
                }, this::showError);
    }

    private void showError(Throwable throwable) {
        Timber.e(throwable, "showError");
        new AlertDialog.Builder(this)
                .setTitle(throwable.getClass()
                        .getSimpleName()
                        .replaceAll("(\\p{Ll})(\\p{Lu})", "$1 $2"))
                .setMessage(throwable.getMessage())
                .setPositiveButton(android.R.string.ok, null)
                .create()
                .show();
    }

    private interface ProgressShowListener {

        void showProgress();
    }

    private interface ProgressHideListener {

        void hideProgress();
    }
}
