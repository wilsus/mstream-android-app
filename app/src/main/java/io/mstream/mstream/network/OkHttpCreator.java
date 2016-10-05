package io.mstream.mstream.network;

import android.content.Context;

import java.io.File;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * A class that creates our OkHttpClient
 */

public class OkHttpCreator {
    //The name of the directory used for http caching
    private static final String CACHE_DIR = "httpCache";

    // The size of the http cache (10mb)
    private static final int CACHE_SIZE = 10 * 1024 * 1024;

    private OkHttpCreator() {
    }

    public static OkHttpClient getClient(Context context) {
        File baseCacheDir = context.getCacheDir();
        File cacheDir = new File(baseCacheDir, CACHE_DIR);

        // Add an interceptor to log all requests and responses
        // NOTE: This needs to be the last interceptor added, so other interceptors will execute first
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);

        return new OkHttpClient.Builder()
                // Set a short timeout on connects and reads
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                // Configure caching
                .cache(new Cache(cacheDir, CACHE_SIZE))
                .addInterceptor(loggingInterceptor)
                .build();
    }
}
