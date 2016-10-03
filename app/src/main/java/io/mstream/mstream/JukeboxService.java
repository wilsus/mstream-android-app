package io.mstream.mstream;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

import io.mstream.mstream.serverlist.ServerItem;


public class JukeboxService extends Service {
    private static final String TAG = "JukeboxService";

    MediaPlayer jukebox = new MediaPlayer();

    // We need to track the status
    boolean isSongLoaded = false;

    // Playlist is a linked list
    public LinkedList<ListItem> playlist = new LinkedList<>();

    // Keep a cache of the currently playing song position
    Integer playlistCache = 0;

    // Store the server list here
    ArrayList serverList = new ArrayList();

    public void addServer(ServerItem server) {

    }

    // TODO Is this needed anymore ?
    public JukeboxService() {

    }

    public LinkedList<ListItem> getPlaylist() {
        return this.playlist;
    }

    // Add a track to playlist
    public void addTrackToPlaylist(ListItem trackItem) {
        // If the linked list is empty, set the currently playing status
        if (this.playlist.isEmpty()) {
            trackItem.setCurrrentlyPlaying(true);

            // Set the cache to 1, because there will only be one item on the list
            this.playlistCache = 0;

            // Set Media//////////////////////
            String url = trackItem.getItemLink();
            playTrack(url);
            ////////////////////////////////////
        }

        this.playlist.addLast(trackItem);
    }


    public int getPos() {
        return this.jukebox.getCurrentPosition();
    }

    public boolean isPlaying() {
        return this.jukebox.isPlaying();
    }

    public int getDur() {
        return this.jukebox.getDuration();
    }

    //
    public void playTrack(String url) {
        try {
            this.isSongLoaded = false;

            jukebox.stop();
            jukebox.reset();

            //url = url.replace(" ", "%20");
            jukebox.setDataSource(getApplicationContext(), Uri.parse(url));

            // Old way of doing things
//            jukebox.prepare();
//            jukebox.start();
//            this.isSongLoaded = true;
//            sendMessage("new-track");

            jukebox.prepareAsync();

            jukebox.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                public void onPrepared(MediaPlayer mp) {
                    mp.start();
                    isSongLoaded = true;

                    // Broadcast message
                    sendMessage("new-track");
                }
            });

        } catch (IllegalArgumentException | IllegalStateException | IOException e) {
            e.getCause();
            //Toast.makeText(getApplicationContext(), "You might not set the URI correctly!", Toast.LENGTH_LONG).show();
        }
    }

    public void removeTrackFromPlaylist(/*???*/) {

    }

    public void seek(int time) {
        this.jukebox.seekTo(time);
    }

    public void goToNextTrack() {
        Integer currentTrackIndex = findCurrentlyPlaying();
        Integer nextTrackIndex = currentTrackIndex;
        nextTrackIndex++;
        ListItem currentTrackItem = this.playlist.get(currentTrackIndex);

        // Check if it's the last element in the list
        if (currentTrackItem == this.playlist.getLast()) {
            return;
        }

        ListItem nextTrackItem = this.playlist.get(nextTrackIndex);
        currentTrackItem.setCurrrentlyPlaying(false);
        nextTrackItem.setCurrrentlyPlaying(true);

        playTrack(nextTrackItem.getItemLink());
    }

    public void goToPreviousTrack() {

    }

    public void goToSelectedTrack(ListItem item) {
        // TODO: make this more efficient
        // Check the playlsit cache
        // Remove currently playing status if true
        // Loop through playlist
        // Break if we find the item

        // Loop through playlist
        for (int i = 0; i < this.playlist.size(); i++) {
            // If we find the currently playing item
            if (checkIfCurrentlyPlaying(i)) {
                // Remove currently playing status
                this.playlist.get(i).setCurrrentlyPlaying(false);

            }

            // if the items match
            if (item == this.playlist.get(i)) {
                // Reset the playlist cache, and set the item to currntlyPlaying = true
                this.playlist.get(i).setCurrrentlyPlaying(true);
                this.playlistCache = i;

                // Start playing
                playTrack(this.playlist.get(i).getItemLink());

            }
        }

    }

    public void goToRandomTrack() {

    }

    public void changeTrackPosition() {

    }

    public void playPause() {
        try {
            if (isPlaying()) {
                jukebox.pause();
            } else if (this.isSongLoaded) {
                jukebox.start();
            }
        } catch (Exception e) {
            Log.d(TAG, "Exception thrown  :" + e);
        }

    }

    public void startPlaying() {

    }

    public void stopPlaying() {

    }

    // Delete entire playlist
    public void blowAwayPlaylist() {

    }
    // Todo: This var will be a cache of what song is currently playling

    // Bind to a View
    IBinder mBinder = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class LocalBinder extends Binder {
        public JukeboxService getServerInstance() {
            return JukeboxService.this;
        }
    }

//    @Override
//    public IBinder onBind(Intent intent) {
//        // TODO: Return the communication channel to the service.
//        throw new UnsupportedOperationException("Not yet implemented");
//    }

    /**
     * Initialize the media player
     */
    @Override
    public void onCreate() {

        // Set an on song completion listener
        this.jukebox.setOnCompletionListener(new OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
                goToNextTrack();
            }
        });
    }

    // Find Global
    // TODO: Should this return the integer, or actual song item
    public Integer findCurrentlyPlaying() {
        // TODO: If playlist is empty, return -1
        if (this.playlist.isEmpty()) {
            return -1;
        }

        // Check Playlist Cache
        if (checkIfCurrentlyPlaying(this.playlistCache)) {
            return this.playlistCache;
        } else {
            // TODO: Log an error.  The playlist cache should theoretically never be off

            // Loop through the linked list
            for (int i = 0; i < this.playlist.size(); i++) {

                if (checkIfCurrentlyPlaying(i)) {
                    this.playlistCache = i;
                    return i;
                }
            }
        }

        // If nothing is currently set as playing, something has gone wrong
        // Return -2 as a type of error
        // TODO: Throw an error?
        return -2;
    }

    // TODO: Function that checks if linked linked list item is currently playing
    public boolean checkIfCurrentlyPlaying(int index) {
        ListItem testThisItem = this.playlist.get(index);

        return testThisItem.getCurrentlyPlayingStatus();
    }

    private void sendMessage(String message) {
        Intent intent = new Intent("new-track");
        // You can also include some extra data.
        intent.putExtra("message", message);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
