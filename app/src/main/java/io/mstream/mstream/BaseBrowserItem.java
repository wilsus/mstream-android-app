package io.mstream.mstream;

import android.support.annotation.NonNull;

public class BaseBrowserItem {

    // TYPES
        // file
        // directory
        // artist
        // album
        // playlist
    private String type;
    private String typeProp;     // Each type has a single property that is used on a click event
    private String text1;
    private String text2;
    private String image;
    private String backgroundColor;
    private MetadataObject metadata;


    private BaseBrowserItem(Builder builder) {
        this.type = builder.type;
        this.typeProp = builder.typeProp;
        this.text1 = builder.text1;
        this.text2 = builder.text2;
        this.image = builder.image;
        this.backgroundColor = builder.backgroundColor;
        this.metadata = builder.metadata;
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

    public MetadataObject getMetadata() {
        return this.metadata;
    }


    public static class Builder {
        private String type;
        private String typeProp;
        private String text1;
        private String text2;
        private String image;
        private String backgroundColor;
        private MetadataObject metadata;


        public Builder(@NonNull String type, @NonNull String typeProp, @NonNull String mainText) {
            this.type = type;
            this.typeProp = typeProp;
            this.text1 = mainText;
        }

        public Builder text2(String text2) {
            this.text2 = text2;
            return this;
        }

        public Builder image(String image) {
            this.image = image;
            return this;
        }

        public Builder backgroundColor(String backgroundColor) {
            this.backgroundColor = backgroundColor;
            return this;
        }

        public Builder metadata(MetadataObject metadata){
            this.metadata = metadata;
            return this;
        }

        public BaseBrowserItem build() {
            return new BaseBrowserItem(this);
        }
    }

}
