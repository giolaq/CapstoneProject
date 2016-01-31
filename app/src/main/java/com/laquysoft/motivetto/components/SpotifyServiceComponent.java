package com.laquysoft.motivetto.components;

import com.laquysoft.motivetto.modules.SpotifyServiceModule;

import dagger.Component;
import kaaes.spotify.webapi.android.SpotifyService;

/**
 * Created by joaobiriba on 30/01/16.
 */
@Component(modules = {SpotifyServiceModule.class})
public interface SpotifyServiceComponent {
    SpotifyService provideSpotifyService();
}