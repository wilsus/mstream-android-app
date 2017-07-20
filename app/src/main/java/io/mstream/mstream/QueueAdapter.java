package io.mstream.mstream;

import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import io.mstream.mstream.playlist.MstreamQueueObject;
import io.mstream.mstream.playlist.QueueManager;
import io.mstream.mstream.serverlist.ServerStore;
import io.mstream.mstream.ui.ArrayAdapter;

/**
 * Created by paul on 7/10/2017.
 */

class QueueAdapter extends ArrayAdapter<MstreamQueueObject, QueueAdapter.QueueAdapterViewHolder> {
    private OnClickQueueItem onClickHandler;

    QueueAdapter(List<MstreamQueueObject> items, OnClickQueueItem handler) {
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
        MstreamQueueObject item = getItem(position);
//        holder.filename.setText(item.getMetadata().getFilename());
        String artist = item.getMetadata().getArtist();
        String album = item.getMetadata().getAlbum();
        String title = item.getMetadata().getTitle();

        String displayString;
        if(title == null || !title.isEmpty()){
            displayString = title;
            if(artist == null || !artist.isEmpty()){
                displayString = artist + " - " + title;
            }
        }else{
            displayString = item.getMetadata().getFilename();
        }

        holder.filename.setText(displayString);

        // Show a directory icon or a file icon as appropriate
        // TODO: Load in the image another way that we can support album art

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

            final ImageButton btn = (ImageButton)view.findViewById(R.id.browser_more_options);
            btn.setOnClickListener(new View.OnClickListener() {


                @Override
                public void onClick(final View arg0) {
                    //Creating the instance of PopupMenu
                    PopupMenu popup = new PopupMenu(arg0.getContext(), btn);
                    //Inflating the Popup using xml file
                    popup.getMenuInflater()
                            .inflate(R.menu.queue_item_poppup, popup.getMenu());

                    //registering popup with OnMenuItemClickListener
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            if(item.getTitleCondensed().equals("remove_queue_item")){
                                // TODO: Remove from queue
                            }
                            return true;
                        }
                    });

                    popup.show(); //showing popup menu
                }
            });
        }

        @Override
        public void onClick(View view) {
            int itemPos = getAdapterPosition();

            MediaSessionCompat.QueueItem item = getItem(itemPos).getQueueItem();
            onClickHandler.onQueueClick(item, itemPos);
        }
    }

    interface OnClickQueueItem {
        void onQueueClick(MediaSessionCompat.QueueItem item, int itemPos);
    }
}
