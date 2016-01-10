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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
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
public class GameplayFragment extends Fragment implements OnClickListener,
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {

    private static final String LOG_TAG = GameplayFragment.class.getSimpleName();
    int mRequestedScore = 5000;

    static int[] MY_BUTTONS = {
            R.id.digit_button_0, R.id.digit_button_1, R.id.digit_button_2,
            R.id.digit_button_3, R.id.digit_button_4, R.id.digit_button_5,
            R.id.digit_button_6, R.id.digit_button_7, R.id.digit_button_8,
            R.id.digit_button_9, R.id.digit_button_clear, R.id.ok_score_button
    };
    private MediaPlayer mMediaPlayer;

    @Override
    public void onCompletion(MediaPlayer mp) {

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.e(LOG_TAG, "Error during Playback!");
        return false;    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.d(LOG_TAG, "Track prepared");
        mMediaPlayer.seekTo(0);
        mMediaPlayer.start();
    }

    public interface Listener {
        public void onEnteredScore(int score);

        public String onAccessToken();
    }

    Listener mListener = null;


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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_gameplay, container, false);
        for (int i : MY_BUTTONS) {
            ((Button) v.findViewById(i)).setOnClickListener(this);
        }
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
        for (String s : selectWords(4)) {
            builder.append(s + " ");
        }

        spotify.searchTracks(builder.toString(), parameters, new Callback<TracksPager>() {
            @Override
            public void success(TracksPager tracksPager, Response response) {
                if (tracksPager.tracks.items.size() > 0) {
                    String trackUrl = tracksPager.tracks.items.get(0).preview_url;
                    Log.d("Track success", trackUrl);
                    playTrack(trackUrl);
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
        switch (view.getId()) {
            case R.id.digit_button_clear:
                mRequestedScore = 0;
                updateUi();
                break;
            case R.id.digit_button_0:
            case R.id.digit_button_1:
            case R.id.digit_button_2:
            case R.id.digit_button_3:
            case R.id.digit_button_4:
            case R.id.digit_button_5:
            case R.id.digit_button_6:
            case R.id.digit_button_7:
            case R.id.digit_button_8:
            case R.id.digit_button_9:
                int x = Integer.parseInt(((Button) view).getText().toString().trim());
                mRequestedScore = (mRequestedScore * 10 + x) % 10000;
                updateUi();
                break;
            case R.id.ok_score_button:
                mListener.onEnteredScore(mRequestedScore);
                break;
        }
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

    private void playTrack(String previewUrl) {

        //Start Media Player
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnErrorListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        try {
            mMediaPlayer.setDataSource(previewUrl);
            mMediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
