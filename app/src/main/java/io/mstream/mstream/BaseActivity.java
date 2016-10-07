package io.mstream.mstream;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
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

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import io.mstream.mstream.filebrowser.FileBrowserFragment;
import io.mstream.mstream.player.MStreamAudioService;
import io.mstream.mstream.playlist.MediaControllerConnectedEvent;
import io.mstream.mstream.playlist.PlaylistFragment;
import io.mstream.mstream.serverlist.ServerItem;
import io.mstream.mstream.serverlist.ServerListAdapter;
import io.mstream.mstream.serverlist.ServerStore;


public class BaseActivity extends AppCompatActivity {
    private static final String TAG = "BaseActivity";

    public SeekBar seekBar;
    private DrawerLayout mDrawerLayout;
    private RecyclerView navigationMenu;

    // Media Controls!
    private MediaBrowserCompat mediaBrowser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        // Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeAsUpIndicator(R.drawable.ic_settings_white_24dp);
        actionBar.setDisplayHomeAsUpEnabled(true);

        // Navigation Menu
        navigationMenu = (RecyclerView) findViewById(R.id.navigation_view);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        // Player controls
        seekBar = (SeekBar) findViewById(R.id.seek_bar);
        Button playButton = (Button) findViewById(R.id.play_button);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        // Start the Audio Service
        mediaBrowser = new MediaBrowserCompat(this, new ComponentName(this, MStreamAudioService.class),
                new MediaBrowserCompat.ConnectionCallback() {
                    @Override
                    public void onConnected() {
                        try {
                            Log.d(TAG, "MediaBrowser onConnected");
                            MediaSessionCompat.Token token = mediaBrowser.getSessionToken();
                            // This is what gives us access to everything!
                            MediaControllerCompat controller = new MediaControllerCompat(BaseActivity.this, token);
                            setSupportMediaController(controller);
                            EventBus.getDefault().postSticky(new MediaControllerConnectedEvent());
                        } catch (RemoteException e) {
                            Log.e(BaseActivity.class.getSimpleName(), "Error creating controller", e);
                        }
                    }

                    @Override
                    public void onConnectionSuspended() {
                        Log.d(TAG, "MediaBrowser connection suspended");
                        EventBus.getDefault().removeStickyEvent(MediaControllerConnectedEvent.class);
                    }

                    @Override
                    public void onConnectionFailed() {
                        Log.d(TAG, "MediaBrowser failed!!!");
                        EventBus.getDefault().removeStickyEvent(MediaControllerConnectedEvent.class);
                    }
                }, null);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mediaBrowser.connect();
        // Get the active server, if it's been changed outside this Activity
        List<ServerItem> serverItems = ServerStore.getServers();
        // If there are no servers, direct the user to add a server
        if (serverItems.isEmpty()) {
            startActivity(new Intent(this, AddServerActivity.class));
        } else {
            // Add the servers to the navigation menu
            navigationMenu.setLayoutManager(new LinearLayoutManager(this));
            ServerListAdapter adapter = new ServerListAdapter(serverItems);
            navigationMenu.setAdapter(adapter);
            changeToBrowser();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mediaBrowser.disconnect();
    }

    @Override
    public void onBackPressed() {
        // If the drawer is open when the user presses Back, close it first
        DrawerLayout drawer = ((DrawerLayout) findViewById(R.id.drawer_layout));
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            // If it's already closed, finish the activity.
            super.onBackPressed();
        }
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
    private void changeToPlaylist() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, PlaylistFragment.newInstance()).commit();
    }

    private void changeToBrowser() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, FileBrowserFragment.newInstance()).commit();
    }

    public MediaBrowserCompat getMediaBrowser() {
        return mediaBrowser;
    }
}
