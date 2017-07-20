package io.mstream.mstream.player;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationManagerCompat;
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

import java.lang.ref.WeakReference;
import java.util.List;

import io.mstream.mstream.BaseActivity;
import io.mstream.mstream.MetadataObject;
import io.mstream.mstream.R;
import io.mstream.mstream.playlist.MstreamQueueObject;
import io.mstream.mstream.playlist.QueueManager;

import static io.mstream.mstream.R.string.play;

public class MStreamAudioService extends MediaBrowserServiceCompat implements PlaybackManager.PlaybackServiceCallback {
    private static final String TAG = "MStreamAudioService";
    private static final int NOTIFICATION_ID = 6689;

    // the Media Session allows the system to know what we've got going on
    private MediaSessionCompat mediaSession;
    // The playbackManager!
    private PlaybackManager playbackManager;

    // Delay stopSelf by using a handler.
    private final DelayedStopHandler delayedStopHandler = new DelayedStopHandler(this);
    private static final int STOP_DELAY = 30000;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Creating audio service!");

        // Create a queue manager to handle the playlist
        QueueManager queueManager = new QueueManager(
                new QueueManager.MetadataUpdateListener() {
                    @Override
                    public void onMetadataChanged(MediaMetadataCompat metadata) {
                        mediaSession.setMetadata(metadata);
                    }

                    @Override
                    public void onMetadataRetrieveError() {
                        playbackManager.updatePlaybackState(getString(R.string.error_no_metadata));
                    }

                    @Override
                    public void onCurrentQueueIndexUpdated(int queueIndex) {
                        playbackManager.handlePlayRequest();
                    }

                    @Override
                    public void onQueueUpdated(String title, List<MediaSessionCompat.QueueItem> newQueue) {
                        mediaSession.setQueue(newQueue);
                        mediaSession.setQueueTitle(title);
                    }
                });
        // Create the actual playback class that will be emitting music
        AudioPlayer playback = new AudioPlayer(this);
        // Set it to the playback manager, which will handle the sound and the playlist
        playbackManager = new PlaybackManager(this, queueManager, playback);

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
        mediaSession.setCallback(playbackManager.getMediaSessionCallback());

        startService(new Intent(getApplicationContext(), MStreamAudioService.class));
        // testPlay(); // TODO: SEGFFGWERGRWEG
    }

    @Override
    public int onStartCommand(@NonNull Intent intent, int flags, int startId) {
        // Log.d(TAG, "onStartCommand " + intent.getAction());
        // Handle the Media Button Receiver automatic intents
        MediaButtonReceiver.handleIntent(mediaSession, intent);
        // Reset the delay handler to enqueue a message to stop the service if
        // nothing is playing.
        delayedStopHandler.removeCallbacksAndMessages(null);
        delayedStopHandler.sendEmptyMessageDelayed(0, STOP_DELAY);
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
        Log.d(TAG, "onLoadChildren, asking queue for its items");
        // Get the current queue
        result.sendResult(playbackManager.getQueueAsMediaItems());
    }

//    public void testPlay() {
//        // Set the queue?
//        playbackManager.setCurrentMediaId("https://paulserver.mstre.am:5050/6553fd58-c032-401c-ae9d-68eb5d394c26/Feed%20Me/Feed%20Me%20-%20Calamari%20Tuesday%20[V0]/04.%20Ebb%20&%20Flow.mp3?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VybmFtZSI6InBhdWwiLCJpYXQiOjE0OTc5NzU5Mzl9.Y4B3kHhExuq0nCPMxZoxfbSibb7HbQ6S2ZDPD8ep6xA");
//
//        // Finally, play the item with the metadata specified above.
//        playbackManager.handlePlayRequest();
//    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        playbackManager.handleStopRequest(null);
        NotificationManagerCompat.from(this).cancel(NOTIFICATION_ID);

        delayedStopHandler.removeCallbacksAndMessages(null);
        mediaSession.release();
        super.onDestroy();
    }

    private Notification buildNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        MetadataObject moo = QueueManager.getIt().get(QueueManager.getIndex()).getMetadata();

        String p1 = "";
        String p2 = "";

        String artist = moo.getArtist();
        String title = moo.getTitle();
        String filename = moo.getFilename();

        if(artist != null && !artist.isEmpty()){
            p1 = artist;
            if(title != null && !title.isEmpty()){
                p2 = title;
            }else{
                p2 = filename;
            }
        }else{
            p1 = filename;
        }

        // TODO: figure out a good icon, maybe a custom tiny mstream logo in one channel
        builder.setSmallIcon(R.drawable.ic_notification)
                .setLargeIcon(null)
                .setContentTitle(p1)
                .setContentText(p2)
//                .setSubText("LOL3")
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
        builder.addAction(new NotificationCompat.Action(R.drawable.ic_skip_previous_white_36dp, "Previous", getActionIntent(KeyEvent.KEYCODE_MEDIA_PREVIOUS)));
        // Then add a play/pause action
        addPlayPauseAction(builder);
        builder.addAction(new NotificationCompat.Action(R.drawable.ic_skip_next_white_36dp, "Previous", getActionIntent(KeyEvent.KEYCODE_MEDIA_NEXT)));
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
            builder.addAction(new NotificationCompat.Action(R.drawable.ic_play_arrow_white_36dp, getString(play),
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

    /**
     * Callback method called from PlaybackManager whenever the music is about to play.
     */
    @Override
    public void onPlaybackStart() {
        if (!mediaSession.isActive()) {
            mediaSession.setActive(true);
        }
        delayedStopHandler.removeCallbacksAndMessages(null);

        // The service needs to continue running even after the bound client (usually a
        // MediaController) disconnects, otherwise the music playback will stop.
        // Calling startService(Intent) will keep the service running until it is explicitly killed.
        startService(new Intent(getApplicationContext(), MStreamAudioService.class));
    }

    /**
     * Callback method called from PlaybackManager whenever the music stops playing.
     */
    @Override
    public void onPlaybackStop() {
        // Reset the delayed stop handler, so after STOP_DELAY it will be executed again,
        // potentially stopping the service.
        delayedStopHandler.removeCallbacksAndMessages(null);
        delayedStopHandler.sendEmptyMessageDelayed(0, STOP_DELAY);
        stopForeground(false);
    }

    @Override
    public void onNotificationRequired() {
        startForeground(NOTIFICATION_ID, buildNotification());
    }

    @Override
    public void onPlaybackStateUpdated(PlaybackStateCompat newState) {
        mediaSession.setPlaybackState(newState);
    }

    /**
     * A simple handler that stops the service if playback is not active (playing)
     */
    private static class DelayedStopHandler extends Handler {
        private final WeakReference<MStreamAudioService> mWeakReference;

        private DelayedStopHandler(MStreamAudioService service) {
            mWeakReference = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            MStreamAudioService service = mWeakReference.get();
            if (service != null && service.playbackManager.getPlayback() != null) {
                if (service.playbackManager.getPlayback().isPlaying()) {
                    Log.d(TAG, "Ignoring delayed stop since the media player is in use.");
                    return;
                }
                Log.d(TAG, "Stopping service with delay handler.");
                service.stopSelf();
            }
        }
    }
}
