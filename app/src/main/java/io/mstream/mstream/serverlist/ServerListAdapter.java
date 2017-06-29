package io.mstream.mstream.serverlist;

import android.content.Intent;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import io.mstream.mstream.AddServerActivity;
import io.mstream.mstream.LocalPreferences;
import io.mstream.mstream.R;
import io.mstream.mstream.ui.ArrayAdapter;

/**
 * An adapter for the nav drawer recyclerview.
 */

public class ServerListAdapter extends ArrayAdapter<ServerItem, ServerListAdapter.NavDrawerViewHolder> {

    public ServerListAdapter(List<ServerItem> items) {
        super(items);
    }

    @Override
    public NavDrawerViewHolder onCreateViewHolder(ViewGroup parent, @LayoutRes int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
        return new NavDrawerViewHolder(view);
    }

    @Override
    public int getItemViewType(int position) {
        return R.layout.nav_drawer_server_item;
    }

    @Override
    public void onBindViewHolder(NavDrawerViewHolder holder, int position) {
        ServerItem item = getItem(position);
        holder.serverName.setText(item.getServerName());
        if (item.equals(ServerStore.currentServer)) {
            holder.isDefaultIcon.setVisibility(View.VISIBLE);
            holder.notDefaultIcon.setVisibility(View.GONE);
        } else {
            holder.notDefaultIcon.setVisibility(View.VISIBLE);
            holder.isDefaultIcon.setVisibility(View.GONE);
        }
    }

    private void handleClick(int position, View v) {
        // This is a "select server" click
        ServerStore.currentServer = getItem(position );
        // By using notifyItemRangeChanged instead of notifyDataSetChanged, we get animation!
        notifyItemRangeChanged(0, getItemCount() );
    }

    class NavDrawerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView serverName;
        private ImageView isDefaultIcon;
        private ImageView notDefaultIcon;

        NavDrawerViewHolder(View view) {
            super(view);
            serverName = (TextView) view.findViewById(R.id.item_text);
            isDefaultIcon = (ImageView) view.findViewById(R.id.icon_is_default);
            notDefaultIcon = (ImageView) view.findViewById(R.id.icon_not_default);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            handleClick(getAdapterPosition(), view);
        }

    }
}
