/*
 * Copyright (C) 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.laquysoft.motivetto;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.laquysoft.motivetto.common.MainThreadBus;
import com.laquysoft.motivetto.components.DaggerEventBusComponent;
import com.laquysoft.motivetto.components.EventBusComponent;
import com.laquysoft.motivetto.events.TrackPausedEvent;
import com.laquysoft.motivetto.events.TrackPlayingEvent;
import com.laquysoft.motivetto.model.ParcelableSpotifyObject;
import com.laquysoft.motivetto.modules.EventBusModule;
import com.squareup.otto.Subscribe;

import java.io.IOException;
import java.util.ArrayList;
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
 * Fragment for the gameplay portion of the game. It shows the keypad
 * where the user can request their score.
 *
 * @author Bruno Oliveira (Google)
 */
public class GameplayFragment extends Fragment implements OnClickListener {

    private static final String LOG_TAG = GameplayFragment.class.getSimpleName();
    int mRequestedScore = 5000;


    @Inject
    MainThreadBus bus;


    public interface Listener {
        public void onEnteredScore(int score);

        public String onAccessToken();
    }

    Listener mListener = null;

    private int trackseconds = 0;


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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBusComponent component = DaggerEventBusComponent.builder().eventBusModule(new EventBusModule()).build();
        bus = component.provideMainThreadBus();
        bus.register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_gameplay, container, false);
        return v;
    }

    public void setListener(Listener l) {
        mListener = l;
    }

    @Override
    public void onStart() {
        super.onStart();
        updateUi();
        retrieveSpotifyRandomSongs();
    }

    private void retrieveSpotifyRandomSongs() {
        SpotifyApi api = new SpotifyApi();


// Most (but not all) of the Spotify Web API endpoints require authorisation.
// If you know you"ll only use the ones that don"t require authorisation you can skip this step
        api.setAccessToken(mListener.onAccessToken());

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
                    Toast.makeText(getActivity(), "Track not found, please refine your search", Toast.LENGTH_LONG).show();
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
                        ArrayList<ParcelableSpotifyObject> tracks = new ArrayList<ParcelableSpotifyObject>();
                        tracks.add(parcelableSpotifyObject);
                        MediaPlayerService.setTracks(getContext(), tracks);

                    }
                }

            }

            @Override
            public void failure(RetrofitError error) {
                Log.d("Album failure", error.toString());
            }
        });
    }

    void updateUi() {
        if (getActivity() == null) return;
        TextView scoreInput = ((TextView) getActivity().findViewById(R.id.score_input));
        if (scoreInput != null) scoreInput.setText(String.format("%04d", mRequestedScore));
    }

    @Override
    public void onClick(View view) {
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


    @Subscribe
    public void getTrackPlaying(TrackPlayingEvent trackPlayingEvent) {
        trackseconds++;
        Log.d(LOG_TAG, "trackplay " + trackPlayingEvent.getProgress());
        if ( trackseconds == 3 ) {
            MediaPlayerService.pauseTrack(getContext());
        }

    }

    @Subscribe
    public void getTrackPaused(TrackPausedEvent trackPausedEvent) {
        trackseconds = 0;
    }

}
