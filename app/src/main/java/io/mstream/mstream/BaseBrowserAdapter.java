package io.mstream.mstream;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import io.mstream.mstream.BaseBrowserItem;
import io.mstream.mstream.R;
import io.mstream.mstream.ui.ArrayAdapter;

/**
 * An adapter for the file browser recyclerview.
 */
class BaseBrowserAdapter extends ArrayAdapter<BaseBrowserItem, BaseBrowserAdapter.BaseBrowserViewHolder> {
    private OnClickFileItem onClickHandler;

    BaseBrowserAdapter(List<BaseBrowserItem> items, OnClickFileItem handler) {
        super(items);
        onClickHandler = handler;
    }

    @Override
    public BaseBrowserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.file_browse_item_layout, parent, false);
        return new BaseBrowserAdapter.BaseBrowserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(BaseBrowserAdapter.BaseBrowserViewHolder holder, int position) {
        BaseBrowserItem item = getItem(position);
        holder.filename.setText(item.getItemText1());
//        // Show a directory icon or a file icon as appropriate
//        if (item.getItemType().equals(FileItem.DIRECTORY)) {
//            holder.directoryIcon.setVisibility(View.VISIBLE);
//            holder.fileIcon.setVisibility(View.GONE);
//        } else {
//            holder.fileIcon.setVisibility(View.VISIBLE);
//            holder.directoryIcon.setVisibility(View.GONE);
//        }
        holder.fileIcon.setVisibility(View.VISIBLE);
        holder.directoryIcon.setVisibility(View.GONE);
    }

    class BaseBrowserViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView filename;
        private ImageView directoryIcon;
        private ImageView fileIcon;

        BaseBrowserViewHolder(View view) {
            super(view);
            filename = (TextView) view.findViewById(R.id.filename);
            directoryIcon = (ImageView) view.findViewById(R.id.directory_icon);
            fileIcon = (ImageView) view.findViewById(R.id.file_icon);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            BaseBrowserItem item = getItem(getAdapterPosition());
            if (item.getItemType().equals("directory")) {
                onClickHandler.onDirectoryClick(item);
            }
// else {
//                onClickHandler.onFileClick(item);
//            }
        }
    }

    interface OnClickFileItem {
        void onDirectoryClick(BaseBrowserItem item);

        void onFileClick(BaseBrowserItem item);
    }
}
