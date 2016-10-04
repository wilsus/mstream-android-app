package io.mstream.mstream.serverlist;

import io.mstream.mstream.LocalPreferences;

/**
 * A store to abstract writing and reading a ServerItem from LocalPreferences.
 */

public final class ServerStore {

    private ServerStore() {
    }

    // TODO: extend this for multiple stored servers, keyed from URL

    public static ServerItem getServer() {
        String name = LocalPreferences.getInstance().getServerNickname();
        String url = LocalPreferences.getInstance().getServerUrl();
        String username = LocalPreferences.getInstance().getUsername();
        String password = LocalPreferences.getInstance().getPassword();
        boolean isDefault = LocalPreferences.getInstance().getIsDefault();

        return new ServerItem.Builder(name, url)
                .username(username)
                .password(password)
                .isDefault(isDefault).build();
    }

    public static void setServer(ServerItem serverItem) {
        LocalPreferences.getInstance().setServerNickname(serverItem.getServerName());
        LocalPreferences.getInstance().setServerUrl(serverItem.getServerUrl());
        LocalPreferences.getInstance().setUsername(serverItem.getServerUsername());
        LocalPreferences.getInstance().setPassword(serverItem.getServerPassword());
//        LocalPreferences.getInstance().setIsDefault(serverItem.isDefault());
        // TODO: for now, since there's only one server, it's always the default
        LocalPreferences.getInstance().setIsDefault(true);
    }
}
