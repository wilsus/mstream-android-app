package io.mstream.mstream;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import io.mstream.mstream.player.MStreamAudioService;
import io.mstream.mstream.playlist.MediaControllerConnectedEvent;
import io.mstream.mstream.playlist.MstreamQueueObject;
import io.mstream.mstream.playlist.QueueManager;
import io.mstream.mstream.serverlist.ServerListAdapter;
import io.mstream.mstream.serverlist.ServerStore;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class BaseActivity extends AppCompatActivity {
    private static final String TAG = "BaseActivity";

    // Left hand nav menu
    private DrawerLayout drawerLayout;
    private RecyclerView navigationMenu;
    // Media Controls!
    private MediaBrowserCompat mediaBrowser;
    // Main browser
    private BaseBrowserAdapter baseBrowserAdapter;
    // Playlist Adapter
    private QueueAdapter queueAdapter;

    // Player buttons
    private ImageButton playPauseButton;
    private SeekBar seekBar;
    private ImageButton nextButton;
    private ImageButton previousButton;

    private TextView timeLeftText;

    // Search
    private  EditText searchBrowser;

    private ArrayList<BaseBrowserItem> baseBrowserList = new ArrayList<>(); // This is what gets plugged into the adapter
    private ArrayList<BaseBrowserItem> backupBrowserList = new ArrayList<>(); // Stores the full array for searches


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

        // Play/pause button
        playPauseButton = (ImageButton) findViewById(R.id.play_pause);
        playPauseButton.setEnabled(true);
        playPauseButton.setOnClickListener(playPauseButtonListener);

        // Next Button
        nextButton = (ImageButton) findViewById(R.id.next_song);
        nextButton.setEnabled(true);
        nextButton.setOnClickListener(nextButtonListener);

        // Previous Button
        previousButton = (ImageButton) findViewById(R.id.previous_song);
        previousButton.setEnabled(true);
        previousButton.setOnClickListener(previousButtonListener);

        // Time left text
        // timeLeftText = (TextView) findViewById(R.id.time_left_text);

        // Search
        searchBrowser = (EditText) findViewById(R.id.search_response);
        searchBrowser.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                // you can call or do what you want with your EditText here
                String matchString = searchBrowser.getText().toString();

                if(matchString.isEmpty()){
                    baseBrowserList.clear();
                    for (int i=0; i<backupBrowserList.size(); i++) {                        // Add to array if match
                        baseBrowserList.add(backupBrowserList.get(i));
                    }
                }else{
                    //
                    baseBrowserList.clear();

                    // Loop through
                    for (int i=0; i<backupBrowserList.size(); i++) {                        // Add to array if match
                        if(backupBrowserList.get(i).getItemText1().toLowerCase().contains(matchString.toLowerCase())){
                            baseBrowserList.add(backupBrowserList.get(i));
                        }
                    }

                    // Add
                }
                baseBrowserAdapter.clear();
                baseBrowserAdapter.add(baseBrowserList);
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        // Seekbar
        seekBar = (SeekBar) findViewById(R.id.seek_bar);
        seekBar.setPadding(0, 0, 0, 0);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //timeLeftText.setText(DateUtils.formatElapsedTime((seekBar.getMax() - progress) / 1000));
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
        // TODO: remove, used only for testing particular hardcoded track
        seekBar.setMax(192470);

        // Add server button
        findViewById(R.id.button9).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                ((EditText) findViewById(R.id.search_response)).getText().clear();
                ((EditText) findViewById(R.id.search_response)).clearFocus();
                v.getContext().startActivity(new Intent(v.getContext(), AddServerActivity.class));
            }
        });

        // Files Button
        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                ((EditText) findViewById(R.id.search_response)).getText().clear();
                ((EditText) findViewById(R.id.search_response)).clearFocus();
                getFiles("");
            }
        });

        // Artists Button
        findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                ((EditText) findViewById(R.id.search_response)).getText().clear();
                ((EditText) findViewById(R.id.search_response)).clearFocus();
                getArtists();
            }
        });

        // Artists Button
        findViewById(R.id.button3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                ((EditText) findViewById(R.id.search_response)).getText().clear();
                ((EditText) findViewById(R.id.search_response)).clearFocus();
                getAlbums();
            }
        });

        // Playlists button
        findViewById(R.id.button4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                ((EditText) findViewById(R.id.search_response)).getText().clear();
                ((EditText) findViewById(R.id.search_response)).clearFocus();
                getPlaylists();
            }
        });

        // Add all button
        findViewById(R.id.button6).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Loop through
                for (int i=0; i<baseBrowserList.size(); i++) {                        // Add to array if match
                    MstreamQueueObject mqo = new MstreamQueueObject();
                    mqo.setMetadata(baseBrowserList.get(i).getMetadata());
                    mqo.constructQueueItem();
                    // addIt(mqo.getQueueItem());
                    QueueManager.addToQueue3(mqo.getQueueItem());
                }
                // Add all
                queueAdapter.clear();
                queueAdapter.add(QueueManager.getIt());
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

        // TODO: What exactly is this?
        EventBus.getDefault().register(this);
        MediaControllerCompat controller = getSupportMediaController();
        if (controller != null) {
            onConnected();
        }
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

        // Queue Adapter
        RecyclerView queueView = (RecyclerView) findViewById(R.id.queue_recycler);
        queueView.setLayoutManager(new LinearLayoutManager(this));
        queueAdapter = new QueueAdapter(new ArrayList<MediaSessionCompat.QueueItem>(), new QueueAdapter.OnClickQueueItem() {
            @Override
            public void onQueueClick(MediaSessionCompat.QueueItem item, int itemPos){
                // Go To Song
                // TODO
                QueueManager.goToQueuePosition(itemPos);
                // QueueManager.updateMetadata();

                // Play
                playMedia();

                // update view
                queueAdapter.notifyDataSetChanged();
            }
        });
        queueAdapter.clear();
        queueAdapter.add(QueueManager.getIt()); // TODO
        queueView.setAdapter(queueAdapter);


        // Browser Adapter
        RecyclerView filesListView = (RecyclerView) findViewById(R.id.base_recycle_view);
        filesListView.setLayoutManager(new LinearLayoutManager(this));

        baseBrowserAdapter = new BaseBrowserAdapter(new ArrayList<BaseBrowserItem>(),
                new BaseBrowserAdapter.OnClickFileItem() {
                    @Override
                    public void onDirectoryClick(BaseBrowserItem item) {
                        //goToDirectory(directory);
                        View view = getCurrentFocus();
                        if (view != null) {
                            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        }
                        ((EditText) findViewById(R.id.search_response)).getText().clear();
                        ((EditText) findViewById(R.id.search_response)).clearFocus();

                        getFiles(item.getTypeProp());
                    }

                    @Override
                    public void onFileClick(BaseBrowserItem item) {
                        //addTrackToPlaylist(item);
                        // TODO: Add file to queue and retrieve metadata
                        MediaControllerCompat controller = getSupportMediaController();
                        if (controller != null) {
                            // QueueManager.addToQueue(item.getMetadata()); // TODO
//                            queueAdapter.clear();
//                            queueAdapter.add(QueueManager.getInstance());
                            // TODO: why doesn't `queueAdapter.notifyDataSetChanged();` work here
                            MstreamQueueObject mqo = new MstreamQueueObject();
                            mqo.setMetadata(item.getMetadata());
                            mqo.constructQueueItem();
                            // addIt(mqo.getQueueItem());
                            QueueManager.addToQueue3(mqo.getQueueItem());

                            queueAdapter.clear();
                            queueAdapter.add(QueueManager.getIt());
                        }
                    }

                    @Override
                    public void onArtistClick(BaseBrowserItem item) {
                        //addTrackToPlaylist(item);
                        View view = getCurrentFocus();
                        if (view != null) {
                            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        }
                        ((EditText) findViewById(R.id.search_response)).getText().clear();
                        ((EditText) findViewById(R.id.search_response)).clearFocus();

                        getArtistsAlbums(item.getTypeProp());
                    }

                    @Override
                    public void onAlbumClick(BaseBrowserItem item) {
                        //addTrackToPlaylist(item);
                        View view = getCurrentFocus();
                        if (view != null) {
                            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        }
                        ((EditText) findViewById(R.id.search_response)).getText().clear();
                        ((EditText) findViewById(R.id.search_response)).clearFocus();

                        getAlbumSongs(item.getTypeProp());
                    }
                });
        baseBrowserAdapter.clear();
        baseBrowserAdapter.add(baseBrowserList); // TODO
        filesListView.setAdapter(baseBrowserAdapter);

        // TODO: Check if current server has been changed.  Update browser accordingly

        // TODO: Store state so when the user reopens the app it goes to the same state
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

    public void getPlaylists(){
        String loginURL = Uri.parse(ServerStore.currentServer.getServerUrl()).buildUpon().appendPath("playlist").appendPath("getall").build().toString();
        Request request = new Request.Builder()
                .url(loginURL)
                .addHeader("x-access-token", ServerStore.currentServer.getServerJWT())
                .build();

        // Callback
        Callback loginCallback = new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                toastIt("Failed To Connect To Server");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.code() != 200){
                    toastIt("Files Failed");
                }else{
                    // Get the vPath and JWT
                    try {
                        JSONArray contents = new JSONArray(response.body().string());
                        // JSONArray contents = responseJson.getJSONArray("albums");
                        // final ArrayList<BaseBrowserItem> serverFileList = new ArrayList<>();
                        baseBrowserList.clear();
                        backupBrowserList.clear();

                        for (int i = 0; i < contents.length(); i++) {
                            String playlist = contents.getString(i);
                            JSONObject responseJson = new JSONObject(playlist);
                            playlist = responseJson.getString("name");

                            // For directories use the relative directory path
                            baseBrowserList.add(new BaseBrowserItem.Builder("playlist", playlist, playlist).build());
                            backupBrowserList.add(new BaseBrowserItem.Builder("playlist", playlist, playlist).build());
                        }

                        addIt(baseBrowserList);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        toastIt("Failed to decoded server response. WTF");
                    }
                }
            }
        };

        // Make call
        OkHttpClient okHttpClient = ((MStreamApplication) getApplicationContext()).getOkHttpClient();
        okHttpClient.newCall(request).enqueue(loginCallback);
    }

    public void getAlbums(){
        String loginURL = Uri.parse(ServerStore.currentServer.getServerUrl()).buildUpon().appendPath("db").appendPath("albums").build().toString();
        Request request = new Request.Builder()
                .url(loginURL)
                .addHeader("x-access-token", ServerStore.currentServer.getServerJWT())
                .build();

        // Callback
        Callback loginCallback = new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                toastIt("Failed To Connect To Server");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.code() != 200){
                    toastIt("Files Failed");
                }else{
                    // Get the vPath and JWT
                    try {
                        JSONObject responseJson = new JSONObject(response.body().string());
                        JSONArray contents = responseJson.getJSONArray("albums");
                        // final ArrayList<BaseBrowserItem> serverFileList = new ArrayList<>();
                        baseBrowserList.clear();
                        backupBrowserList.clear();


                        for (int i = 0; i < contents.length(); i++) {
                            String artist = contents.getString(i);

                            // For directories use the relative directory path
                            baseBrowserList.add(new BaseBrowserItem.Builder("album", artist, artist).build());
                            backupBrowserList.add(new BaseBrowserItem.Builder("album", artist, artist).build());
                        }

                        addIt(baseBrowserList);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        toastIt("Failed to decoded server response. WTF");
                    }
                }
            }
        };

        // Make call
        OkHttpClient okHttpClient = ((MStreamApplication) getApplicationContext()).getOkHttpClient();
        okHttpClient.newCall(request).enqueue(loginCallback);
    }

    public void getArtists(){
        String loginURL = Uri.parse(ServerStore.currentServer.getServerUrl()).buildUpon().appendPath("db").appendPath("artists").build().toString();
        Request request = new Request.Builder()
                .url(loginURL)
                .addHeader("x-access-token", ServerStore.currentServer.getServerJWT())
                .build();

        // Callback
        Callback loginCallback = new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                toastIt("Failed To Connect To Server");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.code() != 200){
                    toastIt("Files Failed");
                }else{
                    // Get the vPath and JWT
                    try {
                        JSONObject responseJson = new JSONObject(response.body().string());
                        JSONArray contents = responseJson.getJSONArray("artists");
                        // final ArrayList<BaseBrowserItem> serverFileList = new ArrayList<>();
                        baseBrowserList.clear();
                        backupBrowserList.clear();


                        for (int i = 0; i < contents.length(); i++) {
                            String artist = contents.getString(i);

                            // For directories use the relative directory path
                            baseBrowserList.add(new BaseBrowserItem.Builder("artist", artist, artist).build());
                            backupBrowserList.add(new BaseBrowserItem.Builder("artist", artist, artist).build());
                        }

                        addIt(baseBrowserList);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        toastIt("Failed to decoded server response. WTF");
                    }
                }
            }
        };

        // Make call
        OkHttpClient okHttpClient = ((MStreamApplication) getApplicationContext()).getOkHttpClient();
        okHttpClient.newCall(request).enqueue(loginCallback);
    }

    public void getArtistsAlbums(String artist){
        JSONObject jsonObj = new JSONObject();
        try{
            jsonObj.put("artist", artist);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        okhttp3.RequestBody body = RequestBody.create(JSON, jsonObj.toString());

        String loginURL = Uri.parse(ServerStore.currentServer.getServerUrl()).buildUpon().appendPath("db").appendPath("artists-albums").build().toString();
        Request request = new Request.Builder()
                .url(loginURL)
                .addHeader("x-access-token", ServerStore.currentServer.getServerJWT())
                .post(body)
                .build();

        // Callback
        Callback loginCallback = new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                toastIt("Failed To Connect To Server");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.code() != 200){
                    toastIt("Files Failed");
                }else{
                    // Get the vPath and JWT
                    try {
                        JSONObject responseJson = new JSONObject(response.body().string());
                        JSONArray contents = responseJson.getJSONArray("albums");
                        // final ArrayList<BaseBrowserItem> serverFileList = new ArrayList<>();
                        baseBrowserList.clear();
                        backupBrowserList.clear();

                        for (int i = 0; i < contents.length(); i++) {
                            String artist = contents.getString(i);

                            // For directories use the relative directory path
                            baseBrowserList.add(new BaseBrowserItem.Builder("album", artist, artist).build());
                            backupBrowserList.add(new BaseBrowserItem.Builder("album", artist, artist).build());
                        }

                        addIt(baseBrowserList);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        toastIt("Failed to decoded server response. WTF");
                    }
                }
            }
        };

        // Make call
        OkHttpClient okHttpClient = ((MStreamApplication) getApplicationContext()).getOkHttpClient();
        okHttpClient.newCall(request).enqueue(loginCallback);
    }

    public void getAlbumSongs(String album){
        JSONObject jsonObj = new JSONObject();
        try{
            jsonObj.put("album", album);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        okhttp3.RequestBody body = RequestBody.create(JSON, jsonObj.toString());

        String loginURL = Uri.parse(ServerStore.currentServer.getServerUrl()).buildUpon().appendPath("db").appendPath("album-songs").build().toString();
        Request request = new Request.Builder()
                .url(loginURL)
                .addHeader("x-access-token", ServerStore.currentServer.getServerJWT())
                .post(body)
                .build();

        // Callback
        Callback loginCallback = new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                toastIt("Failed To Connect To Server");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.code() != 200){
                    toastIt("Files Failed");
                }else{
                    // Get the vPath and JWT
                    try {
                        // JSONObject responseJson = new JSONObject(response.body().string());
                        JSONArray contents = new JSONArray(response.body().string());
                        // final ArrayList<BaseBrowserItem> serverFileList = new ArrayList<>();
                        baseBrowserList.clear();
                        backupBrowserList.clear();


                        for (int i = 0; i < contents.length(); i++) {
                            JSONObject fileJson = contents.getJSONObject(i);
                            String filepath = fileJson.getString("filepath");
                            String link;

                            // For music we provide the whole URL
                            // This way the playlist can handle files from multiple servers
                            // String fileUrl = serverUrl + currentPath + fileJson.getString("name");
                            String fileUrl = Uri.parse(ServerStore.currentServer.getServerUrl()).buildUpon().appendPath(ServerStore.currentServer.getServerVPath()).build().toString();
                            if(fileUrl.charAt(fileUrl.length() - 1) != '/'){
                                fileUrl = fileUrl + "/";
                            }
                            fileUrl = fileUrl + filepath;
                            fileUrl = Uri.parse(fileUrl).buildUpon().appendQueryParameter("token", ServerStore.currentServer.getServerJWT()).build().toString();

                            try {
                                // We need to encode the URL to handle files with special characters
                                // Thank You stack overflow
                                URL url = new URL(fileUrl);
                                URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
                                link = uri.toASCIIString();
                            } catch (MalformedURLException | URISyntaxException e) {
                                link = ""; // TODO: Better exception handling
                            }

                            // TODO: Better handling of metadata
                            String[] split = filepath.split("/");

                            MetadataObject tempMeta = new MetadataObject.Builder(link).build();
                            BaseBrowserItem tempItem = new BaseBrowserItem.Builder("file", link,  split[split.length-1]).metadata(tempMeta).build( );

                            baseBrowserList.add(tempItem);
                            backupBrowserList.add(tempItem);
                        }

                        addIt(baseBrowserList);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        toastIt("Failed to decoded server response. WTF");
                    }
                }
            }
        };

        // Make call
        OkHttpClient okHttpClient = ((MStreamApplication) getApplicationContext()).getOkHttpClient();
        okHttpClient.newCall(request).enqueue(loginCallback);
    }

    public void getFiles(String directroy){
        JSONObject jsonObj = new JSONObject();
        try{
            jsonObj.put("dir", directroy);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        okhttp3.RequestBody body = RequestBody.create(JSON, jsonObj.toString());

        String loginURL = Uri.parse(ServerStore.currentServer.getServerUrl()).buildUpon().appendPath("dirparser").build().toString();
        Request request = new Request.Builder()
                .url(loginURL)
                .addHeader("x-access-token", ServerStore.currentServer.getServerJWT())
                .post(body)
                .build();

        // Callback
        Callback loginCallback = new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                toastIt("Failed To Connect To Server");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.code() != 200){
                    toastIt("Files Failed");
                }else{
                    // Get the vPath and JWT
                    try {
                        JSONObject responseJson = new JSONObject(response.body().string());
                        JSONArray contents = responseJson.getJSONArray("contents");
                        // final ArrayList<BaseBrowserItem> serverFileList = new ArrayList<>();
                        baseBrowserList.clear();
                        backupBrowserList.clear();

                        String currentPath = responseJson.getString("path");

                        for (int i = 0; i < contents.length(); i++) {
                            JSONObject fileJson = contents.getJSONObject(i);
                            String type = fileJson.getString("type");
                            String link;
                            if (type.equals("directory")) {
                                // For directories use the relative directory path
                                String name = fileJson.getString("name");
                                link = currentPath + name + "/";
                                baseBrowserList.add(new BaseBrowserItem.Builder("directory", link, name).build());
                                backupBrowserList.add(new BaseBrowserItem.Builder("directory", link, name).build());

                            } else {
                                String name = fileJson.getString("name");

                                // For music we provide the whole URL
                                // This way the playlist can handle files from multiple servers
                                // String fileUrl = serverUrl + currentPath + fileJson.getString("name");
                                String fileUrl = Uri.parse(ServerStore.currentServer.getServerUrl()).buildUpon().appendPath(ServerStore.currentServer.getServerVPath()).build().toString();
                                if(fileUrl.charAt(fileUrl.length() - 1) != '/'){
                                    fileUrl = fileUrl + "/";
                                }
                                fileUrl = fileUrl + currentPath  + fileJson.getString("name");
                                fileUrl = Uri.parse(fileUrl).buildUpon().appendQueryParameter("token", ServerStore.currentServer.getServerJWT()).build().toString();

                                try {
                                    // We need to encode the URL to handle files with special characters
                                    // Thank You stack overflow
                                    URL url = new URL(fileUrl);
                                    URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
                                    link = uri.toASCIIString();
                                } catch (MalformedURLException | URISyntaxException e) {
                                    link = ""; // TODO: Better exception handling
                                }

                                MetadataObject tempMeta = new MetadataObject.Builder(link).build();
                                BaseBrowserItem tempItem = new BaseBrowserItem.Builder("file", link, name).metadata(tempMeta).build( );

                                backupBrowserList.add(tempItem);
                                baseBrowserList.add(tempItem);
                            }
                        }

                        addIt(baseBrowserList);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        toastIt("Failed to decoded server response. WTF");
                    }
                }
            }
        };

        // Make call
        OkHttpClient okHttpClient = ((MStreamApplication) getApplicationContext()).getOkHttpClient();
        okHttpClient.newCall(request).enqueue(loginCallback);
    }

    private void toastIt(final String toastText){
        runOnUiThread(new Runnable() {
            public void run()
            {
                Toast.makeText(BaseActivity.this, toastText, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addIt(final ArrayList<BaseBrowserItem> serverFileList){
        runOnUiThread(new Runnable() {
            public void run() {
                baseBrowserAdapter.clear();
                baseBrowserAdapter.add(serverFileList);
            }
        });
    }

    // Play/pause button listener
    private final View.OnClickListener nextButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            goToNextSong();
        }
    };

    // Play/pause button listener
    private final View.OnClickListener previousButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            goToPreviousSong();
        }
    };

    // Play/pause button listener
    private final View.OnClickListener playPauseButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            MediaControllerCompat controller = getSupportMediaController();
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
        }
    };

    // Receive callbacks from the MediaController. Here we update our state such as which queue
    // is being shown, the current title and description and the PlaybackState.
    private final MediaControllerCompat.Callback mediaControllerCallback = new MediaControllerCompat.Callback() {
//        @Override
//        public void onPlaybackStateChanged(@NonNull PlaybackStateCompat state) {
//            Log.d(TAG, "Received playback state change to state " + state.getState());
//            this.onPlaybackStateChanged(state);
//        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            if (metadata == null) {
                return;
            }
            Log.d(TAG, "Received metadata state change to mediaId=" + metadata.getDescription().getMediaId() +
                    " song=" + metadata.getDescription().getTitle());
            // this.onMetadataChanged(metadata);
        }
    };

    private void playMedia() {
        MediaControllerCompat controller = getSupportMediaController();
        if (controller != null) {
            controller.getTransportControls().play();
        }
    }

    private void pauseMedia() {
        MediaControllerCompat controller = getSupportMediaController();
        if (controller != null) {
            controller.getTransportControls().pause();
        }
    }

    private void seekMedia(int progress) {
        MediaControllerCompat controller = getSupportMediaController();
        if (controller != null) {
            controller.getTransportControls().seekTo(progress);
        }
    }

    private void addIt(MediaSessionCompat.QueueItem xxx){
        MediaControllerCompat controller = getSupportMediaController();
        Bundle bbb = new Bundle();
        bbb.putString("lol", xxx.getDescription().getMediaUri().toString());


        if (controller != null) {
            controller.getTransportControls().sendCustomAction("addToQueue", bbb );
        }
    }

    private void goToNextSong(){
        MediaControllerCompat controller = getSupportMediaController();
        if (controller != null) {
            controller.getTransportControls().skipToNext();
            queueAdapter.notifyDataSetChanged();

        }
    }

    private void goToPreviousSong(){
        MediaControllerCompat controller = getSupportMediaController();
        if (controller != null) {
            controller.getTransportControls().skipToPrevious();
            queueAdapter.notifyDataSetChanged();

        }
    }

    public void onConnected() {
        MediaControllerCompat controller = getSupportMediaController();
        Log.d(TAG, "onConnected, mediaController==null? " + (controller == null));
        if (controller != null) {
            onMetadataChanged(controller.getMetadata());
            onPlaybackStateChanged(controller.getPlaybackState());
            controller.registerCallback(mediaControllerCallback);
        }
    }

    @Subscribe(sticky = true)
    public void onConnectedToMediaController(MediaControllerConnectedEvent e) {
        Log.d(TAG, "Received an event! " + MediaControllerConnectedEvent.class.getName());
        // Add MediaController callback so we can redraw the list when metadata changes:
        MediaControllerCompat controller = getSupportMediaController();
        if (controller != null) {
            Log.d(TAG, "Registering callback.");
            controller.registerCallback(mediaControllerCallback);
        }
    }


    private void onPlaybackStateChanged(PlaybackStateCompat state) {
        Log.d(TAG, "onPlaybackStateChanged " + state);
//        if (getActivity() == null) {
//            Log.w(TAG, "onPlaybackStateChanged called when getActivity null," +
//                    "this should not happen if the callback was properly unregistered. Ignoring.");
//            return;
//        }
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
                Toast.makeText(this, "Playback error: " + state.getErrorMessage(), Toast.LENGTH_LONG).show();
                break;
            case PlaybackStateCompat.STATE_BUFFERING:
            case PlaybackStateCompat.STATE_CONNECTING:
            case PlaybackStateCompat.STATE_FAST_FORWARDING:
            case PlaybackStateCompat.STATE_NONE:
            case PlaybackStateCompat.STATE_PLAYING:
            case PlaybackStateCompat.STATE_REWINDING:
            case PlaybackStateCompat.STATE_SKIPPING_TO_NEXT:
            case PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS:
            case PlaybackStateCompat.STATE_SKIPPING_TO_QUEUE_ITEM:
            default:
                break;
        }

        if (enablePlay) {
            playPauseButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_play_arrow_black_36dp));
        } else {
            playPauseButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_pause_black_36dp));
        }

        // TODO: does this work?
        Log.d(TAG, "seekbar! state progress is " + state.getPosition() + " and state buffer is " + state.getBufferedPosition());
        seekBar.setSecondaryProgress((int) state.getBufferedPosition());
        seekBar.setProgress((int) state.getPosition());
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
    }



    // Create Local DB
        // path, metadata, hash

    // Function that downloads file
        // Add to mstream directory
        // add entry to DB

    // Function that checks if hash is in DB

    // Function that grabs metadata from server

}
