package flussonic.watcher.sample.presentation.camera_list;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import flussonic.watcher.sample.R;
import flussonic.watcher.sample.presentation.core.BaseActivity;
import flussonic.watcher.sdk.domain.pojo.Camera;
import timber.log.Timber;

abstract class AbstractCameraViewHolder extends RecyclerView.ViewHolder {

    @NonNull
    protected  TextView textViewTitle;
    @NonNull
    protected  TextView textViewStatus;

    @NonNull
    protected  View errorView;
    @NonNull
    protected  View.OnClickListener onClickListener;

    @Nullable
    protected Camera camera;

    AbstractCameraViewHolder(View itemView, @NonNull OnCameraClickListener listener) {
        super(itemView);
        textViewTitle = itemView.findViewById(R.id.camera_title);
        textViewStatus = itemView.findViewById(R.id.camera_status);
        initView(itemView, listener);
        errorView = itemView.findViewById(R.id.camera_preview_error);
    }

    abstract void bind(@NonNull Camera camera);
    abstract void initView(View itemView, OnCameraClickListener listener);

    abstract void onViewAttached();

    abstract void onViewDetached();

    abstract void setCacheKey(String cacheKey);

    public interface OnCameraClickListener {
        void onCameraClick(@NonNull Camera camera);
    }


}
