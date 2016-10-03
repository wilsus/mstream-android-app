package io.mstream.mstream;

import android.app.Application;

/**
 * An Application class used for custom logic and application context.
 */

public class MStreamApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        LocalPreferences.init(this);
    }
}
