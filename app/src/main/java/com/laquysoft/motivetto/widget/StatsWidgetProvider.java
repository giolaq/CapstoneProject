package com.laquysoft.motivetto.widget;


import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.laquysoft.motivetto.data.StatsContract;


/**
 * Created by joaobiriba on 16/04/16.
 */
public class StatsWidgetProvider extends AppWidgetProvider {

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        context.startService(new Intent(context, StatsWidgetIntentService.class));
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager,
                                          int appWidgetId, Bundle newOptions) {
        context.startService(new Intent(context, StatsWidgetIntentService.class));
    }

    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        super.onReceive(context, intent);
        if (StatsContract.ACTION_STATS_DATA_UPDATED.equals(intent.getAction())) {
            context.startService(new Intent(context, StatsWidgetIntentService.class));
        }
    }
}