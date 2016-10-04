package io.mstream.mstream;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

/**
 * A wrapper for SharedPrefs that makes it easy to get/set items.
 */

public final class LocalPreferences {

    // Name of file where preferences are stored
    private static final String FILE_NAME = LocalPreferences.class.getPackage().getName();

    // Preference names
    private static final String DEFAULT_SERVER_URL = FILE_NAME + ".a";
    private static final String SERVER_JSON_STRINGS = FILE_NAME + ".b";

    // Singleton instance of this class
    private static LocalPreferences instance;

    // Shared preferences instance
    private SharedPreferences prefs;

    /**
     * Private constructor that gets an instance of SharedPreferences
     * @param context A Context used to get SharedPreferences.
     */
    private LocalPreferences(Context context) {
        prefs = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Initializes the singleton for LocalPreferences
     * @param context A Context that can be used to initialize this class.
     */
    public static void init(Context context) {
        instance = new LocalPreferences(context);
    }

    /**
     * Returns an instance of LocalPreferences
     * @return The singleton instance of LocalPreferences
     */
    public static LocalPreferences getInstance() {
        return instance;
    }

    public void setServers(Set<String> json) {
        prefs.edit().putStringSet(SERVER_JSON_STRINGS, json).apply();
    }

    public Set<String> getServers() {
        return prefs.getStringSet(SERVER_JSON_STRINGS, new HashSet<String>());
    }

    /**
     * The default server URL
     */
    public void setDefaultServerUrl(String defaultServerUrl) {
        prefs.edit().putString(DEFAULT_SERVER_URL, defaultServerUrl).apply();
    }

    public String getDefaultServerUrl() {
        return prefs.getString(DEFAULT_SERVER_URL, null);
    }
}