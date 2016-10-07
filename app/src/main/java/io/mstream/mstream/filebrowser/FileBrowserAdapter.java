package io.mstream.mstream.filebrowser;

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
 * An adapter for the file browser recyclerview.
 */
class FileBrowserAdapter extends ArrayAdapter<FileItem, FileBrowserAdapter.FileBrowserViewHolder> {
    private OnClickFileItem onClickHandler;

    FileBrowserAdapter(List<FileItem> items, OnClickFileItem handler) {
        super(items);
        onClickHandler = handler;
    }

    @Override
    public FileBrowserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.file_browse_item_layout, parent, false);
        return new FileBrowserAdapter.FileBrowserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FileBrowserAdapter.FileBrowserViewHolder holder, int position) {
        FileItem item = getItem(position);
        holder.filename.setText(item.getItemName());
        // Show a directory icon or a file icon as appropriate
        if (item.getItemType().equals(FileItem.DIRECTORY)) {
            holder.directoryIcon.setVisibility(View.VISIBLE);
            holder.fileIcon.setVisibility(View.GONE);
        } else {
            holder.fileIcon.setVisibility(View.VISIBLE);
            holder.directoryIcon.setVisibility(View.GONE);
        }
    }

    class FileBrowserViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView filename;
        private ImageView directoryIcon;
        private ImageView fileIcon;

        FileBrowserViewHolder(View view) {
            super(view);
            filename = (TextView) view.findViewById(R.id.filename);
            directoryIcon = (ImageView) view.findViewById(R.id.directory_icon);
            fileIcon = (ImageView) view.findViewById(R.id.file_icon);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            FileItem item = getItem(getAdapterPosition());
            if (item.getItemType().equals(FileItem.DIRECTORY)) {
                onClickHandler.onDirectoryClick(item.getItemUrl());
            } else {
                onClickHandler.onFileClick(item);
            }
        }
    }

    interface OnClickFileItem {
        void onDirectoryClick(String directory);

        void onFileClick(FileItem item);
    }
}
