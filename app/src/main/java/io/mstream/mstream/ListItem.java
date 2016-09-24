package io.mstream.mstream;


public class ListItem {

    private String type;
    private String name;
    private String link;
    private boolean currrentlyPlaying;

    public ListItem(String name, String type, String link) {
        this.name = name;
        this.type = type;
        this.link = link;
        this.currrentlyPlaying = false;
    }

    public String getItemName() {
        return this.name;
    }

    public String getItemType() {
        return this.type;
    }

    public String getItemLink() {
        return this.link;

    }

    public void setCurrrentlyPlaying(boolean bool) {
        this.currrentlyPlaying = bool;
    }

    public boolean getCurrentlyPlayingStatus() {
        return this.currrentlyPlaying;

    }


}
