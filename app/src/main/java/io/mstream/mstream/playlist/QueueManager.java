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

/**
 * Based on the Universal Android Music Player queue manager.
 */

public class QueueManager {
    private static final String TAG = "QueueManager";

    private MetadataUpdateListener listener;

    // "Now playing" queue:
    private List<MediaSessionCompat.QueueItem> playlistQueue;
    private int currentIndex;

    public QueueManager(@NonNull MetadataUpdateListener listener) {
        this.listener = listener;

        playlistQueue = Collections.synchronizedList(new ArrayList<MediaSessionCompat.QueueItem>());
        currentIndex = 0;
    }

    private void setCurrentQueueIndex(int index) {
        if (index >= 0 && index < playlistQueue.size()) {
            currentIndex = index;
            listener.onCurrentQueueIndexUpdated(currentIndex);
        }
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

    public boolean skipQueuePosition(int amount) {
        int index = currentIndex + amount;
        if (index < 0) {
            // skip backwards before the first song will keep you on the first song
            index = 0;
        } else {
            // skip forwards when in last song will cycle back to start of the queue
            // TODO: is this desired behaviour?
            index %= playlistQueue.size();
        }
        if (!MediaUtils.isIndexPlayable(index, playlistQueue)) {
            Log.e(TAG, "Cannot increment queue index by " + amount + ". Current=" + currentIndex + " queue length=" + playlistQueue.size());
            return false;
        }
        currentIndex = index;
        return true;
    }

    public void setQueueFromMusic(String filename) {
        Log.d(TAG, "setQueueFromMusic " + filename);

        List<MediaSessionCompat.QueueItem> playlist = new ArrayList<>(1);
        MediaDescriptionCompat description = new MediaDescriptionCompat.Builder()
                .setMediaUri(Uri.parse(filename))
                .setMediaId(filename)
                // TODO: something a bit less hacky, maybe a Utils method
                .setTitle(MediaUtils.titleFromFilename(filename))
                .build();
        playlist.add(new MediaSessionCompat.QueueItem(description, 0));
        setCurrentQueue("", playlist, filename);
        updateMetadata();
    }

    public MediaSessionCompat.QueueItem getCurrentMusic() {
        if (!MediaUtils.isIndexPlayable(currentIndex, playlistQueue)) {
            return null;
        }
        return playlistQueue.get(currentIndex);
    }

    public int getCurrentQueueSize() {
        if (playlistQueue == null) {
            return 0;
        }
        return playlistQueue.size();
    }

    protected void setCurrentQueue(String title, List<MediaSessionCompat.QueueItem> newQueue) {
        setCurrentQueue(title, newQueue, null);
    }

    protected void setCurrentQueue(String title, List<MediaSessionCompat.QueueItem> newQueue, String initialMediaId) {
        playlistQueue = newQueue;
        int index = 0;
//        if (initialMediaId != null) {
//            index = MediaUtils.getMusicIndexOnQueue(playlistQueue, initialMediaId);
//        }
        currentIndex = Math.max(index, 0);
        listener.onQueueUpdated(title, newQueue);
    }

    public void updateMetadata() {
        MediaSessionCompat.QueueItem currentMusic = getCurrentMusic();
        if (currentMusic == null) {
            listener.onMetadataRetrieveError();
            return;
        }

        listener.onMetadataChanged(MediaUtils.getMetadataFromDescription(currentMusic.getDescription()));
    }

    public List<MediaBrowserCompat.MediaItem> getQueueAsMediaItems() {
        Log.d(TAG, "Playlist queue has size " + playlistQueue.size());
        ArrayList<MediaBrowserCompat.MediaItem> mediaItems = new ArrayList<>(playlistQueue.size());
        for (MediaSessionCompat.QueueItem queueItem : playlistQueue) {
            mediaItems.add(MediaUtils.getMediaItemFromQueueItem(queueItem));
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
