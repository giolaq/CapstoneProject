package com.laquysoft.motivetto.events;

import com.laquysoft.motivetto.model.ParcelableSpotifyObject;

/**
 * Created by joaobiriba on 30/01/16.
 */
public class TrackPlayingEvent {

    ParcelableSpotifyObject mTrack;
    int mProgress;

    public TrackPlayingEvent(ParcelableSpotifyObject track, int progress) {
        mTrack = track;
        mProgress = progress;
    }

    public ParcelableSpotifyObject getTrack() {
        return mTrack;
    }
    public int getProgress() {
        return mProgress;
    }

    public static TrackPlayingEvent newInstance(ParcelableSpotifyObject track, int progress) {
        return new TrackPlayingEvent(track, progress);
    }
}