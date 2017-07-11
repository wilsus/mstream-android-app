package io.mstream.mstream.playlist;

import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;

import java.util.List;

import io.mstream.mstream.R;

/**
 * Utility class to help on queue related tasks.
 */

class MediaUtils {

    static boolean isIndexPlayable(int index, List<MediaSessionCompat.QueueItem> queue) {
        return (queue != null && index >= 0 && index < queue.size());
    }

    static MediaMetadataCompat getMetadataFromDescription(MediaDescriptionCompat description) {
        MediaMetadataCompat.Builder builder = new MediaMetadataCompat.Builder();
        if (description.getTitle() != null) {
            builder.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, description.getTitle().toString());
        }
        if (description.getSubtitle() != null) {
            builder.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, description.getSubtitle().toString());
        }
        if (description.getDescription() != null) {
            builder.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION, description.getDescription().toString());
        }
        if (description.getMediaUri() != null) {
            builder.putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, description.getMediaUri().toString());
        }
        if (description.getMediaId() != null) {
            builder.putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, description.getMediaId());
        }
        return builder.build();
    }

    public static MediaBrowserCompat.MediaItem getMediaItemFromQueueItem(MediaSessionCompat.QueueItem queueItem) {
        return new MediaBrowserCompat.MediaItem(queueItem.getDescription(), MediaBrowserCompat.MediaItem.FLAG_PLAYABLE);
    }

    public static MediaMetadataCompat getDisplayTextTestMetadata(Resources res) {
        return new MediaMetadataCompat.Builder()
                // TODO: where does the display title show up?
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, "this is the display title")
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, "this is the display subtitle")
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION, "this is the display description")
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, "https://paulserver.mstre.am:5050/6553fd58-c032-401c-ae9d-68eb5d394c26/Feed%20Me/Feed%20Me%20-%20Calamari%20Tuesday%20[V0]/04.%20Ebb%20&%20Flow.mp3?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VybmFtZSI6InBhdWwiLCJpYXQiOjE0OTc5NzU5Mzl9.Y4B3kHhExuq0nCPMxZoxfbSibb7HbQ6S2ZDPD8ep6xA")
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, BitmapFactory.decodeResource(res, R.mipmap.ic_launcher))
                .build();
    }

    // TODO: is this the least hacky it's gonna get?
    static CharSequence titleFromFilename(String filename) {
        return Uri.decode(filename.substring(filename.lastIndexOf('/') + 1, filename.lastIndexOf('.')));
    }
}
