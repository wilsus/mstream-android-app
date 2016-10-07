package io.mstream.mstream.playlist;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import io.mstream.mstream.BaseActivity;
import io.mstream.mstream.R;

/**
 * A fragment that displays the Playlist.
 */
public class PlaylistFragment extends Fragment {
    private static final String TAG = "PlaylistFragment";

    private PlaylistAdapter playlistAdapter;

    // Receive callbacks from the MediaController. Here we update our state such as which queue
    // is being shown, the current title and description and the PlaybackState.
    private final MediaControllerCompat.Callback mediaControllerCallback =
            new MediaControllerCompat.Callback() {
                @Override
                public void onMetadataChanged(MediaMetadataCompat metadata) {
                    super.onMetadataChanged(metadata);
                    if (metadata == null) {
                        return;
                    }
                    // TODO: figure out why this isn't firing
                    Log.d(TAG, "Received metadata change to media " + metadata.getDescription().getMediaId());
                    playlistAdapter.setCurrentlyPlayingItemTitle(metadata.getDescription().getTitle().toString());
                }

                @Override
                public void onPlaybackStateChanged(@NonNull PlaybackStateCompat state) {
                    super.onPlaybackStateChanged(state);
                    Log.d(TAG, "Received state change: " + state);
                }
            };

    private final MediaBrowserCompat.SubscriptionCallback subscriptionCallback =
            new MediaBrowserCompat.SubscriptionCallback() {
                @Override
                public void onChildrenLoaded(@NonNull String parentId, List<MediaBrowserCompat.MediaItem> children) {
                    try {
                        Log.d(TAG, "fragment onChildrenLoaded, parentId=" + parentId + " count=" + children.size());
                        playlistAdapter.clear();
                        for (MediaBrowserCompat.MediaItem item : children) {
                            playlistAdapter.add(item);
                        }
                    } catch (Throwable t) {
                        Log.e(TAG, "Error in onChildrenLoaded", t);
                    }
                }

                @Override
                public void onError(@NonNull String id) {
                    Log.e(TAG, "browse fragment subscription onError, id=" + id);
                    Toast.makeText(getActivity(), R.string.error_loading_media, Toast.LENGTH_LONG).show();
                }
            };

    /**
     * Use this factory method to create a new instance of this Fragment.
     * @return A new instance of fragment PlaylistFragment.
     */
    public static PlaylistFragment newInstance() {
        PlaylistFragment fragment = new PlaylistFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_playlist, container, false);

        RecyclerView mediaItemListView = (RecyclerView) view.findViewById(R.id.playlist_recycler_view);
        mediaItemListView.setLayoutManager(new LinearLayoutManager(getActivity()));
        playlistAdapter = new PlaylistAdapter(new ArrayList<MediaBrowserCompat.MediaItem>(),
                new PlaylistAdapter.OnClickMediaItem() {
                    @Override
                    public void onMediaItemClick(MediaBrowserCompat.MediaItem item) {
                        // TODO: play the item
                    }
                });
        mediaItemListView.setAdapter(playlistAdapter);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Subscribe(sticky = true)
    public void onConnectedToMediaController(MediaControllerConnectedEvent e) {
        Log.d(TAG, "Received an event! " + MediaControllerConnectedEvent.class.getName());

        // TODO: I think the ID has to be some kind of parent id??? playlist id maybe? but it's all local, right?
        ((BaseActivity) getActivity()).getMediaBrowser().subscribe("0", subscriptionCallback);

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
    }
}
