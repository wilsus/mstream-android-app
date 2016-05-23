package io.mstream.mstream;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Bundle;

import android.os.IBinder;
import android.content.ComponentName;
import android.content.ServiceConnection;

import io.mstream.mstream.JukeboxService.LocalBinder;

import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class BaseActivity extends FragmentActivity  {
    JukeboxService mJukebox;
    boolean mBounded;

    public SeekBar seekBar;
    private Handler myHandler = new Handler();



    Spinner serverSpinner;




    // Server Options
    public ServerItem selectedServer = null;
    // public ArrayList<String> listOfServers =  new ArrayList<String>();
    HashMap <String,ServerItem> mapOfServers = new HashMap<String, ServerItem>();

    public void saveServerList(){

        String jsonStr = "[";

        for (HashMap.Entry<String, ServerItem> entry : mapOfServers.entrySet()) {
            ServerItem thisItem =  entry.getValue();

             jsonStr +=
                    "{" +
                        "\"name\": \"" + thisItem.getServerName() + "\"," +
                        "\"link\": \"" + thisItem.getServerLink() + "\"," +
                        "\"username\": \""+ thisItem.getServerUsername() +"\"," +
                        "\"password\": \""+ thisItem.getServerPassword() +"\"," +
                        "\"isDefault\": " + Boolean.toString(thisItem.getDefaultVal()) +
                    "},";
        }
        // Remove trailing comma
        jsonStr =  jsonStr.substring(0, jsonStr.length() - 1);

        jsonStr += "]";



        // Save it
        System.out.println( jsonStr );

        SharedPreferences.Editor editor = getSharedPreferences("mstream-settings", MODE_PRIVATE).edit();
        editor.putString("servers", jsonStr);
        editor.commit();
    }

    public void getServerList(){
        // get server list from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("mstream-settings", MODE_PRIVATE);
        String restoredText = prefs.getString("servers", null);

        System.out.println( "PULLING LIST:" );
        System.out.println( restoredText );


        // if there is nothing in SharedPreferences, direct user to the ManageServersFragment
        if (restoredText == null) {
            return;
        }



        try{
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
                if(isDefault.equals(true)){
                    selectedServer = newServerItem;
                }

                mapOfServers.put(name, newServerItem);
            }
        }catch( JSONException e){
            // TODO:

        }

    }

    public void addItemToServerList(ServerItem serverItem){
        // TODO: Check to see if server name is already being used


        // Add item
        mapOfServers.put(serverItem.getServerName(), serverItem);

        // repopulate spinner
        populateSpinner();

        // Save
        this.saveServerList();
    }

    public void removeServerItemFromList(){
        // TODO: Find and remove item

        populateSpinner();

        // Save list
        this.saveServerList();
    }

    public void populateSpinner(){
        // TODO: Handle an empty list of servers

        ArrayList<String> listOfServerNames =  new ArrayList<>();
        for(String key : mapOfServers.keySet() ){
            listOfServerNames.add(key);
        }

        serverSpinner = (Spinner) findViewById(R.id.serverSpinner);
        ArrayAdapter<String> serverSpinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listOfServerNames);
        serverSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        serverSpinner.setAdapter(serverSpinnerAdapter);
        // serverSpinner.setSelection(0);
        serverSpinner.setOnItemSelectedListener( new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                String serverName = serverSpinner.getSelectedItem().toString();
                selectedServer = mapOfServers.get(serverName);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }
        } );
    }



    ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceDisconnected(ComponentName name) {
            mBounded = false;
            mJukebox = null;
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            mBounded = true;
            JukeboxService.LocalBinder mLocalBinder = (JukeboxService.LocalBinder)service;
            mJukebox = mLocalBinder.getServerInstance();
        }
    };







    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_browser);

        // setContentView(R.layout.fragment_file_browser);

        this.seekBar = (SeekBar) findViewById(R.id.seekBar);
        // this.seekBar.setClickable(false);

        // seekBar.setOnSeekBarChangeListener(this);
        //seekBar.setEnabled(false);

        // Start the Jukebox Service
        startService(new Intent(getBaseContext(), JukeboxService.class));

        // Bind the jukebox
        Intent mIntent = new Intent(this, JukeboxService.class);
        bindService(mIntent, mConnection, Context.BIND_AUTO_CREATE);


        // On play/pause button click
        Button playButton = (Button) findViewById(R.id.playButton);
        playButton.setOnClickListener( new View.OnClickListener() {

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

        // TODO: Remove this
        // mapOfServers.put("Server 1", new ServerItem("Server 1", "http://209.6.75.121:3030/", null, null));

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
            if(mapOfServers.isEmpty()){
                changeToManageServers();
            }else{
                changeToBrowser();
            }


            // TODO: Mashing the switch button crashes the app
            // TODO: Fragments don't hold their state


            // File Browser Button
            ImageButton fileBrowserButton = (ImageButton) findViewById(R.id.file_browser);
            fileBrowserButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    changeToBrowser();
                }
            });

            // Playlist Button
            ImageButton playlistButton = (ImageButton) findViewById(R.id.playlist_button);
            playlistButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    changeToPlaylist();
                }
            });

            // Add Server Button
            Button addServerButton = (Button) findViewById(R.id.add_server_button);
            addServerButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    changeToManageServers();
                }
            });


        }

    }


    // Functions to switch between fragments
    public void changeToPlaylist(){
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container ,new PlaylistFragment()).commit();
    }
    public void changeToBrowser(){
        if(selectedServer == null){
            Toast.makeText(getApplicationContext(), "You need to select a server", Toast.LENGTH_LONG).show();
            return;
        }

        Bundle bundle = new Bundle();
        String server = selectedServer.getServerLink();
        bundle.putString("server", server );
        FileBrowserFragment fileBrowserFrag = new FileBrowserFragment();
        fileBrowserFrag.setArguments(bundle);

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fileBrowserFrag ).commit();
    }
    public void changeToManageServers(){
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ManageServersFragment()).commit();
    }


    // Listen for new song calls
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            seekBar.setMax( mJukebox.getDur());
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




    public void addTrack(aListItem selectedItem) {
        mJukebox.addTrackToPlaylist(selectedItem);
    }

    public LinkedList getPlaylist(){
        return mJukebox.getPlaylist();
    }

    public void goToSelectedTrack(aListItem item){
        mJukebox.goToSelectedTrack(item);
    }

    public void playPause(){
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