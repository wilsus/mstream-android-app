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
        // Add a dummy so we can offset the Add Server item
        items.add(new ServerItem.Builder("", "").build());
    }

    @Override
    public NavDrawerViewHolder onCreateViewHolder(ViewGroup parent, @LayoutRes int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
        return new NavDrawerViewHolder(view);
    }

    @Override
    public int getItemViewType(int position) {
        return position > 0 ? R.layout.nav_drawer_server_item : R.layout.nav_drawer_add_server;
    }

    @Override
    public void onBindViewHolder(NavDrawerViewHolder holder, int position) {
        if (position == 0) {
            // This is the Add Server special item. Bind the layout differently.
        } else {
            ServerItem item = getItem(position - 1);
            holder.serverName.setText(item.getServerName());
            if (item.getServerUrl().equals(LocalPreferences.getInstance().getDefaultServerUrl())) {
                holder.isDefaultIcon.setVisibility(View.VISIBLE);
                holder.notDefaultIcon.setVisibility(View.GONE);
            } else {
                holder.notDefaultIcon.setVisibility(View.VISIBLE);
                holder.isDefaultIcon.setVisibility(View.GONE);
            }
        }
    }

    private void handleClick(int position, View v) {
        if (position > 0) {
            // This is a "select server" click
            ServerStore.setDefaultServer(getItem(position - 1));
            // By using notifyItemRangeChanged instead of notifyDataSetChanged, we get animation!
            notifyItemRangeChanged(1, getItemCount() - 1);
        } else {
            // This is an "add server" click
            v.getContext().startActivity(new Intent(v.getContext(), AddServerActivity.class));
        }
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
