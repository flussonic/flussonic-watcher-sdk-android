package flussonic.watcher.sample.presentation.camera_list;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;

import flussonic.watcher.sample.R;
import flussonic.watcher.sdk.domain.pojo.Camera;
import flussonic.watcher.sdk.presentation.thumbnail.FlussonicThumbnailView;
import timber.log.Timber;

class CameraViewHolder extends AbstractCameraViewHolder {
    @Nullable
    private Camera camera;

    private FlussonicThumbnailView thumbnailView;

    CameraViewHolder(View itemView, @NonNull OnCameraClickListener listener) {
        super(itemView, listener);
    }

    void initView(View itemView, OnCameraClickListener listener) {
        thumbnailView = this.itemView.findViewById(R.id.camera_preview);
        thumbnailView.setStatusListener((status, flussonicThumbnailError) -> {
            if (flussonicThumbnailError != null) {
                Timber.e("onStatus %s, %s, %s", status, flussonicThumbnailError.code(), flussonicThumbnailError.message());
            }
        });
        onClickListener = v -> {
            if (camera != null) {
                listener.onCameraClick(camera);
            } else {
                Timber.e("Camera is null");
            }
        };

    }

    @Override
    void onViewAttached() {

    }

    @Override
    void onViewDetached() {

    }

    @Override
    void setCacheKey(String cacheKey) {
        thumbnailView.setCacheKey(cacheKey);
    }

    void bind(@NonNull Camera camera) {
        this.camera = camera;
        textViewTitle.setText(camera.title());
        if (camera.hasAnError()) {
            textViewStatus.setCompoundDrawablesWithIntrinsicBounds(R.drawable.camera_status_error, 0, 0, 0);
            textViewStatus.setText(R.string.inactive);
            itemView.setOnClickListener(onClickListener);
            thumbnailView.setVisibility(View.GONE);
            errorView.setVisibility(View.VISIBLE);
        } else {
            textViewStatus.setCompoundDrawablesWithIntrinsicBounds(R.drawable.camera_status_ok, 0, 0, 0);
            textViewStatus.setText(R.string.active);
            itemView.setOnClickListener(onClickListener);
            thumbnailView.setVisibility(View.VISIBLE);
            thumbnailView.show(camera, null);
            errorView.setVisibility(View.GONE);
        }
    }
}
