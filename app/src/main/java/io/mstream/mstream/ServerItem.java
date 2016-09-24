package io.mstream.mstream;

/**
 * Created by paul on 2/22/2016.
 */
public class ServerItem {
    private String name;
    private String link;
    private String username;
    private String password;
    private boolean isDefault = false;

    public ServerItem(String name, String link, String username, String password) {
        this.name = name;
        this.link = link;
        this.username = username;
        this.password = password;
    }

    public String getServerName() {
        return this.name;
    }

    public String getServerLink() {
        return this.link;
    }

    public String getServerUsername() {
        return this.username;
    }

    public String getServerPassword() {
        return this.password;
    }

    public boolean getDefaultVal() {
        return this.isDefault;
    }

    public void setDefault(boolean defaultVal) {
        this.isDefault = defaultVal;
    }
}
