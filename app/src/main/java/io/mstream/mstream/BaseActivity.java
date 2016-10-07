package io.mstream.mstream;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;

import java.util.List;

import io.mstream.mstream.filebrowser.FileBrowserFragment;
import io.mstream.mstream.filebrowser.FileItem;
import io.mstream.mstream.player.MStreamAudioService;
import io.mstream.mstream.playlist.PlaylistFragment;
import io.mstream.mstream.serverlist.ServerItem;
import io.mstream.mstream.serverlist.ServerListAdapter;
import io.mstream.mstream.serverlist.ServerStore;


public class BaseActivity extends AppCompatActivity {
    private static final String TAG = "BaseActivity";

    public SeekBar seekBar;
    private Handler myHandler = new Handler();

    // Server Options
    public ServerItem selectedServer = null;

    private DrawerLayout mDrawerLayout;
    private RecyclerView navigationMenu;

    // Media Controls!
    private MediaBrowserCompat mediaBrowser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_browser);

        // Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeAsUpIndicator(R.drawable.ic_settings_white_24dp);
        actionBar.setDisplayHomeAsUpEnabled(true);
//
        // Navigation Menu
        navigationMenu = (RecyclerView) findViewById(R.id.navigation_view);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        // Player controls
        seekBar = (SeekBar) findViewById(R.id.seek_bar);
        Button playButton = (Button) findViewById(R.id.play_button);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPause();
            }
        });

        // Start the Audio Service
        mediaBrowser = new MediaBrowserCompat(this, new ComponentName(this, MStreamAudioService.class),
                new MediaBrowserCompat.ConnectionCallback() {
                    @Override
                    public void onConnected() {
                        try {
                            Log.d(TAG, "MediaBrowser onConnected");
                            // Ah, here’s our Token again
                            MediaSessionCompat.Token token = mediaBrowser.getSessionToken();
                            // This is what gives us access to everything
                            MediaControllerCompat controller = new MediaControllerCompat(BaseActivity.this, token);
                            setSupportMediaController(controller);
                        } catch (RemoteException e) {
                            Log.e(BaseActivity.class.getSimpleName(), "Error creating controller", e);
                        }
                    }

                    @Override
                    public void onConnectionSuspended() {
                        // We were connected, but no longer :-(
                        Log.d(TAG, "MediaBrowser connection suspended");
                    }

                    @Override
                    public void onConnectionFailed() {
                        // The attempt to connect failed completely.
                        // Check the ComponentName!
                        Log.d(TAG, "MediaBrowser failed!!!");
                    }
                }, null);
        mediaBrowser.connect();

        // Check that the activity is using the layout version with the fragment_container FrameLayout
        if (findViewById(R.id.fragment_container) != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }

            // Create a new Fragment to be placed in the activity layout
//            final FileBrowserFragment fileBrowserFragment = new FileBrowserFragment();
//            final PlaylistFragment playlistFragment = new PlaylistFragment();


            // In case this activity was started with special instructions from an
            // Intent, pass the Intent's extras to the fragment as arguments
//            this.fileBrowserFragment.setArguments(getIntent().getExtras());
//            this.playlistFragment.setArguments(getIntent().getExtras());

            // TODO: Mashing the switch button crashes the app
            // TODO: Fragments don't hold their state
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Get the active server, if it's been changed outside this Activity
        List<ServerItem> serverItems = ServerStore.getServers();
        // If there are no servers, direct the user to add a server
        if (serverItems.isEmpty()) {
            startActivity(new Intent(this, AddServerActivity.class));
        } else {
            // Set the selected server
            ServerItem defaultServer = ServerStore.getDefaultServer();
            if (defaultServer != null && defaultServer.getServerUrl() != null) {
                selectedServer = defaultServer;
            }
            // Add the servers to the navigation menu
            navigationMenu.setLayoutManager(new LinearLayoutManager(this));
            ServerListAdapter adapter = new ServerListAdapter(serverItems);
            navigationMenu.setAdapter(adapter);
            changeToBrowser();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaBrowser.disconnect();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.action_browser:
                changeToBrowser();
                return true;

            case R.id.action_playlist:
                changeToPlaylist();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    // Functions to switch between fragments
    public void changeToPlaylist() {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new PlaylistFragment()).commit();
    }

    public void changeToBrowser() {
        if (selectedServer == null) {
            Toast.makeText(getApplicationContext(), "You need to select a server", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, AddServerActivity.class));
            return;
        }

        Bundle bundle = new Bundle();
        String server = selectedServer.getServerUrl();
        bundle.putString("server", server);
        FileBrowserFragment fileBrowserFrag = new FileBrowserFragment();
        fileBrowserFrag.setArguments(bundle);

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fileBrowserFrag).commit();
    }

    public void addTrack(FileItem selectedItem) {
//        audioService.addTrackToPlaylist(selectedItem);
    }

//    public LinkedList getPlaylist() {
//        return audioService.getPlaylist();
//    }

    public void goToSelectedTrack(FileItem item) {
//        audioService.goToSelectedTrack(item);
    }

    public void playPause() {
        getSupportMediaController().getTransportControls().play();
    }

//    public int getDuration(){
//        return audioService.getDuration();
//    }
//
//    public int getPosition(){
//        return audioService.getPosition();
//    }
}
