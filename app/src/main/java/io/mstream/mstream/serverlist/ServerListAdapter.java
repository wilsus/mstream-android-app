package io.mstream.mstream.serverlist;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import io.mstream.mstream.AddServerActivity;
import io.mstream.mstream.BaseActivity;
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

            final ImageButton btn = (ImageButton)view.findViewById(R.id.server_more_options);

            btn.setOnClickListener(new View.OnClickListener() {


                @Override
                public void onClick(final View arg0) {
                    //Creating the instance of PopupMenu
                    PopupMenu popup = new PopupMenu(arg0.getContext(), btn);
                    //Inflating the Popup using xml file
                    popup.getMenuInflater()
                            .inflate(R.menu.server_popup_menu, popup.getMenu());

                    //registering popup with OnMenuItemClickListener
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            if(item.getTitleCondensed().equals("make_default")){ServerStore.makeDefault(getItem(getAdapterPosition()));

                            }
                            if(item.getTitleCondensed().equals("delete_server")){
                                ServerStore.removeServer(getItem(getAdapterPosition()));
                                notifyDataSetChanged();
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
            handleClick(getAdapterPosition(), view);
        }

    }
}
