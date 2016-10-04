package io.mstream.mstream.filebrowser;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

import io.mstream.mstream.serverlist.ServerStore;

/**
 * Encapsulate the network portion of FileBrowserFragment
 */

public class FileStore {

    public static void callServer(Context context, final String directory, Response.Listener responseListener) {
        // Server URL
        String url = ServerStore.getDefaultServer().getServerUrl() + "/dirparser";

        // Send POST request to server
        StringRequest postRequest = new StringRequest(Request.Method.POST, url, responseListener,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {  // Hash Map of POST params
                Map<String, String> params = new HashMap<>();
                // the POST parameters:
                params.put("dir", directory);
                params.put("filetypes", "[\"mp3\",\"flac\",\"wav\",\"ogg\"]");

                return params;
            }
        };

        // Send request
        Volley.newRequestQueue(context).add(postRequest);
    }
}
