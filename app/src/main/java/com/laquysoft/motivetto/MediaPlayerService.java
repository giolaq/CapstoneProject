package com.laquysoft.motivetto;

/**
 * Created by joaobiriba on 30/01/16.
 */

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.laquysoft.motivetto.common.MainThreadBus;
import com.laquysoft.motivetto.components.DaggerEventBusComponent;
import com.laquysoft.motivetto.components.EventBusComponent;
import com.laquysoft.motivetto.events.TrackLoadedEvent;
import com.laquysoft.motivetto.events.TrackPausedEvent;
import com.laquysoft.motivetto.events.TrackPlayingEvent;
import com.laquysoft.motivetto.model.ParcelableSpotifyObject;
import com.laquysoft.motivetto.modules.EventBusModule;

import java.io.IOException;
import java.util.ArrayList;

import javax.inject.Inject;

/**
 * Created by joaobiriba on 06/07/15.
 */
public class MediaPlayerService extends Service implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {

    public static final String LOG_TAG = MediaPlayerService.class.getSimpleName();

    //Available Actions
    public static final String ACTION_PLAY_TRACK = "action_play_track";
    public static final String ACTION_PAUSE_TRACK = "action_pause_track";
    public static final String ACTION_RESUME_TRACK = "action_resume_track";
    public static final String ACTION_PLAY_PREVIOUS_TRACK = "action_previous_track";
    public static final String ACTION_PLAY_NEXT_TRACK = "action_next_track";
    public static final String ACTION_SET_TRACKS = "action_add_tracks";
    public static final String ACTION_SET_TRACK_PROGRESS_TO = "action_set_track_progress_to";
    public static final String ACTION_BROADCAST_CURRENT_TRACK = "action_broadcast_current_track";
    public static final int NOTIFICATION_ID = 3000;

    //Constants
    private static final String TRACKS_LIST = "tracks_list";
    private static final String TRACK_ID = "track_id";
    private static final String TRACK_PROGRESS = "track_progress";
    private static final String PREF_SHOW_PLAYBACK_CONTROLS_IN_LOCKSCREEN = "pref_show_playback_controls_in_lockscreen";

    //Variables
    ParcelableSpotifyObject mCurrentTrack;
    int mCurrentTrackIndex;
    MediaPlayer mMediaPlayer;
    BroadcastTrackProgressTask mBroadcastTrackProgressTask;
    ArrayList<ParcelableSpotifyObject> mTracksList;

    @Inject
    MainThreadBus bus;

    /**
     * Constructor
     */
    public MediaPlayerService() {
        EventBusComponent component = DaggerEventBusComponent.builder().eventBusModule(new EventBusModule()).build();

        bus = component.provideMainThreadBus();
        bus.register(this);

    }

    /**
     * StartService Helpers
     */
    public static void setTracks(Context context, ArrayList<ParcelableSpotifyObject> tracksList) {
        Intent serviceIntent = new Intent(context, MediaPlayerService.class);
        serviceIntent.setAction(ACTION_SET_TRACKS);
        serviceIntent.putExtra(TRACKS_LIST, tracksList);
        context.startService(serviceIntent);
    }

    public static void playTrack(Context context, int trackId) {
        Log.d(LOG_TAG, "play!");
        Intent serviceIntent = new Intent(context, MediaPlayerService.class);
        serviceIntent.setAction(ACTION_PLAY_TRACK);
        serviceIntent.putExtra(TRACK_ID, trackId);
        context.startService(serviceIntent);
    }

    public static void pauseTrack(Context context) {
        context.startService(getPauseTrackIntent(context));
    }

    public static Intent getPauseTrackIntent(Context context) {
        Intent serviceIntent = new Intent(context, MediaPlayerService.class);
        serviceIntent.setAction(ACTION_PAUSE_TRACK);
        return serviceIntent;
    }

    public static void resumeTrack(Context context) {
        context.startService(getResumeTrackIntent(context));
    }

    public static Intent getResumeTrackIntent(Context context) {
        Intent serviceIntent = new Intent(context, MediaPlayerService.class);
        serviceIntent.setAction(ACTION_RESUME_TRACK);
        return serviceIntent;
    }

    public static void playNextTrack(Context context) {
        context.startService(getPlayNextTrackIntent(context));
    }

    public static Intent getPlayNextTrackIntent(Context context) {
        Intent serviceIntent = new Intent(context, MediaPlayerService.class);
        serviceIntent.setAction(ACTION_PLAY_NEXT_TRACK);
        return serviceIntent;
    }

    public static void playPreviousTrack(Context context) {
        context.startService(getPlayPreviousTrackIntent(context));
    }

    public static Intent getPlayPreviousTrackIntent(Context context) {
        Intent serviceIntent = new Intent(context, MediaPlayerService.class);
        serviceIntent.setAction(ACTION_PLAY_PREVIOUS_TRACK);
        return serviceIntent;
    }

    public static void setTrackProgressTo(Context context, int progress) {
        Intent serviceIntent = new Intent(context, MediaPlayerService.class);
        serviceIntent.setAction(ACTION_SET_TRACK_PROGRESS_TO);
        serviceIntent.putExtra(TRACK_PROGRESS, progress);
        context.startService(serviceIntent);
    }

    public static void broadcastCurrentTrack(Context context) {
        Intent serviceIntent = new Intent(context, MediaPlayerService.class);
        serviceIntent.setAction(ACTION_BROADCAST_CURRENT_TRACK);
        context.startService(serviceIntent);
    }

