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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.laquysoft.motivetto.common.MainThreadBus;
import com.laquysoft.motivetto.components.DaggerEventBusComponent;
import com.laquysoft.motivetto.components.EventBusComponent;
import com.laquysoft.motivetto.events.TrackPausedEvent;
import com.laquysoft.motivetto.events.TrackPlayingEvent;
import com.laquysoft.motivetto.model.ParcelableSpotifyObject;
import com.laquysoft.motivetto.modules.EventBusModule;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.inject.Inject;

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


    public int seconds = 0;
    public int minutes = 0;
    private Timer t;
    private boolean isRegistered = false;
    private String trackName;
    private String trackArtistName;
    private int solvedTime;
    private int solvedMoves;

    GameBoardView gameBoardView;
    private boolean mode = false;


    MySpotifyServiceReceiver mySpotifyServiceReceiver;

    public void incrementMovesNumber(int moves) {
        solvedMoves = moves;
        TextView movesNumber = ((TextView) getView().findViewById(R.id.moves_number));
        movesNumber.setText(Integer.toString(moves));
    }

    public String getTrackName() {
        return trackName;
    }

    public String getTrackArtist() {
        return trackArtistName;
    }

    public int getSolvedTime() {
        return solvedTime;
    }

    public int getSolvedMoves() {
        return solvedMoves;
    }

    public void setMode(boolean mode) {
        this.mode = mode;
    }


    public interface Listener {
        public void onEnteredScore();

        public String onAccessToken();

        public void onIncrementMoves(int moves);

        public void onSetMode(boolean mode);
    }

    Listener mListener = null;

    private int trackseconds = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBusComponent component = DaggerEventBusComponent.builder().eventBusModule(new EventBusModule()).build();
        bus = component.provideMainThreadBus();
        if (!isRegistered) {
            bus.register(this);
            isRegistered = true;
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_gameplay, container, false);
        gameBoardView = (GameBoardView) v.findViewById(R.id.gameboard);
        return v;
    }

    public void setListener(Listener l) {
        mListener = l;
    }

    @Override
    public void onStart() {
        super.onStart();

        IntentFilter filter = new IntentFilter();
        filter.addAction(MySpotifyServiceReceiver.PROCESS_RESPONSE);
        filter.addAction(MySpotifyServiceReceiver.PROCESS_RESPONSE_NO_TRACK);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        mySpotifyServiceReceiver = new MySpotifyServiceReceiver();
        getActivity().registerReceiver(mySpotifyServiceReceiver, filter);

        gameBoardView.setMode(mode);
        updateUi();
        retrieveSpotifyRandomSongs();
    }

    @Override
    public void onStop() {
        super.onStop();

        getActivity().unregisterReceiver(mySpotifyServiceReceiver);
        t.cancel();
        t.purge();
    }

    public void startTimer() {
        minutes = 0;
        seconds = 0;
        //Declare the timer
        t = new Timer();
        //Set the schedule function and rate
        t.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                getActivity().runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        TextView tv = (TextView) getActivity().findViewById(R.id.timer);
                        tv.setText(String.valueOf(minutes) + ":" + String.valueOf(seconds));
                        seconds += 1;

                        if (seconds == 60) {
                            tv.setText(String.valueOf(minutes) + ":" + String.valueOf(seconds));

                            seconds = 0;
                            minutes = minutes + 1;

                        }


                    }

                });
            }

        }, 0, 1000);
    }


    public int stopTimer() {
        t.cancel();
        solvedTime = minutes * 60 + seconds;
        MediaPlayerService.setTrackProgressTo(getActivity(), 0);
        MediaPlayerService.playTrackWin(getActivity(), 0);
        return solvedTime;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(LOG_TAG, "onDetach: ");
        MediaPlayerService.pauseTrack(getActivity());

    }

    private void retrieveSpotifyRandomSongs() {

        Intent msgIntent = new Intent(getContext(), SpotifyIntentService.class);

        msgIntent.putExtra(SpotifyIntentService.TOKEN, mListener.onAccessToken());
        getActivity().startService(msgIntent);

    }


    void updateUi() {
        if (getActivity() == null) return;
    }

    @Override
    public void onClick(View view) {
    }

    @Subscribe
    public void getTrackPlaying(TrackPlayingEvent trackPlayingEvent) {
        trackseconds++;
        Log.d(LOG_TAG, "trackplay " + trackPlayingEvent.getProgress());
        if (trackseconds == 3) {
            MediaPlayerService.pauseTrack(getContext());
        }

    }

    @Subscribe
    public void getTrackPaused(TrackPausedEvent trackPausedEvent) {
        trackseconds = 0;
    }


    public class MySpotifyServiceReceiver extends BroadcastReceiver {

        public static final String PROCESS_RESPONSE = "com.laquysoft.motivetto.PROCESS_RESPONSE";
        public static final String PROCESS_RESPONSE_NO_TRACK = "com.laquysoft.motivetto.PROCESS_RESPONSE_NO_TRACK";

        @Override
        public void onReceive(Context context, Intent intent) {

            if ( PROCESS_RESPONSE.contains(intent.getAction())) {
                ParcelableSpotifyObject parcelableSpotifyObject = intent.getExtras().getParcelable("spotifyobject");

                TextView trackNameTv = ((TextView) getActivity().findViewById(R.id.track_name));
                TextView trackArtistNameTv = ((TextView) getActivity().findViewById(R.id.track_artist));

                trackName = parcelableSpotifyObject.mName;
                trackNameTv.setText(trackName);
                trackNameTv.setVisibility(View.VISIBLE);
                trackArtistName = parcelableSpotifyObject.mArtistName;
                trackArtistNameTv.setText(trackArtistName);
                trackArtistNameTv.setVisibility(View.VISIBLE);


                ArrayList<ParcelableSpotifyObject> tracks = new ArrayList<ParcelableSpotifyObject>();
                tracks.add(parcelableSpotifyObject);
                MediaPlayerService.setTracks(getContext(), tracks);
                startTimer();

            } else {
                Log.d(LOG_TAG, "onReceive: retry to retrieve songs");
                retrieveSpotifyRandomSongs();
            }

        }


    }


}
