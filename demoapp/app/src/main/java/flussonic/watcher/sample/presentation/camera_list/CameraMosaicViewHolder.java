package flussonic.watcher.sample.presentation.camera_list;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.PopupMenu;

import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Locale;

import flussonic.watcher.sample.BuildConfig;
import flussonic.watcher.sample.R;
import flussonic.watcher.sample.presentation.core.BaseActivity;
import flussonic.watcher.sdk.domain.pojo.Camera;
import flussonic.watcher.sdk.domain.pojo.Quality;
import flussonic.watcher.sdk.domain.pojo.Track;
import flussonic.watcher.sdk.domain.pojo.UpdateProgressEvent;
import flussonic.watcher.sdk.presentation.core.FlussonicControlsVisibilityListener;
import flussonic.watcher.sdk.presentation.core.listeners.FlussonicBufferingListener;
import flussonic.watcher.sdk.presentation.core.listeners.FlussonicUpdateProgressEventListener;
import flussonic.watcher.sdk.presentation.watcher.FlussonicMosaicView;
import timber.log.Timber;

class CameraMosaicViewHolder extends AbstractCameraViewHolder implements FlussonicBufferingListener, FlussonicUpdateProgressEventListener, FlussonicMosaicView.FlussonicExoPlayerErrorListener, FlussonicControlsVisibilityListener {

    @NonNull
    private FlussonicMosaicView flussonicMosaicView;

    private String session;

    @Nullable
    private Camera camera;
    private BaseActivity activity;
    private AppCompatImageButton muteButton;
    private AppCompatImageButton mediaButton;
    private boolean mute = false;
    private Quality quality = null;

    private PopupMenu playQualityMenu;

    CameraMosaicViewHolder(View itemView, String session, @NonNull OnCameraClickListener listener, BaseActivity activity) {
        super(itemView, listener);
        this.session = session;
        this.activity = activity;
    }

    void bind(@NonNull Camera camera) {
        this.camera = camera;
        textViewTitle.setText(camera.title());
        if (camera.hasAnError()) {
            handleError();
        } else {
            textViewStatus.setCompoundDrawablesWithIntrinsicBounds(R.drawable.camera_status_ok, 0, 0, 0);
            textViewStatus.setText(R.string.active);
            itemView.setOnClickListener(onClickListener);
            initMosaicView();
            errorView.setVisibility(View.GONE);
        }
    }

    @Override
    void initView(View itemView, OnCameraClickListener listener) {
        flussonicMosaicView = this.itemView.findViewById(R.id.camera_mosaic);
        mediaButton = this.itemView.findViewById(R.id.mosaic_quality_btn);
        muteButton = this.itemView.findViewById(R.id.mosaic_mute_btn);
        onClickListener = v -> {
            if (camera != null) {
                listener.onCameraClick(camera);
            } else {
                Timber.e("Camera is null");
            }
        };
        muteButton.setOnClickListener(view -> {
            if (muteButton != null) {
                setMute(!mute);
            }
        });
        mediaButton.setOnClickListener(view -> {
            if (quality != null) {
                showQualityMenu(quality);
            }
        });
        createQualityMenu();
    }

    private void showQualityMenu(@NonNull Quality quality) {
        Menu menu = playQualityMenu.getMenu();
        menu.getItem(quality.ordinal()).setChecked(true);
        playQualityMenu.show();
    }

    private void handleError() {
        textViewStatus.setCompoundDrawablesWithIntrinsicBounds(R.drawable.camera_status_error, 0, 0, 0);
        textViewStatus.setText(R.string.inactive);
        itemView.setOnClickListener(null);
        flussonicMosaicView.setVisibility(View.GONE);
        errorView.setVisibility(View.VISIBLE);
    }

    private void initMosaicView() {
        flussonicMosaicView.setVisibility(View.VISIBLE);

        // Чтобы плеер не пересоздавался (и в частности чтобы пауза плеера сохранялась)
            // при повороте экрана, добавьте в манифест активити, использующей
            // FlussonicMosaicView, атрибут android:configChanges="orientation|screenSize"

            // Инициализация параметров подключения к камере
            flussonicMosaicView.initialize(activity);
            setMute(true);
            setUrl();

            setQuality(Quality.SD);
            setMuteVisibility(true);
            flussonicMosaicView.setMaxFrameRate(2);
            flussonicMosaicView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
            // Установка слушателя на события буферизации
            flussonicMosaicView.setBufferingListener(this);
            // Установка слушателя, который вызывается раз в секунду, в параметре передается текущее
            // время проигрывания, UTC, в секундах
            //noinspection Convert2Lambda
            flussonicMosaicView.setUpdateProgressEventListener(this);
            flussonicMosaicView.setExoPlayerErrorListener(this);
            flussonicMosaicView.setControlsVisibilityListener(this);
        }

    @Override
    public void onExoPlayerError(String code, String message, String url) {
        Timber.e("onExoPlayerError code: %s, message: %s, player_url: %s", code, message, url);
        handleError();
    }

