package com.example.elite.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.elite.R;
import com.example.elite.models.MediaItem;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.MediaViewHolder> {

    private List<MediaItem> mediaList;
    private Context context;
    private OnMediaActionListener listener;

    public interface OnMediaActionListener {
        void onMediaClick(MediaItem mediaItem);
        void onMediaDelete(MediaItem mediaItem);
    }

    public MediaAdapter(List<MediaItem> mediaList, Context context, OnMediaActionListener listener) {
        this.mediaList = mediaList;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MediaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_media, parent, false);
        return new MediaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MediaViewHolder holder, int position) {
        MediaItem mediaItem = mediaList.get(position);
        
        holder.textFileName.setText(mediaItem.getFileName());
        holder.textUploadedBy.setText("By: " + (mediaItem.getUploadedBy() != null ? mediaItem.getUploadedBy() : "Unknown"));
        
        // Format upload time
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        holder.textUploadTime.setText(sdf.format(new Date(mediaItem.getUploadTime())));
        
        // Set media type icon and thumbnail
        if (mediaItem.isImage()) {
            holder.iconMediaType.setImageResource(R.drawable.ic_image);
            // Load image thumbnail using Glide
            Glide.with(context)
                    .load(mediaItem.getDownloadUrl())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.ic_image)
                    .error(R.drawable.ic_image)
                    .into(holder.imageThumbnail);
            holder.imageThumbnail.setVisibility(View.VISIBLE);
        } else if (mediaItem.isVideo()) {
            holder.iconMediaType.setImageResource(R.drawable.ic_video);
            // For videos, show video icon as thumbnail
            holder.imageThumbnail.setImageResource(R.drawable.ic_video);
            holder.imageThumbnail.setVisibility(View.VISIBLE);
        }
        
        // Click listeners
        holder.cardView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMediaClick(mediaItem);
            }
        });
        
        holder.buttonDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMediaDelete(mediaItem);
            }
        });
        
        holder.buttonOpen.setOnClickListener(v -> openMedia(mediaItem));
    }

    @Override
    public int getItemCount() {
        return mediaList.size();
    }

    private void openMedia(MediaItem mediaItem) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(mediaItem.getDownloadUrl()));
            context.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(context, "Cannot open media file", Toast.LENGTH_SHORT).show();
            Log.e("MediaAdapter", "Error opening media", e);
        }
    }

    public void updateMediaList(List<MediaItem> newMediaList) {
        this.mediaList = newMediaList;
        notifyDataSetChanged();
    }

    static class MediaViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        ImageView imageThumbnail, iconMediaType;
        TextView textFileName, textUploadedBy, textUploadTime;
        MaterialButton buttonOpen, buttonDelete;

        public MediaViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_media);
            imageThumbnail = itemView.findViewById(R.id.image_thumbnail);
            iconMediaType = itemView.findViewById(R.id.icon_media_type);
            textFileName = itemView.findViewById(R.id.text_file_name);
            textUploadedBy = itemView.findViewById(R.id.text_uploaded_by);
            textUploadTime = itemView.findViewById(R.id.text_upload_time);
            buttonOpen = itemView.findViewById(R.id.button_open);
            buttonDelete = itemView.findViewById(R.id.button_delete);
        }
    }
}