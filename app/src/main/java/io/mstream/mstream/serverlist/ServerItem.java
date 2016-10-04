package io.mstream.mstream.serverlist;

/**
 * POJO representing an mStream server
 */
public class ServerItem {
    private String name;
    private String link;
    private String username;
    private String password;
    private boolean isDefault = false;

    public ServerItem(String name, String link, String username, String password) {
        new ServerItem(name, link, username, password, false);
    }

    public ServerItem(String name, String link, String username, String password, boolean isDefault) {
        this.name = name;
        this.link = link;
        this.username = username;
        this.password = password;
        this.isDefault = isDefault;
    }

    public String getServerName() {
        return this.name;
    }

    public String getServerUrl() {
        return this.link;
    }

    public String getServerUsername() {
        return this.username;
    }

    public String getServerPassword() {
        return this.password;
    }

    public boolean isDefault() {
        return this.isDefault;
    }

    public void setIsDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }
}
