package io.mstream.mstream.serverlist;

import android.support.annotation.NonNull;

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

    @NonNull
    public static ServerItem getDefaultServer() {
        // TODO: improve perf here, cache the default?
        Set<String> servers = LocalPreferences.getInstance().getServers();
        String defaultServerUrl = LocalPreferences.getInstance().getDefaultServerUrl();
        for (String serverJson : servers) {
            ServerItem item = ServerItem.fromJsonString(serverJson);
            if (item != null && item.getServerUrl().equals(defaultServerUrl)) {
                return item;
            }
        }
        return new ServerItem.Builder("", "").build();
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
    }
}