    @Override
    public void onUpdateProgress(@NonNull UpdateProgressEvent event) {
        // event.currentUtcInSeconds() is the same as flussonicMosaicView.getCurrentUtcInSeconds()
        // event.playbackStatus() is the same as flussonicMosaicView.getPlaybackStatus()
        // event.speed() is the same as flussonicMosaicView.getSpeed()
        List<Track> tracks = flussonicMosaicView.getAvailableTracks();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tracks.size(); i++) {
            sb.append(tracks.get(i).trackId());
            if (i < tracks.size() - 1) {
                sb.append(", ");
            }
        }
        String tracksString = sb.toString();
        Track currentTrack = flussonicMosaicView.getCurrentTrack();
        String currId = currentTrack == null ? null : currentTrack.trackId();
        Timber.d("Tracks: %s, current: %s",tracksString, currId);
        updateActionButtons(tracks);

//                    textViewUtc.setText(String.valueOf(event.currentUtcInSeconds()));
//                    textViewStatus.setText(String.valueOf(event.playbackStatus()));
//                    textViewSpeed.setText(String.format(Locale.US, "%.1f", event.speed()));
//                    textViewTracks.setText(tracksString.isEmpty() ? "NO" : tracksString);
//                    textViewCurrentTrack.setText(currentTrack == null ? "NO" : currentTrack.trackId());
    }

    @Override
    public void onBufferingStart() {
        Timber.d("onBufferingStart");
//                    textViewInfoLeft.setText(R.string.buffering);
    }

    @Override
    public void onBufferingStop() {
        Timber.d("onBufferingStop");
//                    textViewInfoLeft.setText("");
    }

    @Override
    void setCacheKey(String cacheKey) { // noop
    }

    private void updateActionButtons(List<Track> tracks) {
        int count = getVideoTracksCount(tracks);
        if (count == 0) {
            return;
        }
        if (quality != null) {
            boolean isShow = count > 1;
        }
        boolean hasAudioTrack = hasAudioTrack(tracks);
        setMuteVisibility(hasAudioTrack);
    }

    private boolean hasAudioTrack(List<Track> tracks) {
        for (int i = 0; i < tracks.size(); i++) {
            boolean hasAudio = tracks.get(i).isAudioTrack();
            if (hasAudio) {
                return true;
            }
        }
        return false;
    }

    private int getVideoTracksCount(List<Track> tracks) {
        int count = 0;
        for (int i = 0; i < tracks.size(); i++) {
            int trackWidth = tracks.get(i).width();
            if (trackWidth > 0) {
                count++;
            }
        }
        return count;
    }

    private void setUrl() {
        try {
            URL serverUrl = new URL(BuildConfig.SERVER);
            String urlString = String.format(Locale.US, "%s://%s@%s/%s%s",
                    serverUrl.getProtocol(),
                    session,
                    serverUrl.getAuthority(),
                    camera.name(),
                    /*setStartPositionFromUrl ? "?from=" + startPosition : */"");
            flussonicMosaicView.setUrl(urlString);
        } catch (MalformedURLException e) {
            throw new RuntimeException("failed to set url", e);
        }
    }

    private void createQualityMenu() {
        playQualityMenu = new PopupMenu(mediaButton.getContext(), mediaButton);
        Menu menu = playQualityMenu.getMenu();
        int groupId = 1;

        for (Quality q : Quality.values()) {
            String title = mediaButton.getResources().getString(getQualityString(q));
            menu.add(groupId, q.ordinal(), q.ordinal(), title);
        }
        menu.setGroupCheckable(groupId, true, true);
        playQualityMenu.setOnMenuItemClickListener(this::onQualityChosen);
    }

    private boolean onQualityChosen(MenuItem menuItem) {
        Quality checkedQuality = Quality.values()[menuItem.getItemId()];
        if (checkedQuality != null) {
            setQuality(checkedQuality);
            return true;
        }
        return false;
    }

    public void setMute(boolean mute) {
        this.mute = mute;
        muteButton.setImageResource(mute ? R.drawable.fs_ic_mute : R.drawable.fs_ic_unmute);
        flussonicMosaicView.disableAudio(mute);
    }

    public void setMuteVisibility(boolean visible) {
        muteButton.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override
    void onViewAttached() {
        if (camera != null) {
            bind(camera);
        }
    }

    @Override
    void onViewDetached() {
        flussonicMosaicView.release();
    }

    private void setQuality(@Nullable Quality quality) {
        this.quality = quality;
        flussonicMosaicView.setQuality(quality);
        mediaButton.setVisibility(quality == null ? View.GONE : View.VISIBLE);
        mediaButton.setImageResource(getQualityIcon(quality));
    }

    private static int getQualityString(@NonNull Quality quality) {
        switch (quality) {
            case AUTO:
                return R.string.fs_quality_auto;
            case HD:
                return R.string.fs_quality_hd;
            case SD:
                return R.string.fs_quality_sd;
            default:
                return 0;
        }
    }

    @DrawableRes
    private static int getQualityIcon(@Nullable Quality quality) {
        if (quality == null) {
            return 0;
        }
        switch (quality) {

            case HD:
                return R.drawable.fs_hd;

            case SD:
                return R.drawable.fs_sd;

            case AUTO:
                return R.drawable.fs_auto;

            default:
                return 0;
        }
    }

    @Override
    public void onVisibilityChanged(boolean pauseButtonVisible) {
        Timber.d("Pause button visible: %b", pauseButtonVisible);
    }
}
