package io.mstream.mstream.playlist;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.mstream.mstream.MetadataObject;
import io.mstream.mstream.R;

/**
 * Based on the Universal Android Music Player queue manager.
 */

public class QueueManager {
    private static final String TAG = "QueueManager";

    private MetadataUpdateListener listener;

    // "Now playing" queue
    private static List<MstreamQueueObject> playlistQueue = new ArrayList<>();
    private static int currentIndex;

    private static boolean shouldLoop = true;
    // private static boolean shouldShuffle = false;

    // private List<MediaSessionCompat.QueueItem> shuffledQueue = new ArrayList<>();

//    private static void setShouldShuffle(boolean newVal){
//
//    }

    public static List<MstreamQueueObject> getIt(){
        return playlistQueue;
    }

    public static int getIndex(){
        return currentIndex;
    }

    public QueueManager(@NonNull MetadataUpdateListener listener) {
        this.listener = listener;

        playlistQueue = Collections.synchronizedList(new ArrayList<MstreamQueueObject>());
        currentIndex = 0;
    }

    public void callListener(){
        // TODO/ Get Playlist Queue
        List<MediaSessionCompat.QueueItem> pQ = new ArrayList<>();
        for( MstreamQueueObject mqo : playlistQueue){
            pQ.add(mqo.getQueueItem());
        }

        // Call listenr
        // listener.onQueueUpdated("lol", pQ);
    }

    private void setCurrentQueueIndex(int index) {
        if (index >= 0 && index < playlistQueue.size()) {
            currentIndex = index;
            listener.onCurrentQueueIndexUpdated(currentIndex);
        }
    }

    public void setShouldLoop(boolean newVal){
        shouldLoop = newVal;
    }

    public static boolean getShouldLoop(){
        return shouldLoop;
    }

    public static boolean toggleShouldLoop(){
        shouldLoop = !shouldLoop;
        return shouldLoop;
    }

    // TODO: see if we need this
//    public boolean setCurrentQueueItem(long queueId) {
//        // set the current index on queue from the queue Id:
//        int index = MediaUtils.getMusicIndexOnQueue(playlistQueue, queueId);
//        setCurrentQueueIndex(index);
//        return index >= 0;
//    }

    // TODO: see if we need this
//    public boolean setCurrentQueueItem(String mediaId) {
//        // set the current index on queue from the music Id:
//        int index = MediaUtils.getMusicIndexOnQueue(playlistQueue, mediaId);
//        setCurrentQueueIndex(index);
//        return index >= 0;
//    }

    public List<MstreamQueueObject> getInstance(){
        return playlistQueue;
    }

    public boolean skipQueuePosition(int amount) {
        int index = currentIndex + amount;

        // If there is nothing to skip to, don't do anything
        if(playlistQueue.size() == 0){
            return false;
        }

        if (index < 0) {
            // skip backwards before the first song will keep you on the first song
            index = 0;
        } else {

            if(shouldLoop) {
                // skip forwards when in last song will cycle back to start of the queue
                index %= playlistQueue.size();  // wtf is tis clever shit
            }else{
                if(index == playlistQueue.size()){
                    return false;
                }
            }

        }
        if (!MediaUtils.isIndexPlayable(index, playlistQueue)) {
            Log.e(TAG, "Cannot increment queue index by " + amount + ". Current=" + currentIndex + " queue length=" + playlistQueue.size());
            // Go to first song
            index = 0;
        }
        currentIndex = index;
        return true;
    }

    public static boolean goToQueuePosition(int pos){
        if (!MediaUtils.isIndexPlayable(pos, playlistQueue)) {
            Log.e(TAG, "Cannot go to position. Current=" + currentIndex + " queue length=" + playlistQueue.size());
            return false;
        }

        currentIndex = pos;
        return true;
    }



//    public void setQueueFromMusic(String filename) {
//        Log.d(TAG, "setQueueFromMusic " + filename);
//
//        List<MediaSessionCompat.QueueItem> playlist = new ArrayList<>(1);
//        MediaDescriptionCompat description = new MediaDescriptionCompat.Builder()
//                .setMediaUri(Uri.parse(filename))
//                .setMediaId(filename)
//                // TODO: something a bit less hacky, maybe a Utils method
//                .setTitle(MediaUtils.titleFromFilename(filename))
//                .build();
//        playlist.add(new MediaSessionCompat.QueueItem(description, 0));
//        setCurrentQueue("", playlist, filename);
//        updateMetadata();
//    }

//    public void addToQueue(MetadataObject metadata){
//        // Call the server if necessary
//        MstreamQueueObject mqo = new MstreamQueueObject();
//        mqo.setMetadata(metadata);
//        mqo.constructQueueItem();
//
//        playlistQueue.add(mqo.getQueueItem());
//
//        listener.onQueueUpdated("lol", playlistQueue);
//    }

