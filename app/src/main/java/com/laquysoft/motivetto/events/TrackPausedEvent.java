package com.laquysoft.motivetto.events;

import com.laquysoft.motivetto.model.ParcelableSpotifyObject;

/**
 * Created by joaobiriba on 31/01/16.
 */

public class TrackPausedEvent {

    ParcelableSpotifyObject mTrack;
    int mProgress;

    public TrackPausedEvent(ParcelableSpotifyObject track, int progress) {
        mTrack = track;
        mProgress = progress;
    }

    public ParcelableSpotifyObject getTrack() {
        return mTrack;
    }
    public int getProgress() {
        return mProgress;
    }

    public static TrackPausedEvent newInstance(ParcelableSpotifyObject track, int progress) {
        return new TrackPausedEvent(track, progress);
    }
}