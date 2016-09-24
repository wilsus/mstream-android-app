package io.mstream.mstream;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.design.widget.NavigationView;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;


public class BaseActivity extends AppCompatActivity {
    private static final String TAG = "BaseActivity";

    JukeboxService mJukebox;
    boolean mBounded;

    public SeekBar seekBar;
    private Handler myHandler = new Handler();

    // Server Options
    public ServerItem selectedServer = null;
    HashMap<String, ServerItem> mapOfServers = new HashMap<String, ServerItem>();
    Spinner serverSpinner;

    Toolbar toolbar;
    private DrawerLayout mDrawerLayout;

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


    public void saveServerList() {
        String jsonStr = "[";
        for (HashMap.Entry<String, ServerItem> entry : mapOfServers.entrySet()) {
            ServerItem thisItem = entry.getValue();

            jsonStr +=
                    "{" +
                            "\"name\": \"" + thisItem.getServerName() + "\"," +
                            "\"link\": \"" + thisItem.getServerLink() + "\"," +
                            "\"username\": \"" + thisItem.getServerUsername() + "\"," +
                            "\"password\": \"" + thisItem.getServerPassword() + "\"," +
                            "\"isDefault\": " + Boolean.toString(thisItem.getDefaultVal()) +
                            "},";
        }
        // Remove trailing comma
        jsonStr = jsonStr.substring(0, jsonStr.length() - 1);

        jsonStr += "]";

        // Save it
        Log.d(TAG, jsonStr);
        SharedPreferences.Editor editor = getSharedPreferences("mstream-settings", MODE_PRIVATE).edit();
        editor.putString("servers", jsonStr);
        editor.apply();
    }

    public void getServerList() {
        // get server list from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("mstream-settings", MODE_PRIVATE);
        String restoredText = prefs.getString("servers", null);

        Log.d(TAG, "PULLING LIST:");
        Log.d(TAG, restoredText);

        // if there is nothing in SharedPreferences, direct user to the ManageServersFragment
        if (restoredText == null) {
            return;
        }

        try {
            // "I want to iterate though the objects in the array..."
            JSONArray jsonArray = new JSONArray(restoredText);
//            JSONObject innerObject = outerObject.getJSONObject("JObjects");
//            JSONArray jsonArray = innerObject.getJSONArray("JArray1");
            for (int i = 0, size = jsonArray.length(); i < size; i++) {
                JSONObject objectInArray = jsonArray.getJSONObject(i);

                String name = objectInArray.getString("name");
                String url = objectInArray.getString("link");
                String username = objectInArray.getString("username");
                String password = objectInArray.getString("password");
                Boolean isDefault = objectInArray.getBoolean("isDefault");

                ServerItem newServerItem = new ServerItem(name, url, username, password);

                // If it's the default server, set it here
                if (isDefault.equals(true)) {
                    selectedServer = newServerItem;

                }

                mapOfServers.put(name, newServerItem);
            }

        } catch (JSONException e) {
            // TODO:
        }
    }

    // TODO: Test This!!!!
    // I think the default server status is still showing some bugs
    public Boolean addItemToServerList(ServerItem serverItem) {
        String newServerName = serverItem.getServerName();

        //  Check to see if server name is already being used
        if (mapOfServers.get(newServerName) != null || mapOfServers.containsKey(newServerName)) {
            return false;
        }

        // If this is marked to be the default server
        if (serverItem.getDefaultVal()) {

            // Loop through
            for (HashMap.Entry<String, ServerItem> entry : mapOfServers.entrySet()) {
                // Change all default statuses to fault
                ServerItem thisItem = entry.getValue();
                thisItem.setDefault(false);
            }
        }

        // Is this is the first server to be added, make it the default
        if (mapOfServers.isEmpty()) {
            serverItem.setDefault(true);
        }

        // Add item
        mapOfServers.put(newServerName, serverItem);

        // Save
        this.saveServerList();
        Toast.makeText(getApplicationContext(), "Server Added", Toast.LENGTH_LONG).show();

        // repopulate spinner
        populateSpinner();

        // Make this new server the current server
        selectedServer = serverItem;

        // Go to file explorer (or whatever the default view is
        changeToBrowser();

        // Check if no view has focus:
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        return true;
    }

    public void removeServerItemFromList() {
        // TODO: Find and remove item

        populateSpinner();

        // Save list
        this.saveServerList();
    }

