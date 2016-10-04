package io.mstream.mstream.serverlist;

/**
 * POJO representing an mStream server
 */
public class ServerItem {
    private final String name;
    private final String url;
    private final String username;
    private final String password;
    private final boolean isDefault;

    private ServerItem(Builder builder) {
        this.name = builder.name;
        this.url = builder.url;
        this.username = builder.username;
        this.password = builder.password;
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

    public boolean isDefault() {
        return this.isDefault;
    }

    public static class Builder {
        private final String name;
        private final String url;
        private String username;
        private String password;
        private boolean isDefault;

        public Builder(String name, String url) {
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

        public Builder isDefault(boolean isDefault) {
            this.isDefault = isDefault;
            return this;
        }

        public ServerItem build() {
            return new ServerItem(this);
        }
    }
}
