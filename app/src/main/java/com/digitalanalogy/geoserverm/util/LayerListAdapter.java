package com.digitalanalogy.geoserverm.util;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.digitalanalogy.geoserverm.R;

public class LayerListAdapter extends RecyclerView.Adapter<LayerListAdapter.ItemViewHolder> {
    private Context context;
    private OnLayerClickListener layerClickListener;

    public LayerListAdapter(@NonNull Context context, @NonNull OnLayerClickListener layerClickListener) {
        this.context = context;
        this.layerClickListener = layerClickListener;
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ItemViewHolder(LayoutInflater.from(context).inflate(R.layout.item_layer, parent, false));
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        final int pos = position;
        holder.nameText.setText(ResponseProcessor.getLayers().get(position));
        holder.nameText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layerClickListener.onClick(pos);
            }
        });
    }

    @Override
    public int getItemCount() {
        return ResponseProcessor.getLayers().size();
    }

    // Interface for handling layer clicks (i.e. to open the layer details)
    public interface OnLayerClickListener{
        void onClick(int layerPosition);
    }

    class ItemViewHolder extends RecyclerView.ViewHolder{
        private TextView nameText;

        ItemViewHolder(View itemView) {
            super(itemView);

            this.nameText = (TextView) itemView.findViewById(R.id.item_txt_layer_name);
        }
    }
}
