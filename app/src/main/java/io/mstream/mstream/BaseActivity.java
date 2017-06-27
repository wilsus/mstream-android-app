package io.mstream.mstream;

import android.content.ComponentName;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import io.mstream.mstream.filebrowser.FileBrowserFragment;
import io.mstream.mstream.player.MStreamAudioService;
import io.mstream.mstream.playlist.MediaControllerConnectedEvent;
import io.mstream.mstream.playlist.PlaylistFragment;
import io.mstream.mstream.serverlist.ServerItem;
import io.mstream.mstream.serverlist.ServerListAdapter;
import io.mstream.mstream.serverlist.ServerStore;
import io.mstream.mstream.ui.ViewPagerAdapter;


public class BaseActivity extends AppCompatActivity {
    private static final String TAG = "BaseActivity";

    private DrawerLayout drawerLayout;
    private RecyclerView navigationMenu;

    // Media Controls!
    private MediaBrowserCompat mediaBrowser;

    private BaseBrowserAdapter baseBrowserAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_base);
        ServerStore.loadServers();

        // Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeAsUpIndicator(R.drawable.ic_settings_white_24dp);
        actionBar.setDisplayHomeAsUpEnabled(true);

        // Navigation Menu
        navigationMenu = (RecyclerView) findViewById(R.id.navigation_view);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        // Main navigation - viewpager for Browse and Playlist
//        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
//        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
//        adapter.addFragment(FileBrowserFragment.newInstance(), getString(R.string.browse));
//        adapter.addFragment(PlaylistFragment.newInstance(), getString(R.string.playlist));
//        viewPager.setAdapter(adapter);
//        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
//        tabLayout.setupWithViewPager(viewPager);

        // Set up the tab coloration with an initial state and a listener for other states
        // TODO: clean this up, make more robust/generic
//        int tabIconColor = ContextCompat.getColor(BaseActivity.this, R.color.medium_grey);
//        Drawable browse = ContextCompat.getDrawable(this, R.drawable.ic_folder_open_white_24dp);
//        Drawable playlist = ContextCompat.getDrawable(this, R.drawable.ic_playlist_play_white_24dp);
//        playlist.setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN);
//        tabLayout.getTabAt(0).setIcon(browse);
//        tabLayout.getTabAt(1).setIcon(playlist);
//        // Highlight selected tab in white, and deselected in grey, to match the text
//        tabLayout.addOnTabSelectedListener(
//                new TabLayout.ViewPagerOnTabSelectedListener(viewPager) {
//                    @Override
//                    public void onTabSelected(TabLayout.Tab tab) {
//                        super.onTabSelected(tab);
//                        int tabIconColor = ContextCompat.getColor(BaseActivity.this, R.color.almost_white);
//                        tab.getIcon().setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN);
//                    }
//
//                    @Override
//                    public void onTabUnselected(TabLayout.Tab tab) {
//                        super.onTabUnselected(tab);
//                        int tabIconColor = ContextCompat.getColor(BaseActivity.this, R.color.medium_grey);
//                        tab.getIcon().setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN);
//                    }
//                }
//        );

        // Add server button
        findViewById(R.id.button9).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.getContext().startActivity(new Intent(v.getContext(), AddServerActivity.class));
            }
        });


        RecyclerView filesListView = (RecyclerView) findViewById(R.id.base_recycle_view);
        filesListView.setLayoutManager(new LinearLayoutManager(this));
        baseBrowserAdapter = new BaseBrowserAdapter(new ArrayList<BaseBrowserItem>(),
                new BaseBrowserAdapter.OnClickFileItem() {
                    @Override
                    public void onDirectoryClick(String directory) {
                        //goToDirectory(directory);
                    }

                    @Override
                    public void onFileClick(BaseBrowserItem item) {
                        //addTrackToPlaylist(item);
                    }
                });
        filesListView.setAdapter(baseBrowserAdapter);


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
        // If there are no servers, direct the user to add a server
        if (ServerStore.serverList.isEmpty()) {
            startActivity(new Intent(this, AddServerActivity.class));
        } else {
            // Add the servers to the navigation menu
            navigationMenu.setLayoutManager(new LinearLayoutManager(this));
            ServerListAdapter adapter = new ServerListAdapter(ServerStore.serverList);
            navigationMenu.setAdapter(adapter);
        }

        // TODO: Check if current server has been changed.  Update browser accordingly
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
        if (item.getItemId() == android.R.id.home) {
            drawerLayout.openDrawer(GravityCompat.START);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    public MediaBrowserCompat getMediaBrowser() {
        return mediaBrowser;
    }


}
