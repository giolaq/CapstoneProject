package com.laquysoft.motivetto;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.laquysoft.motivetto.model.ParcelableSpotifyObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

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
    /**
     * Words list to select from
     */
    String wordsList[] = {
            "Help",
            "Love",
            "Hate",
            "Desperate",
            "Open",
            "Close",
            "Baby",
            "Girl",
            "Yeah",
            "Whoa",
            "Start",
            "Finish",
            "Beginning",
            "End",
            "Fight",
            "War",
            "Running",
            "Want",
            "Need",
            "Fire",
            "Myself",
            "Alive",
            "Life",
            "Dead",
            "Death",
            "Kill",
            "Different",
            "Alone",
            "Lonely",
            "Darkness",
            "Home",
            "Gone",
            "Break",
            "Heart",
            "Floating",
            "Searching",
            "Dreaming",
            "Serenity",
            "Star",
            "Recall",
            "Think",
            "Feel",
            "Slow",
            "Speed",
            "Fast",
            "World",
            "Work",
            "Miss",
            "Stress",
            "Please",
            "More",
            "Less",
            "only",
            "World",
            "Moving",
            "lasting",
            "Rise",
            "Save",
            "Wake",
            "Over",
            "High",
            "Above",
            "Taking",
            "Go",
            "Why",
            "Before",
            "After",
            "Along",
            "See",
            "Hear",
            "Feel",
            "Change",
            "Body",
            "Being",
            "Soul",
            "Spirit",
            "God",
            "Angel",
            "Devil",
            "Demon",
            "Believe",
            "Away",
            "Everything",
            "Shared",
            "Something",
            "Everything",
            "Control",
            "Heart",
            "Away",
            "Waiting",
            "Loyalty",
            "Shared",
            "Remember",
            "Yesterday",
            "Today",
            "Tomorrow",
            "Fall",
            "Memories",
            "Apart",
            "Time",
            "Forever",
            "Breath",
            "Lie",
            "Sleep",
            "Inside",
            "Outside",
            "Catch",
            "Be",
            "Pretending"
    };

    public static final String TOKEN = "token";

    public static final String ACTION = "com.laquysoft.motivetto.SpotifyIntentService";


    public SpotifyIntentService() {
        super("SpotifyIntentService");
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
        int listLength = wordsList.length;
        String words[] = new String[howMany];
        for (int i = 0; i < howMany; i++) {
            int r = new Random().nextInt(listLength - 0 + 1);
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

                    Intent broadcastIntent = new Intent();
                    broadcastIntent.setAction(GameplayFragment.MySpotifyServiceReceiver.PROCESS_RESPONSE_NO_TRACK);
                    broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
                    sendBroadcast(broadcastIntent);
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


                        Intent broadcastIntent = new Intent();
                        broadcastIntent.setAction(GameplayFragment.MySpotifyServiceReceiver.PROCESS_RESPONSE);
                        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
                        broadcastIntent.putExtra("spotifyobject", parcelableSpotifyObject);
                        sendBroadcast(broadcastIntent);

                    }
                }

            }

            @Override
            public void failure(RetrofitError error) {
                Log.d("Album failure", error.toString());
            }
        });
    }

}
