package flussonic.watcher.sample.presentation.camera_list;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import flussonic.watcher.sample.R;
import flussonic.watcher.sample.presentation.core.BaseActivity;
import flussonic.watcher.sdk.domain.pojo.Camera;
import timber.log.Timber;

abstract class AbstractCameraAdapter<T extends AbstractCameraViewHolder> extends RecyclerView.Adapter<T> {

    @NonNull
    protected final List<Camera> items;

    @NonNull
    private final List<AbstractCameraViewHolder> holders;

    @NonNull
    protected final T.OnCameraClickListener onCameraClickListener;

    AbstractCameraAdapter(@NonNull List<Camera> items,
                          @NonNull T.OnCameraClickListener listener) {
        this.items = new ArrayList<>(items);
        this.holders = new ArrayList<>();
        this.onCameraClickListener = listener;
    }

    @NonNull
    @Override
    public abstract T onCreateViewHolder(@NonNull ViewGroup parent, int viewType);

    @Override
    public void onBindViewHolder(@NonNull T holder, int position) {
        holder.bind(items.get(position));
        holders.add(holder);
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

    @Override
    public void onViewAttachedToWindow(@NonNull T holder) {
        super.onViewAttachedToWindow(holder);
        holder.onViewAttached();
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull T holder) {
        super.onViewDetachedFromWindow(holder);
        holder.onViewDetached();
    }

    public void updateCacheKey(String cacheKey) {
        for (AbstractCameraViewHolder holder: holders
             ) {
            holder.setCacheKey(cacheKey);
        }
    }
}
