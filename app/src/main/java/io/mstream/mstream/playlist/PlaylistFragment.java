package io.mstream.mstream.playlist;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;

import io.mstream.mstream.R;

/**
 * A fragment that displays the Playlist.
 */
public class PlaylistFragment extends Fragment {
    private PlaylistAdapter playlistAdapter;

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

    @Subscribe
    public void setNewTrack(NewTrackPlayingEvent e) {
        playlistAdapter.setCurrentlyPlayingItemTitle("");
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

}
