package io.mstream.mstream;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Patterns;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import io.mstream.mstream.serverlist.ServerItem;
import io.mstream.mstream.serverlist.ServerStore;

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
    }

    public void addServer(View button) {
        if (validate()) {
            button.setEnabled(false);

            String name = nameText.getText().toString();
            String url = urlText.getText().toString();
            String password = passwordText.getText().toString();
            String username = usernameText.getText().toString();
            boolean isDefault = makeDefault.isChecked();

            // Create new server Item
            ServerItem newServerItem = new ServerItem.Builder(name, url)
                    .username(username)
                    .password(password)
                    .build();

            ServerStore.addServer(newServerItem);

            // TODO: Test connection to server.  Return an error if it can't connect

            // Send serverItem to the main activity to be added the list of servers
            // TODO: Check if this function returns an error?
//            boolean status = ((BaseActivity) getActivity()).addItemToServerList(newServerItem);
//            if (!status) {
//                Toast.makeText(this, "Server Name Already Exists", Toast.LENGTH_LONG).show();
//            }
            finish();
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
}
