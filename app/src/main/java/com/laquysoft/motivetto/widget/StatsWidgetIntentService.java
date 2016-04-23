package com.laquysoft.motivetto.widget;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.widget.RemoteViews;

import com.laquysoft.motivetto.MainActivity;
import com.laquysoft.motivetto.R;
import com.laquysoft.motivetto.data.StatsContract;

/**
 * Created by joaobiriba on 16/04/16.
 */

public class StatsWidgetIntentService extends IntentService {

    private static final String[] STAT_COLUMNS = {
            StatsContract.StatsEntry.COLUMN_TRACK_NAME,
            StatsContract.StatsEntry.COLUMN_TRACK_SOLVED_TIME,
            StatsContract.StatsEntry.COLUMN_TRACK_SOLVED_MOVES
    };

    public StatsWidgetIntentService() {
        super("StatsWidgetIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Retrieve all of the Stats widget ids: these are the widgets we need to update
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this,
                StatsWidgetProvider.class));

        // Get stats data from the ContentProvider
        Uri statsUri = StatsContract.StatsEntry.CONTENT_URI;
        Cursor data = getContentResolver().query(statsUri, STAT_COLUMNS, null,
                null, null);
        if (data == null) {
            return;
        }
        if (!data.moveToFirst()) {
            data.close();
            return;
        }

        int solved_time = Integer.parseInt(data.getString(data.getColumnIndex(StatsContract.StatsEntry.COLUMN_TRACK_SOLVED_TIME)));
        int solved_moves = Integer.parseInt(data.getString(data.getColumnIndex(StatsContract.StatsEntry.COLUMN_TRACK_SOLVED_MOVES)));
        int points = 1000 / solved_moves * solved_time;


        while (data.moveToNext()) {
            solved_time = Integer.parseInt(data.getString(data.getColumnIndex(StatsContract.StatsEntry.COLUMN_TRACK_SOLVED_TIME)));
            solved_moves = Integer.parseInt(data.getString(data.getColumnIndex(StatsContract.StatsEntry.COLUMN_TRACK_SOLVED_MOVES)));
            points += 1000 / solved_moves * solved_time;
        }

        int total_tracks_solved = data.getCount();

        data.close();

        // Perform this loop procedure for each Today widget
        for (int appWidgetId : appWidgetIds) {
            // Find the correct layout based on the widget's width
            int widgetWidth = getWidgetWidth(appWidgetManager, appWidgetId);
            int defaultWidth = getResources().getDimensionPixelSize(R.dimen.widget_stats_default_width);
            int largeWidth = getResources().getDimensionPixelSize(R.dimen.widget_stats_large_width);
            int layoutId;
            //if (widgetWidth >= largeWidth) {
            //  layoutId = R.layout.widget_stats;
            //} else if (widgetWidth >= defaultWidth) {
            layoutId = R.layout.widget_stats;
            //} else {
            //   layoutId = R.layout.widget_today_small;
            // }
            RemoteViews views = new RemoteViews(getPackageName(), layoutId);


            // Content Descriptions for RemoteViews were only added in ICS MR1
            // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            //   setRemoteContentDescription(views, description);
            //}
            //views.setTextViewText(R.id.widget_description, description);

            views.setTextViewText(R.id.total_track_textview,
                    getResources().getString(R.string.total_tracks_solved, total_tracks_solved));

            views.setTextViewText(R.id.points_textview,
                    getResources().getString(R.string.total_points, points));

            // Create an Intent to launch MainActivity
            Intent launchIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);
            views.setOnClickPendingIntent(R.id.widget, pendingIntent);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    private int getWidgetWidth(AppWidgetManager appWidgetManager, int appWidgetId) {
        // Prior to Jelly Bean, widgets were always their default size
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
//            return getResources().getDimensionPixelSize(R.dimen.widget_today_default_width);
        }
        // For Jelly Bean and higher devices, widgets can be resized - the current size can be
        // retrieved from the newly added App Widget Options
        return getWidgetWidthFromOptions(appWidgetManager, appWidgetId);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private int getWidgetWidthFromOptions(AppWidgetManager appWidgetManager, int appWidgetId) {
        Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);
        if (options.containsKey(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)) {
            int minWidthDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
            // The width returned is in dp, but we'll convert it to pixels to match the other widths
            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, minWidthDp,
                    displayMetrics);
        }
        return getResources().getDimensionPixelSize(R.dimen.widget_stats_default_width);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
    private void setRemoteContentDescription(RemoteViews views, String description) {
        views.setContentDescription(R.id.widget_icon, description);
    }
}