package io.mstream.mstream.playlist;

import android.support.v4.media.MediaBrowserCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import io.mstream.mstream.R;
import io.mstream.mstream.ui.ArrayAdapter;

/**
 * An adapter to render a queue of media items
 */

class PlaylistAdapter extends ArrayAdapter<MediaBrowserCompat.MediaItem, PlaylistAdapter.PlaylistItemViewHolder> {
    private OnClickMediaItem onClickHandler;
    private String currentlyPlayingItemTitle = "";

    PlaylistAdapter(List<MediaBrowserCompat.MediaItem> items, OnClickMediaItem handler) {
        super(items);
        onClickHandler = handler;
    }

    public void setCurrentlyPlayingItemTitle(String currentlyPlayingItemTitle) {
        this.currentlyPlayingItemTitle = currentlyPlayingItemTitle;
    }

    @Override
    public PlaylistItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.playlist_item_layout, parent, false);
        return new PlaylistItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PlaylistItemViewHolder holder, int position) {
        MediaBrowserCompat.MediaItem item = getItem(position);
        holder.filename.setText(item.getDescription().getTitle());
        // Show a now-playing icon or not, as appropriate
        if (currentlyPlayingItemTitle.equals(item.getDescription().getTitle())) {
            holder.nowPlayingIcon.setVisibility(View.VISIBLE);
        } else {
            holder.nowPlayingIcon.setVisibility(View.GONE);
        }
    }

    class PlaylistItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView filename;
        private ImageView nowPlayingIcon;

        PlaylistItemViewHolder(View view) {
            super(view);
            filename = (TextView) view.findViewById(R.id.filename);
            nowPlayingIcon = (ImageView) view.findViewById(R.id.now_playing_icon);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            MediaBrowserCompat.MediaItem item = getItem(getAdapterPosition());
            onClickHandler.onMediaItemClick(item);
        }
    }

    interface OnClickMediaItem {
        void onMediaItemClick(MediaBrowserCompat.MediaItem item);
    }
}
