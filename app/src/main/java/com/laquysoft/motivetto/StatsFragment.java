package com.laquysoft.motivetto;

import android.annotation.TargetApi;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.laquysoft.motivetto.data.StatsContract;

/**
 * Created by joaobiriba on 03/03/16.
 */
public class StatsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public final static String LOG_TAG = StatsFragment.class.getSimpleName();


    private RecyclerView mRecyclerView;
    private StatsAdapter mStatsAdapter;



    private static final int STAT_LOADER = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View rootView = inflater.inflate(R.layout.fragment_stats, container, false);

        // Get a reference to the RecyclerView, and attach this adapter to it.
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview_forecast);

        // Set the layout manager
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        View emptyView = rootView.findViewById(R.id.recyclerview_forecast_empty);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // The StatAdapter will take data from a source and
        // use it to populate the RecyclerView it's attached to.
        mStatsAdapter = new StatsAdapter(getActivity(), new StatsAdapter.StatsAdapterOnClickHandler() {
            @Override
            public void onClick(Long date, StatsAdapter.StatsAdapterViewHolder vh) {

            }
        }, emptyView);

        // specify an adapter (see also next example)
        mRecyclerView.setAdapter(mStatsAdapter);

       /* final View parallaxView = rootView.findViewById(R.id.parallax_bar);
        if (null != parallaxView) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);
                        int max = parallaxView.getHeight();
                        if (dy > 0) {
                            parallaxView.setTranslationY(Math.max(-max, parallaxView.getTranslationY() - dy / 2));
                        } else {
                            parallaxView.setTranslationY(Math.min(0, parallaxView.getTranslationY() - dy / 2));
                        }
                    }
                });
            }
        }*/

        final AppBarLayout appbarView = (AppBarLayout) rootView.findViewById(R.id.appbar);
        if (null != appbarView) {
            ViewCompat.setElevation(appbarView, 0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                        if (0 == mRecyclerView.computeVerticalScrollOffset()) {
                            appbarView.setElevation(0);
                        } else {
                            appbarView.setElevation(appbarView.getTargetElevation());
                        }
                    }
                });
            }
        }

        // If there's instance state, mine it for useful information.
        // The end-goal here is that the user never knows that turning their device sideways
        // does crazy lifecycle related things.  It should feel like some stuff stretched out,
        // or magically appeared to take advantage of room, but data or place in the app was never
        // actually *lost*.
        if (savedInstanceState != null) {
            //mStatsAdapter.onRestoreInstanceState(savedInstanceState);
        }

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(STAT_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }


    private static final String[] STAT_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            StatsContract.StatsEntry.TABLE_NAME + "." + StatsContract.StatsEntry._ID,
            StatsContract.StatsEntry.COLUMN_TRACK_NAME,
            StatsContract.StatsEntry.COLUMN_TRACK_SOLVED_TIME,
            StatsContract.StatsEntry.COLUMN_TRACK_SOLVED_MOVES
    };


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // This is called when a new Loader needs to be created.  This
        // fragment only uses one loader, so we don't care about checking the id.
        String[] projection = {StatsContract.StatsEntry.COLUMN_TRACK_NAME, StatsContract.StatsEntry.COLUMN_TRACK_SOLVED_TIME,
            StatsContract.StatsEntry.COLUMN_TRACK_SOLVED_MOVES};

        return new CursorLoader(getActivity(),
                StatsContract.StatsEntry.CONTENT_URI,
                STAT_COLUMNS,
                null,
                null,
                null);    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {
        mStatsAdapter.swapCursor(data);
        if (data.getCount() == 0) {
        } else {
            int count = data.getCount();
            int dateColumn = data.getColumnIndex(StatsContract.StatsEntry.COLUMN_TRACK_NAME);
            for (int i = 0; i < count; i++) {
                data.moveToPosition(i);
                Log.d(LOG_TAG, "onLoadFinished: " + data.getString(dateColumn));
            }
        }
           /* mRecyclerView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    // Since we know we're going to get items, we keep the listener around until
                    // we see Children.
                    if (mRecyclerView.getChildCount() > 0) {
                        mRecyclerView.getViewTreeObserver().removeOnPreDrawListener(this);
                        int position = mStatsAdapter.getSelectedItemPosition();
                        if (position == RecyclerView.NO_POSITION &&
                                -1 != mInitialSelectedDate) {
                            Cursor data = mStatsAdapter.getCursor();
                            int count = data.getCount();
                            int dateColumn = data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DATE);
                            for ( int i = 0; i < count; i++ ) {
                                data.moveToPosition(i);
                                if ( data.getLong(dateColumn) == mInitialSelectedDate ) {
                                    position = i;
                                    break;
                                }
                            }
                        }
                        if (position == RecyclerView.NO_POSITION) position = 0;
                        // If we don't need to restart the loader, and there's a desired position to restore
                        // to, do so now.
                        mRecyclerView.smoothScrollToPosition(position);
                        RecyclerView.ViewHolder vh = mRecyclerView.findViewHolderForAdapterPosition(position);
                        if (null != vh ) {
                            mStatsAdapter.selectView(vh);
                        }

                        return true;
                    }
                    return false;
                }
            });*/
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {
        mStatsAdapter.swapCursor(null);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != mRecyclerView) {
            mRecyclerView.clearOnScrollListeners();
        }
    }
}
