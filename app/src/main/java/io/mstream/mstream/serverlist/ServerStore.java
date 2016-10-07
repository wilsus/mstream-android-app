package io.mstream.mstream.serverlist;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import io.mstream.mstream.LocalPreferences;

/**
 * A store to abstract writing and reading a ServerItem from LocalPreferences.
 */

public final class ServerStore {

    private ServerStore() {
    }

    public static String getDefaultServerUrl() {
        return LocalPreferences.getInstance().getDefaultServerUrl();
    }

    public static List<ServerItem> getServers() {
        Set<String> servers = LocalPreferences.getInstance().getServers();
        List<ServerItem> list = new ArrayList<>(servers.size());
        for (String serverJson : servers) {
            ServerItem item = ServerItem.fromJsonString(serverJson);
            if (item != null) {
                list.add(item);
            }
        }
        return list;
    }

    public static void addServer(ServerItem serverItem) {
        Set<String> servers = LocalPreferences.getInstance().getServers();

        if (servers.isEmpty()) {
            // This is the first server we're adding. Set it as the default.
            setDefaultServer(serverItem);
        }

        servers.add(serverItem.toJsonString());
        LocalPreferences.getInstance().setServers(servers);
    }

    public static void setDefaultServer(ServerItem item) {
        LocalPreferences.getInstance().setDefaultServerUrl(item.getServerUrl());
        // Now that we've set a new default server, emit an event to let interested app elements know.
        EventBus.getDefault().post(new NewDefaultServerEvent());
    }
}
