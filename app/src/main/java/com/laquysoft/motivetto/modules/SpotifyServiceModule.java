package com.laquysoft.motivetto.modules;

import dagger.Module;
import dagger.Provides;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;

/**
 * Created by joaobiriba on 30/01/16.
 */
@Module
public class SpotifyServiceModule {

    @Provides
    SpotifyService provideSpotifyService() {
        return new SpotifyApi().getService();
    }
}