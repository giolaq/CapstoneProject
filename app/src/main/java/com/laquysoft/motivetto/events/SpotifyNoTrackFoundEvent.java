package com.laquysoft.motivetto.events;

import com.laquysoft.motivetto.model.ParcelableSpotifyObject;

/**
 * Created by joaobiriba on 20/04/16.
 */
public class SpotifyNoTrackFoundEvent {

    public static SpotifyNoTrackFoundEvent newInstance() {
        return new SpotifyNoTrackFoundEvent();
    }
}
