package com.laquysoft.motivetto.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by joaobiriba on 28/02/16.
 */
/*
/**
 * Manages a local database for statistic data associated to a gamer profile.
 */
public class StatsDBHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 2;

    static final String DATABASE_NAME = "stats.db";

    public StatsDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {


        final String SQL_CREATE_STATS_TABLE = "CREATE TABLE " + StatsContract.StatsEntry.TABLE_NAME + " (" +
                StatsContract.StatsEntry._ID + " INTEGER PRIMARY KEY," +
                StatsContract.StatsEntry.COLUMN_PLAYER_NAME + " TEXT NOT NULL, " +
                StatsContract.StatsEntry.COLUMN_TRACK_NAME + " TEXT NOT NULL, " +
                StatsContract.StatsEntry.COLUMN_TRACK_ARTIST + " TEXT NOT NULL, " +
                StatsContract.StatsEntry.COLUMN_TRACK_SOLVED_TIME + " INTEGER NOT NULL, " +
                StatsContract.StatsEntry.COLUMN_TRACK_SOLVED_MOVES + " INTEGER NOT NULL " +
                StatsContract.StatsEntry.COLUMN_TRACK_HARD_MODE + " INTEGER " +
                " );";


        sqLiteDatabase.execSQL(SQL_CREATE_STATS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        onCreate(sqLiteDatabase);
    }
}