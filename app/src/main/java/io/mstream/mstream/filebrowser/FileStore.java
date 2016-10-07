package io.mstream.mstream.filebrowser;

import android.content.Context;
import android.util.Log;

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

import io.mstream.mstream.MStreamApplication;
import io.mstream.mstream.serverlist.ServerStore;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Encapsulate the network portion of FileBrowserFragment
 */

public class FileStore {
    private static final String TAG = "FileStore";

    // Strings for the request
    private static final String DIR = "dir";
    private static final String FILETYPES = "filetypes";
    private static final String FILETYPES_REQUESTED = "[\"mp3\",\"flac\",\"wav\",\"ogg\"]";
    private static final String DIRPARSER_PATH = "/dirparser";

    // Strings to parse the response
    // TODO: move to own class?
    private static final String CONTENTS = "contents";
    private static final String TYPE = "type";
    private static final String PATH = "path";
    private static final String NAME = "name";

    private OkHttpClient okHttpClient;

    public FileStore(Context context) {
        okHttpClient = ((MStreamApplication) context.getApplicationContext()).getOkHttpClient();

    }

    public void getFiles(final String directory, final OnFilesReturnedListener listener) {
        RequestBody formBody = new FormBody.Builder()
                .add(DIR, directory)
                .add(FILETYPES, FILETYPES_REQUESTED)
                .build();
        final String serverUrl = ServerStore.getDefaultServer().getServerUrl();

        // TODO: This code should be replaced with a proper URL builder
        String modifiedServerUrl = serverUrl;
        // Remove slash if necessary
        if(modifiedServerUrl.charAt(modifiedServerUrl.length() - 1) == '/'){
            modifiedServerUrl = modifiedServerUrl.substring(0, modifiedServerUrl.length()-1);
        }

        Request request = new Request.Builder()
                .url(modifiedServerUrl + DIRPARSER_PATH)
                .post(formBody)
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }
                final String responseString = response.body().string();
                Log.d(TAG, responseString);
                // Parse JSON and build objects from there
                //{"path":"/","contents":[{"type":"directory","name":"FLAC"},{"type":"directory","name":"MP3"}]
                try {
                    JSONObject responseJson = new JSONObject(responseString);
                    JSONArray contents = responseJson.getJSONArray(CONTENTS);
                    ArrayList<FileItem> serverFileList = new ArrayList<>();
                    String currentPath = responseJson.getString(PATH);

                    for (int i = 0; i < contents.length(); i++) {
                        JSONObject fileJson = contents.getJSONObject(i);
                        String type = fileJson.getString(TYPE);
                        String link;
                        if (type.equals(FileItem.DIRECTORY)) {
                            // For directories use the relative directory path
                            link = currentPath + fileJson.getString(NAME) + "/";
                        } else {
                            // For music we provide the whole URL
                            // This way the playlist can handle files from multiple servers
                            String fileUrl = serverUrl + currentPath + fileJson.getString(NAME);

                            try {
                                // We need to encode the URL to handle files with special characters
                                // Thank You stack overflow
                                URL url = new URL(fileUrl);
                                URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
                                link = uri.toASCIIString();
                            } catch (MalformedURLException | URISyntaxException e) {
                                link = ""; // TODO: Better exception handling
                            }
                        }
                        String name = fileJson.getString(NAME);
                        serverFileList.add(new FileItem(name, type, link));
                    }

                    listener.onFilesReturned(serverFileList);
                } catch (JSONException e) {
                    Log.e(TAG, "Problem parsing file response json", e);
                }
            }
        });
    }

    interface OnFilesReturnedListener {
        void onFilesReturned(List<FileItem> files);
    }
}
