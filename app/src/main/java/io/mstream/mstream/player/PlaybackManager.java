package io.mstream.mstream.player;

import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import java.util.List;

import io.mstream.mstream.MetadataObject;
import io.mstream.mstream.playlist.MstreamQueueObject;
import io.mstream.mstream.playlist.QueueManager;

/**
 * A manager that handles the active Playback and also the Queue, so the Service doesn't have to
 */

class PlaybackManager implements Playback.Callback {
    private static final String TAG = "PlaybackManager";

    private QueueManager queueManager;
    private Playback playback;
    private PlaybackServiceCallback serviceCallback;
    private MediaSessionCallback mediaSessionCallback;

    PlaybackManager(PlaybackServiceCallback serviceCallback, QueueManager queueManager, Playback playback) {
        this.serviceCallback = serviceCallback;
        this.queueManager = queueManager;
        mediaSessionCallback = new MediaSessionCallback();
        this.playback = playback;
        this.playback.setCallback(this);
    }

    Playback getPlayback() {
        return playback;
    }

    MediaSessionCompat.Callback getMediaSessionCallback() {
        return mediaSessionCallback;
    }

    /**
     * Handle a request to play music
     */
    void handlePlayRequest() {
        Log.d(TAG, "handlePlayRequest: mState=" + playback.getState());
        MediaSessionCompat.QueueItem currentMusic = queueManager.getCurrentMusic();
        if (currentMusic != null) {
            serviceCallback.onPlaybackStart();
            playback.play(currentMusic);
        }
    }

    /**
     * Handle a request to pause music
     */
    void handlePauseRequest() {
        Log.d(TAG, "handlePauseRequest: mState=" + playback.getState());
        if (playback.isPlaying()) {
            playback.pause();
            serviceCallback.onPlaybackStop();
        }
    }

    /**
     * Handle a request to stop music
     * @param withError Error message in case the stop has an unexpected cause. The error
     *                  message will be set in the PlaybackState and will be visible to
     *                  MediaController clients.
     */
    void handleStopRequest(String withError) {
        Log.d(TAG, "handleStopRequest: mState=" + playback.getState() + " error=" + withError);
        playback.stop(true);
        serviceCallback.onPlaybackStop();
        updatePlaybackState(withError);
    }

    /**
     * Update the current media player state, optionally showing an error message.
     * @param error if not null, error message to present to the user.
     */
    void updatePlaybackState(String error) {
        Log.d(TAG, "updatePlaybackState, playback state=" + playback.getState());
        long position = PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN;
        if (playback != null && playback.isConnected()) {
            position = playback.getCurrentStreamPosition();
        }

        //noinspection ResourceType
        PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder().setActions(getAvailableActions());
        int state = playback.getState();

        // If there is an error message, send it to the playback state:
        if (error != null) {
            // Error states are really only supposed to be used for errors that cause playback to
            // stop unexpectedly and persist until the user takes action to fix it.
            stateBuilder.setErrorMessage(error);
            state = PlaybackStateCompat.STATE_ERROR;
        }
        //noinspection ResourceType
        stateBuilder.setState(state, position, 1.0f, SystemClock.elapsedRealtime());

        stateBuilder.setBufferedPosition(playback.getBufferedPosition());

        // Set the activeQueueItemId if the current index is valid.
        MediaSessionCompat.QueueItem currentMusic = queueManager.getCurrentMusic();
        if (currentMusic != null) {
            stateBuilder.setActiveQueueItemId(currentMusic.getQueueId());
        }

        serviceCallback.onPlaybackStateUpdated(stateBuilder.build());

        if (state == PlaybackStateCompat.STATE_PLAYING ||
                state == PlaybackStateCompat.STATE_PAUSED) {
            serviceCallback.onNotificationRequired();
        }
    }

    private long getAvailableActions() {
        long actions =
                PlaybackStateCompat.ACTION_PLAY |
                        PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID |
                        PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH |
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT;
        if (playback.isPlaying()) {
            actions |= PlaybackStateCompat.ACTION_PAUSE;
        }
        return actions;
    }

    /**
     * Implementation of the Playback.Callback interface
     */
    @Override
    public void onCompletion() {
        // The media player finished playing the current song, so we go ahead
        // and start the next.
        if (queueManager.skipQueuePosition(1)) {
            handlePlayRequest();
            queueManager.updateMetadata();
        } else {
            // If skipping was not possible, we stop and release the resources:
            handleStopRequest(null);
        }
    }

    @Override
    public void onPlaybackStatusChanged(int state) {
        updatePlaybackState(null);
    }

    @Override
    public void onError(String error) {
        updatePlaybackState(error);
    }

    @Override
    // TODO: We prob don't need this but it complains when I remove it
    public void setCurrentMediaId(String mediaId) {
        Log.d(TAG, "setCurrentMediaId" + mediaId);
        // TODO: problems?
        // queueManager.setQueueFromMusic(mediaId);
    }

    List<MediaBrowserCompat.MediaItem> getQueueAsMediaItems() {
        return queueManager.getQueueAsMediaItems();
    }

    /**
     * This callback allows us to handle the parsed events that MediaSession emits from a MEDIA_BUTTON action.
     */
    private class MediaSessionCallback extends MediaSessionCompat.Callback {
        @Override
        public void onPlay() {
            Log.d(TAG, "play");
            handlePlayRequest();
        }

        @Override
        public void onSkipToQueueItem(long queueId) {
            Log.d(TAG, "OnSkipToQueueItem:" + queueId);
            // TODO: problems?
//            queueManager.setCurrentQueueItem(queueId);
            queueManager.updateMetadata();
        }

        @Override
        public void onSeekTo(long position) {
            Log.d(TAG, "onSeekTo:" + position);
            playback.seekTo((int) position);
        }

//        @Override
//        public void onPlayFromMediaId(String mediaId, Bundle extras) {
//            Log.d(TAG, "playFromMediaId mediaId:" + mediaId + " extras=" + extras);
//            // TODO: problems?
//            queueManager.setQueueFromMusic(mediaId);
//            handlePlayRequest();
//        }

        @Override
        public void onPause() {
            Log.d(TAG, "pause. current state=" + playback.getState());
            handlePauseRequest();
        }

        @Override
        public void onStop() {
            Log.d(TAG, "stop. current state=" + playback.getState());
            handleStopRequest(null);
        }

        @Override
        public void onSkipToNext() {
            Log.d(TAG, "skipToNext");
            if (queueManager.skipQueuePosition(1)) {
                handlePlayRequest();
            } else {
                handleStopRequest("Cannot skip");
            }
            queueManager.updateMetadata();
        }

        @Override
        public void onSkipToPrevious() {
            if (queueManager.skipQueuePosition(-1)) {
                handlePlayRequest();
            } else {
                handleStopRequest("Cannot skip");
            }
            queueManager.updateMetadata();
        }
    }

    interface PlaybackServiceCallback {
        void onPlaybackStart();

        void onNotificationRequired();

        void onPlaybackStop();

        void onPlaybackStateUpdated(PlaybackStateCompat newState);
    }
}
