package io.mstream.mstream.serverlist;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

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
    public NavDrawerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.nav_drawer_server_item, parent, false);
        return new NavDrawerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(NavDrawerViewHolder holder, int position) {
        ServerItem item = getItem(position);
        holder.serverName.setText(item.getServerName());
    }

    class NavDrawerViewHolder extends RecyclerView.ViewHolder {
        private TextView serverName;

        NavDrawerViewHolder(View view) {
            super(view);
            serverName = (TextView) view;
        }
    }
}