    public void populateSpinner() {
        // TODO: Handle an empty list of servers
        String selectdeKey = null;

        ArrayList<String> listOfServerNames = new ArrayList<>();
        for (String key : mapOfServers.keySet()) {
            listOfServerNames.add(key);

            if (selectedServer != null && key.equals(selectedServer.getServerName())) {
                selectdeKey = key;
            }
        }

        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        serverSpinner = (Spinner) navigationView.getMenu().findItem(R.id.navigation_drawer_item3).getActionView();

        // serverSpinner = (Spinner) findViewById(R.id.serverSpinner);
        ArrayAdapter<String> serverSpinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listOfServerNames);
        serverSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        serverSpinner.setAdapter(serverSpinnerAdapter);
        // serverSpinner.setSelection(0);

        if (selectdeKey != null) {
            serverSpinner.setSelection(serverSpinnerAdapter.getPosition(selectdeKey));
        }

        serverSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                String serverName = serverSpinner.getSelectedItem().toString();
                selectedServer = mapOfServers.get(serverName);

                // TODO: Reload current fragment
                changeToBrowser();

                // close drawer
                mDrawerLayout.closeDrawers();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }
        });
    }


    ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceDisconnected(ComponentName name) {
            mBounded = false;
            mJukebox = null;
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            mBounded = true;
            JukeboxService.LocalBinder mLocalBinder = (JukeboxService.LocalBinder) service;
            mJukebox = mLocalBinder.getServerInstance();
        }
    };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_browser);

        // Toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeAsUpIndicator(R.drawable.ic_settings_white_24dp);
        actionBar.setDisplayHomeAsUpEnabled(true);
//
        // Navigation Menu
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {

//                menuItem.setChecked(true);
                mDrawerLayout.closeDrawers();

                if (menuItem.getItemId() == R.id.navigation_item_add_server) {
                    changeToManageServers();
                }

                return true;
            }
        });

        // setContentView(R.layout.fragment_file_browser);

        this.seekBar = (SeekBar) findViewById(R.id.seek_bar);
        // this.seekBar.setClickable(false);

        // seekBar.setOnSeekBarChangeListener(this);
        //seekBar.setEnabled(false);

        // Start the Jukebox Service
        startService(new Intent(getBaseContext(), JukeboxService.class));

        // Bind the jukebox
        Intent mIntent = new Intent(this, JukeboxService.class);
        bindService(mIntent, mConnection, Context.BIND_AUTO_CREATE);

        // On play/pause button click
        Button playButton = (Button) findViewById(R.id.play_button);
        playButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                playPause();
            }
        });

        //Broadcast Manager
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("new-track"));


// Spinner for selecting servers
//////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////
        getServerList();

        // ??? TODO: Handle no selected server
        // Might not be nexessary since the first server added is always set to default


        populateSpinner();


//////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////


        // TODO: Seekbar Change
//        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
//
//            }
////
////            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                if (mJukebox != null && fromUser) {
//                    mJukebox.seek(progress * 1000);
//                    //myHandler.postDelayed(mJukebox, 100);
//
//                }
//            }
//       });


        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
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


            // Add the ManageServersFragment is there are no servers
            if (mapOfServers.isEmpty()) {
                changeToManageServers();
            } else {
                changeToBrowser();
            }

            // TODO: Mashing the switch button crashes the app
            // TODO: Fragments don't hold their state
        }
    }


    // Functions to switch between fragments
    public void changeToPlaylist() {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new PlaylistFragment()).commit();
    }

    public void changeToBrowser() {
        if (selectedServer == null) {
            Toast.makeText(getApplicationContext(), "You need to select a server", Toast.LENGTH_LONG).show();
            return;
        }

        Bundle bundle = new Bundle();
        String server = selectedServer.getServerLink();
        bundle.putString("server", server);
        FileBrowserFragment fileBrowserFrag = new FileBrowserFragment();
        fileBrowserFrag.setArguments(bundle);

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fileBrowserFrag).commit();
    }

    public void changeToManageServers() {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ManageServersFragment()).commit();
    }


    // Listen for new song calls
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            seekBar.setMax(mJukebox.getDur());
            seekBar.setProgress(mJukebox.getPos());
            myHandler.postDelayed(UpdateSongTime, 100); // TODO: Should we call this again
        }
    };

    //
    private Runnable UpdateSongTime = new Runnable() {
        public void run() {
            int startTime = mJukebox.getPos();

            seekBar.setProgress(startTime);
            myHandler.postDelayed(this, 100);
        }
    };


    public void addTrack(ListItem selectedItem) {
        mJukebox.addTrackToPlaylist(selectedItem);
    }

    public LinkedList getPlaylist() {
        return mJukebox.getPlaylist();
    }

    public void goToSelectedTrack(ListItem item) {
        mJukebox.goToSelectedTrack(item);
    }

    public void playPause() {
        mJukebox.playPause();
    }


//    public int getDur(){
//        return mJukebox.getDur();
//    }
//
//    public int getPos(){
//        return mJukebox.getPos();
//    }
}