package com.erlyvideo.sample;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.erlyvideo.sample.Adapters.CameraViewHolder;
import com.erlyvideo.sample.Adapters.CamsListAdapter;
import com.erlyvideo.sample.Api.ServerApi;
import com.erlyvideo.sample.Common.NavigationTab;
import com.erlyvideo.sample.Control.ServerConnect;
import com.erlyvideo.sample.Models.User;

import java.util.ArrayList;
import java.util.List;

import flussonic.watcher.sdk.data.network.mappers.CameraDtoToCameraMapper;
import flussonic.watcher.sdk.domain.pojo.Camera;
import flussonic.watcher.sdk.domain.utils.RxUtils;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class CamsActivity extends AppCompatActivity implements CameraViewHolder.OnCameraClickListener /*CamsListAdapter.OnCameraClickListener*/ {

    private NavigationTab navigationTab;
    private RecyclerView camsListRecycler;

    private List<Camera> camList;
    private long startPosition;
    private boolean startPlayingFromStartPosition;

    private ServerConnect serverConnect;
    private ServerApi serverApi;
    Intent intent;
    private String session;
    private String login;
    private String password;
    private User user;

    private final CameraDtoToCameraMapper cameraDtoToCameraMapper = new CameraDtoToCameraMapper();
    private final ServerApi sampleNetworkService = new ServerConnect()
            .provideSampleNetworkService("https://cloud.vsaas.io");
    private Disposable loadDisposable;
    private ArrayList<Camera> cameras;
    ContentLoadingProgressBar progressIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cams);

        navigationTab = new NavigationTab(findViewById(R.id.navigation));
        navigationTab.initList(this);

        camsListRecycler = findViewById(R.id.cams_list);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        camsListRecycler.setLayoutManager(layoutManager);

        progressIndicator = findViewById(R.id.login_progress_indicator);

        serverConnect = new ServerConnect();

        intent = getIntent();
        user = new User();

        login = intent.getStringExtra("Login");
        password = intent.getStringExtra("Password");
        user.setLogin(login);
        user.setPassword(password);

        if (login!=null && password!=null){
            getCams(user);
        }
        //getCams(session);

    }

    public void getCams(User user) {
        RxUtils.dispose(loadDisposable);
            loadDisposable = sampleNetworkService.login(user)
                    .observeOn(AndroidSchedulers.mainThread())
                    .map(response -> {
                        session = response.getSession();
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
                    .doOnSubscribe(disposable -> showProgress())
                    .doFinally(this::closeProgress)
                    .doOnSuccess(this::setCameras)
                    .subscribe(this::setItems, this::showError);
    }

    private void showError(Throwable throwable) {
        Log.d("Login", "ERRROR = " + throwable.getMessage());
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

    public void setItems(@NonNull List<Camera> items) {
    }

    private void setCameras(@NonNull List<Camera> cameras) {
        this.cameras = new ArrayList<>(cameras);
        CamsListAdapter camsListAdapter = new CamsListAdapter(this, cameras, this);
        camsListRecycler.setAdapter(camsListAdapter);
    }

    @Override
    public void onCameraClick(@NonNull Camera camera) {
        Intent intent = new Intent(this, CameraActivity.class);
        this.startActivity(intent);
        startActivity(CameraActivity.getStartIntent(this,
                camera, session, cameras,
                startPlayingFromStartPosition ? startPosition : 0, login, password));
    }

    @Override
    protected void onResume() {
        super.onResume();
        overridePendingTransition(0, 0);
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }

    protected void showProgress() {
        progressIndicator.setVisibility(View.VISIBLE);
    }

    protected void closeProgress() {
        progressIndicator.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
