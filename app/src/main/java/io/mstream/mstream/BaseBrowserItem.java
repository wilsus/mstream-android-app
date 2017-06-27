package io.mstream.mstream;

public class BaseBrowserItem {


    private String type;
    // TYPES
        // file
        // directory
        // artist
        // album
        // playlist
        // queue-item

    private String typeProp;
    // Each type hasa single property that is used on a click event


    // Text lines
    private String text1;
    private String text2;

    // Image
    private String image;

    // Background color
    private String backgroundColor;


    public BaseBrowserItem(String type, String typeProp, String text1, String text2, String image, String backgroundColor) {
        this.type = type;
        this.typeProp = typeProp;
        this.text1 = text1;
        this.text2 = text2;
        this.image = image;
        this.backgroundColor = backgroundColor;

    }


    public String getItemType() {
        return this.type;
    }

    public String getItemText1() {
        return this.text1;
    }

    public String getItemText2() {
        return this.text2;
    }

    public String getImage() {
        return this.image;
    }

    public String getTypeProp() {
        return this.typeProp;
    }

    public String getBackgroundColor() {
        return this.backgroundColor;
    }

}
