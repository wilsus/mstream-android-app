package io.mstream.mstream;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

import io.mstream.mstream.player.MStreamAudioService;
import io.mstream.mstream.playlist.MediaControllerConnectedEvent;
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

        // Add server button
        findViewById(R.id.button9).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.getContext().startActivity(new Intent(v.getContext(), AddServerActivity.class));
            }
        });

        // Files Button
        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFiles("");
            }
        });

        // Artists Button
        findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getArtists();
            }
        });

        // Artists Button
        findViewById(R.id.button3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getAlbums();
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
        // If there are no servers, direct the user to add a server
        if (ServerStore.serverList.isEmpty()) {
            startActivity(new Intent(this, AddServerActivity.class));
        } else {
            // Add the servers to the navigation menu
            navigationMenu.setLayoutManager(new LinearLayoutManager(this));
            ServerListAdapter adapter = new ServerListAdapter(ServerStore.serverList);
            navigationMenu.setAdapter(adapter);
        }

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
                            QueueManager.addToQueue(item.getMetadata());
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
        filesListView.setAdapter(baseBrowserAdapter);

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
                    toastIt("Files Success");

                    // Get the vPath and JWT
                    try {
                        JSONObject responseJson = new JSONObject(response.body().string());
                        JSONArray contents = responseJson.getJSONArray("albums");
                        final ArrayList<BaseBrowserItem> serverFileList = new ArrayList<>();

                        for (int i = 0; i < contents.length(); i++) {
                            String artist = contents.getString(i);

                            // For directories use the relative directory path
                            serverFileList.add(new BaseBrowserItem.Builder("album", artist, artist).build());
                        }

                        addIt(serverFileList);
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
                    toastIt("Files Success");

                    // Get the vPath and JWT
                    try {
                        JSONObject responseJson = new JSONObject(response.body().string());
                        JSONArray contents = responseJson.getJSONArray("artists");
                        final ArrayList<BaseBrowserItem> serverFileList = new ArrayList<>();

                        for (int i = 0; i < contents.length(); i++) {
                            String artist = contents.getString(i);

                            // For directories use the relative directory path
                            serverFileList.add(new BaseBrowserItem.Builder("artist", artist, artist).build());
                        }

                        addIt(serverFileList);
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
                    toastIt("Files Success");

                    // Get the vPath and JWT
                    try {
                        JSONObject responseJson = new JSONObject(response.body().string());
                        JSONArray contents = responseJson.getJSONArray("albums");
                        final ArrayList<BaseBrowserItem> serverFileList = new ArrayList<>();

                        for (int i = 0; i < contents.length(); i++) {
                            String artist = contents.getString(i);

                            // For directories use the relative directory path
                            serverFileList.add(new BaseBrowserItem.Builder("album", artist, artist).build());
                        }

                        addIt(serverFileList);
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
                    toastIt("Files Success");

                    // Get the vPath and JWT
                    try {
                        // JSONObject responseJson = new JSONObject(response.body().string());
                        JSONArray contents = new JSONArray(response.body().string());
                        final ArrayList<BaseBrowserItem> serverFileList = new ArrayList<>();

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

                            // TODO: Better handleing of metadata
                            String[] split = filepath.split("/");

                            MetadataObject tempMeta = new MetadataObject.Builder(link).build();
                            BaseBrowserItem tempItem = new BaseBrowserItem.Builder("file", link,  split[split.length-1]).metadata(tempMeta).build( );

                            serverFileList.add(tempItem);
                        }

                        addIt(serverFileList);
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
                    toastIt("Files Success");

                    // Get the vPath and JWT
                    try {
                        JSONObject responseJson = new JSONObject(response.body().string());
                        JSONArray contents = responseJson.getJSONArray("contents");
                        final ArrayList<BaseBrowserItem> serverFileList = new ArrayList<>();
                        String currentPath = responseJson.getString("path");

                        for (int i = 0; i < contents.length(); i++) {
                            JSONObject fileJson = contents.getJSONObject(i);
                            String type = fileJson.getString("type");
                            String link;
                            if (type.equals("directory")) {
                                // For directories use the relative directory path
                                String name = fileJson.getString("name");
                                link = currentPath + name + "/";
                                serverFileList.add(new BaseBrowserItem.Builder("directory", link, name).build());
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

                                serverFileList.add(tempItem);
                            }
                        }

                        addIt(serverFileList);
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

}
