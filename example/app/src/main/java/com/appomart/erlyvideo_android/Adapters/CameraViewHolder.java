package com.appomart.erlyvideo_android.Adapters;

import android.content.Context;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.appomart.erlyvideo_android.R;

import flussonic.watcher.sdk.domain.pojo.Camera;
import flussonic.watcher.sdk.presentation.thumbnail.FlussonicThumbnailView;

public class CameraViewHolder extends RecyclerView.ViewHolder {

    private final FlussonicThumbnailView watcher_view;
    private final View.OnClickListener onClickListener;
    private final FlussonicThumbnailView thumbnailView;
    public TextView name_cam;
    public TextView cam_status;
    private Camera camera;

    public CameraViewHolder(View itemView, @NonNull OnCameraClickListener listener) {
        super(itemView);
        name_cam = itemView.findViewById(R.id.name_cam);
        cam_status = itemView.findViewById(R.id.cam_status);
        watcher_view = itemView.findViewById(R.id.watcher_view);
        thumbnailView = itemView.findViewById(R.id.camera_preview);
        onClickListener = v -> {
            if (camera != null) {
                listener.onCameraClick(camera);
            }
        };
    }

    void bind(@NonNull Camera camera, Context context) {
        this.camera = camera;
        if (camera.hasAnError()) {
            name_cam.setText(camera.name());
            cam_status.setTextColor(context.getResources().getColor(R.color.cam_offline));
            cam_status.setText(context.getResources().getText(R.string.cam_offline));
            thumbnailView.setVisibility(View.GONE);
            itemView.setOnClickListener(null);
        } else {
            name_cam.setText(camera.name());
            cam_status.setTextColor(context.getResources().getColor(R.color.cam_online));
            cam_status.setText(context.getResources().getText(R.string.cam_online));
            itemView.setOnClickListener(onClickListener);
            thumbnailView.setVisibility(View.VISIBLE);
            thumbnailView.show(camera, null);
            thumbnailView.setStatusListener(new FlussonicThumbnailView.StatusListener() {
                @Override
                public void onStatus(@NonNull FlussonicThumbnailView.Status status, String s, String s1) {
                }
            });
        }
    }

    public interface OnCameraClickListener {
        void onCameraClick(@NonNull Camera camera);
    }
}
