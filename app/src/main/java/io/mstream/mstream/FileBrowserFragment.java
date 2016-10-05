package io.mstream.mstream;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;



import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedList;

import io.mstream.mstream.filebrowser.FileBrowserAdapter;
import io.mstream.mstream.filebrowser.FileItem;
import io.mstream.mstream.filebrowser.FileStore;
import io.mstream.mstream.serverlist.ServerStore;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

// TODO: Replace Volley with OkHTTP
// TODO: Replace the map Object with an Array

public class FileBrowserFragment extends Fragment implements FileBrowserAdapter.OnClickFileItem {
    private static final String TAG = "FileBrowserFragment";

    public LinkedList<FileItem> serverFileList;
    private LinkedList<String> directoryMap = new LinkedList<>();
    public String currentServerAddress = "";
    private RecyclerView filesListView;

    private  final OkHttpClient httpClient = new OkHttpClient();

    public FileBrowserFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment FileBrowserFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FileBrowserFragment newInstance() {
        FileBrowserFragment fragment = new FileBrowserFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        Log.d(TAG, ServerStore.getDefaultServer().getServerName());

        // Get Server
        this.currentServerAddress = ServerStore.getDefaultServer().getServerUrl();

        // Call the server
        goToDirectory("");
    }

    // TODO: Make multiple server select
    public String getCurrentServerString() {
        return this.currentServerAddress;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_file_browser, container, false);

        // Back Button click
        // TODO: should use device's back button or show a breadcrumb
        Button backButton = (Button) view.findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                goBack();
            }
        });

        // Add All Button Click
        Button addAllButton = (Button) view.findViewById(R.id.add_all);
        addAllButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                for (int i = 0; i < serverFileList.size(); i++) {
                    FileItem item = serverFileList.get(i);
                    // Don't add directories
                    if (!item.getItemType().equals(FileItem.DIRECTORY)) {
                        addTrackToPlaylist(item);
                    }
                }
            }
        });

        filesListView = (RecyclerView) view.findViewById(R.id.browse_recycler_view);
        filesListView.setLayoutManager(new LinearLayoutManager(getActivity()));

        return view;
    }

    public void addTrackToPlaylist(FileItem selectedItem) {
        ((BaseActivity) getActivity()).addTrack(selectedItem);
    }

    public void writeToList(String response, boolean goBack) {
        // Parse JSON and build objects from there
        try {
            JSONObject decodedServerObject = new JSONObject(response);
            JSONArray jObj = decodedServerObject.getJSONArray("contents");
            serverFileList = new LinkedList<>();
            String currentPath = decodedServerObject.getString("path");

            for (int i = 0; i < jObj.length(); i++) {
                JSONObject newObj = jObj.getJSONObject(i);
                String serverAddress = getCurrentServerString();
                String type = newObj.getString("type");
                String link;
                if (type.equals(FileItem.DIRECTORY)) {
                    // For directories use the relative directory path
                    link = currentPath + newObj.getString("name") + "/";
                } else {
                    // For music we provide the whole URL
                    // This way the playlist can handle files from multiple servers
                    String urlStr = serverAddress + currentPath + newObj.getString("name");

                    try {
                        // We need to encode the URL to handle files with special characters
                        // Thank You stack overflow
                        URL url = new URL(urlStr);
                        URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
                        link = uri.toASCIIString();

                    } catch (MalformedURLException | URISyntaxException e) {
                        link = ""; // TODO: Better exception handling
                    }
                }
                String name = newObj.getString("name");
                serverFileList.addLast(new FileItem(name, type, link));
                filesListView.setAdapter(new FileBrowserAdapter(serverFileList, this));
            }

            // TODO: ensure scroll position is saved
        } catch (JSONException e) {
            JSONObject decodedServerObject = new JSONObject();
            // TODO: Do Something
        }
    }

    public void goToDirectory(final String directory) {
        directoryMap.addLast(directory);
//        FileStore.callServer(getActivity().getApplicationContext(), directory, httpClient, new Response.Listener<String>() {
//            @Override
//            public void onResponse(Response response)  throws IOException {
//                final String responseString = response.body().string();
//                writeToList(responseString, false);
//            }
//        });

        callServer( directoryMap.getLast(), false);

    }

    public void goBack() {
        if (!directoryMap.getLast().equals("")) {
            directoryMap.removeLast();
//            FileStore.callServer(getActivity().getApplicationContext(), directoryMap.getLast(), httpClient , new Response.Listener<String>() {
//                @Override
//                public void onResponse(Response response)  throws IOException {
//                    final String responseString = response.body().string();
//                    writeToList(responseString, true);
//                }
//            });

            callServer( directoryMap.getLast(), true);

        }
    }




    public void callServer(final String directory, final Boolean goBack)  {

        RequestBody formBody = new FormBody.Builder()
                .add("dir", directory)
                .add("filetypes", "[\"mp3\",\"flac\",\"wav\",\"ogg\"]")
                .build();

        Request request = new Request.Builder()
                .url(getCurrentServerString() + "dirparser")
                .post(formBody)
                .build();



        httpClient.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);


                final String responseString = response.body().string();

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        writeToList(responseString, goBack);
                    }
                });

            }
        });

    }



    @Override
    public void onDirectoryClick(String directory) {
        goToDirectory(directory);
    }

    @Override
    public void onFileClick(FileItem item) {
        addTrackToPlaylist(item);
    }
}
