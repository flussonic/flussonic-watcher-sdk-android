package flussonic.watcher.sample.presentation.camera_list;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import flussonic.watcher.sample.R;
import flussonic.watcher.sample.presentation.core.BaseActivity;
import flussonic.watcher.sdk.domain.pojo.Camera;

class CameraMosaicAdapter extends AbstractCameraAdapter<CameraMosaicViewHolder> {
    private String session;
    private BaseActivity activity;

    CameraMosaicAdapter(@NonNull List<Camera> items,
                        @NonNull String session,
                        @NonNull AbstractCameraViewHolder.OnCameraClickListener listener, BaseActivity activity) {
        super(items, listener);
        this.session = session;
        this.activity = activity;
    }

    CameraMosaicAdapter(@NonNull List<Camera> items,
                        @NonNull AbstractCameraViewHolder.OnCameraClickListener listener, BaseActivity activity) {
        super(items, listener);
        this.activity = activity;
    }

    @NonNull
    @Override
    public CameraMosaicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context ctx = parent.getContext();
        View view = LayoutInflater.from(ctx)
                .inflate(R.layout.item_camera_mosaic, parent, false);
        if(session != null) {
            return new CameraMosaicViewHolder(view, session, onCameraClickListener, activity);
        } else {
            return new CameraMosaicViewHolder(view, onCameraClickListener, activity);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull CameraMosaicViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setItems(@NonNull List<Camera> items) {
        this.items.clear();
        this.items.addAll(items);
        notifyDataSetChanged();
    }
}
