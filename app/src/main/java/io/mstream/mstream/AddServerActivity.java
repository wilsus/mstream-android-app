package io.mstream.mstream;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import io.mstream.mstream.serverlist.ServerItem;
import io.mstream.mstream.serverlist.ServerStore;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static io.mstream.mstream.R.string.url;

/**
 * An activity dedicated to adding a server to the app.
 */

public class AddServerActivity extends AppCompatActivity {
    private EditText nameText;
    private EditText urlText;
    private TextInputLayout nameTextLayout;
    private TextInputLayout urlTextLayout;
    private EditText usernameText;
    private EditText passwordText;
    private CheckBox makeDefault;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_server);

        nameText = (EditText) findViewById(R.id.input_name);
        nameTextLayout = (TextInputLayout) findViewById(R.id.input_name_layout);
        usernameText = (EditText) findViewById(R.id.input_username);
        passwordText = (EditText) findViewById(R.id.input_password);
        urlText = (EditText) findViewById(R.id.input_url);
        urlTextLayout = (TextInputLayout) findViewById(R.id.input_url_layout);
        makeDefault = (CheckBox) findViewById(R.id.make_default);

        // Add Server Button
        findViewById(R.id.add_server).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validate()) {
                    final String name = nameText.getText().toString();
                    final String url = urlText.getText().toString();
                    final String password = passwordText.getText().toString();
                    final String username = usernameText.getText().toString();
                    final boolean isDefault = makeDefault.isChecked();

                    // Check for valid url
                    if (url.isEmpty() || !Patterns.WEB_URL.matcher(url).matches()) {
                        toastIt("Invalid URL");
                        return;
                    }

                    // Initialize vars
                    Request request;
                    // v.setEnabled(false);
                    final boolean loginFlag;

                    // Handle login if necessary
                    if(username.isEmpty() && password.isEmpty()){
                        loginFlag = false;

                        String loginURL = Uri.parse(url).buildUpon().appendPath("ping").build().toString();
                        request = new Request.Builder()
                                .url(loginURL)
                                .build();
                    }else{
                        loginFlag = true;
                        JSONObject jsonObj = new JSONObject();
                        try{
                            jsonObj.put("username", username);
                            jsonObj.put("password", password);
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                        okhttp3.RequestBody body = RequestBody.create(JSON, jsonObj.toString());

                        String loginURL = Uri.parse(url).buildUpon().appendPath("login").build().toString();
                        request = new Request.Builder()
                                .url(loginURL)
                                .post(body)
                                .build();
                    }

                    // Callback
                    Callback loginCallback = new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            toastIt("Failed To Connect To Server");
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            if(response.code() != 200){
                                toastIt("Login Failed");
                            }else{
                                JSONObject responseObj;
                                String jwt = "";
                                String vPath = "";

                                // Get the vPath and JWT
                                try {
                                    responseObj = new JSONObject(response.body().string());
                                    if (responseObj.has("vPath")) {
                                        vPath = responseObj.getString("vPath");
                                    }
                                    if (responseObj.has("token")) {
                                        jwt = responseObj.getString("token");
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    toastIt("Failed to decoded server response. WTF");
                                    return;
                                }

                                toastIt("Login Success");

                                ServerItem newServerItem;

                                if(loginFlag){
                                    // Create new server Item
                                    newServerItem = new ServerItem.Builder(name, url)
                                            .username(username)
                                            .password(password)
                                            .vPath(vPath)
                                            .jwt(jwt)
                                            .isDefault(isDefault)
                                            .build();
                                }else{
                                    // Create new server Item
                                    newServerItem = new ServerItem.Builder(name, url).vPath(vPath).isDefault(isDefault).build();
                                }

                                // Add the server
                                ServerStore.addServer(newServerItem);
                                finish();
                            }
                        }
                    };

                    // Make call
                    OkHttpClient okHttpClient = ((MStreamApplication) getApplicationContext()).getOkHttpClient();
                    okHttpClient.newCall(request).enqueue(loginCallback);

                }
            }
        });

        // Test Server Button
        findViewById(R.id.test_server).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String url = urlText.getText().toString();
                String password = passwordText.getText().toString();
                String username = usernameText.getText().toString();

                if (url.isEmpty() || !Patterns.WEB_URL.matcher(url).matches()) {
                    toastIt("Invalid URL");
                    return;
                }

                Callback loginCallback = new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        toastIt("Failed To Connect To Server");
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if(response.code() != 200){
                            toastIt("Login Failed");
                        }else{
                            toastIt("Login Success");
                        }
                    }
                };


                JSONObject jsonObj = new JSONObject();
                try{
                    jsonObj.put("username", username);
                    jsonObj.put("password", password);
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                okhttp3.RequestBody body = RequestBody.create(JSON, jsonObj.toString());

                String loginURL = Uri.parse(url).buildUpon().appendPath("login").build().toString();
                Request request = new Request.Builder()
                        .url(loginURL)
                        .post(body)
                        .build();

                OkHttpClient okHttpClient = ((MStreamApplication) getApplicationContext()).getOkHttpClient();
                okHttpClient.newCall(request).enqueue(loginCallback);

            }
        });


        // If there are no servers yet, ensure the Make Default box is checked
        if (ServerStore.serverList.isEmpty()) {
            makeDefault.setChecked(true);
            makeDefault.setEnabled(false);
        }
    }

    private boolean validate() {
        boolean valid = true;
        String name = nameText.getText().toString();
        String url = urlText.getText().toString();

        if (name.isEmpty() || name.length() < 3) {
            nameTextLayout.setError(getString(R.string.server_name_valid));
            valid = false;
        } else {
            nameTextLayout.setError(null);
        }

        if (url.isEmpty() || !Patterns.WEB_URL.matcher(url).matches()) {
            urlTextLayout.setError(getString(R.string.server_url_valid));
            valid = false;
        } else {
            urlTextLayout.setError(null);
        }
        return valid;
    }


    private void toastIt(final String toastText){
        runOnUiThread(new Runnable() {
            public void run()
            {
                Toast.makeText(AddServerActivity.this, toastText, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
