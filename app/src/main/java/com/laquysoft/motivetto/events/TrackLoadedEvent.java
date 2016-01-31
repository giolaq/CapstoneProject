package com.laquysoft.motivetto.events;

import com.laquysoft.motivetto.model.ParcelableSpotifyObject;

/**
 * Created by joaobiriba on 30/01/16.
 */
public class TrackLoadedEvent {

    ParcelableSpotifyObject mTrack;

    public TrackLoadedEvent(ParcelableSpotifyObject track) {
        mTrack = track;
    }

    public ParcelableSpotifyObject getTrack() {
        return mTrack;
    }

    public static TrackLoadedEvent newInstance(ParcelableSpotifyObject track) {
        return new TrackLoadedEvent(track);
    }
}