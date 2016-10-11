package io.mstream.mstream;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import io.mstream.mstream.playlist.MediaControllerConnectedEvent;

/**
 * A wrapper for the Media Controls set of views.
 */

public class PlayerControlsFragment extends Fragment {
    private final static String TAG = "PlayerControlsFragment";

    private final View.OnClickListener playPauseButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            MediaControllerCompat controller = getActivity().getSupportMediaController();
            PlaybackStateCompat stateObj = controller.getPlaybackState();
            final int state = stateObj == null ? PlaybackStateCompat.STATE_NONE : stateObj.getState();
            Log.d(TAG, "Button pressed, in state " + state);
            switch (v.getId()) {
                case R.id.play_pause:
                    Log.d(TAG, "Play/Pause button pressed, in state " + state);
                    if (state == PlaybackStateCompat.STATE_PAUSED ||
                            state == PlaybackStateCompat.STATE_STOPPED ||
                            state == PlaybackStateCompat.STATE_NONE) {
                        playMedia();
                    } else if (state == PlaybackStateCompat.STATE_PLAYING ||
                            state == PlaybackStateCompat.STATE_BUFFERING ||
                            state == PlaybackStateCompat.STATE_CONNECTING) {
                        pauseMedia();
                    }
                    break;
                // TODO: add buttons for skip?
                default:
                    break;
            }
            // TODO: remove, used only for testing particular hardcoced track
            seekBar.setMax(180000);
        }
    };

    // Receive callbacks from the MediaController. Here we update our state such as which queue
    // is being shown, the current title and description and the PlaybackState.
    private final MediaControllerCompat.Callback mediaControllerCallback = new MediaControllerCompat.Callback() {
        @Override
        public void onPlaybackStateChanged(@NonNull PlaybackStateCompat state) {
            Log.d(TAG, "Received playback state change to state " + state.getState());
            PlayerControlsFragment.this.onPlaybackStateChanged(state);
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            if (metadata == null) {
                return;
            }
            Log.d(TAG, "Received metadata state change to mediaId=" + metadata.getDescription().getMediaId() +
                    " song=" + metadata.getDescription().getTitle());
            PlayerControlsFragment.this.onMetadataChanged(metadata);
        }
    };

    private ImageButton playPauseButton;
    private SeekBar seekBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_playback_controls, container, false);

        playPauseButton = (ImageButton) rootView.findViewById(R.id.play_pause);
        playPauseButton.setEnabled(true);
        playPauseButton.setOnClickListener(playPauseButtonListener);

        seekBar = (SeekBar) rootView.findViewById(R.id.seek_bar);
        seekBar.setPadding(0, 0, 0, 0);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // TODO: actual current time text?
//                startText.setText(DateUtils.formatElapsedTime(progress / 1000));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO: used for trick play, if we're not getting progress events all the time
//                stopSeekbarUpdate();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekMedia(seekBar.getProgress());
                // TODO: used for trick play, if we're not getting progress events all the time
//                scheduleSeekbarUpdate();
            }
        });

        return rootView;
    }

    private void onPlaybackStateChanged(PlaybackStateCompat state) {
        Log.d(TAG, "onPlaybackStateChanged " + state);
        if (getActivity() == null) {
            Log.w(TAG, "onPlaybackStateChanged called when getActivity null," +
                    "this should not happen if the callback was properly unregistered. Ignoring.");
            return;
        }
        if (state == null) {
            return;
        }
        boolean enablePlay = false;
        switch (state.getState()) {
            case PlaybackStateCompat.STATE_PAUSED:
            case PlaybackStateCompat.STATE_STOPPED:
                enablePlay = true;
                break;
            case PlaybackStateCompat.STATE_ERROR:
                Log.e(TAG, "error playbackstate: " + state.getErrorMessage());
                Toast.makeText(getActivity(), state.getErrorMessage(), Toast.LENGTH_LONG).show();
                break;
            default:
                break;
        }

        if (enablePlay) {
            playPauseButton.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_play_arrow_black_36dp));
        } else {
            playPauseButton.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_pause_black_36dp));
        }

        // TODO: does this work?
        Log.d(TAG, "seekbar! state progress is " + state.getPosition() + " and state buffer is " + state.getBufferedPosition());
        seekBar.setSecondaryProgress((int) state.getBufferedPosition());
        seekBar.setProgress((int) state.getPosition());
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        MediaControllerCompat controller = getActivity().getSupportMediaController();
        if (controller != null) {
            onConnected();
        }
    }

    @Subscribe(sticky = true)
    public void onConnectedToMediaController(MediaControllerConnectedEvent e) {
        Log.d(TAG, "Received an event! " + MediaControllerConnectedEvent.class.getName());
        // Add MediaController callback so we can redraw the list when metadata changes:
        MediaControllerCompat controller = getActivity().getSupportMediaController();
        if (controller != null) {
            Log.d(TAG, "Registering callback.");
            controller.registerCallback(mediaControllerCallback);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
        MediaControllerCompat controller = getActivity().getSupportMediaController();
        if (controller != null) {
            controller.unregisterCallback(mediaControllerCallback);
        }
    }

    public void onConnected() {
        MediaControllerCompat controller = getActivity().getSupportMediaController();
        Log.d(TAG, "onConnected, mediaController==null? " + (controller == null));
        if (controller != null) {
            onMetadataChanged(controller.getMetadata());
            onPlaybackStateChanged(controller.getPlaybackState());
            controller.registerCallback(mediaControllerCallback);
        }
    }

    private void onMetadataChanged(MediaMetadataCompat metadata) {
        if (metadata == null) {
            return;
        }

        // TODO: have to set this metadata when adding the track. can we get it from the server?
        // Otherwise, need to set up event system between media player and this view.
        int duration = (int) metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
        Log.d(TAG, "updating duration to " + duration);
        seekBar.setMax(duration);
        // TODO: textviews for actual duration/progress?
//        endText.setText(DateUtils.formatElapsedTime(duration/1000));
    }


    private void playMedia() {
        MediaControllerCompat controller = getActivity().getSupportMediaController();
        if (controller != null) {
            controller.getTransportControls().play();
        }
    }

    private void pauseMedia() {
        MediaControllerCompat controller = getActivity().getSupportMediaController();
        if (controller != null) {
            controller.getTransportControls().pause();
        }
    }

    private void seekMedia(int progress) {
        MediaControllerCompat controller = getActivity().getSupportMediaController();
        if (controller != null) {
            controller.getTransportControls().seekTo(progress);
        }
    }
}
