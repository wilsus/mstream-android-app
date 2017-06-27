package io.mstream.mstream.serverlist;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.mstream.mstream.LocalPreferences;

/**
 * A store to abstract writing and reading a ServerItem from LocalPreferences.
 */

public final class ServerStore {

    // List of all servers
    public static List<ServerItem> serverList = new ArrayList<>();

    // The currently selected server
    public static ServerItem currentServer;

    private ServerStore() {
    }

    // Loads all server from storage into the serverList
    public static void loadServers() {
        // Get servers from storage
        Set<String> servers = LocalPreferences.getInstance().getServers();
        serverList.clear();

        // Loop through and add to serverList
        for (String serverJson : servers) {
            // Servers are stored as JSON and need to be converted
            ServerItem item = ServerItem.fromJsonString(serverJson);
            if (item != null) {
                serverList.add(item);
            }
        }

        // Set the current server if it's not already set
        if(currentServer == null && !serverList.isEmpty()){
            // Find the default server or load this first server on the list
            for(ServerItem thisServer : serverList){
                if(thisServer.getServerDefaultStatus()){
                    currentServer = thisServer;
                }
            }

            // If for some reason no server is set to default
            if(currentServer == null){
                currentServer = serverList.get(0);
            }
        }
    }

    public static void addServer(ServerItem serverItem) {
        // Server list is empty
        if (serverList.isEmpty()) {
            // This is the first server we're adding. Set it as the default.
            serverItem.setServerDefaultStatus(true);
            currentServer = serverItem;
        }

        // User selected this as the default server
        if(!serverList.isEmpty() && serverItem.getServerDefaultStatus()){
            // Loop through and remove default status from all servers
            for(ServerItem thisServer : serverList){
                thisServer.setServerDefaultStatus(false);
            }
        }

        // Add to list
        serverList.add(serverItem);
//        if(serverItem.getServerDefaultStatus()){
//            // Add to top
//            serverList.add(serverItem);
//        }else{
//            // Add to bottom
//            serverList.add(0, serverItem);
//        }

        // Save the new list
        saveServers();
    }

    public static void saveServers(){
        Set<String> saveThis = new HashSet<>();

        for(ServerItem thisServer : serverList){
            // TODO: We have to check for empty urls because the ServerListAdapter adds empty server items
            if(!thisServer.getServerUrl().isEmpty()){
                saveThis.add(thisServer.toJsonString());
            }
        }
        LocalPreferences.getInstance().setServers(saveThis);
    }



    // TODO: Delete Server

    // TODO: Edit Server

    // TODO: Update current server vTokenm

    // TODO: Update current server jkwt
}
