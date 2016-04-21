package com.laquysoft.motivetto;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.laquysoft.motivetto.common.MainThreadBus;
import com.laquysoft.motivetto.components.DaggerEventBusComponent;
import com.laquysoft.motivetto.components.EventBusComponent;
import com.laquysoft.motivetto.events.SpotifyNoTrackFoundEvent;
import com.laquysoft.motivetto.events.SpotifyTrackFoundEvent;
import com.laquysoft.motivetto.model.ParcelableSpotifyObject;
import com.laquysoft.motivetto.modules.EventBusModule;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.inject.Inject;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.ArtistSimple;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.TracksPager;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by joaobiriba on 19/04/16.
 */
public class SpotifyIntentService extends IntentService {


    private static final String LOG_TAG = SpotifyIntentService.class.getSimpleName();

    @Inject
    MainThreadBus bus;

    public static final String TOKEN = "token";

    public static final String ACTION = "com.laquysoft.motivetto.SpotifyIntentService";


    public SpotifyIntentService() {
        super("SpotifyIntentService");

        EventBusComponent component = DaggerEventBusComponent.builder().eventBusModule(new EventBusModule()).build();

        bus = component.provideMainThreadBus();
        bus.register(this);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        String token = intent.getStringExtra(TOKEN);

        retrieveSpotifyRandomSongs(token);

    }

    /**
     * Select 1 to `max` words from the words list
     */
    public String[] selectWords(int max) {
        if (max < 1) max = 1;
        int howMany = new Random().nextInt(max) + 1;
        String[] wordsList = getResources().getStringArray(R.array.words_in_track);
        int listLength = wordsList.length;
        String words[] = new String[howMany];
        for (int i = 0; i < howMany; i++) {
            int r = new Random().nextInt(listLength);
            words[i] = wordsList[r];
        }
        return words;
    }


    private void retrieveSpotifyRandomSongs(String token) {
        SpotifyApi api = new SpotifyApi();


// Most (but not all) of the Spotify Web API endpoints require authorisation.
// If you know you"ll only use the ones that don"t require authorisation you can skip this step
        api.setAccessToken(token);

        SpotifyService spotify = api.getService();

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("limit", 1);
        parameters.put("offset", new Random().nextInt(5));

        StringBuilder builder = new StringBuilder();
        for (String s : selectWords(2)) {
            builder.append(s + " ");
        }

        spotify.searchTracks(builder.toString(), parameters, new Callback<TracksPager>() {
            @Override
            public void success(TracksPager tracksPager, Response response) {
                if (tracksPager.tracks.items.size() > 0) {
                    String trackUrl = tracksPager.tracks.items.get(0).preview_url;
                    Log.d("Track success", trackUrl);
                    //playTrack(trackUrl);
                }

                if (tracksPager.tracks.items.size() <= 0) {
                    Log.d(LOG_TAG, "success() but no tracks called with: response = [" + response + "]");
                    launchNoTrackFoundEvent();

                } else {
                    String smallImageUrl = "";
                    String bigImageUrl = "";
                    for (Track track : tracksPager.tracks.items) {
                        if (!track.album.images.isEmpty()) {
                            smallImageUrl = track.album.images.get(0).url;
                        }
                        if (track.album.images.size() > 1) {
                            bigImageUrl = track.album.images.get(1).url;
                        }
                        StringBuilder builder = new StringBuilder();
                        for (ArtistSimple artist : track.artists) {
                            if (builder.length() > 0) builder.append(", ");
                            builder.append(artist.name);
                        }

                        ParcelableSpotifyObject parcelableSpotifyObject = new ParcelableSpotifyObject(track.name,
                                track.album.name,
                                builder.toString(),
                                smallImageUrl,
                                bigImageUrl,
                                track.preview_url);


                        SpotifyTrackFoundEvent event = SpotifyTrackFoundEvent.newInstance(parcelableSpotifyObject);
                        bus.post(event);

                    }
                }

            }

            @Override
            public void failure(RetrofitError error) {
                Log.d("Album failure", error.toString());
                launchNoTrackFoundEvent();
            }
        });
    }

    private void launchNoTrackFoundEvent() {
        SpotifyNoTrackFoundEvent event = SpotifyNoTrackFoundEvent.newInstance();
        bus.post(event);
    }
}