    /**
     * Binder interface
     */
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not available");
    }


    /**
     * Custom methods
     */
    @Override
    public void onDestroy() {
        super.onDestroy();

        //Cancel notification
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //Set tracks
        if (intent.getAction().equals(ACTION_SET_TRACKS)) {
            setTracks(intent);
            loadTrack(0);
        }

        //Previous track
        if (intent.getAction().equals(ACTION_PLAY_PREVIOUS_TRACK)) {
            playPreviousTrack();
        }

        //Play track
        if (intent.getAction().equals(ACTION_PLAY_TRACK)) {
            int trackId = intent.getIntExtra(TRACK_ID, -1);
            Log.d(LOG_TAG, "let's play with track " + trackId);
            playTrack(trackId);
        }

        //Pause track
        if (intent.getAction().equals(ACTION_PAUSE_TRACK)) {
            pauseTrack();
        }

        //Resume track
        if (intent.getAction().equals(ACTION_RESUME_TRACK)) {
            resumeTrack();
        }

        //Next track
        if (intent.getAction().equals(ACTION_PLAY_NEXT_TRACK)) {
            playNextTrack();
        }

        //Set track progress
        if (intent.getAction().equals(ACTION_SET_TRACK_PROGRESS_TO)) {
            int progress = intent.getIntExtra(TRACK_PROGRESS, 0);
            setTrackProgressTo(progress);
        }

        //Request current track broadcast
        if (intent.getAction().equals(ACTION_BROADCAST_CURRENT_TRACK)) {
            if (mCurrentTrack != null)
                broadcastTrackToBePlayed();
        }

        return START_NOT_STICKY;
    }


    private void setTracks(Intent data) {

        mTracksList = data.getParcelableArrayListExtra(TRACKS_LIST);
    }

    private void playPreviousTrack() {
        int previousTrackIndex = mCurrentTrackIndex - 1;
        if (mTracksList == null || previousTrackIndex < 0)
            return;

        playTrack(previousTrackIndex);
    }

    private void playNextTrack() {
        int nextTrackIndex = mCurrentTrackIndex + 1;

        if (mTracksList == null || nextTrackIndex >= mTracksList.size())
            return;

        playTrack(nextTrackIndex);
    }

    private void stopPlayback() {
        if (mMediaPlayer == null)
            return;

        if (mMediaPlayer.isPlaying())
            mMediaPlayer.stop();

        mMediaPlayer.setOnPreparedListener(null);
        mMediaPlayer.reset();
        mMediaPlayer.release();
        mMediaPlayer = null;

        if (mBroadcastTrackProgressTask != null)
            mBroadcastTrackProgressTask.cancel(true);
    }


    private void loadTrack(int trackId) {
        Log.d(LOG_TAG, "Size in loadTrack " + mTracksList.size());
        //Get track
        mCurrentTrack = mTracksList.get(trackId);
        mCurrentTrackIndex = mTracksList.indexOf(mCurrentTrack);
        String trackUrl = mCurrentTrack.previewUrl;

        //Notify track to be played
        broadcastTrackToBePlayed();

        //Start Media Player
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnErrorListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        try {
            mMediaPlayer.setDataSource(trackUrl);
            mMediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void playTrack(int trackId) {
        resumeTrack();
    }

    private void pauseTrack() {
        if (mMediaPlayer == null)
            return;

        broadcastTrackPause();
        mMediaPlayer.pause();

        if (mBroadcastTrackProgressTask != null)
            mBroadcastTrackProgressTask.cancel(true);

    }

    private void resumeTrack() {
        Log.d(LOG_TAG, "resume Track");
        if (mMediaPlayer == null)
            return;

        mMediaPlayer.start();

        mBroadcastTrackProgressTask = new BroadcastTrackProgressTask();
        mBroadcastTrackProgressTask.execute();

    }

    private void setTrackProgressTo(int progress) {
        if ( mMediaPlayer != null) {
           // if (mMediaPlayer.isPlaying())
                mMediaPlayer.seekTo(progress);
            Log.d(LOG_TAG, "Seek to " + progress);
        }
    }

    /**
     * Player broadcasts
     */
    private void broadcastTrackToBePlayed() {
        TrackLoadedEvent event = new TrackLoadedEvent(mCurrentTrack);
        bus.post(event);

    }



    @Override
    public void onCompletion(MediaPlayer mp) {

    }


    /**
     * BroadcastTrackProgressTask: reports song that is being played and progress
     */
    class BroadcastTrackProgressTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            while (!isCancelled()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (!mMediaPlayer.isPlaying())
                    return null;

                broadcastTrackPlayingProgress();
            }

            return null;
        }
    }

    private void broadcastTrackPlayingProgress() {
        Log.d(LOG_TAG, "broadcastTrackPlayingProgress: ");
        TrackPlayingEvent event = TrackPlayingEvent.newInstance(
                mCurrentTrack,
                mMediaPlayer.getCurrentPosition()
        );
        bus.post(event);
    }

    private void broadcastTrackPause() {
        TrackPausedEvent event = TrackPausedEvent.newInstance(
                mCurrentTrack,
                mMediaPlayer.getCurrentPosition()
        );
        bus.post(event);
    }


    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        broadcastTrackPlayingProgress();
        //resumeTrack();
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        Log.e(LOG_TAG, "Error during Playback!");
        return false;
    }


}