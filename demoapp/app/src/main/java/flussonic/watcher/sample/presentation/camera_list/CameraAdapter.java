package flussonic.watcher.sample.presentation.camera_list;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import flussonic.watcher.sample.R;
import flussonic.watcher.sdk.domain.pojo.Camera;
import flussonic.watcher.sdk.presentation.thumbnail.FlussonicThumbnailView;

class CameraAdapter extends AbstractCameraAdapter<CameraViewHolder> {
    CameraAdapter(@NonNull List<Camera> items,
                  @NonNull CameraViewHolder.OnCameraClickListener listener) {
        super(items, listener);
    }

    @NonNull
    @Override
    public CameraViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_camera, parent, false);
        return new CameraViewHolder(view, onCameraClickListener);
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull @NotNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        if (recyclerView != null) FlussonicThumbnailView.trimMemory(recyclerView.getContext());
    }
}
