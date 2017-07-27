package io.mstream.mstream.syncer;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import io.mstream.mstream.MetadataObject;

/**
 * Created by paul on 7/22/2017.
 */

public class DatabaseHelper extends SQLiteOpenHelper {
    // This DB is currently formatted in the exact smae fashion as the mStream DB
    // This allows us to do clever shit, like easily compare the two to find differences
    // That's why it has unnecesary columns, like 'user'

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "mstream.db";
    public static final String TABLE_NAME = "files";
    public static final String COL_ID = "ID";
    public static final String COL_TITLE = "TITLE";
    public static final String COL_ARTIST = "ARTIST";
    public static final String COL_YEAR = "YEAR";
    public static final String COL_ALBUM = "ALBUM";
    public static final String COL_PATH = "PATH";
    public static final String COL_FORMAT = "FORMAT";
    public static final String COL_TRACK = "TRACK";
    public static final String COL_DISK = "DISK";
    public static final String COL_USER = "USER"; //
    public static final String COL_FILESIZE = "FILESIZE";
    public static final String COL_FILE_CREATED_DATE = "FILE_CREATED_DATE";
    public static final String COL_MODIFIED_DATE = "FILE_MODIFIED_DATE";
    public static final String COL_HASH = "HASH";
    public static final String COL_ALBUM_ART_FILE = "ALBUM_ART_FILE";
    public static final String COL_TIMESTAMP = "TIMESTAMP";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + DatabaseHelper.TABLE_NAME + " (" +
                    DatabaseHelper.COL_ID + " INTEGER PRIMARY KEY," +
                    DatabaseHelper.COL_TITLE + " varchar DEFAULT NULL," +
                    DatabaseHelper.COL_ARTIST + " varchar DEFAULT NULL," +
                    DatabaseHelper.COL_YEAR + " int DEFAULT NULL," +
                    DatabaseHelper.COL_ALBUM + " varchar DEFAULT NULL," +
                    DatabaseHelper.COL_PATH + " TEXT NOT NULL," +
                    DatabaseHelper.COL_FORMAT + " varchar DEFAULT NULL," +
                    DatabaseHelper.COL_TRACK + " int DEFAULT NULL," +
                    DatabaseHelper.COL_DISK + " int DEFAULT NULL," +
                    DatabaseHelper.COL_USER + " varchar DEFAULT NULL," +
                    DatabaseHelper.COL_FILESIZE + " int DEFAULT NULL," +
                    DatabaseHelper.COL_FILE_CREATED_DATE + " int DEFAULT NULL," +
                    DatabaseHelper.COL_MODIFIED_DATE + " int DEFAULT NULL," +
                    DatabaseHelper.COL_HASH + " varchar DEFAULT NULL," +
                    DatabaseHelper.COL_ALBUM_ART_FILE + " TEXT DEFAULT NULL," +
                    DatabaseHelper.COL_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP)";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
        onCreate(db);
    }


    // Checks if we have a local file with a matching hash
    public String checkForHash(String hash){
        SQLiteDatabase db = this.getReadableDatabase();
        String path= "";

        Cursor response = db.rawQuery("SELECT path from " + TABLE_NAME + " WHERE hash='" + hash + "'", null );

        try {
            // TODO: check if empty and return  path
            if (response.getCount() > 0) {
                response.moveToFirst();
                path = response.getString(response.getColumnIndex(COL_PATH));
            }
            return path;

        }finally {
            response.close();
        }
    }

    public boolean addFileToDataBase(MetadataObject moo){
        SQLiteDatabase db = this.getWritableDatabase();

        // TODO: If hash doesn't exist, just hash it here

        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_HASH,moo.getSha256Hash());
        contentValues.put(COL_PATH,moo.getLocalFile());

        contentValues.put(COL_TITLE,moo.getTitle());
        contentValues.put(COL_ARTIST,moo.getArtist());
        contentValues.put(COL_ALBUM,moo.getAlbum());
        contentValues.put(COL_YEAR,moo.getYear());
        contentValues.put(COL_TRACK,moo.getTrackNumber());
        contentValues.put(COL_ALBUM_ART_FILE,moo.getAlbumArtURL());




        // Get the file local file path
        // get the hash

        // Get all other metadata

        // Save to db
        long result = db.insert(TABLE_NAME, null ,contentValues);

        if(result == -1){
            return false;
        } else{
            return true;
        }
    }
}
