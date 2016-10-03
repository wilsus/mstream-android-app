package io.mstream.mstream;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;

/**
 * A wrapper for SharedPrefs that makes it easy to get/set items.
 */

public final class LocalPreferences {

    // Name of file where preferences are stored
    private static final String FILE_NAME = LocalPreferences.class.getPackage().getName();

    // Preference names
    private static final String SERVER_NICKNAME = FILE_NAME + ".a";
    private static final String SERVER_URL = FILE_NAME + ".b";
    private static final String USERNAME = FILE_NAME + ".c";
    private static final String PASSWORD = FILE_NAME + ".d";
    private static final String IS_DEFAULT = FILE_NAME + ".e";

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

    public void setServerNickname(String serverNickname) {
        prefs.edit().putString(SERVER_NICKNAME, serverNickname).apply();
    }

    @Nullable
    public String getServerNickname() {
        return prefs.getString(SERVER_NICKNAME, null);
    }

    public void setServerUrl(String serverUrl) {
        prefs.edit().putString(SERVER_URL, serverUrl).apply();
    }

    @Nullable
    public String getServerUrl() {
        return prefs.getString(SERVER_URL, null);
    }

    public void setUsername(String username) {
        prefs.edit().putString(USERNAME, username).apply();
    }

    @Nullable
    public String getUsername() {
        return prefs.getString(USERNAME, null);
    }

    // TODO: probably shouldn't be storing this?
    public void setPassword(String password) {
        prefs.edit().putString(PASSWORD, password).apply();
    }

    @Nullable
    public String getPassword() {
        return prefs.getString(PASSWORD, null);
    }

    public void setIsDefault(boolean isDefault) {
        prefs.edit().putBoolean(IS_DEFAULT, isDefault).apply();
    }

    @Nullable
    public boolean getIsDefault() {
        return prefs.getBoolean(IS_DEFAULT, false);
    }
}