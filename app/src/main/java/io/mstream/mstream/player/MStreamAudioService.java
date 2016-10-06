package io.mstream.mstream.player;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.KeyEvent;

import java.util.LinkedList;
import java.util.List;

import io.mstream.mstream.BaseActivity;
import io.mstream.mstream.R;
import io.mstream.mstream.filebrowser.FileItem;

public class MStreamAudioService extends MediaBrowserServiceCompat {
    private static final String TAG = "MStreamAudioService";

    // the Media Session allows the system to know what we've got going on
    private MediaSessionCompat mediaSession;
    // The player!
    private AudioPlayer player;

    // We need to track the status
    boolean isSongLoaded = false;
    // Playlist is a linked list
    public LinkedList<FileItem> playlist = new LinkedList<>();
    // Keep a cache of the currently playing song position
    private Integer playlistCache = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Creating audio service!");

        // Actually play audio, how about that
        player = new AudioPlayer(this);

        // Set up Media Session
        mediaSession = new MediaSessionCompat(this, TAG);
        // Call to super to set up the session
        setSessionToken(mediaSession.getSessionToken());
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_PAUSED, 0, 0)
                .setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE)
                .build());
        // Set the Activity that the media session is tied to - probably just BaseActivity.
        // This will launch when the user taps our notification.
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 667,
                new Intent(this, BaseActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
        mediaSession.setSessionActivity(pendingIntent);
        // Set up callbacks - these will be called via the onStartCommand's registration of the MediaButtonReceiver
        mediaSession.setCallback(
                new MediaSessionCompat.Callback() {
                    @Override
                    public boolean onMediaButtonEvent(Intent mediaButtonEvent) {
                        final String intentAction = mediaButtonEvent.getAction();
                        if (Intent.ACTION_MEDIA_BUTTON.equals(intentAction)) {
                            final KeyEvent event = mediaButtonEvent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
                            if (event == null) {
                                return super.onMediaButtonEvent(mediaButtonEvent);
                            }
                            final int keycode = event.getKeyCode();
                            final int action = event.getAction();
                            if (event.getRepeatCount() == 0 && action == KeyEvent.ACTION_DOWN) {
                                switch (keycode) {
                                    // Do what you want in here
                                    case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                                        Log.d(TAG, "KEYCODE_MEDIA_PLAY_PAUSE");
                                        if (isPlaying()) {
                                            pause();
                                        } else {
                                            play();
                                        }
                                        break;
                                    case KeyEvent.KEYCODE_MEDIA_PAUSE:
                                        Log.d(TAG, "KEYCODE_MEDIA_PAUSE");
                                        pause();
                                        break;
                                    case KeyEvent.KEYCODE_MEDIA_PLAY:
                                        Log.d(TAG, "KEYCODE_MEDIA_PLAY");
                                        play();
                                        break;
                                    case KeyEvent.KEYCODE_MEDIA_STOP:
                                        Log.d(TAG, "KEYCODE_MEDIA_STOP");
                                        stop();
                                        break;
                                    default:
                                        Log.d(TAG, "Keycode is " + keycode);
                                        break;
                                }
                            }
                        }
                        return super.onMediaButtonEvent(mediaButtonEvent);
                    }
                }
        );
        startService(new Intent(getApplicationContext(), MStreamAudioService.class));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
//        // Set an on song completion listener
//        this.mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//            @Override
//            public void onCompletion(MediaPlayer mp) {
//                goToNextTrack();
//            }
//        });
        // Handle the Media Button Receiver automatic intents
        MediaButtonReceiver.handleIntent(mediaSession, intent);
        return START_STICKY;
    }

    // Overrides for MediaBrowser
    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        // Returning null == no one can connect, so we’ll return something
        return new BrowserRoot(getString(R.string.app_name), null);
    }

    // Gives a list of all the items able to be browsed. covers playable and nonplayable items (files and folders).
    // Should be using this to populate the UI!
    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        result.sendResult(null);
    }

    private void play() {
        mediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_PLAYING, 0, 1.0f)
                .setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE).build());

        // Update the metadata for the playing track
        // TODO: live values
        MediaMetadataCompat metadata = new MediaMetadataCompat.Builder()
                // TODO: where does the display title show up?
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, "this is the display title")
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, "this is the display subtitle")
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION, "this is the display description")
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, "http://darncoyotes.mstream.io/MP3/Darn%20Coyotes%20-%2005%20From%20Athens.mp3")
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .build();
        // Set the metadata for the session
        mediaSession.setMetadata(metadata);
        // Then build the notification, since it requires the session metadata!
        updateNotification();
        // Finally, play the item with the metadata specified above.
        player.play(new MediaSessionCompat.QueueItem(metadata.getDescription(), 123));
    }

    private void updateNotification() {
        startForeground(6689, buildNotification());
    }

    private void pause() {
        player.pause();
        mediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_PAUSED, 0, 0.0f)
                .setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE).build());
        updateNotification();
        // allow the user to swipe us away when paused, but keep the notification up.
        stopForeground(false);
    }

    private void stop() {
        player.stop(false);
        // Stop hogging audio focus
        ((AudioManager) getSystemService(Context.AUDIO_SERVICE)).abandonAudioFocus(null);
        mediaSession.setActive(false);
        // user has stopped us, remove the notification as it no longer applies.
        stopForeground(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaSession.release();
    }

    private Notification buildNotification() {
        MediaDescriptionCompat description = mediaSession.getController().getMetadata().getDescription();

        // TODO: any way to get metadata from mStream? Or just the filename?
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        // TODO: figure out a good icon, maybe a custom tiny mstream logo in one channel
        builder.setSmallIcon(R.drawable.ic_notification)
                .setLargeIcon(description.getIconBitmap())
                .setContentTitle(description.getTitle())
                .setContentText(description.getSubtitle())
                .setSubText(description.getDescription())
                // when tapped, launch the mstream activity (have to set this elsewhere)
                .setContentIntent(mediaSession.getController().getSessionActivity())
                // Media controls should be publicly visible
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                // When swiped away, stop playback.
                .setDeleteIntent(getActionIntent(KeyEvent.KEYCODE_MEDIA_STOP))
                // TODO: test out the coloration
                .setColor(getResources().getColor(R.color.colorPrimaryDark));
        // Add some actions
        // ...
        // Then add a play/pause action
        addPlayPauseAction(builder);
        // Set the style and configure the action buttons
        builder.setStyle(new NotificationCompat.MediaStyle()
                // Show the first button we added, in this cause, pause
                .setShowActionsInCompactView(0)
                .setMediaSession(mediaSession.getSessionToken())
                // Add a little 'x' to allow users to tap it to exit playback, in addition to swiping away
                .setShowCancelButton(true)
                .setCancelButtonIntent(getActionIntent(KeyEvent.KEYCODE_MEDIA_STOP)));

        return builder.build();
    }

    private void addPlayPauseAction(NotificationCompat.Builder builder) {
        if (isPlaying()) {
            builder.addAction(new NotificationCompat.Action(R.drawable.ic_pause_white_36dp, getString(R.string.pause),
                    getActionIntent(KeyEvent.KEYCODE_MEDIA_PAUSE)));
        } else {
            builder.addAction(new NotificationCompat.Action(R.drawable.ic_play_arrow_white_36dp, getString(R.string.play),
                    getActionIntent(KeyEvent.KEYCODE_MEDIA_PLAY)));
        }
    }

    /**
     * A helper method to get a PendingIntent based on a media key function.
     */
    private PendingIntent getActionIntent(int mediaKeyEvent) {
        Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        intent.setPackage(this.getPackageName());
        intent.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, mediaKeyEvent));
        return PendingIntent.getBroadcast(this, mediaKeyEvent, intent, 0);
    }

    public boolean isPlaying() {
        return mediaSession.getController().getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING;
    }


    /////////////////////////////////
    //          OLD STUFF          //
    /////////////////////////////////

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
        return player.getCurrentStreamPosition();
    }

    public void playTrack(String url) {
//        try {
//            this.isSongLoaded = false;
//            player.stop(false);
//            player.setDataSource(getApplicationContext(), Uri.parse(url));
//            player.prepareAsync();
//            player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//                public void onPrepared(MediaPlayer mp) {
//                    mp.start();
//                    isSongLoaded = true;
//
//                    // Broadcast message
//                    sendMessage("new-track");
//                }
//            });
//        } catch (IllegalArgumentException | IllegalStateException | IOException e) {
//            e.getCause();
//            //Toast.makeText(getApplicationContext(), "You might not set the URI correctly!", Toast.LENGTH_LONG).show();
//        }
    }

    public void seekTo(int time) {
        this.player.seekTo(time);
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
