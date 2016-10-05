package io.mstream.mstream.player;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.IOException;
import java.util.LinkedList;

import io.mstream.mstream.filebrowser.FileItem;


public class MStreamAudioService extends Service {
    private static final String TAG = "MStreamAudioService";

    MediaPlayer jukebox = new MediaPlayer();

    // We need to track the status
    boolean isSongLoaded = false;

    // Playlist is a linked list
    public LinkedList<FileItem> playlist = new LinkedList<>();

    // Keep a cache of the currently playing song position
    Integer playlistCache = 0;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Set an on song completion listener
        this.jukebox.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                goToNextTrack();
            }
        });
        return super.onStartCommand(intent, flags, startId);
    }

    public LinkedList<FileItem> getPlaylist() {
        return this.playlist;
    }

    // Add a track to playlist
    public void addTrackToPlaylist(FileItem trackItem) {
        // If the linked list is empty, set the currently playing status
        if (this.playlist.isEmpty()) {
            trackItem.setCurrentlyPlaying(true);

            // Set the cache to 1, because there will only be one item on the list
            this.playlistCache = 0;

            // Set Media//////////////////////
            String url = trackItem.getItemUrl();
            playTrack(url);
            ////////////////////////////////////
        }

        this.playlist.addLast(trackItem);
    }

    public int getPosition() {
        return this.jukebox.getCurrentPosition();
    }

    public boolean isPlaying() {
        return this.jukebox.isPlaying();
    }

    public int getDuration() {
        return this.jukebox.getDuration();
    }

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

    public void seekTo(int time) {
        this.jukebox.seekTo(time);
    }

    public void goToNextTrack() {
        Integer currentTrackIndex = findCurrentlyPlaying();
        Integer nextTrackIndex = currentTrackIndex;
        nextTrackIndex++;
        FileItem currentTrackItem = this.playlist.get(currentTrackIndex);

        // Check if it's the last element in the list
        if (currentTrackItem == this.playlist.getLast()) {
            return;
        }

        FileItem nextTrackItem = this.playlist.get(nextTrackIndex);
        currentTrackItem.setCurrentlyPlaying(false);
        nextTrackItem.setCurrentlyPlaying(true);

        playTrack(nextTrackItem.getItemUrl());
    }

    public void goToPreviousTrack() {

    }

    public void goToSelectedTrack(FileItem item) {
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
                this.playlist.get(i).setCurrentlyPlaying(false);
            }

            // if the items match
            if (item == this.playlist.get(i)) {
                // Reset the playlist cache, and set the item to currntlyPlaying = true
                this.playlist.get(i).setCurrentlyPlaying(true);
                this.playlistCache = i;

                // Start playing
                playTrack(this.playlist.get(i).getItemUrl());
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

    @Override
    public IBinder onBind(Intent intent) {
        return null;
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
        FileItem testThisItem = this.playlist.get(index);
        return testThisItem.getCurrentlyPlayingStatus();
    }

    private void sendMessage(String message) {
        Intent intent = new Intent("new-track");
        // You can also include some extra data.
        intent.putExtra("message", message);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
