package io.mstream.mstream.player;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * A BroadcastReceiver that pauses the music when e.g. headphones are disconnected
 */
public class MediaBecomingNoisyReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: pause the audio. maybe move this class into the service, or use eventbus to emit the broadcast.
    }
}
