package io.mstream.mstream.playlist;

import android.net.Uri;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.session.MediaSessionCompat;

import java.io.File;

import io.mstream.mstream.MetadataObject;

/**
 * Created by paul on 7/3/2017.
 */

public class MstreamQueueObject {

    private MetadataObject metadata;
    private MediaSessionCompat.QueueItem queueItem;

    public MetadataObject getMetadata(){
        return metadata;
    }
    public MediaSessionCompat.QueueItem getQueueItem(){
        return queueItem;
    }

    public MstreamQueueObject(MetadataObject mo) {
        if(mo != null){
            metadata = mo;
        }
    }

    public void setQueueItem(MediaSessionCompat.QueueItem q){
        this.queueItem = q;
    }
    public void setMetadata(MetadataObject metadata){
        // TODO: Should we clone the item so each one of these has a unique version
        // ODO: That way two of these can;t point to the same reference.  It might save some headaches later
        this.metadata = metadata;
    }


    public void constructQueueItem(){
        // TODO: Check for local file and use that over a url
        String finalPath;
        Uri MediaURI;
        String mediaDescription;

        if(metadata.getLocalFile() != null && !metadata.getLocalFile().isEmpty()){
            finalPath = metadata.getLocalFile();
            MediaURI = Uri.fromFile(new File(finalPath));
            mediaDescription = "file";
        }else{
            finalPath = metadata.getUrl();
            MediaURI = Uri.parse(finalPath);
            mediaDescription = "network";
        }



        // TODO: Construct queueItem based on metadata
        MediaDescriptionCompat description = new MediaDescriptionCompat.Builder()
                .setMediaUri(MediaURI)
                .setMediaId(finalPath)
                .setDescription(mediaDescription)
                // TODO: something a bit less hacky, maybe a Utils method
                .setTitle(MediaUtils.titleFromFilename(metadata.getUrl()))
                .build();

        this.queueItem =  new MediaSessionCompat.QueueItem(description, 0);
    }
}
