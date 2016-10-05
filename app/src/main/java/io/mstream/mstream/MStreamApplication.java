package io.mstream.mstream;

import android.app.Application;

import io.mstream.mstream.network.OkHttpCreator;
import okhttp3.OkHttpClient;

/**
 * An Application class used for custom logic and application context.
 */

public class MStreamApplication extends Application {

    private OkHttpClient okHttpClient;

    @Override
    public void onCreate() {
        super.onCreate();
        LocalPreferences.init(this);

        okHttpClient = OkHttpCreator.getClient(getApplicationContext());
    }

    public OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }
}
