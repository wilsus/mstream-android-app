package io.mstream.mstream.filebrowser;

public class FileItem {

    private String type;
    private String name;
    private String url;
    private boolean currentlyPlaying;

    public FileItem(String name, String type, String url) {
        this.name = name;
        this.type = type;
        this.url = url;
        this.currentlyPlaying = false;
    }

    public String getItemName() {
        return this.name;
    }

    public String getItemType() {
        return this.type;
    }

    public String getItemUrl() {
        return this.url;
    }

    public void setCurrentlyPlaying(boolean bool) {
        this.currentlyPlaying = bool;
    }

    public boolean getCurrentlyPlayingStatus() {
        return this.currentlyPlaying;
    }
}
