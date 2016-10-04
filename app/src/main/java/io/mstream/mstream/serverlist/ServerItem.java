package io.mstream.mstream.serverlist;

import android.support.annotation.NonNull;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * POJO representing an mStream server
 */
public class ServerItem {
    private static final String KEY_NAME = "name";
    private static final String KEY_URL = "url";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";

    private final String name;
    private final String url;
    private final String username;
    private final String password;

    private ServerItem(Builder builder) {
        this.name = builder.name;
        this.url = builder.url;
        this.username = builder.username;
        this.password = builder.password;
    }

    public String getServerName() {
        return this.name;
    }

    public String getServerUrl() {
        return this.url;
    }

    public String getServerUsername() {
        return this.username;
    }

    public String getServerPassword() {
        return this.password;
    }

    public String toJsonString() {
        JSONObject obj = new JSONObject();
        try {
            obj.put(KEY_NAME, name);
            obj.put(KEY_URL, url);
            obj.put(KEY_USERNAME, username);
            obj.put(KEY_PASSWORD, password);
        } catch (JSONException e) {
            Log.e("ServerItemFromJSON", e.getLocalizedMessage());
        }
        return obj.toString();
    }

    public static ServerItem fromJsonString(String json) {
        try {
            JSONObject obj = new JSONObject(json);
            if (obj.has(KEY_NAME) && obj.has(KEY_URL)) {
                Builder builder = new Builder(obj.getString(KEY_NAME), obj.getString(KEY_URL));
                if (obj.has(KEY_USERNAME) && obj.has(KEY_PASSWORD)) {
                    builder = builder.username(obj.getString(KEY_USERNAME)).password(obj.getString(KEY_PASSWORD));
                }
                return builder.build();
            } else {
                return null;
            }
        } catch (JSONException e) {
            return null;
        }
    }

    public static class Builder {
        private final String name;
        private final String url;
        private String username;
        private String password;

        public Builder(@NonNull String name, @NonNull String url) {
            this.name = name;
            this.url = url;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public ServerItem build() {
            return new ServerItem(this);
        }
    }
}
