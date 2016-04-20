package com.laquysoft.motivetto.events;

import com.laquysoft.motivetto.model.ParcelableSpotifyObject;

/**
 * Created by joaobiriba on 20/04/16.
 */
public class SpotifyTrackFoundEvent {
    ParcelableSpotifyObject mTrack;

    public SpotifyTrackFoundEvent(ParcelableSpotifyObject track) {
        mTrack = track;
    }

    public ParcelableSpotifyObject getTrack() {
        return mTrack;
    }

    public static SpotifyTrackFoundEvent newInstance(ParcelableSpotifyObject track) {
        return new SpotifyTrackFoundEvent(track);
    }
}

