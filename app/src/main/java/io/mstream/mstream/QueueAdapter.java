package io.mstream.mstream;

import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import io.mstream.mstream.playlist.QueueManager;
import io.mstream.mstream.ui.ArrayAdapter;

/**
 * Created by paul on 7/10/2017.
 */

class QueueAdapter extends ArrayAdapter<MediaSessionCompat.QueueItem, QueueAdapter.QueueAdapterViewHolder> {
    private OnClickQueueItem onClickHandler;

    QueueAdapter(List<MediaSessionCompat.QueueItem> items, OnClickQueueItem handler) {
        super(items);
        onClickHandler = handler;
    }

    @Override
    public QueueAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.file_browse_item_layout, parent, false);
        return new QueueAdapter.QueueAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(QueueAdapter.QueueAdapterViewHolder holder, int position) {
        MediaSessionCompat.QueueItem item = getItem(position);
        holder.filename.setText(item.getDescription().getTitle());
        // Show a directory icon or a file icon as appropriate
        // TODO: Load in the image another way that we can support album art
//        if (item.getItemType().equals("directory")) {
//            holder.directoryIcon.setVisibility(View.VISIBLE);
//            holder.fileIcon.setVisibility(View.GONE);
//        } else {
//            holder.fileIcon.setVisibility(View.VISIBLE);
//            holder.directoryIcon.setVisibility(View.GONE);
//        }

        // TODO: Change this modify the background color \
        if(position == QueueManager.getIndex()){
            holder.directoryIcon.setVisibility(View.VISIBLE);
            holder.fileIcon.setVisibility(View.GONE);
        }else{
            holder.fileIcon.setVisibility(View.VISIBLE);
            holder.directoryIcon.setVisibility(View.GONE);
        }


    }

    class QueueAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView filename;
        private ImageView directoryIcon;
        private ImageView fileIcon;

        QueueAdapterViewHolder(View view) {
            super(view);
            filename = (TextView) view.findViewById(R.id.filename);
            directoryIcon = (ImageView) view.findViewById(R.id.directory_icon);
            fileIcon = (ImageView) view.findViewById(R.id.file_icon);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int itemPos = getAdapterPosition();

            MediaSessionCompat.QueueItem item = getItem(itemPos);
            onClickHandler.onQueueClick(item, itemPos);
        }
    }

    interface OnClickQueueItem {
        void onQueueClick(MediaSessionCompat.QueueItem item, int itemPos);
    }
}
