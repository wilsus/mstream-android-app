package io.mstream.mstream.syncer;

import io.mstream.mstream.LocalPreferences;

/**
 * Created by paul on 7/23/2017.
 */

public final class SyncSettingsStore {
    public static String storagePath;

    private SyncSettingsStore(){
    }

    public static void loadSyncSettings(){
        // TODO: Check if the path is real (may have been removed if user removed SD card)

        storagePath = LocalPreferences.getInstance().getSyncPath();
    }

    public static void setSyncPath(String path){
        // TODO: Check if path is real

        storagePath = path;
        LocalPreferences.getInstance().setSyncPath(path);
    }

    public static boolean checkIfPathExists(String path){
        // TODO
        return true;
    }

}
