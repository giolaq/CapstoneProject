package com.laquysoft.motivetto;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.laquysoft.motivetto.data.StatsContract;

/**
 * Created by joaobiriba on 03/03/16.
 */
public class StatsAdapter extends RecyclerView.Adapter<StatsAdapter.StatsAdapterViewHolder> {



    private Cursor mCursor;
    final private Context mContext;
    final private StatsAdapterOnClickHandler mClickHandler;
    final private View mEmptyView;

    /**
     * Cache of the children views for a stat list item.
     */
    public class StatsAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final TextView mTrackNameView;
        public final TextView mTrackSolvedTimeView;

        public StatsAdapterViewHolder(View view) {
            super(view);
            mTrackNameView = (TextView) view.findViewById(R.id.list_item_trackname_textview);
            mTrackSolvedTimeView = (TextView) view.findViewById(R.id.list_item_tracksolvedtime_textview);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);
        }
    }

    public static interface StatsAdapterOnClickHandler {
        void onClick(Long date, StatsAdapterViewHolder vh);
    }

    public StatsAdapter(Context context, StatsAdapterOnClickHandler dh, View emptyView) {
        mContext = context;
        mClickHandler = dh;
        mEmptyView = emptyView;

    }

    /*
        This takes advantage of the fact that the viewGroup passed to onCreateViewHolder is the
        RecyclerView that will be used to contain the view, so that it can get the current
        ItemSelectionManager from the view.

        One could implement this pattern without modifying RecyclerView by taking advantage
        of the view tag to store the ItemChoiceManager.
     */
    @Override
    public StatsAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        if ( viewGroup instanceof RecyclerView ) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_stats, viewGroup, false);
            view.setFocusable(true);
            return new StatsAdapterViewHolder(view);
        } else {
            throw new RuntimeException("Not bound to RecyclerView");
        }
    }

    @Override
    public void onBindViewHolder(StatsAdapterViewHolder forecastAdapterViewHolder, int position) {
        mCursor.moveToPosition(position);
        String trackName = mCursor.getString(mCursor.getColumnIndex(StatsContract.StatsEntry.COLUMN_TRACK_NAME));
        String trackSolvedTime = mCursor.getString(mCursor.getColumnIndex(StatsContract.StatsEntry.COLUMN_TRACK_SOLVED_TIME));

        forecastAdapterViewHolder.mTrackNameView.setText(trackName);
        forecastAdapterViewHolder.mTrackSolvedTimeView.setText(trackSolvedTime);
    }

    @Override
    public int getItemCount() {
        if ( null == mCursor ) return 0;
        return mCursor.getCount();
    }

    public void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
        mEmptyView.setVisibility(getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    public Cursor getCursor() {
        return mCursor;
    }

    public void selectView(RecyclerView.ViewHolder viewHolder) {
        if ( viewHolder instanceof StatsAdapterViewHolder ) {
            StatsAdapterViewHolder vfh = (StatsAdapterViewHolder)viewHolder;
            vfh.onClick(vfh.itemView);
        }
    }
}
