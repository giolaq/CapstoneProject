package com.laquysoft.motivetto.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.format.Time;

/**
 * Created by joaobiriba on 28/02/16.
 */
public class StatsContract {

    // The "Content authority" is a name for the entire content provider, similar to the
    // relationship between a domain name and its website.  A convenient string to use for the
    // content authority is the package name for the app, which is guaranteed to be unique on the
    // device.
    public static final String CONTENT_AUTHORITY = "com.laquysoft.motivetto.app";

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Possible paths (appended to base content URI for possible URI's)
    public static final String PATH_STATS = "stats";

    // To make it easy to query for the exact date, we normalize all dates that go into
    // the database to the start of the the Julian day at UTC.
    public static long normalizeDate(long startDate) {
        // normalize the start date to the beginning of the (UTC) day
        Time time = new Time();
        time.set(startDate);
        int julianDay = Time.getJulianDay(startDate, time.gmtoff);
        return time.setJulianDay(julianDay);
    }

    /* Inner class that defines the table contents of the stats table */
    public static final class StatsEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_STATS).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_STATS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_STATS;

        // Table name
        public static final String TABLE_NAME = "stats";


        public static final String COLUMN_PLAYER_NAME = "player_name";

        public static final String COLUMN_TRACK_NAME = "track_name";

        public static final String COLUMN_TRACK_ARTIST = "track_artist";

        public static final String COLUMN_TRACK_SOLVED_TIME = "track_solved_time";

        public static final String COLUMN_TRACK_SOLVED_MOVES = "track_solved_moves";


        public static Uri buildStatsUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

}
