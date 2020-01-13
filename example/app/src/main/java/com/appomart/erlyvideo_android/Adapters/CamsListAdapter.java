package com.appomart.erlyvideo_android.Adapters;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.appomart.erlyvideo_android.R;

import java.util.List;

import flussonic.watcher.sdk.domain.pojo.Camera;

public class CamsListAdapter extends RecyclerView.Adapter<CameraViewHolder> {

    private Context context;
    private List<Camera> camList;
    private Camera camera;

    private final CameraViewHolder.OnCameraClickListener onCameraClickListener;

    public CamsListAdapter(Context context, List<Camera> camList,CameraViewHolder.OnCameraClickListener listener ){
        this.context = context;
        this.camList = camList;
        this.onCameraClickListener = listener;
    }

    @Override
    public CameraViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cam, parent, false);

        return new CameraViewHolder(view, onCameraClickListener);
    }

    @Override
    public void onBindViewHolder(final CameraViewHolder holder, final int position) {
        holder.bind(camList.get(position), context);
    }

    @Override
    public int getItemCount() {
        return camList.size();
    }

}
