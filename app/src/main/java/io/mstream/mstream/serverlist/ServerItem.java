package io.mstream.mstream.serverlist;

import android.support.annotation.NonNull;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * POJO representing an mStream server
 */
public class ServerItem {
    private static final String KEY_NAME = "name";
    private static final String KEY_URL = "url";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";
    private static  String KEY_VPATHS = "vpaths";
    private static  String KEY_JWT = "jwt";  // JSON Web Token
    private static  String KEY_ISDEFAULT = "isDefault";


    private final String name;
    private final String url;
    private final String username;
    private final String password;
    private ArrayList<String> vPaths;
    private String jwt = "";
    private boolean isDefault = false;

    private ServerItem(Builder builder) {
        this.name = builder.name;
        this.url = builder.url;
        this.username = builder.username;
        this.password = builder.password;
        this.vPaths = builder.vPaths;
        this.jwt = builder.jwt;
        this.isDefault = builder.isDefault;
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

    // TODO: Change Name to getServerVPaths
    public ArrayList<String> getServerVPaths() {
        return this.vPaths;
    }

    public String getServerJWT() {
        if(this.jwt == null){
            return "";
        }
        return this.jwt;
    }

    public boolean getServerDefaultStatus() {
        return this.isDefault;
    }

    public void setServerJWT(String jwt) {
        this.jwt = jwt;
    }

    // TODO:
    public void setServerVPaths(ArrayList vPaths) {
        this.vPaths = vPaths;
    }

    public void setServerDefaultStatus(boolean isDefault) {
        this.isDefault = isDefault;
    }


    public String toJsonString() {
        JSONObject obj = new JSONObject();
        try {
            obj.put(KEY_NAME, name);
            obj.put(KEY_URL, url);
            obj.put(KEY_USERNAME, username);
            obj.put(KEY_PASSWORD, password);
            obj.put(KEY_VPATHS, vPaths); // TODO:
            obj.put(KEY_JWT, jwt);
            obj.put(KEY_ISDEFAULT, isDefault);
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
//                if (obj.has(KEY_VPATHS)) {
//                    ArrayList<String> listdata = new ArrayList<>();
//                    JSONArray jArray = obj.getJSONArray(KEY_VPATHS);
//                    if (jArray != null) {
//                        for (int i=0;i<jArray.length();i++){
//                            listdata.add(jArray.getString(i));
//                        }
//                    }
//                    builder = builder.vPaths(listdata);
//                }
                if (obj.has(KEY_JWT)) {
                    builder = builder.jwt(obj.getString(KEY_JWT));
                }else{
                    builder = builder.jwt("");
                }
                if (obj.has(KEY_ISDEFAULT)) {
                    builder = builder.isDefault(obj.getBoolean(KEY_ISDEFAULT));
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
        private ArrayList vPaths;
        private String jwt;
        private boolean isDefault;


        public Builder(@NonNull String name, @NonNull String url) {
            this.name = name.trim();
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

        // TODO
        public Builder vPaths(ArrayList vPaths) {
            this.vPaths = vPaths;
            return this;
        }

        public Builder jwt(String jwt) {
            this.jwt = jwt;
            return this;
        }

        public Builder isDefault(boolean isDefault) {
            this.isDefault = isDefault;
            return this;
        }
        public ServerItem build() {
            return new ServerItem(this);
        }
    }
}
