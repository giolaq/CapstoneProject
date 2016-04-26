package com.laquysoft.motivetto;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.laquysoft.motivetto.common.MainThreadBus;
import com.laquysoft.motivetto.components.DaggerEventBusComponent;
import com.laquysoft.motivetto.components.EventBusComponent;
import com.laquysoft.motivetto.events.SpotifyNoTrackFoundEvent;
import com.laquysoft.motivetto.events.SpotifyTrackFoundEvent;
import com.laquysoft.motivetto.events.TrackPausedEvent;
import com.laquysoft.motivetto.events.TrackPlayingEvent;
import com.laquysoft.motivetto.model.ParcelableSpotifyObject;
import com.laquysoft.motivetto.modules.EventBusModule;
import com.squareup.otto.Subscribe;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javax.inject.Inject;


/**
 * Fragment for the gameplay portion of the game.
 */
public class GameplayFragment extends Fragment {

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


    private ProgressBar progressBar;

    public void incrementMovesNumber(int moves) {
        solvedMoves = moves;
        TextView movesNumber = ((TextView) getView().findViewById(R.id.moves_number));
        movesNumber.setText(getActivity().getResources().getString(R.string.moves, moves));
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
        progressBar = (ProgressBar) v.findViewById(R.id.progress);

        int minutes = 0;
        int seconds = 0;
        TextView tv =  (TextView) v.findViewById(R.id.timer);
        tv.setText(getActivity().getResources().getString(R.string.timer, minutes, seconds));

        int moves = 0;
        TextView movesNumber = ((TextView) v.findViewById(R.id.moves_number));
        movesNumber.setText(getActivity().getResources().getString(R.string.moves, moves));
        return v;
    }

    public void setListener(Listener l) {
        mListener = l;
    }

    @Override
    public void onStart() {
        super.onStart();

        progressBar.setVisibility(View.VISIBLE);

        gameBoardView.setMode(mode);

        retrieveSpotifyRandomSongs();
    }

    @Override
    public void onStop() {
        super.onStop();
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
                        tv.setText(getActivity().getResources().getString(R.string.timer, minutes, seconds));
                        seconds += 1;

                        if (seconds == 60) {
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

        Intent msgIntent = new Intent(getActivity(), SpotifyIntentService.class);

        msgIntent.putExtra(SpotifyIntentService.TOKEN, mListener.onAccessToken());
        getActivity().startService(msgIntent);

    }


    private void updateUI(ParcelableSpotifyObject track) {
        TextView trackNameTv = ((TextView) getActivity().findViewById(R.id.track_name));
        TextView trackArtistNameTv = ((TextView) getActivity().findViewById(R.id.track_artist));

        trackName = track.mName;
        trackNameTv.setText(trackName);
        trackNameTv.setVisibility(View.VISIBLE);
        trackArtistName = track.mArtistName;
        trackArtistNameTv.setText(trackArtistName);
        trackArtistNameTv.setVisibility(View.VISIBLE);

        progressBar.setVisibility(View.GONE);

        ArrayList<ParcelableSpotifyObject> tracks = new ArrayList<ParcelableSpotifyObject>();
        tracks.add(track);
        MediaPlayerService.setTracks(getActivity(), tracks);
        startTimer();

    }


    @Subscribe
    public void getTrackPlaying(TrackPlayingEvent trackPlayingEvent) {
        trackseconds++;
        Log.d(LOG_TAG, "trackplay " + trackPlayingEvent.getProgress());
        if (trackseconds == 3) {
            MediaPlayerService.pauseTrack(getActivity());
        }

    }

    @Subscribe
    public void getTrackPaused(TrackPausedEvent trackPausedEvent) {
        trackseconds = 0;
    }


    @Subscribe
    public void getSpotifyTrackFound(SpotifyTrackFoundEvent spotifyTrackFoundEvent) {
        updateUI(spotifyTrackFoundEvent.getTrack());
    }

    @Subscribe
    public void getSpotifyNoTrackFound(SpotifyNoTrackFoundEvent spotifyNoTrackFoundEvent) {
        Log.d(LOG_TAG, "getSpotifyNoTrackFound: ");
        retrieveSpotifyRandomSongs();
    }


}