    public void addToQueue2(MediaSessionCompat.QueueItem q){
        MstreamQueueObject mqo = new MstreamQueueObject(null);
        mqo.setQueueItem(q);
        playlistQueue.add(mqo);
        // listener.onQueueUpdated("lol", playlistQueue);
    }

    public static void addToQueue3(MediaSessionCompat.QueueItem q){
        MstreamQueueObject mqo = new MstreamQueueObject(null);
        mqo.setQueueItem(q);
        playlistQueue.add(mqo);
        // listener.onQueueUpdated("lol", playlistQueue); // TODO: Does not having this break anything
    }

    public static void addToQueue4(MstreamQueueObject mqo){
        mqo.constructQueueItem();
        playlistQueue.add(mqo);
    }

    public MediaSessionCompat.QueueItem getCurrentMusic() {
        if (!MediaUtils.isIndexPlayable(currentIndex, playlistQueue)) {
            return null;
        }
        return playlistQueue.get(currentIndex).getQueueItem();
    }

    public int getCurrentIndex(){
        return currentIndex;
    }

    public int getCurrentQueueSize() {
        if (playlistQueue == null) {
            return 0;
        }
        return playlistQueue.size();
    }


    public static void clearQueue(){
        // Clear Queue
        playlistQueue.clear();

        // Reset Index
        currentIndex = -1; /// hmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm
    }

    public static void removeFromQueue(MstreamQueueObject mqo){
        playlistQueue.remove(mqo);
    }

//    protected void setCurrentQueue(String title, List<MediaSessionCompat.QueueItem> newQueue) {
//        setCurrentQueue(title, newQueue, null);
//    }

//    protected void setCurrentQueue(String title, List<MediaSessionCompat.QueueItem> newQueue, String initialMediaId) {
//        playlistQueue = newQueue;
//        int index = 0;
////        if (initialMediaId != null) {
////            index = MediaUtils.getMusicIndexOnQueue(playlistQueue, initialMediaId);
////        }
//        currentIndex = Math.max(index, 0);
//        listener.onQueueUpdated(title, newQueue);
//    }

    public void updateMetadata() {
        MediaSessionCompat.QueueItem currentMusic = getCurrentMusic();
        if (currentMusic == null) {
            listener.onMetadataRetrieveError();
            return;
        }

        listener.onMetadataChanged(MediaUtils.getMetadataFromDescription(currentMusic.getDescription()));
    }

    public static void updateHashToLocalFile(String hash, String filepath){
        // Loop through Queue
        for(MstreamQueueObject mqo: playlistQueue){
            if(mqo.getMetadata().getSha256Hash().equals(hash)){
                mqo.getMetadata().setLocalFile(filepath);
                mqo.constructQueueItem();
            }
        }


        // If the hash matches, update the filepath and rebuild it
    }

    public List<MediaBrowserCompat.MediaItem> getQueueAsMediaItems() {
        Log.d(TAG, "Playlist queue has size " + playlistQueue.size());
        ArrayList<MediaBrowserCompat.MediaItem> mediaItems = new ArrayList<>(playlistQueue.size());
        for (MstreamQueueObject queueItem : playlistQueue) {
            mediaItems.add(MediaUtils.getMediaItemFromQueueItem(queueItem.getQueueItem()));
        }
        return mediaItems;
    }

    public interface MetadataUpdateListener {
        void onMetadataChanged(MediaMetadataCompat metadata);

        void onMetadataRetrieveError();

        void onCurrentQueueIndexUpdated(int queueIndex);

        void onQueueUpdated(String title, List<MediaSessionCompat.QueueItem> newQueue);
    }
}
